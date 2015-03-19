package com.timboudreau.scamper.chat.swing.client;

import java.awt.Color;

/**
 *
 * @author Tim Boudreau
 */
public enum NotificationType {

    SERVER,
    ERROR,
    NOTIFICATION;
    
    public Notification notification(String msg) {
        return new Notification(this, msg);
    }
    
    public Color background() {
        switch(this) {
            case SERVER:
                return new Color(220, 220, 255);
            case ERROR :
                return new Color(140, 80, 80);
            case NOTIFICATION :
                return new Color(240, 240, 200);
            default :
                throw new AssertionError();
        }
    }
    
    public Color foreground() {
        switch(this) {
            case SERVER :
                return new Color(0, 0, 40);
            case ERROR :
                return new Color(255, 0, 0);
            case NOTIFICATION :
                return new Color(120, 120, 0);
            default :
                throw new AssertionError();
        }
    }
}
