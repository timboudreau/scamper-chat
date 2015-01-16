package com.mastfrog.scamper.hub;

import com.google.common.base.Objects;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.MessageHandler;
import com.mastfrog.scamper.hub.impl.Rooms;
import com.mastfrog.scamper.hub.impl.ServerRoom;
import com.mastfrog.scamper.chat.api.ListUsersReply;
import com.mastfrog.scamper.chat.api.ListUsersRequest;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.REPLY_LIST_USERS;
import io.netty.channel.ChannelHandlerContext;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author Tim Boudreau
 */
public class ListUsersHandler extends MessageHandler<ListUsersReply, ListUsersRequest> {

    private final Rooms rooms;

    @Inject
    ListUsersHandler(Rooms rooms) {
        super(ListUsersRequest.class);
        this.rooms = rooms;
    }

    @Override
    public Message<ListUsersReply> onMessage(Message<ListUsersRequest> data, ChannelHandlerContext ctx) {
        ServerRoom room = rooms.find(ctx);
        String target = data.body.room;
        if (!Objects.equal(target, room == null ? null : room.name())) {
            ListUsersReply reply = new ListUsersReply(room.name(), new String[]{"You are not in " + target});
            return REPLY_LIST_USERS.newMessage(reply);
        }
        if (room == null) {
            return null;
        }
        List<String> users = room.users();
        Collections.sort(users);
        ListUsersReply reply = new ListUsersReply(room.name(), users.toArray(new String[users.size()]));
        return REPLY_LIST_USERS.newMessage(reply);
    }
}
