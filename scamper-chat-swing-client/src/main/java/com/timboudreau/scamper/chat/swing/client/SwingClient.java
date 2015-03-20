package com.timboudreau.scamper.chat.swing.client;

import com.google.inject.AbstractModule;
import com.mastfrog.scamper.chat.api.ListRoomsReply;
import com.mastfrog.scamper.chat.api.RoomMessage;
import com.mastfrog.scamper.chat.spi.Client;
import com.mastfrog.scamper.chat.spi.ClientControl;
import com.mastfrog.scamper.chat.spi.Room;
import com.mastfrog.scamper.chat.base.ScamperClient;
import com.timboudreau.scamper.chat.swing.client.Prefs.PrefsListener;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 *
 * @author Tim Boudreau
 */
@Singleton
public class SwingClient implements Client {

    private ClientControl ctrl;
    private final UIModels ui;
    private Room room;

    @Inject
    SwingClient(UIModels ui) {
        this.ui = ui;
    }

    @Override
    public void onInit(ClientControl ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public void onConnectFailed() {
        ui.onNotification(new Notification(NotificationType.ERROR, "Could not connect to server"));
    }

    @Override
    public long onMessage(String room, RoomMessage msg) {
        if (ui.isRoom(room)) {
            ui.onMessage(msg);
        }
        return msg.id;
    }

    @Override
    public void onMessageAcknowledged(long id, int sentTo) {
//        ui.onNotification(new Notification(NotificationType.NOTIFICATION, "Message " + id + " sent to " + sentTo));
    }

    @Override
    public void onRoomJoined(Room room) {
        this.room = room;
        ui.setRoom(room);
        ui.onNotification(NotificationType.NOTIFICATION.notification("Joined " + room.name()));
    }

    @Override
    public void onUserJoinedRoom(String user) {
        ui.onNotification(NotificationType.NOTIFICATION.notification(user + " joined " + (room == null ? "" : room.name())));
        ui.userJoinedRoom(user);
    }

    @Override
    public void onUserLeftRoom(String user) {
        ui.onNotification(NotificationType.NOTIFICATION.notification(user + " left " + (room == null ? "" : room.name())));
        ui.userLeftRoom(user);
    }

    @Override
    public void onUserNicknameChanged(String old, String nue) {
        ui.onNotification(NotificationType.NOTIFICATION.notification(old + " is now known as " + nue + " in " + (room == null ? "" : room.name())));
        ui.nicknameChanged(old, nue);
    }

    @Override
    public void onServerMessage(String message, boolean shutdownAdvised) {
        ui.onNotification(NotificationType.SERVER.notification(message));
        if (shutdownAdvised) {
            ui.onNotification(NotificationType.SERVER.notification("Shutdown advised"));
        }
    }

    @Override
    public void onListUsersReply(String room, String users) {
        if (ui.isRoom(room)) {
            String[] u = users.split(",");
            for (int i = 0; i < u.length; i++) {
                u[i] = u[i].trim();
            }
            ui.setRoomMembers(u);
        }
    }

    @Override
    public void onRoomJoinFailed(String room) {
        ui.onNotification(NotificationType.ERROR.notification("Could not join '" + room + "'"));
    }

    @Override
    public void onListRoomsReply(ListRoomsReply rooms) {
        ui.setRooms(rooms);
    }

    @Override
    public void onError(Throwable error) {
        ui.onNotification(NotificationType.ERROR.notification(error.getMessage() == null ? error.getClass().getName() : error.getMessage()));
    }

    static PrefsListener pl;

    static {
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("swing.aatext", "true");
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
    }

    public static void main(final String[] args) throws IOException, InterruptedException, UnsupportedLookAndFeelException {
        System.setProperty("io.netty.leakDetectionLevel", "disable");
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Prefs prefs = new Prefs();
        final ScamperClient client = new ScamperClient(SwingClient.class, new PrefsModule(prefs));
        final UIModels mdls = client.start(prefs.getHost(), prefs.getPort(), UIModels.class, args);
        final AtomicReference<MainPanel> pnl = new AtomicReference<>();
        prefs.addListener(pl = new PrefsListener() {

            @Override
            public void change(Set<Prefs.PrefValue> set, Prefs src) {
                if (set.contains(Prefs.PrefValue.SERVER)) {
                    mdls.shutdown();
                }
                MainPanel mp = pnl.get();
                if (mp != null) {
                    JFrame jf = (JFrame) mp.getTopLevelAncestor();
                    if (jf != null) {
                        jf.setVisible(false);
                        jf.dispose();
                    }
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                main(args);
                            } catch (IOException | InterruptedException | UnsupportedLookAndFeelException ex) {
                                Logger.getLogger(SwingClient.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                }
            }

        });

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                MainPanel mp = MainPanel.showUI(mdls, prefs);
                pnl.set(mp);
            }
        });
    }

    static class PrefsModule extends AbstractModule {

        private final Prefs prefs;

        public PrefsModule(Prefs prefs) {
            this.prefs = prefs;
        }

        @Override
        protected void configure() {
            bind(Prefs.class).toInstance(prefs);
        }
    }
}
