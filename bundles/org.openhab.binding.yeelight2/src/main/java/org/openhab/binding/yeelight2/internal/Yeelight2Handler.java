/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.yeelight2.internal;

import static org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yeelight2.internal.action.Yeelight2Action;
import org.openhab.binding.yeelight2.internal.device.property.YeelightDeviceProperty;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link Yeelight2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tiph - Initial contribution
 */
@NonNullByDefault
public class Yeelight2Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory
            .getLogger(Yeelight2Handler.class.getName() + "." + thing.getUID().getId());

    private @Nullable Yeelight2Configuration config;

    // Used to keep last state of the properties and also to determine if a channel is available on this Thing
    private final EnumMap<YeelightDeviceProperty, String> stateByProp = new EnumMap<>(YeelightDeviceProperty.class);

    private final @Nullable Yeelight2Selector selectorProvider;

    private @Nullable SocketChannel socketChannel;
    private final ByteBuffer senderBb = ByteBuffer.allocateDirect(3072);

    private final StringBuilder cmdBuilder = new StringBuilder();

    private @Nullable ScheduledFuture<?> heartbeatTask;
    private @Nullable ScheduledFuture<?> reconnectTask;

    // If channel need to be checked (Remove channel that are not supported)
    private boolean shouldCheckChannel = true;

    public Yeelight2Handler(Thing thing, @Nullable Yeelight2Selector selector) {
        super(thing);
        logger.trace("Constructing: {}", thing.getUID());
        this.selectorProvider = selector;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        handleCommand(channelUID, RefreshType.REFRESH);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getIdWithoutGroup();

        YeelightDeviceProperty property = YeelightDeviceProperty.fromPropertyName(id);
        if (command instanceof RefreshType) {
            if (stateByProp.containsKey(property)) {
                updateState(property, stateByProp.get(property));
            }
            return;
        }

        if (property.isWrittable()) {
            cmdBuilder.setLength(0);
            cmdBuilder.append("{\"id\":"); // {"id":
            cmdBuilder.append(ID_ACTION_QUERY); // 1
            cmdBuilder.append(","); // ,
            cmdBuilder.append("\"method\":\""); // "method":"
            cmdBuilder.append(property.getSetterName()); // set_power
            cmdBuilder.append("\","); // ",
            cmdBuilder.append("\"params\":["); // "params":[
            cmdBuilder.append(property.getSetterValue(command)); // param value
            cmdBuilder.append(","); // ,
            if (config.isSmooth()) {
                cmdBuilder.append("\"smooth\","); // "smooth",
                cmdBuilder.append(config.getSmoothDuration()); // 500
            } else {
                cmdBuilder.append("\"sudden\""); // "sudden",
            }
            cmdBuilder.append("]}"); // ]}

            sendCommand(cmdBuilder.toString());
        }
    }

    @Override
    public void initialize() {
        logger.trace("initializing: {}", thing.getUID());
        config = getConfigAs(Yeelight2Configuration.class);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::startClient);
    }

    @Override
    public void dispose() {
        logger.trace("Disposing: {}", thing.getUID());
        stopClient();
        stopReconnectTask();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(Yeelight2Action.class);
    }

    private void startClient() {
        logger.trace("Starting client");
        try {
            socketChannel = SocketChannel
                    .open(new InetSocketAddress(config.getIp(), Yeelight2BindingConstants.YEELIGHT_PORT));
            socketChannel.configureBlocking(false);
            selectorProvider.register(socketChannel, this);
            if (heartbeatTask == null) {
                heartbeatTask = scheduler.scheduleAtFixedRate(() -> sendCommand(GET_PROP_QUERY), 0,
                        config.getRefreshInterval(), TimeUnit.SECONDS);
            }
            stopReconnectTask();
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.warn("Thing not reachable: {}", e.toString());
            if (logger.isDebugEnabled()) {
                logger.error(e.getMessage(), e);
            }
            handleSocketException(e);
        }
    }

    private void stopClient() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
            socketChannel = null;
            if (heartbeatTask != null) {
                heartbeatTask.cancel(true);
                heartbeatTask = null;
            }
        } catch (IOException e) {
            logger.error("Error while closing socket", e);
        }
    }

    private void startReconnectTask() {
        if (reconnectTask == null) {
            reconnectTask = scheduler.scheduleWithFixedDelay(this::startClient, 0, config.getReconnectInterval(),
                    TimeUnit.SECONDS);
        }
    }

    private void stopReconnectTask() {
        if (reconnectTask != null) {
            reconnectTask.cancel(true);
            reconnectTask = null;
        }
    }

    protected void handleSocketException(IOException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
        stopClient();
        startReconnectTask();
    }

    public void sendCommand(String jsonCmd) {
        logger.debug("Sending command: {}", jsonCmd);
        if (socketChannel != null && socketChannel.isConnected() && getThing().getStatus() == ThingStatus.ONLINE) {
            senderBb.clear();
            senderBb.put(jsonCmd.getBytes());
            senderBb.put((byte) '\r'); // CR endings
            senderBb.put((byte) '\n'); // LF endings
            try {
                socketChannel.write(senderBb.flip());
            } catch (IOException e) {
                logger.error("Error while sending command: {}, Exception: {}", jsonCmd, e.getMessage());
            }
        }
    }

    public void handleMessage(String msg) {
        logger.debug("Received: {}", msg);
        JsonObject json = new Gson().fromJson(msg, JsonObject.class);
        if (json != null) {
            JsonElement id = json.get("id");
            JsonElement method = json.get("method");
            if (id != null && id.getAsInt() != ID_ALL_PROP_QUERY && id.getAsInt() != ID_ACTION_QUERY) {
                // If result is not from internal command update "command result" channel
            } else if (id != null && json.get("error") != null) {
                logger.debug("Invalid command");
            } else if (id != null) {
                handleResponse(id.getAsInt(), json.get("result").getAsJsonArray());
            } else if (method != null && method.getAsString().equals("props")) {
                handleUpdate(json.get("params").getAsJsonObject());
            }
        }
    }

    /**
     * Handle response to command
     *
     * @param id
     * @param result
     */
    private void handleResponse(int id, JsonArray result) {
        if (id == ID_ALL_PROP_QUERY && result.size() == YeelightDeviceProperty.values().length) {
            for (YeelightDeviceProperty p : YeelightDeviceProperty.values()) {
                updateState(p, result.get(p.ordinal()).getAsString());
            }
            // Check and remove unsupported channels
            if (shouldCheckChannel) {
                hideChannelIfNotSupported();
                shouldCheckChannel = false;
            }
        }
    }

    /**
     * Handle update from device
     *
     * @param props
     */
    private void handleUpdate(JsonObject props) {
        for (String propName : props.keySet()) {
            YeelightDeviceProperty prop = YeelightDeviceProperty.fromPropertyName(propName);
            if (prop != null) {
                updateState(YeelightDeviceProperty.fromPropertyName(propName), props.get(propName).getAsString());
            }
        }
    }

    private void hideChannelIfNotSupported() {

        ThingBuilder thingBuilder = editThing();

        for (YeelightDeviceProperty p : YeelightDeviceProperty.values()) {
            if (!isPropertyAvailable(p)) {
                logger.debug("hidding channel: {}", p.getPropertyName());
                ChannelUID defaultChannelUID = new ChannelUID(this.getThing().getUID(), p.getChannelGroupId(),
                        p.getPropertyName());
                thingBuilder.withoutChannel(defaultChannelUID);
            }
        }
        updateThing(thingBuilder.build());

    }

    private boolean isPropertyAvailable(YeelightDeviceProperty property) {
        return stateByProp.containsKey(property.getChannelProperty());
    }

    private void updateState(YeelightDeviceProperty property, String value) {
        if (!value.isEmpty()) {

            stateByProp.put(property, value);
            ChannelUID defaultChannelUID = new ChannelUID(this.getThing().getUID(), property.getChannelGroupId(),
                    property.getPropertyName());
            updateState(defaultChannelUID, property.getState(value));
        }
    }

}
