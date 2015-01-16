package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public class RoomJoinedAcknowledgement implements Serializable {
    public final String room;
    public final int members;
    public final boolean failed;

    @JsonCreator
    public RoomJoinedAcknowledgement(@JsonProperty("room") String room, @JsonProperty("members") int members, @JsonProperty("failed") boolean failed) {
        this.room = room;
        this.members = members;
        this.failed = failed;
    }
}
