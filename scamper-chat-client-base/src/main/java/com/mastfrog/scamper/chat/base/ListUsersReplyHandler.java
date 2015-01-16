package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.api.ListUsersReply;
import com.mastfrog.scamper.chat.spi.Client;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author Tim Boudreau
 */
final class ListUsersReplyHandler extends MessageHandler<Void, ListUsersReply> {
    private final Client client;

    @Inject
    ListUsersReplyHandler(Client client) {
        super(ListUsersReply.class);
        this.client = client;
    }

    @Override
    public Message<Void> onMessage(Message<ListUsersReply> data, ChannelHandlerContext ctx) {
        client.onListUsersReply(data.body.room, data.body.toString());
        return null;
    }
}
