package com.mastfrog.scamper.hub;

import com.mastfrog.scamper.Address;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.Sender;
import com.mastfrog.scamper.hub.impl.ServerRoom;
import com.mastfrog.scamper.chat.api.JoinLeaveNotification;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.USER_JOINED_ROOM;
import java.net.InetSocketAddress;
import javax.inject.Inject;

/**
 *
 * @author Tim Boudreau
 */
public final class RoomJoinLeaveProcessor {

    private final Sender sender;

    @Inject
    RoomJoinLeaveProcessor(Sender sender) {
        this.sender = sender;
    }

    public void onRoomChange(String userName, ServerRoom old, ServerRoom roomEntered) {
        if (old != null) {
            JoinLeaveNotification leave = new JoinLeaveNotification(userName, false);
            Message<JoinLeaveNotification> msg = USER_JOINED_ROOM.newMessage(leave);
            for (InetSocketAddress addr : old.addresses()) {
                Address a = new Address(addr);
                sender.send(a, msg);
            }
        }
        if (roomEntered != null) {
            JoinLeaveNotification join = new JoinLeaveNotification(userName, true);
            Message<JoinLeaveNotification> msg = USER_JOINED_ROOM.newMessage(join);
            for (InetSocketAddress addr : roomEntered.addresses()) {
                Address a = new Address(addr);
                sender.send(a, msg);
            }
        }
    }
}
