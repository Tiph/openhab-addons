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
package org.openhab.binding.yeelight2.internal.action;

import static org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants.BINDING_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.yeelight2.internal.Yeelight2Handler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Yeelight2Action} is responsible for handling actions
 *
 * @author Tiph - Initial contribution
 */
@ThingActionsScope(name = BINDING_ID)
@NonNullByDefault
public class Yeelight2Action implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(Yeelight2Action.class);

    private @Nullable Yeelight2Handler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (Yeelight2Handler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "sendCommand", description = "Send a Command through the Yeelight API")
    public void sendCommand(
            @ActionInput(name = "command", label = "@text/actionInputValueLabel", description = "@text/actionInputValueDesc") @Nullable String command) {
        if (handler != null && command != null) {
            handler.sendCommand(command);
        } else {
            logger.warn("Cannot send command: {} to handler: {}", command, handler);
        }
    }

    // mandatory method to support DSL rule (@see: https://www.openhab.org/docs/developer/bindings/)
    public static void sendCommand(@Nullable ThingActions actions, @Nullable String command) {
        if (actions instanceof Yeelight2Action) {
            ((Yeelight2Action) actions).sendCommand(command);
        } else {
            throw new IllegalArgumentException("Instance is not an Yeelight2Action class.");
        }
    }

}
