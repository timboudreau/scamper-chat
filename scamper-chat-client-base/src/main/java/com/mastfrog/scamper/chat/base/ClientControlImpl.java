package com.mastfrog.scamper.chat.base;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mastfrog.scamper.Address;
import com.mastfrog.scamper.Control;
import com.mastfrog.scamper.Message;
import com.mastfrog.scamper.Sender;
import com.mastfrog.scamper.chat.api.ListRoomsRequest;
import com.mastfrog.scamper.chat.messages.ChatMessageTypes;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.REQUEST_LIST_USERS;
import com.mastfrog.scamper.chat.api.ListUsersRequest;
import com.mastfrog.scamper.chat.api.RoomDescriptor;
import com.mastfrog.scamper.chat.api.RoomJoinedAcknowledgement;
import com.mastfrog.scamper.chat.api.RoomMessage;
import static com.mastfrog.scamper.chat.messages.ChatMessageTypes.REQUEST_LIST_ROOMS;
import com.mastfrog.scamper.chat.spi.Client;
import com.mastfrog.scamper.chat.spi.ClientControl;
import com.mastfrog.scamper.chat.spi.Room;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author Tim Boudreau
 */
@Singleton
class ClientControlImpl implements ClientControl {

    final Control<Sender> sender;
    final Address address;
    String name = "me";
    private final AtomicLong ids = new AtomicLong(1);
    private final Client client;
    public static final String PREFS_KEY_NICKNAME = "nickname";

    @Inject
    @SuppressWarnings("LeakingThisInConstructor")
    ClientControlImpl(Control<Sender> sender, Address address, Client client) {
        this.sender = sender;
        this.address = address;
        this.client = client;
        name = prefs().get(PREFS_KEY_NICKNAME, name);
    }

    private Preferences prefs() {
        return Preferences.userNodeForPackage(ClientControlImpl.class);
    }

    void init() {
        client.onInit(this);
    }

    public void setUserName(String un) {
        this.name = un;
        Preferences prefs = prefs();
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(ClientControlImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void disconnect() {
        sender.shutdown();
    }

    @Override
    public void joinRoom(String room) {
        joinRoom(room, null);
    }

    Room newRoom(RoomJoinedAcknowledgement ack) {
        return new RoomImpl(ack);
    }

    @Override
    public void joinRoom(String room, String password) {
        try {
            Message<RoomDescriptor> joinMessage = ChatMessageTypes.JOIN_ROOM.newMessage(new RoomDescriptor(name, room, password == null ? null : password.toCharArray()));
            Sender s = sender.get();
            s.send(address, joinMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listUsers(String room) {
        sender.get().send(address, REQUEST_LIST_USERS.newMessage(new ListUsersRequest(room)));
    }

    @Override
    public void listRooms() {
        sender.get().send(address, REQUEST_LIST_ROOMS.newMessage(new ListRoomsRequest()));
    }

    private class RoomImpl implements Room {

        private final String room;
        private int initialMembers;

        public RoomImpl(RoomJoinedAcknowledgement roomAck) {
            this.room = roomAck.room;
            initialMembers = roomAck.members;
        }

        public String name() {
            return room;
        }

        @Override
        public void send(String msg) {
            Message<RoomMessage> roomMessage = ChatMessageTypes.SEND_MESSAGE_TO_ROOM.newMessage(new RoomMessage(room, ids.getAndIncrement(), msg, name));
            sender.get().send(address, roomMessage);
        }

        public String toString() {
            return room + " (" + initialMembers + ")";
        }
    }
}
