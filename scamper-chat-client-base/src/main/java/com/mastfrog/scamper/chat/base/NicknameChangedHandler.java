package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.api.NameChangeNotification;
import com.mastfrog.scamper.chat.spi.Client;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author Tim Boudreau
 */
final class NicknameChangedHandler extends MessageHandler<Void, NameChangeNotification> {

    private final Client client;

    @Inject
    NicknameChangedHandler(Client client) {
        super(NameChangeNotification.class);
        this.client = client;
    }

    @Override
    public Message<Void> onMessage(Message<NameChangeNotification> data, ChannelHandlerContext ctx) {
        client.onUserNicknameChanged(data.body.oldName, data.body.newName);
        return null;
    }
}
