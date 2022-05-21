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
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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

    private final Logger logger = LoggerFactory.getLogger(Yeelight2Handler.class);

    private @Nullable Yeelight2Configuration config;
    private final Map<YeelightDeviceProperty, String> stateByProp = new HashMap<>();

    private @Nullable final Yeelight2Selector selectorProvider;

    private @Nullable SocketChannel socketChannel;
    private final ByteBuffer senderBb = ByteBuffer.allocateDirect(3072);

    private final StringBuilder cmdBuilder = new StringBuilder();

    private @Nullable ScheduledFuture<?> heartbeatTask;
    private @Nullable ScheduledFuture<?> reconnectTask;

    public Yeelight2Handler(Thing thing, @Nullable Yeelight2Selector selector) {
        super(thing);
        this.selectorProvider = selector;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        String id = channelUID.getId();

        if (id.equals(CHANNEL_COMMAND)) {
            return;
        }

        YeelightDeviceProperty property = YeelightDeviceProperty.fromPropertyName(id);
        String value = stateByProp.get(property);
        if (value != null) {
            updateState(property, value, true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();

        if (id.equals(CHANNEL_COMMAND) && command instanceof StringType && !command.toString().isEmpty()) {
            sendCommand(command.toString());
            return;
        }

        YeelightDeviceProperty prop = YeelightDeviceProperty.fromPropertyName(id);
        if (prop.getAssociatedType() == command.getClass()) {
            cmdBuilder.setLength(0);
            cmdBuilder.append("{\"id\":"); // {"id":
            cmdBuilder.append(ID_ACTION_QUERY); // 1
            cmdBuilder.append(","); // ,
            cmdBuilder.append("\"method\":\""); // "method":"
            cmdBuilder.append(prop.getSetterName()); // set_power
            cmdBuilder.append("\","); // ",
            cmdBuilder.append("\"params\":["); // "params":[

            String stringValue;
            switch (prop) {
                case POWER:
                case BG_POWER:
                case MAIN_POWER:
                    stringValue = command.toFullString().toLowerCase();
                    cmdBuilder.append("\"");
                    cmdBuilder.append(stringValue);
                    cmdBuilder.append("\"");
                    break;
                case RGB:
                case BG_RGB:
                    HSBType v = (HSBType) command;
                    stringValue = Integer.toString(v.getRGB() & 0xFFFFFF);
                    cmdBuilder.append(stringValue);
                    break;
                default:
                    stringValue = command.toFullString();
                    cmdBuilder.append(stringValue);
                    break;
            }
            cmdBuilder.append(","); // ,
            if (config.smooth) {
                cmdBuilder.append("\"smooth\","); // "smooth",
                cmdBuilder.append(config.smooth_duration); // 500
            } else {
                cmdBuilder.append("\"sudden\""); // "sudden",
            }
            cmdBuilder.append("]}"); // ]}

            // Update state to change the "stateByProp" property in case we change something that cannot be change
            // e.g. changing brightness while light is off
            updateState(prop, stringValue, false);

            sendCommand(cmdBuilder.toString());
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(Yeelight2Configuration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            startClient();
        });
    }

    @Override
    public void dispose() {
        logger.trace("Disposing: {}", config.id);
        stopClient();
    }

    private void startClient() {
        logger.trace("Starting client");
        try {
            socketChannel = SocketChannel
                    .open(new InetSocketAddress(config.ip, Yeelight2BindingConstants.YEELIGHT_PORT));
            socketChannel.configureBlocking(false);
            selectorProvider.register(socketChannel, this);
            setOnline();
        } catch (ConnectException e) {
            logger.warn("Thing not reachable: {}", e.toString());
            setOffline(ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
            if (logger.isDebugEnabled()) {
                logger.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            logger.warn("Error while starting client: {}", e.toString());
            setOffline(ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
            if (logger.isDebugEnabled()) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void stopClient() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
                socketChannel = null;
            }
            if (heartbeatTask != null) {
                heartbeatTask.cancel(true);
                heartbeatTask = null;
            }
        } catch (IOException e) {
            logger.error("Error while closing socket", e);
        }
    }

    private void sendCommand(String jsonCmd) {
        logger.debug("Sending command: {}", jsonCmd);
        if (socketChannel.isConnected() && getThing().getStatus() == ThingStatus.ONLINE) {
            senderBb.clear();
            senderBb.put(jsonCmd.getBytes());
            senderBb.put((byte) '\r'); // CR endings
            senderBb.put((byte) '\n'); // LF endings
            try {
                socketChannel.write(senderBb.flip());
            } catch (IOException e) {
                logger.error("Error while sending command: {}", jsonCmd);
            }
        }
    }

    public void handleMessage(String msg) {
        logger.debug("Received: {}", msg);
        JsonObject json = new Gson().fromJson(msg, JsonObject.class);
        if (json != null) {
            JsonElement id = json.get("id");
            JsonElement method = json.get("method");
            if (id != null && json.get("error") != null) {
                logger.debug("Invalid command");
            } else if (id != null) {
                handleResponse(id.getAsInt(), json.get("result").getAsJsonArray());
            } else if (method != null && method.getAsString().equals("props")) {
                handleUpdate(json.get("params").getAsJsonObject());
            }
        }
    }

    private void updateState(YeelightDeviceProperty property, String value, boolean forceUpdate) {
        if (!value.isEmpty() && (!value.equals(stateByProp.put(property, value)) || forceUpdate)) {
            updateState(property.getPropertyName(), (State) property.getType(value));
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
                updateState(p, result.get(p.ordinal()).getAsString(), false);
            }
        } else {
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
                updateState(YeelightDeviceProperty.fromPropertyName(propName), props.get(propName).getAsString(),
                        false);
            }
        }
    }

    public void setOnline() {
        updateStatus(ThingStatus.ONLINE);
        if (reconnectTask != null) {
            reconnectTask.cancel(true);
            reconnectTask = null;
        }
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            sendCommand(GET_PROP_QUERY);
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void setOffline(ThingStatusDetail thingStatusDetail, String description) {
        updateStatus(ThingStatus.OFFLINE, thingStatusDetail, description);
        stopClient();
        if (reconnectTask == null) {
            reconnectTask = scheduler.scheduleWithFixedDelay(() -> {
                startClient();
            }, 0, 10, TimeUnit.SECONDS);
        }
    }

    // private void updateChannels() {
    // String a = BG_RGB.getPropertyName();
    // ChannelBuilder powerChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), a))
    // .withType(new ChannelTypeUID(Yeelight2BindingConstants.BINDING_ID, a));
    // updateThing(editThing().withChannel(powerChannel.build()).build());
    // a = BRIGHTNESS.getPropertyName();
    // powerChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), a))
    // .withType(new ChannelTypeUID(Yeelight2BindingConstants.BINDING_ID, a));
    // updateThing(editThing().withChannel(powerChannel.build()).build());
    // a = COLOR_TEMPERATURE.getPropertyName();
    // powerChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), a))
    // .withType(new ChannelTypeUID(Yeelight2BindingConstants.BINDING_ID, a));
    // updateThing(editThing().withChannel(powerChannel.build()).build());
    // a = RGB.getPropertyName();
    //
    // a = COLOR_MODE.getPropertyName();
    // powerChannel = ChannelBuilder.create(new ChannelUID(thing.getUID(), a))
    // .withType(new ChannelTypeUID(Yeelight2BindingConstants.BINDING_ID, a));
    // updateThing(editThing().withChannel(powerChannel.build()).build());
    //
    // }
}
