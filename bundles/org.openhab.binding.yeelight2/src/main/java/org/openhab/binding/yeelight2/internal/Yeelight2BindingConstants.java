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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Yeelight2BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tiph - Initial contribution
 */
@NonNullByDefault
public class Yeelight2BindingConstants {

    public static final String BINDING_ID = "yeelight2";

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BASE = new ThingTypeUID(BINDING_ID, "base");

    // List of all Channel ids
    public static final String CHANNEL_COMMAND = "command";

    // Thing Configuration names
    public static final String PARAMETER_IP = "ip";
    public static final String PARAMETER_ID = "id";
    public static final String PARAMETER_SMOOTH = "smooth";
    public static final String PARAMETER_SMOOTH_DURATION = "smooth_duration";

    // Thing Properties names
    public static final String NAME = "Name";
    public static final String SUPPORTED_FUNCTION = "Supported function";

    public static final String MULTI_CAST_HOST = "239.255.255.250";
    public static final int MULTI_CAST_PORT = 1982;
    public static final int YEELIGHT_PORT = 55443;

    public static final String DISCOVER_STRING = "M-SEARCH * HTTP/1.1\r\n" + "HOST:" + MULTI_CAST_HOST + ":"
            + MULTI_CAST_PORT + "\r\n" + "MAN:\"ssdp:discover\"\r\n" + "ST:wifi_bulb\r\n";

    public static final int ID_ALL_PROP_QUERY = 424242;
    public static final int ID_ACTION_QUERY = 424243;

    public static final String GET_PROP_QUERY;

    static {
        String cmd = "{\"id\":" + ID_ALL_PROP_QUERY + ",\"method\":\"get_prop\",\"params\":[";
        YeelightDeviceProperty[] values = YeelightDeviceProperty.values();
        for (YeelightDeviceProperty p : values) {
            cmd += "\"" + p.getPropertyName() + "\"";
            if (p.ordinal() < values.length - 1) {
                cmd += ",";
            }
        }
        cmd += "]}";
        GET_PROP_QUERY = cmd;
    }

}
