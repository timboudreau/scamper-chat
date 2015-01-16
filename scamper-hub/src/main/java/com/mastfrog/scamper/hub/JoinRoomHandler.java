package com.mastfrog.scamper.hub;

import com.mastfrog.scamper.hub.impl.Rooms;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.chat.messages.ChatMessageTypes;
import com.mastfrog.scamper.hub.impl.ServerRoom;
import com.mastfrog.scamper.chat.api.RoomDescriptor;
import com.mastfrog.scamper.chat.api.RoomJoinedAcknowledgement;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import javax.inject.Inject;

/**
 *
 * @author Tim Boudreau
 */
public class JoinRoomHandler extends MessageHandler<RoomJoinedAcknowledgement, RoomDescriptor> {

    private final Rooms rooms;
    public static final AttributeKey<ServerRoom> ROOM_KEY = AttributeKey.valueOf(JoinRoomHandler.class, "room");

    @Inject
    public JoinRoomHandler(Rooms rooms) {
        super(RoomDescriptor.class);
        this.rooms = rooms;
    }

    @Override
    public Message<RoomJoinedAcknowledgement> onMessage(Message<RoomDescriptor> data, ChannelHandlerContext ctx) {
        ServerRoom room = rooms.addOrCreate(data.body, data.body.roomPassword, ctx);
        if (room == null) {
            return ChatMessageTypes.ROOM_JOIN_OR_LEAVE.newMessage(new RoomJoinedAcknowledgement(data.body.room, 0, true));
        }
        Attribute<ServerRoom> roomAttr = ctx.attr(ROOM_KEY);
        roomAttr.set(room);
        Attribute<String> userNameAttr = ctx.attr(Rooms.USER_NAME_KEY);
        userNameAttr.set(data.body.userName);
        return ChatMessageTypes.ROOM_JOIN_OR_LEAVE.newMessage(new RoomJoinedAcknowledgement(data.body.room, room.size(), false));
    }
}
