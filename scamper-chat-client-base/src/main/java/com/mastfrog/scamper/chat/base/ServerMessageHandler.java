package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.api.ServerMessage;
import com.mastfrog.scamper.chat.spi.Client;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author Tim Boudreau
 */
final class ServerMessageHandler extends MessageHandler<Void, ServerMessage> {

    private final Client client;

    @Inject
    ServerMessageHandler(Client client) {
        super(ServerMessage.class);
        this.client = client;
    }

    @Override
    public Message<Void> onMessage(Message<ServerMessage> data, ChannelHandlerContext ctx) {
        client.onServerMessage(data.body.message, data.body.clientShutdownAdvised);
        return null;
    }
}
