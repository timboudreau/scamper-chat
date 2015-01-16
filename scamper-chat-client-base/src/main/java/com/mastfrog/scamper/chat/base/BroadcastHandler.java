package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.api.RoomMessage;
import com.mastfrog.scamper.chat.spi.Client;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author Tim Boudreau
 */
final class BroadcastHandler extends MessageHandler<Void, RoomMessage> {

    private final Client client;

    @Inject
    BroadcastHandler(Client client) {
        super(RoomMessage.class);
        this.client = client;
    }

    @Override
    public Message<Void> onMessage(Message<RoomMessage> data, ChannelHandlerContext ctx) {
        try {
            client.onMessage(data.body.room, data.body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
