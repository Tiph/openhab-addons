package org.openhab.binding.yeelight2.internal;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import org.eclipse.jdt.annotation.NonNull;

public interface Yeelight2Selector {

    void register(AbstractSelectableChannel channel, Yeelight2SocketHandler socketMessageHandler)
            throws ClosedChannelException;

    public interface Yeelight2SocketHandler {
        void handleSocketMessage(@NonNull SelectableChannel channel);
    }

}
