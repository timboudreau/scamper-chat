package com.mastfrog.scamper.chat.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Tim Boudreau
 */
public class NameChangeNotification implements Serializable {

    public final String oldName;
    public final String newName;

    @JsonCreator
    public NameChangeNotification(@JsonProperty("oldName") String oldName, @JsonProperty("newName") String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }
}
