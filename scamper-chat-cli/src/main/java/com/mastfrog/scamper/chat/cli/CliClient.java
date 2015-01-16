package com.mastfrog.scamper.chat.cli;

import com.mastfrog.scamper.chat.api.ListRoomsReply;
import com.mastfrog.scamper.chat.api.RoomMessage;
import com.mastfrog.scamper.chat.spi.Client;
import com.mastfrog.scamper.chat.spi.ClientControl;
import com.mastfrog.scamper.chat.spi.Room;
import com.mastfrog.util.Checks;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.fusesource.jansi.Ansi;
import static org.fusesource.jansi.Ansi.ansi;

/**
 *
 * @author Tim Boudreau
 */
@Singleton
public class CliClient implements Client {

    private final AtomicReference<ClientControl> control = new AtomicReference<>();
    private final CLI cli;

    @Inject
    CliClient(CLI cli) {
        this.cli = cli;
    }

    @Override
    public void onInit(ClientControl ctrl) {
        Checks.notNull("ctrl", ctrl);
        control.set(ctrl);
        cli.start();
    }

    @Override
    public void onConnectFailed() {
        cli.println(CliMessageType.SYSTEM, "Connect failed");;
    }

    @Override
    public long onMessage(String room, RoomMessage msg) {
        cli.printMessage(msg);
        return msg.id;
    }

    @Override
    public void onMessageAcknowledged(long id, int sentTo) {
        cli.println(CliMessageType.ACK, id + " received by " + sentTo);
    }

    @Override
    public void onRoomJoined(Room room) {
        cli.setRoom(room);
    }

    @Override
    public void onUserJoinedRoom(String user) {
        cli.println(CliMessageType.SYSTEM, user + " joined.");
    }

    @Override
    public void onUserLeftRoom(String user) {
        cli.println(CliMessageType.SYSTEM, user + " left");
    }

    @Override
    public void onUserNicknameChanged(String old, String nue) {
        cli.println(CliMessageType.SYSTEM, old + " is now known as " + nue);
    }

    @Override
    public void onServerMessage(String message, boolean shutdownAdvised) {
        cli.println(CliMessageType.ERROR, message);
        if (shutdownAdvised) {
            cli.println(CliMessageType.SYSTEM, "Exiting.");
            cli.shutdown();
        }
    }

    @Override
    public void onListUsersReply(String room, String users) {
        cli.println(CliMessageType.SYSTEM, "Users in " + room + ": " + users);
    }

    public void onRoomJoinFailed(String room) {
        cli.println(CliMessageType.ERROR, "Incorrect password for " + room);
    }

    @Override
    public void onListRoomsReply(ListRoomsReply rooms) {
        cli.println(CliMessageType.SYSTEM, "Rooms:");
        Ansi ansi = ansi();
        for (ListRoomsReply.RoomInfo info : rooms) {
            if (info.hasPassword) {
                ansi = ansi.fgBright(Ansi.Color.YELLOW).a(Ansi.Attribute.INTENSITY_BOLD)
                        .a('\t' + info.name).a(Ansi.Attribute.INTENSITY_BOLD_OFF)
                        .a(" [password]\t");
            } else {
                ansi = ansi.fgBright(Ansi.Color.YELLOW).a(Ansi.Attribute.INTENSITY_BOLD)
                        .a(info.name + '\t').a(Ansi.Attribute.INTENSITY_BOLD_OFF);
            }
            for (Iterator<String> it = info.iterator(); it.hasNext();) {
                String user = it.next();
                ansi = ansi.fgBright(Ansi.Color.WHITE).a(user);
                if (it.hasNext()) {
                    ansi = ansi.a(", ");
                }
            }
            ansi = ansi.newline();
        }
        ansi = ansi.a(Ansi.Attribute.RESET);
        cli.println(ansi);
    }
}
