package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.api.RoomJoinedAcknowledgement;
import com.mastfrog.scamper.chat.spi.Client;
import com.mastfrog.scamper.chat.spi.ClientControl;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author Tim Boudreau
 */
final class RoomJoinedHandler extends MessageHandler<Void, RoomJoinedAcknowledgement> {

    private final Client client;
    private final ClientControlImpl ctrlImpl;

    @Inject
    RoomJoinedHandler(Client client, ClientControl ctrl) {
        super(RoomJoinedAcknowledgement.class);
        ctrlImpl = (ClientControlImpl) ctrl;
        this.client = client;
    }

    @Override
    public Message<Void> onMessage(Message<RoomJoinedAcknowledgement> data, ChannelHandlerContext ctx) {
        if (data.body.failed) {
            client.onRoomJoinFailed(data.body.room);
        } else {
            client.onRoomJoined(ctrlImpl.newRoom(data.body));
        }
        return null;
    }

}
