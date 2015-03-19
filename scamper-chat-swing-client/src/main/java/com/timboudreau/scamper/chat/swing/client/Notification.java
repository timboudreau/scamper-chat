package com.timboudreau.scamper.chat.swing.client;

/**
 *
 * @author Tim Boudreau
 */
public class Notification {

    public final NotificationType type;
    public final String message;

    public Notification(NotificationType type, String message) {
        this.type = type;
        this.message = message;
    }

    public String toString() {
        return type.name() + ": " + message;
    }
}
