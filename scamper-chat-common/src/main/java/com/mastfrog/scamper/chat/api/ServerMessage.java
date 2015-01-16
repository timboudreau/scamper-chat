package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public class ServerMessage implements Serializable {

    public final String message;
    public final boolean clientShutdownAdvised;

    @JsonCreator
    public ServerMessage(@JsonProperty("message") String message, @JsonProperty("clientShutdownAdvised") boolean clientShutdownAdvised) {
        this.message = message;
        this.clientShutdownAdvised = clientShutdownAdvised;
    }
}
