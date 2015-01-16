package com.mastfrog.scamper.hub;

import com.mastfrog.scamper.hub.impl.Rooms;
import com.mastfrog.scamper.Address;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.Sender;
import com.mastfrog.scamper.chat.api.MessageAcknowledgement;
import com.mastfrog.scamper.chat.api.NameChangeNotification;
import com.mastfrog.scamper.chat.api.RoomDescriptor;
import com.mastfrog.scamper.chat.api.RoomMessage;
import static com.mastfrog.scamper.hub.impl.Rooms.USER_NAME_KEY;
import com.mastfrog.scamper.chat.messages.ChatMessageTypes;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.NICKNAME_CHANGED;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

/**
 *
 * @author Tim Boudreau
 */
class SendMessageHandler extends MessageHandler<MessageAcknowledgement, RoomMessage> {

    private final Rooms rooms;
    private final Sender sender;

    @Inject
    SendMessageHandler(Rooms rooms, Sender sender) {
        super(RoomMessage.class);
        this.rooms = rooms;
        this.sender = sender;
    }

    @Override
    public Message<MessageAcknowledgement> onMessage(Message<RoomMessage> data, ChannelHandlerContext ctx) {
        RoomDescriptor desc = rooms.find(data.body.room);
        if (desc == null) {
            return ChatMessageTypes.ACKNOWLEDGE_MESSAGE.newMessage(new MessageAcknowledgement(false, data.body.id, 0));
        }
        Attribute<String> userNameAttribute = ctx.attr(USER_NAME_KEY);
        String oldName = userNameAttribute.get();
        List<ChannelHandlerContext> contexts = rooms.channels(desc);
        if (!Objects.equals(oldName, data.body.from)) {
            userNameAttribute.set(data.body.from);
            NameChangeNotification notification = new NameChangeNotification(oldName, data.body.from);
            Message<NameChangeNotification> message = NICKNAME_CHANGED.newMessage(notification);
            for (ChannelHandlerContext sendTo : contexts) {
                if (sendTo == ctx) {
                    continue;
                }
                if (sendTo.channel().isOpen()) {
                    sender.send(new Address((InetSocketAddress) sendTo.channel().remoteAddress()), message);
                }
            }
        }
        int count = 0;
        for (ChannelHandlerContext sendTo : contexts) {
            if (sendTo == ctx) {
                continue;
            }
            if (sendTo.channel().isOpen()) {
                sender.send(new Address((InetSocketAddress) sendTo.channel().remoteAddress()), data);
                count++;
            }
        }
        return ChatMessageTypes.ACKNOWLEDGE_MESSAGE.newMessage(
                new MessageAcknowledgement(true, data.body.id, count));
    }
}
