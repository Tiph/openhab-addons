package org.openhab.binding.yeelight2.internal;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

public interface Yeelight2Selector {

    void register(SocketChannel channel, Yeelight2Handler thingHandler) throws ClosedChannelException;

}
