package org.openhab.binding.yeelight2.internal;

import static java.nio.channels.SelectionKey.OP_READ;
import static org.openhab.binding.yeelight2.internal.Yeelight2BindingConstants.BINDING_ID;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = "selector." + BINDING_ID)
public class Yeelight2SelectorThread extends Thread implements Yeelight2Selector {

    private final Logger logger = LoggerFactory.getLogger(Yeelight2SelectorThread.class);

    private final Selector selector = Selector.open();

    public Yeelight2SelectorThread() throws IOException {
        setName("Yeelight2 Thread");
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
    public void register(AbstractSelectableChannel channel, Yeelight2SocketHandler thingHandler)
            throws ClosedChannelException {
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
            if (key.isReadable()) {
                try {
                    Yeelight2SocketHandler thingHandler = (Yeelight2SocketHandler) key.attachment();
                    thingHandler.handleSocketMessage(key.channel());
                } catch (Exception e) {
                    logger.error("Failed to handle socket message", e);
                }
            }
        }
    }
}
