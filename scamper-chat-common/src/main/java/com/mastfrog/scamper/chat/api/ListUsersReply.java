package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author Tim Boudreau
 */
public class ListUsersReply implements Iterable<String>, Serializable {

    public final String room;
    public final String[] usernames;

    @JsonCreator
    public ListUsersReply(@JsonProperty("room") String room, @JsonProperty("usernames") String[] usernames) {
        this.room = room;
        this.usernames = usernames;
    }

    @Override
    public Iterator<String> iterator() {
        return Arrays.asList(usernames).iterator();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < usernames.length; i++) {
            sb.append(usernames[i]);
            if (i != usernames.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
