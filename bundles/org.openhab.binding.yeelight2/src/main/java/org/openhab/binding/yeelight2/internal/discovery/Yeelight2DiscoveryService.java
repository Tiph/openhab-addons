/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yeelight2.internal.discovery;

import static org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants;
import org.openhab.binding.yeelight2.internal.Yeelight2HandlerFactory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery." + BINDING_ID)
public class Yeelight2DiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(Yeelight2DiscoveryService.class);

    private @Reference NetworkAddressService netUtil;

    private static final int DISCOVERY_TIME = 10;
    private static final int BUFFER_LENGTH = 2048;

    private @Nullable DatagramSocket clientSocket;
    private @Nullable Thread socketReceiveThread;
    private Set<String> responseIps = new HashSet<>();

    public Yeelight2DiscoveryService() {
        super(Yeelight2HandlerFactory.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME, false);
    }

    @Override
    protected void startScan() {
        logger.trace("Starting Scan");
        final DatagramSocket clientSocket = getSocket();
        if (clientSocket != null) {
            logger.debug("Discovery using socket on {}:{}", clientSocket.getLocalAddress(),
                    clientSocket.getLocalPort());
            discover();
        } else {
            logger.error("Discovery not started. Client DatagramSocket null");
        }
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug(": stopScan");
    }

    private void discover() {
        startReceiverThread();
        responseIps = new HashSet<>();
        sendDiscoveryRequest();
    }

    /**
     * starts the {@link ReceiverThread} thread
     */
    private synchronized void startReceiverThread() {
        final Thread srt = socketReceiveThread;
        if (srt != null) {
            if (srt.isAlive() && !srt.isInterrupted()) {
                return;
            }
        }
        stopReceiverThreat();
        Thread socketReceiveThread = new ReceiverThread();
        socketReceiveThread.start();
        this.socketReceiveThread = socketReceiveThread;
    }

    /**
     * Stops the {@link ReceiverThread} thread
     */
    private synchronized void stopReceiverThreat() {
        if (socketReceiveThread != null) {
            socketReceiveThread.interrupt();
            socketReceiveThread = null;
        }
        closeSocket();
    }

    private void sendDiscoveryRequest() {
        final @Nullable DatagramSocket socket = getSocket();
        if (socket != null) {
            try {
                byte[] sendData = Yeelight2BindingConstants.DISCOVER_STRING.getBytes();
                logger.debug("Send discovery msg to {} from {}:{}", Yeelight2BindingConstants.MULTI_CAST_HOST,
                        socket.getLocalAddress(), socket.getLocalPort());
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        InetAddress.getByName(Yeelight2BindingConstants.MULTI_CAST_HOST),
                        Yeelight2BindingConstants.MULTI_CAST_PORT);
                socket.send(sendPacket);
            } catch (IOException e) {
                logger.error("Discovery on {} failed: {}", Yeelight2BindingConstants.MULTI_CAST_HOST, e.getMessage());
            }
        }
    }

    private synchronized @Nullable DatagramSocket getSocket() {
        DatagramSocket clientSocket = this.clientSocket;
        if (clientSocket != null && clientSocket.isBound()) {
            return clientSocket;
        }
        try {
            logger.debug("Getting new socket for discovery");
            clientSocket = new DatagramSocket(new InetSocketAddress(netUtil.getPrimaryIpv4HostAddress(), 0));
            clientSocket.setReuseAddress(true);
            clientSocket.setBroadcast(true);
            this.clientSocket = clientSocket;
            return clientSocket;
        } catch (SocketException | SecurityException e) {
            logger.error("Error getting socket for discovery: {}", e.getMessage());
        }
        return null;
    }

    private void closeSocket() {
        final @Nullable DatagramSocket clientSocket = this.clientSocket;
        if (clientSocket != null) {
            clientSocket.close();
        } else {
            return;
        }
        this.clientSocket = null;
    }

    /**
     * The thread, which waits for data and submits the unique results addresses to the discovery results
     *
     */
    private class ReceiverThread extends Thread {
        @Override
        public void run() {
            DatagramSocket socket = getSocket();
            if (socket != null) {
                logger.trace("Starting discovery receiver thread for socket on port {}", socket.getLocalPort());
                receiveData(socket);
            }
        }

        /**
         * This method waits for data and submits the unique results addresses to the discovery results
         *
         * @param socket - The multicast socket to (re)use
         */
        private void receiveData(DatagramSocket socket) {
            DatagramPacket receivePacket = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
            try {
                while (!interrupted()) {
                    logger.debug("Thread {} waiting for data on {}:{}", this, socket.getLocalAddress(),
                            socket.getLocalPort());
                    socket.receive(receivePacket);
                    String hostAddress = receivePacket.getAddress().getHostAddress();
                    logger.debug("Received {} bytes response from {}:{} on {}:{}", receivePacket.getLength(),
                            hostAddress, receivePacket.getPort(), socket.getLocalAddress(), socket.getLocalPort());

                    byte[] messageBuf = Arrays.copyOfRange(receivePacket.getData(), receivePacket.getOffset(),
                            receivePacket.getOffset() + receivePacket.getLength());
                    logger.debug("received: {}", new String(messageBuf, StandardCharsets.US_ASCII));
                    if (logger.isTraceEnabled()) {
                        // Message miIoResponse = new Message(messageBuf);
                        // logger.debug("Discovery response received from {} DeviceID: {}\r\n{}", hostAddress,
                        // Utils.getHex(miIoResponse.getDeviceId()), miIoResponse.toSting());
                    }
                    if (!responseIps.contains(hostAddress)) {
                        scheduler.schedule(() -> {
                            try {
                                discovered(hostAddress, messageBuf);
                            } catch (Exception e) {
                                logger.debug("Error submitting discovered Mi IO device at {}", hostAddress, e);
                            }
                        }, 0, TimeUnit.SECONDS);
                    }
                    responseIps.add(hostAddress);
                }
            } catch (SocketException e) {
                logger.error("Receiver thread received SocketException: {}", e.getMessage());
            } catch (IOException e) {
                logger.error("Receiver thread was interrupted");
            }
            logger.trace("Receiver thread ended");
        }
    }

    private static final Pattern HEADER_PATTERN = Pattern.compile("(.*?):(.*)$");

    private void discovered(String ip, byte[] response) {
        String rawMessage = new String(response, StandardCharsets.US_ASCII);
        @NonNull
        String[] lines = rawMessage.split("\r\n");
        Map<String, Object> thingProperties = new HashMap<>();
        for (String line : lines) {
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (matcher.matches()) {
                String header = matcher.group(1).toUpperCase().trim();
                String val = matcher.group(2).trim();
                switch (header) {
                    case "LOCATION":
                        Matcher matcher2 = HEADER_PATTERN.matcher(val.substring("yeelight://".length()));
                        matcher2.matches();
                        thingProperties.put(PARAMETER_IP, matcher2.group(1));
                        break;
                    case "ID":
                        thingProperties.put(PARAMETER_ID, val);
                        break;
                    case "MODEL":
                        thingProperties.put(Thing.PROPERTY_MODEL_ID, val);
                        break;
                    case "NAME":
                        thingProperties.put("name", val);
                        break;
                    case "SUPPORT":
                        thingProperties.put(SUPPORTED_FUNCTION, val);
                        break;
                    case "FW_VER":
                        thingProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, val);
                        break;
                }
            }
        }
        ThingUID uid = new ThingUID(THING_TYPE_BASE, thingProperties.get(PARAMETER_ID).toString());
        DiscoveryResultBuilder dr = DiscoveryResultBuilder.create(uid).withProperties(thingProperties)
                .withRepresentationProperty(PARAMETER_IP)
                .withLabel("Yeelight [model:" + thingProperties.get(Thing.PROPERTY_MODEL_ID) + ", name:"
                        + thingProperties.get("name") + ", id:" + thingProperties.get(PARAMETER_ID) + "]");
        thingDiscovered(dr.build());
    }
}
