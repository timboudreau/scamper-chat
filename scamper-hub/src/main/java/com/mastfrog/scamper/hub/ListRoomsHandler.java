package com.mastfrog.scamper.hub;

import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.hub.impl.Rooms;
import com.mastfrog.scamper.chat.api.*;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.REPLY_LIST_ROOMS;
import com.mastfrog.scamper.hub.impl.ServerRoom;
import io.netty.channel.ChannelHandlerContext;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author Tim Boudreau
 */
public class ListRoomsHandler extends MessageHandler<ListRoomsReply, ListRoomsRequest> {

    private final Rooms rooms;

    @Inject
    ListRoomsHandler(Rooms rooms) {
        super(ListRoomsRequest.class);
        this.rooms = rooms;
    }

    @Override
    public Message<ListRoomsReply> onMessage(Message<ListRoomsRequest> data, ChannelHandlerContext ctx) {
        ListRoomsReply.Builder builder = new ListRoomsReply.Builder();
        boolean hasHidden = false;
        for (ServerRoom room : rooms) {
            if (room.isHidden()) {
                hasHidden = true;
                continue;
            }
            ListRoomsReply.RoomInfo.Builder roomBuilder = builder.addRoom(room.name(), room.hasPassword());
            List<String> users = room.users();
            Collections.sort(users);
            for (String user : users) {
                roomBuilder = roomBuilder.addParticipant(user);
            }
            builder = roomBuilder.close();
        }
        builder.setHasHiddenRooms(hasHidden);
        return REPLY_LIST_ROOMS.newMessage(builder.build());
    }
}
