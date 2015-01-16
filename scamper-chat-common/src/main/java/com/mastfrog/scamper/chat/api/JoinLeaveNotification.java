package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Tim Boudreau
 */
public class JoinLeaveNotification {

    public final String user;
    public final boolean joined;

    @JsonCreator
    public JoinLeaveNotification(@JsonProperty("user") String user, @JsonProperty("joined") boolean joined) {
        this.user = user;
        this.joined = joined;
    }
}
