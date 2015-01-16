package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public class ListUsersRequest implements Serializable {

    public final String room;
    @JsonCreator
    public ListUsersRequest(@JsonProperty("room") String room) {
        this.room = room;
    }
}
