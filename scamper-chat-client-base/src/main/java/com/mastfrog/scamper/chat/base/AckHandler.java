package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.api.MessageAcknowledgement;
import com.mastfrog.scamper.chat.spi.Client;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author Tim Boudreau
 */
final class AckHandler extends MessageHandler<Void, MessageAcknowledgement> {

    private final Client client;

    @Inject
    AckHandler(Client client) {
        super(MessageAcknowledgement.class);
        this.client = client;
    }

    @Override
    public Message<Void> onMessage(Message<MessageAcknowledgement> data, ChannelHandlerContext ctx) {
        client.onMessageAcknowledged(data.body.id, data.body.sentTo);
        return null;
    }
}
