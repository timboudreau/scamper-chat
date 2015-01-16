package com.mastfrog.scamper.chat.cli;

import org.fusesource.jansi.Ansi;

/**
 *
 * @author Tim Boudreau
 */
public enum CliMessageType {
    ERROR,
    SYSTEM,
    MESSAGE,
    ACK;

    public Ansi.Color fg() {
        switch (this) {
            case ACK:
                return Ansi.Color.CYAN;
            case ERROR:
                return Ansi.Color.RED;
            case MESSAGE:
                return Ansi.Color.YELLOW;
            case SYSTEM:
                return Ansi.Color.MAGENTA;
            default:
                throw new AssertionError();
        }
    }
}
