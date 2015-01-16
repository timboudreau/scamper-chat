package com.mastfrog.scamper.chat.spi;

/**
 *
 * @author Tim Boudreau
 */
public interface ClientControl {

    void listUsers(String room);

    void setUserName(String un);

    void disconnect();

    void joinRoom(String room);

    void joinRoom(String room, String password);
    
    void listRooms();
    
}
