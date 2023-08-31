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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.yeelight2.internal.Yeelight2HandlerFactory;
import org.openhab.binding.yeelight2.internal.Yeelight2Selector;
import org.openhab.binding.yeelight2.internal.Yeelight2Selector.Yeelight2SocketHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery." + BINDING_ID)
public class Yeelight2DiscoveryService2 extends AbstractDiscoveryService implements Yeelight2SocketHandler {

    private final Logger logger = LoggerFactory.getLogger(Yeelight2DiscoveryService2.class);

    private static final int DISCOVERY_TIME_SEC = 5;

    private @Reference NetworkAddressService netUtil;

    private @Reference Yeelight2Selector selectorProvider;
    private DatagramChannel datagramChannel;
    private DatagramChannel advertisingDc;

    private final ByteBuffer socketReaderBB = ByteBuffer.allocateDirect(1024);

    public Yeelight2DiscoveryService2() {
        super(Yeelight2HandlerFactory.SUPPORTED_THING_TYPES_UIDS, DISCOVERY_TIME_SEC, false);

    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.trace("Starting background service");
        super.startBackgroundDiscovery();

        try {
            String primaryIpv4HostAddress = netUtil.getPrimaryIpv4HostAddress();
            NetworkInterface primaryInterface = NetworkInterface
                    .getByInetAddress(InetAddress.getByName(primaryIpv4HostAddress));

            advertisingDc = DatagramChannel.open(StandardProtocolFamily.INET);
            advertisingDc.setOption(StandardSocketOptions.IP_MULTICAST_IF, primaryInterface);
            advertisingDc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            advertisingDc.join(InetAddress.getByName(MULTI_CAST_HOST), primaryInterface);
            advertisingDc.configureBlocking(false);
            advertisingDc.bind(new InetSocketAddress(primaryIpv4HostAddress, MULTI_CAST_PORT));

            selectorProvider.register(advertisingDc, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        logger.trace("Stoping background service");
        super.stopBackgroundDiscovery();

        if (advertisingDc != null && advertisingDc.isOpen()) {
            try {
                advertisingDc.close();
            } catch (IOException ignore) {
            }
        }
        advertisingDc = null;
    }

    @Override
    public void startScan() {
        try {
            datagramChannel = DatagramChannel.open(StandardProtocolFamily.INET);
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(netUtil.getPrimaryIpv4HostAddress(), 0));
            selectorProvider.register(datagramChannel, this);

            final InetSocketAddress target = new InetSocketAddress(MULTI_CAST_HOST, MULTI_CAST_PORT);
            ByteBuffer buffer = ByteBuffer.wrap(DISCOVER_STRING.getBytes());
            datagramChannel.send(buffer, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();

        if (datagramChannel != null && datagramChannel.isOpen()) {
            try {
                datagramChannel.close();
            } catch (IOException ignore) {
            }
        }
        datagramChannel = null;
    }

    @Override
    public void handleSocketMessage(SelectableChannel channel) {
        logger.debug("Received discoverying message");
        try {
            ((DatagramChannel) channel).receive(socketReaderBB.clear());
            socketReaderBB.flip();
            Map<String, Object> parseSocketMessage = parseSocketMessage();
            System.err.println(parseSocketMessage);
        } catch (IOException e) {
            logger.error("Error on socket", e);
        }
    }

    private final String HTTP_OK_HEADER = "HTTP/1.1 200 OK";
    private final String NOTIFY_HEADER = "NOTIFY * HTTP/1.1";
    private final String LOCATION_TEXT = "Location: yeelight://";
    private final String ID_PROP = "id: ";
    private final String MODEL_PROP = "model: ";
    private final String FW_VER_PROP = "fw_ver: ";
    private final String NAME_PROP = "name: ";

    private Map<String, Object> parseSocketMessage() {
        Map<String, Object> thingProperties = new HashMap<>();

        int i = 0, j = 0, k = 0, l = 0, m = 0;
        while (socketReaderBB.hasRemaining()) {
            char c = (char) socketReaderBB.get();

            System.err.print(c);
            i = checkPropString(c, i, LOCATION_TEXT, () -> {
                String socketAdress = getPropValue(socketReaderBB);
                String ip = socketAdress.substring(0, socketAdress.length() - 6);
                thingProperties.put(PARAMETER_IP, ip);
            });
            j = checkPropString(c, j, ID_PROP, () -> {
                thingProperties.put(PARAMETER_ID, getPropValue(socketReaderBB));
            });
            k = checkPropString(c, k, MODEL_PROP, () -> {
                thingProperties.put(Thing.PROPERTY_MODEL_ID, getPropValue(socketReaderBB));
            });
            l = checkPropString(c, l, FW_VER_PROP, () -> {
                thingProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, getPropValue(socketReaderBB));
            });
            m = checkPropString(c, m, NAME_PROP, () -> {
                thingProperties.put(NAME, getPropValue(socketReaderBB));
            });
        }

        return thingProperties;
    }

    private static int checkPropString(final char currentChar, final int currentIndex, String propText, Runnable run) {
        int i = currentIndex;

        if (i != -1) {
            if (currentChar == propText.charAt(i)) {
                i++;
            } else {
                i = 0;
            }
            if (i == propText.length()) {
                run.run();
                i = -1;
            }
        }
        return i;
    }

    private static String getPropValue(ByteBuffer socketReaderBB) {
        StringBuilder sb = new StringBuilder();
        for (char c = (char) socketReaderBB.get(); c != '\r'; c = (char) socketReaderBB.get()) {
            System.err.print(c);
            sb.append(c);
        }
        return sb.toString();
    }

}
