package com.timboudreau.scamper.chat.swing.client;

import static com.mastfrog.scamper.ProtocolModule.GUICE_BINDING_SCAMPER_BOSS_THREADS;
import com.mastfrog.scamper.chat.api.ListRoomsReply;
import com.mastfrog.scamper.chat.api.ListRoomsReply.RoomInfo;
import com.mastfrog.scamper.chat.api.RoomMessage;
import com.mastfrog.scamper.chat.crypto.Encrypter;
import com.mastfrog.scamper.chat.spi.ClientControl;
import com.mastfrog.scamper.chat.spi.Room;
import io.netty.channel.EventLoopGroup;
import io.netty.util.CharsetUtil;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author Tim Boudreau
 */
@Singleton
public class UIModels {

    private final ThreadSafeListModel<String> roomMembers = new ThreadSafeListModel<>();
    private final ThreadSafeListModel<RoomInfo> rooms = new ThreadSafeListModel<>();
    private final ClientControl ctrl;
    private final SendAction sendAction = new SendAction();
    private final ExitAction exitAction = new ExitAction();
    private final JoinAction joinAction = new JoinAction();
    private final NewRoomAction newRoomAction = new NewRoomAction();
    private final PrefsAction prefsAction = new PrefsAction();
    private final NameAction nameAction = new NameAction();
    private final ClearAction clearAction = new ClearAction();
    private final RoomInfoCellRenderer renderer;

    private final ListSelectionModel selectedRoom = new DefaultListSelectionModel();
    private final ListSelectionModel selectedMember = new DefaultListSelectionModel();

    private final StyleContext styles = new StyleContext();
    private final Document chatDocument = new DefaultStyledDocument(styles);
    private final Document chatEntry = new PlainDocument();
    private final JMenu fileMenu = new JMenu();
    private final JMenu editMenu = new JMenu();

    private Room room;

    private final List<Reference<NotificationListener>> listeners = new LinkedList<>();
    private final List<Reference<DialogListener>> dlgListeners = new LinkedList<>();
    private final EventLoopGroup workerThreadPool;
    private final Prefs prefs;
    private String userName;

    @Inject
    UIModels(ClientControl ctrl, @Named(GUICE_BINDING_SCAMPER_BOSS_THREADS) EventLoopGroup workerThreadPool, Prefs prefs) {
        userName = prefs.getUserName();
        this.ctrl = ctrl;
        renderer = new RoomInfoCellRenderer(this);
        selectedRoom.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedMember.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatEntry.addDocumentListener(sendAction);
        fileMenu.setText("File");
        fileMenu.setDisplayedMnemonicIndex(0);
        fileMenu.add(newRoomAction);
        fileMenu.add(exitAction);
        editMenu.setText("Edit");
        editMenu.setDisplayedMnemonicIndex(0);
        editMenu.add(nameAction);
        editMenu.add(joinAction);
        editMenu.add(clearAction);
        editMenu.add(prefsAction);
        selectedRoom.addListSelectionListener(joinAction);
        this.workerThreadPool = workerThreadPool;
        this.prefs = prefs;
        ctrl.setUserName(userName);
    }
    
    void addDialogListener(DialogListener l) {
        dlgListeners.add(new WeakReference<>(l));
    }
    
    private void dialogClosed() {
        for (Iterator<Reference<DialogListener>> iter = dlgListeners.iterator(); iter.hasNext();) {
            DialogListener l = iter.next().get();
            if (l == null) {
                iter.remove();
            } else {
                l.onDialogClosed();
            }
        }
    }

    public ListCellRenderer roomRenderer() {
        return renderer;
    }

    public JMenu editMenu() {
        return editMenu;
    }

    public Action clearAction() {
        return clearAction;
    }

    public Action nameAction() {
        return nameAction;
    }

    public Action prefsAction() {
        return prefsAction;
    }

    void shutdown() {
        ctrl.disconnect();
    }

