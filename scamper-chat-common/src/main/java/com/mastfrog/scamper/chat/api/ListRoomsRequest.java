package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public class ListRoomsRequest implements Serializable {

    public final int type;

    @JsonCreator
    public ListRoomsRequest(@JsonProperty("type") int type) {
        // Jackson insistes on at least one property
        this.type = type;
    }

    public ListRoomsRequest() {
        this(1);
    }
}
