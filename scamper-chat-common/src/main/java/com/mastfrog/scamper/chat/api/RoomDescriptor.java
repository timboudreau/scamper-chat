package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public class RoomDescriptor implements Serializable {

    public final String userName;
    public final String room;
    public final char[] roomPassword;

    @JsonCreator
    public RoomDescriptor(@JsonProperty("userName") String userName, @JsonProperty("room") String room, @JsonProperty(value = "password", required = false) char[] roomPassword) {
        if (room == null) {
            throw new NullPointerException("name");
        }
        if (room.isEmpty()) {
            throw new IllegalArgumentException("Empty name");
        }
        this.userName = userName;
        this.room = room;
        this.roomPassword = roomPassword;
    }

    public boolean equals(Object o) {
        return o == this || o instanceof RoomDescriptor && ((RoomDescriptor) o).room.equals(room);
    }

    public int hashCode() {
        return room.hashCode();
    }
}