    public void addNotificationListener(NotificationListener l) {
        boolean wasEmpty = listeners.isEmpty();
        listeners.add(new WeakReference<>(l));
        if (wasEmpty) {
            workerThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    ctrl.joinRoom("Home");
                }
            });
        }
    }

    void checkRooms() {
        workerThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ctrl.listRooms();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void onNotification(final Notification notification) {
        System.err.println(notification);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (Iterator<Reference<NotificationListener>> it = listeners.iterator(); it.hasNext();) {
                    Reference<NotificationListener> r = it.next();
                    NotificationListener n = r.get();
                    if (n == null) {
                        it.remove();
                    } else {
                        n.onNotification(notification);
                    }
                }
            }
        });
        int ind = chatDocument.getEndPosition().getOffset();
        try {
            Style set = styles.getStyle(StyleContext.DEFAULT_STYLE);
//            SimpleAttributeSet set = new SimpleAttributeSet();
            StyleConstants.setBold(set, true);
            StyleConstants.setForeground(set, notification.type.foreground());
            StyleConstants.setBackground(set, notification.type.background());
            StyleConstants.setSpaceBelow(set, 5);
            chatDocument.insertString(ind, notification.message + '\n', set);
        } catch (BadLocationException ex) {
            Logger.getLogger(UIModels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JMenu fileMenu() {
        return fileMenu;
    }

    public ListSelectionModel selectedRoom() {
        return selectedRoom;
    }

    public ListSelectionModel selectedMember() {
        return selectedMember;
    }

    public Document chatDocument() {
        return chatDocument;
    }

    public Document chatEntryDocument() {
        return chatEntry;
    }

    public ListModel roomMembers() {
        return roomMembers;
    }

    public ListModel rooms() {
        return rooms;
    }

    public Action sendAction() {
        return sendAction;
    }

    public Action exitAction() {
        return exitAction;
    }

    public Action newRoomAction() {
        return newRoomAction;
    }

    public Action joinAction() {
        return joinAction;
    }

    boolean isKnownRoom(String room) {
        room = room.trim();
        for (RoomInfo in : rooms) {
            if (in.name.equals(room)) {
                return true;
            }
        }
        return false;
    }

    void setRoom(Room room) {
        if (!room.equals(this.room)) {
            this.room = room;
            roomMembers.clear();
            if (!rooms.contains(room)) {
                rooms.addElement(new RoomInfo(room.name(), true, new String[0]));
            }
            int ix = -1;
            for (int i = 0; i < rooms.size(); i++) {
                RoomInfo rm = rooms.getElementAt(i);
                if (rm.name.equals(room.name())) {
                    ix = i;
                    break;
                }
            }
            selectedRoom.setAnchorSelectionIndex(ix);
            selectedRoom.setLeadSelectionIndex(ix + 1);
            sendAction.checkEnabled();
            ctrl.listUsers(room.name());
            ctrl.setUserName(prefs.getUserName());
        }
    }

    Room room() {
        return this.room;
    }

    boolean isRoom(String room) {
        return this.room != null && this.room.name().equals(room);
    }

    void setRooms(ListRoomsReply rooms) {
        for (ListRoomsReply.RoomInfo room : rooms.rooms) {
            if (!this.rooms.contains(room)) {
                this.rooms.addElement(room);
            }
        }
        Set<RoomInfo> s = new HashSet<>(Arrays.asList(rooms.rooms));
        for (RoomInfo curr : this.rooms) {
            if (!s.contains(curr)) {
                this.rooms.removeElement(curr);
            }
        }
    }

    void setRoomMembers(String[] split) {
        for (String s : split) {
            if (!roomMembers.contains(s)) {
                roomMembers.addElement(s);
            }
        }
        Set<String> s = new HashSet<>(Arrays.asList(split));
        for (String known : roomMembers) {
            if (!s.contains(known)) {
                roomMembers.removeElement(known);
            }
        }
    }

    void nicknameChanged(String old, String nue) {
        int ix = roomMembers.indexOf(old);
        if (ix >= 0) {
            roomMembers.set(ix, nue);
        }
    }

    void userJoinedRoom(String user) {
        roomMembers.addElement(user);
    }

    void userLeftRoom(String user) {
        roomMembers.removeElement(user);
    }

    boolean isUserName(String name) {
        return userName.equals(name);
    }

    void onSendMessage(String msg) {
        int ind = chatDocument.getEndPosition().getOffset();
        try {
//            SimpleAttributeSet set = new SimpleAttributeSet();
            Style set = styles.getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setBold(set, true);
            StyleConstants.setForeground(set, new Color(0, 120, 30));
            StyleConstants.setBackground(set, new Color(240, 240, 255));
            StyleConstants.setSpaceBelow(set, 5);
            chatDocument.insertString(ind, prefs.getUserName(), set);
            ind = chatDocument.getEndPosition().getOffset();
            StyleConstants.setBold(set, false);
            StyleConstants.setBackground(set, new Color(240, 240, 255));
            StyleConstants.setForeground(set, Color.BLACK);
            StyleConstants.setSpaceBelow(set, 5);
            chatDocument.insertString(ind, "\t" + msg, set);
        } catch (BadLocationException ex) {
            Logger.getLogger(UIModels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void onMessage(RoomMessage msg) {
        int ind = chatDocument.getEndPosition().getOffset();
        try {
            String message = msg.message;
            System.out.println("MSG " + message);
            Encrypter enc = cryptoForRoom.get(msg.room);
            if (enc != null) {
                message = enc.decrypt(userName);
            }
            Style set = styles.getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setBackground(set, Color.WHITE);
            StyleConstants.setBold(set, true);
            StyleConstants.setForeground(set, Color.BLUE);
            StyleConstants.setSpaceBelow(set, 5);
            chatDocument.insertString(ind, msg.from, set);
            ind = chatDocument.getEndPosition().getOffset();
            set = styles.getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setBold(set, false);
            StyleConstants.setForeground(set, Color.BLACK);
            StyleConstants.setSpaceBelow(set, 5);
            chatDocument.insertString(ind, "\t" + message + "\n", set);
        } catch (BadLocationException ex) {
            Logger.getLogger(UIModels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class ClearAction extends AbstractAction {

        ClearAction() {
            putValue(NAME, "Clear");
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 0);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK, true));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                chatDocument.remove(0, chatDocument.getLength());
            } catch (BadLocationException ex) {
                Logger.getLogger(UIModels.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    class NameAction extends AbstractAction {

        NameAction() {
            putValue(NAME, "Nickname");
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 3);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK, true));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            String name = JOptionPane.showInputDialog("Set your nickname", userName);
            dialogClosed();
            if (name != null) {
                name = name.trim();
                if (name.length() > 3) {
                    String old = userName;
                    userName = name;
                    prefs.setUserName(userName);
                    ctrl.setUserName(name);
                    for (int i = 0; i < roomMembers.size(); i++) {
                        if (old.equals(roomMembers.get(i))) {
                            roomMembers.setElementAt(userName, i);
                            break;
                        }
                    }
                }
            }
        }
    }

    class PrefsAction extends AbstractAction {

        PrefsAction() {
            putValue(NAME, "Preferences");
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 0);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_DOWN_MASK, true));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            new PrefsPanel(prefs).showDialog();
            dialogClosed();
        }
    }

    class NewRoomAction extends AbstractAction {

        NewRoomAction() {
            putValue(NAME, "Create Room");
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 7);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK, true));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            NewRoomPanel pnl = new NewRoomPanel(UIModels.this);
            String[] result = pnl.showDialog();
            dialogClosed();
            if (result != null) {
                switch (result.length) {
                    case 1:
                        if (result[0].isEmpty()) {
                            onNotification(NotificationType.ERROR.notification("No room specified"));
                            return;
                        }
                        ctrl.joinRoom(result[0]);
                        break;
                    case 2:
                        if (result[0].isEmpty()) {
                            onNotification(NotificationType.ERROR.notification("No room specified"));
                            return;
                        }
                        if (result[1].isEmpty()) {
                            onNotification(NotificationType.ERROR.notification("No password specified"));
                            return;
                        }
                        ctrl.joinRoom(result[0], result[1]);
                        break;
                    default:
                        throw new AssertionError(result.length);
                }
            }
        }
    }

    private final Map<String, Encrypter> cryptoForRoom = new HashMap<>();

    class JoinAction extends AbstractAction implements ListSelectionListener {

        int ix;

        JoinAction() {
            putValue(NAME, "Join");
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 0);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK, true));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int ix = selectedRoom.getAnchorSelectionIndex();
            if (ix >= 0) {
                RoomInfo info = rooms.get(ix);
                if (room != null && room.name().equals(info.name)) {
                    Toolkit.getDefaultToolkit().beep();
                    return;
                } else if (info.hasPassword) {
                    String password = JOptionPane.showInputDialog("Enter the password for this room", "");
                    dialogClosed();
                    if (password != null && !password.isEmpty()) {
                        try {
                            MessageDigest digest = MessageDigest.getInstance("SHA-512");
                            byte[] bytes = digest.digest(password.getBytes(CharsetUtil.UTF_8));
                            password = Base64.getEncoder().encodeToString(bytes);
                            cryptoForRoom.put(info.name, new Encrypter(password));
                            ctrl.joinRoom(info.name, password);
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(UIModels.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        ctrl.joinRoom(info.name);
                    }
                } else {
                    ctrl.joinRoom(info.name);
                }
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            int last = selectedRoom.getAnchorSelectionIndex();
            if (lse.getValueIsAdjusting()) {
                return;
            }
            ix = last;
            if (ix >= 0 && ix < rooms.getSize()) {
                RoomInfo rm = rooms.getElementAt(ix);
                boolean alreadyInThatRoom = room == null ? false : rm.name.equals(room.name());
                setEnabled(!alreadyInThatRoom);
            } else {
                setEnabled(false);
            }
        }
    }

    class ExitAction extends AbstractAction {

        ExitAction() {
            putValue(NAME, "Exit");
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 1);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_DOWN_MASK, true));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            ctrl.disconnect();
            System.exit(0);
        }

    }

    class SendAction extends AbstractAction implements DocumentListener {

        SendAction() {
            putValue(NAME, "Send");
            putValue(DISPLAYED_MNEMONIC_INDEX_KEY, 0);
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (isEnabled()) {
                try {
                    String s = chatEntry.getText(0, chatEntry.getLength());
                    Encrypter enc = cryptoForRoom.get(room.name());
                    if (enc == null) {
                        room.send(s);
                    } else {
                        room.send(enc.encrypt(s));
                    }
                    chatEntry.remove(0, chatEntry.getLength());
                    onSendMessage(s);
                } catch (BadLocationException ex) {
                    Logger.getLogger(UIModels.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public void insertUpdate(DocumentEvent de) {
            change(de);
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            change(de);
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            change(de);
        }

        private final void change(DocumentEvent de) {
            Document d = de.getDocument();
            setEnabled(UIModels.this.room != null && d.getLength() != 0);
        }

        void checkEnabled() {
            setEnabled(room != null);
        }
    }
    
    
    public interface DialogListener {
        void onDialogClosed();
    }
}
