package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public class MessageAcknowledgement implements Serializable {

    public final long id;
    public final int sentTo;
    public final boolean success;

    @JsonCreator
    public MessageAcknowledgement(@JsonProperty("success") boolean success, @JsonProperty("id") long id, @JsonProperty("sentTo") int sentTo) {
        this.success = success;
        this.id = id;
        this.sentTo = sentTo;
    }
}
