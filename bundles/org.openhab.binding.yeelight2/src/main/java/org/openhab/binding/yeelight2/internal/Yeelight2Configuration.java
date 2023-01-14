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

/**
 * The {@link Yeelight2Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Tiph - Initial contribution
 */

public class Yeelight2Configuration {

    private String ip;
    private String id;
    private boolean smooth;
    private int refreshInterval;
    private int reconnectInterval;
    private int smoothDuration;
    
    
    public String getIp() {
        return ip;
    }
    public String getId() {
        return id;
    }
    public boolean isSmooth() {
        return smooth;
    }
    public int getRefreshInterval() {
        return refreshInterval;
    }
    public int getReconnectInterval() {
        return reconnectInterval;
    }
    public int getSmoothDuration() {
        return smoothDuration;
    }
    
    

}
