package org.openhab.binding.yeelight2.internal;

import static java.nio.channels.SelectionKey.OP_READ;
import static org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants.BINDING_ID;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.openhab.core.thing.ThingStatusDetail;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = "selector." + BINDING_ID)
public class Yeelight2SelectorThread extends Thread implements Yeelight2Selector {

    private final Logger logger = LoggerFactory.getLogger(Yeelight2SelectorThread.class);

    private final Selector selector = Selector.open();
    private final ByteBuffer readerBb = ByteBuffer.allocateDirect(8192);
    private final StringBuilder sb = new StringBuilder();

    public Yeelight2SelectorThread() throws IOException {
    }

    @Activate
    private void activate() {
        start();
    }

    @Deactivate
    private void deactivate() {
        interrupt();
    }

    @Override
    public void register(SocketChannel channel, Yeelight2Handler thingHandler) throws ClosedChannelException {
        channel.register(selector, OP_READ, thingHandler);
        selector.wakeup();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                select();
            } catch (Exception e) {
                logger.error("Error while processing message", e);
            }
        }
        logger.trace("Selector thread shuting down");
    }

    private final void select() throws IOException {
        selector.select();
        Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
        while (selectedKeys.hasNext()) {
            SelectionKey key = selectedKeys.next();
            selectedKeys.remove();

            SocketChannel channel = (SocketChannel) key.channel();
            Yeelight2Handler thingHandler = (Yeelight2Handler) key.attachment();

            if (key.isReadable()) {
                try {
                    int read = channel.read(readerBb.clear());
                    readerBb.flip();
                    if (read > 0) {
                        while (readerBb.hasRemaining()) {
                            char c = (char) readerBb.get();
                            if (c == '\r') {
                                continue; // ignore \r\n
                            } else if (c == '\n') {
                                // /!\ presume that messages are not cut /!\
                                thingHandler.handleMessage(sb.toString());
                                sb.setLength(0); // reset for next msg
                            } else {
                                sb.append(c);
                            }
                        }
                    } else if (read == -1) {
                        logger.error("End of socket");
                        thingHandler.setOffline(ThingStatusDetail.COMMUNICATION_ERROR, "Socket down");
                    } else {
                        logger.error("Socket empty");
                    }
                } catch (IOException e) {
                    logger.error("Error on socket", e);
                    thingHandler.setOffline(ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
                }
            }
        }
    }
}
