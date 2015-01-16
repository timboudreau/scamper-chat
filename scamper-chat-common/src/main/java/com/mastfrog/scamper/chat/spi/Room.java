package com.mastfrog.scamper.chat.spi;

/**
 *
 * @author Tim Boudreau
 */
public interface Room {

    public void send(String msg);
    public String name();
}
