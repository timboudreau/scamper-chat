package com.mastfrog.scamper.chat.cli;

import com.mastfrog.giulius.ShutdownHookRegistry;
import com.mastfrog.scamper.chat.api.RoomMessage;
import com.mastfrog.scamper.chat.spi.ClientControl;
import com.mastfrog.scamper.chat.spi.Room;
import io.netty.util.CharsetUtil;
import java.io.Console;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.fusesource.jansi.Ansi;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Attribute.RESET;
import static org.fusesource.jansi.Ansi.Color.*;
import org.fusesource.jansi.AnsiConsole;

/**
 *
 * @author Tim Boudreau
 */
@Singleton
public class CLI implements Runnable {

    private final ExecutorService svc = Executors.newFixedThreadPool(2);
    private final AtomicReference<Room> room = new AtomicReference<>();
    private final ClientControl ctrl;
    private final CountDownLatch onExit = new CountDownLatch(1);
    private final Console console = System.console();
    private final Map<String, Encrypter> cryptoForRoom = new HashMap<>();

    @SuppressWarnings("LeakingThisInConstructor")
    @Inject
    CLI(ClientControl ctrl, ShutdownHookRegistry reg) {
        AnsiConsole.systemInstall();
        this.ctrl = ctrl;
        reg.add(svc);
        if (console == null) {
            System.err.println("Process has no console.");
            System.exit(2);
        }
        svc.submit(this);
    }

    public CLI start() {
        return this;
    }

    public void shutdown() {
        ctrl.disconnect();
        System.exit(0);
    }

    void await() throws InterruptedException {
        onExit.await();
    }

    void println(CliMessageType type, String msg) {
        if (type == CliMessageType.ACK) {
            return;
        }
        switch (type) {
            case SYSTEM:
                System.out.println(ansi().fgBright(type.fg()).a(msg).a(RESET));
                return;
            case ERROR:
                System.out.println(ansi().a(Ansi.Attribute.NEGATIVE_ON).fgBright(type.fg()).a(msg).a(RESET));
                return;
            case ACK:
                return;
        }
        System.out.println(ansi().fg(type.fg()).a("[" + type.name().toLowerCase() + "] ").fgBright(type.fg()).a(msg).fg(DEFAULT));
    }

    void println(Object msg) {
        System.out.println(msg);
    }

    void printMessage(RoomMessage msg) {
        String theMessage = msg.message;
        Room room = this.room.get();
        if (room != null && cryptoForRoom.get(room.name()) != null) {
            Encrypter enc = cryptoForRoom.get(room.name());
            theMessage = enc.decrypt(theMessage);
        }
        System.out.println(ansi().fgBright(Ansi.Color.BLACK).a(msg.from + ": \t").fgBright(Ansi.Color.YELLOW).a(theMessage).fg(DEFAULT));
    }

    synchronized void setRoom(Room room) {
        if (cryptoForRoom.get(room.name()) != null) {
            println(CliMessageType.SYSTEM, "You joined room " + room + " - conversation encrypted.");
        } else {
            println(CliMessageType.SYSTEM, "You joined room " + room + ".");
        }
        this.room.set(room);
    }

    public static final String JOIN_CMD = "join";
    public static final String QUIT_CMD = "quit";
    public static final String NICK_CMD = "nick";
    public static final String WHO_CMD = "who";
    public static final String HELP_CMD = "help";
    public static final String ROOMS_CMD = "rooms";

    private boolean onCommand(String command) {
        switch (command) {
            case QUIT_CMD:
                ctrl.disconnect();
                System.exit(0);
                return true;
            case WHO_CMD:
                Room r = room.get();
                ctrl.listUsers(r.name());
                return true;
            case ROOMS_CMD:
                ctrl.listRooms();
                return true;
            case HELP_CMD:
                println(CliMessageType.SYSTEM, "Help:");
                println(CliMessageType.SYSTEM, "\t/rooms - \tlist rooms");
                println(CliMessageType.SYSTEM, "\t/join [room] - \tjoin or create a room");
                println(CliMessageType.SYSTEM, "\t/join [room] [password] - \tjoin or create a password-protected room. If the room name starts with _ it will not show up in the list from /rooms");
                println(CliMessageType.SYSTEM, "\t/nick [nickname] - \tchange your name");
                println(CliMessageType.SYSTEM, "\t/who - \tlist users in this room");
                println(CliMessageType.SYSTEM, "\t/help - \tshow this help");
                println(CliMessageType.SYSTEM, "\t/quit - \tquit");
                return true;
        }
        return false;
    }

