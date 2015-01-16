package com.mastfrog.scamper.chat.messages;

import com.mastfrog.scamper.MessageType;

/**
 *
 * @author Tim Boudreau
 */
public class ChatMessageTypes {

    public static final MessageType JOIN_ROOM = new MessageType("joinRoom", 1, 1);
    public static final MessageType ROOM_JOIN_OR_LEAVE = new MessageType("roomJoined", 1, 2);
    public static final MessageType SEND_MESSAGE_TO_ROOM = new MessageType("roomMessage", 1, 3);
    public static final MessageType ACKNOWLEDGE_MESSAGE = new MessageType("messageAck", 1, 4);
    public static final MessageType USER_JOINED_ROOM = new MessageType("userJoinedRoom", 1, 5);
    public static final MessageType NICKNAME_CHANGED = new MessageType("nickChanged", 1, 6);
    public static final MessageType SERVER_MESSAGE = new MessageType("serverMessage", 1, 7);
    public static final MessageType REQUEST_LIST_USERS = new MessageType("listUsers", 1, 8);
    public static final MessageType REPLY_LIST_USERS = new MessageType("replyListUsers", 1, 9);
    public static final MessageType REQUEST_LIST_ROOMS = new MessageType("listRooms", 1, 10);
    public static final MessageType REPLY_LIST_ROOMS = new MessageType("replyListRooms", 1, 11);

    private ChatMessageTypes() {
    }
}
