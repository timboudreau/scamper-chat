package com.mastfrog.scamper.chat.spi;

import com.mastfrog.scamper.chat.api.ListRoomsReply;
import com.mastfrog.scamper.chat.api.RoomMessage;

/**
 *
 * @author Tim Boudreau
 */
public interface Client {

    void onInit(ClientControl ctrl);

    void onConnectFailed();

    long onMessage(String room, RoomMessage msg);

    void onMessageAcknowledged(long id, int sentTo);

    void onRoomJoined(Room room);

    void onUserJoinedRoom(String user);

    void onUserLeftRoom(String user);

    void onUserNicknameChanged(String old, String nue);

    void onServerMessage(String message, boolean shutdownAdvised);

    void onListUsersReply(String room, String users);

    void onRoomJoinFailed(String room);
    
    void onListRoomsReply(ListRoomsReply rooms);
}