    private boolean onCommand(String command, String value1, String value2) {
        switch (command) {
            case JOIN_CMD: {
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-512");
                    byte[] bytes = digest.digest(value2.getBytes(CharsetUtil.UTF_8));
                    value2 = Base64.getEncoder().encodeToString(bytes);
                    cryptoForRoom.put(value1, new Encrypter(value2));
                    ctrl.joinRoom(value1, value2);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(CLI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

    private boolean onCommand(String command, String value) {
        if (value.isEmpty()) {
            println(CliMessageType.ERROR, "Empty value for " + command);
            return true;
        }
        switch (command) {
            case JOIN_CMD:
                ctrl.joinRoom(value);
                return true;
            case NICK_CMD:
                ctrl.setUserName(value);
                println(CliMessageType.SYSTEM, "Nickname set to " + value);
                return true;
            default:
                println(CliMessageType.ERROR, "Unknown command '" + command + "'");
                return false;
        }
    }

    public static final Pattern CMD_PATTERN = Pattern.compile("\\/([a-z]*)$");
    public static final Pattern CMD_ARG_PATTERN = Pattern.compile("\\/([a-z]*?)\\s+(.*)");
    public static final Pattern CMD_ARG_ARG_PATTERN = Pattern.compile("\\/([a-z]*?)\\s+(.*?)\\s+(.*)");

    void onLine(String line) {
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }
        if (line.length() > 0 && '/' == line.charAt(0)) {
            Matcher m = CMD_ARG_ARG_PATTERN.matcher(line);
            boolean cmdMatched;
            if (cmdMatched = m.find()) {
                onCommand(m.group(1), m.group(2), m.group(3));
            } else {
                m = CMD_ARG_PATTERN.matcher(line);
                if (cmdMatched = m.find()) {
                    onCommand(m.group(1), m.group(2));
                } else {
                    m = CMD_PATTERN.matcher(line);
                    if (cmdMatched = m.find()) {
                        onCommand(m.group(1));
                    }
                }
            }
            if (!cmdMatched) {
                println(CliMessageType.ERROR, "Unknown command");
            }
            return;
        }
        sendMessage(line);
    }

    private static final int LIMIT = 120;
    private static final int ENC_LIMIT = 80;

    private List<String> trim(String s, boolean encrypt) {
        // Okay, the purpose of this hot little mess:
        // reassembly of fragmented SCTP messages is not working.
        // Something is horribly wrong either with the channel
        // settings, or lksctp or the JDK's sctp implementation.
        // Basically, if the message is longer than an arbitrary
        // threshold, then the recipient recieves *multiple* messages
        // - when part of the point of sctp is that it's message-oriented.
        // Might be a buffer size issue, or might be something else.
        //
        // So for now, we just attempt to ensure that we don't hit the
        // magic limit and pray.
        int limit = encrypt ? ENC_LIMIT : LIMIT;
        if (s.length() < LIMIT) {
            return Collections.singletonList(s);
        }
        List<String> l = new LinkedList<>();
        StringBuilder curr = new StringBuilder();
        String[] words = s.split("\\s+");
        for (String word : words) {
            if (curr.length() + word.length() + 1 > limit) {
                l.add(curr.toString());
                curr = new StringBuilder();
            }
            curr.append(word).append(" ");
        }
        if (curr.length() > 0) {
            l.add(curr.toString());
        }
        return l;
    }

    private void sendMessage(String line) {
        Room room = this.room.get();
        if (room == null) {
            println(CliMessageType.ERROR, "\"Not in a room yet - use /join [room] or wait for acknowldegement if you already have joined one\" " + line);
        } else {
            Encrypter enc = cryptoForRoom.get(room.name());
            if (enc != null) {
                line = enc.encrypt(line);
            }
            room.send(line);
//            List<String> lines = trim(line, enc != null);
//            for (String s : lines) {
//                if (enc != null) {
//                    s = enc.encrypt(s);
//                }
//                room.send(s);
//            }
        }
    }

    @Override
    public void run() {
        System.out.println(ansi().eraseScreen().a(Attribute.RESET).a(Attribute.INTENSITY_BOLD).a(Attribute.NEGATIVE_ON).a("Welcome to Scamper-Chat"));
        System.out.println(ansi().a(Attribute.INTENSITY_BOLD_OFF).a("For help type /help"));
        System.out.println(ansi().a(Attribute.RESET));
        ctrl.joinRoom("Home");
        try {
            for (String line = console.readLine(); line != null; line = console.readLine()) {
                onLine(line.trim());
            }
        } catch (Exception ex) {
            Logger.getLogger(CLI.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            onExit.countDown();
        }
    }
}
