package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public class RoomMessage implements Serializable {

    public final String room;
    public final long id;
    public final String message;
    public final String from;

    @JsonCreator
    public RoomMessage(@JsonProperty("room") String room, @JsonProperty("id") long id, @JsonProperty("message") String message, @JsonProperty("from") String from) {
        this.room = room;
        this.id = id;
        this.message = message;
        this.from = from;
    }
}
