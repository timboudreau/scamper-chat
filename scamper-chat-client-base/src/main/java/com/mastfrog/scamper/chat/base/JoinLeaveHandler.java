package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.api.JoinLeaveNotification;
import com.mastfrog.scamper.chat.spi.Client;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author Tim Boudreau
 */
final class JoinLeaveHandler extends MessageHandler<Void, JoinLeaveNotification> {
    private final Client client;
    
    @Inject
    JoinLeaveHandler(Client client) {
        super(JoinLeaveNotification.class);
        this.client = client;
    }

    @Override
    public Message<Void> onMessage(Message<JoinLeaveNotification> data, ChannelHandlerContext ctx) {
        if (data.body.joined) {
            client.onUserJoinedRoom(data.body.user);
        } else {
            client.onUserLeftRoom(data.body.user);
        }
        return null;
    }
}
