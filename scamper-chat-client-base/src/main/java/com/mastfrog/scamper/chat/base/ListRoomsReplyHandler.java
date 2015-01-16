package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.api.ListRoomsReply;
import com.mastfrog.scamper.chat.spi.Client;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author Tim Boudreau
 */
public class ListRoomsReplyHandler extends MessageHandler<Void, ListRoomsReply> {

    private final Client client;

    @Inject
    ListRoomsReplyHandler(Client client) {
        super(ListRoomsReply.class);
        this.client = client;
    }

    @Override
    public Message<Void> onMessage(Message<ListRoomsReply> data, ChannelHandlerContext ctx) {
        client.onListRoomsReply(data.body);
        return null;
    }
}
