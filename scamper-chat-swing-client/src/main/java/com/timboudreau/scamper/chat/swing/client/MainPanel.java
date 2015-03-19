/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.timboudreau.scamper.chat.swing.client;

import com.timboudreau.scamper.chat.swing.client.Prefs.PrefsListener;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 *
 * @author Tim Boudreau
 */
public class MainPanel extends javax.swing.JPanel implements NotificationListener, PrefsListener, DocumentListener {

    private final UIModels mdls;

    /**
     * Creates new form MainPanel
     *
     * @param mdls
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MainPanel(UIModels mdls, Prefs prefs) {
        this.mdls = mdls;
        initComponents();
        joinRoomButton.setAction(mdls.joinAction());
        newRoomButton.setAction(mdls.newRoomAction());
        sendButton.setAction(mdls.sendAction());
        chatEditorPane.setDocument(mdls.chatDocument());
        membersList.setSelectionModel(mdls.selectedMember());
        membersList.setModel(mdls.roomMembers());
        roomsList.setSelectionModel(mdls.selectedRoom());
        roomsList.setModel(mdls.rooms());
        roomsList.setCellRenderer(new RoomInfoCellRenderer());
        inputTextArea.setDocument(mdls.chatEntryDocument());
        inputTextArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "send");
        inputTextArea.getActionMap().put("send", mdls.sendAction());
        nicknameButton.setAction(mdls.nameAction());
        mdls.addNotificationListener(this);
        fixBorders(this);
        updateFonts(prefs);
        prefs.addListener(this);
        InputMap in = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "prefs");
        am.put("prefs", mdls.prefsAction());
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "name");
        am.put("name", mdls.nameAction());
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "clear");
        am.put("clear", mdls.clearAction());
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "room");
        am.put("room", mdls.newRoomAction());
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "esc");
        am.put("esc", new MnemonicAction(inputTextArea));

        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "etr");
        am.put("etr", new CtrlEntrAction());
        
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK), "file");
        am.put("file", new MnemonicAction(mdls.fileMenu()));
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_DOWN_MASK), "edit");
        am.put("edit", new MnemonicAction(mdls.editMenu()));
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK), "nm");
        am.put("nm", new MnemonicAction(nicknameButton));
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK), "sv");
        am.put("sv", new MnemonicAction(sendButton));
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK), "rm");
        am.put("rm", new MnemonicAction(newRoomButton));

        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK, true), "up");
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK, true), "dn");
        am.put("up", new RoomMove(true));
        am.put("dn", new RoomMove(false));

        chatEditorPane.getDocument().addDocumentListener(this);
    }
    
    class CtrlEntrAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                mdls.chatEntryDocument().insertString(mdls.chatEntryDocument().getLength(), "\n", null);
            } catch (BadLocationException ex) {
                Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
;
        }
        
    }

    class RoomMove extends AbstractAction {

        private final boolean up;

        public RoomMove(boolean up) {
            this.up = up;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int max = roomsList.getModel().getSize();
            if (max == 0) {
                return;
            }
            int ix = roomsList.getSelectedIndex();
            ix = up ? ix + 1 : ix - 1;
            if (ix == max) {
                ix = 0;
            } else if (ix < 0) {
                ix = max - 1;
            }
            roomsList.setSelectedIndex(ix);
        }

    }

    static class MnemonicAction extends AbstractAction {

        private final Component component;

        public MnemonicAction(Component component) {
            this.component = component;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            component.requestFocus();
            if (component instanceof JMenu) {
                ((JMenu) component).doClick();
            }
        }

    }

    final void updateFonts(Prefs prefs) {
        Font msgs = prefs.getMessageFont();
        if (msgs != null) {
            inputTextArea.setFont(msgs);
            chatEditorPane.setFont(msgs);
        }
        Font ui = prefs.getUIFont();
        if (ui != null) {
            membersList.setFont(ui);
            roomsList.setFont(ui);
            notificationLabel.setFont(ui);
        }
        invalidate();
        revalidate();
        repaint();
    }

    private final Border empty = BorderFactory.createEmptyBorder();

    private void fixBorders(Container c) {
        if (c instanceof JScrollPane) {
            JScrollPane jc = (JScrollPane) c;
            jc.setBorder(empty);
            jc.setViewportBorder(empty);
        } else if (c instanceof JSplitPane) {
            JSplitPane p = (JSplitPane) c;
            p.setBorder(empty);
            p.setDividerSize(5);
        }
        for (Component cc : c.getComponents()) {
            if (cc instanceof Container) {
                fixBorders((Container) cc);
            }
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        mdls.checkRooms();
        startTimer();
        inputTextArea.requestFocusInWindow();
    }

    public void removeNotify() {
        stopTimer();
        super.removeNotify();
    }

    private final Timer timer = new Timer(1000 * 60, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            mdls.checkRooms();
        }

    });

    private void startTimer() {
        timer.setRepeats(true);
        timer.start();
    }

    private void stopTimer() {
        timer.stop();
    }

    public static MainPanel showUI(final UIModels mdls, Prefs prefs) {
        final MainPanel pnl = new MainPanel(mdls, prefs);
        final JFrame jf = new JFrame("Scamper Chat");
        jf.setContentPane(pnl);
        jf.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                mdls.exitAction().actionPerformed(null);
            }
        });
        JMenuBar bar = new JMenuBar();
        bar.add(mdls.fileMenu());
        bar.add(mdls.editMenu());
        jf.setJMenuBar(bar);
        jf.pack();
        jf.setVisible(true);
        return pnl;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplit = new javax.swing.JSplitPane();
        roomAndMembersSplit = new javax.swing.JSplitPane();
        roomsPanel = new javax.swing.JPanel();
        roomsLabel = new javax.swing.JLabel();
        roomsScroll = new javax.swing.JScrollPane();
        roomsList = new javax.swing.JList();
        newRoomButton = new javax.swing.JButton();
        joinRoomButton = new javax.swing.JButton();
        membersPanel = new javax.swing.JPanel();
        membersLabel = new javax.swing.JLabel();
        membersScroll = new javax.swing.JScrollPane();
        membersList = new javax.swing.JList();
        mainChatSplit = new javax.swing.JSplitPane();
        chatPanel = new javax.swing.JPanel();
        chatScroll = new javax.swing.JScrollPane();
        chatEditorPane = new javax.swing.JTextPane();
        inputPanel = new javax.swing.JPanel();
        sendButton = new javax.swing.JButton();
        inputScroll = new javax.swing.JScrollPane();
        inputTextArea = new javax.swing.JTextArea();
        nicknameButton = new javax.swing.JButton();
        notificationLabel = new javax.swing.JLabel();

        mainSplit.setDividerLocation(200);

        roomAndMembersSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        roomsLabel.setFont(roomsLabel.getFont().deriveFont(roomsLabel.getFont().getStyle() | java.awt.Font.BOLD, roomsLabel.getFont().getSize()+2));
        roomsLabel.setLabelFor(roomsList);
        roomsLabel.setText("Rooms");

        roomsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        roomsScroll.setViewportView(roomsList);

        newRoomButton.setText("New");

        joinRoomButton.setText("Join");

        javax.swing.GroupLayout roomsPanelLayout = new javax.swing.GroupLayout(roomsPanel);
        roomsPanel.setLayout(roomsPanelLayout);
        roomsPanelLayout.setHorizontalGroup(
            roomsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roomsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(roomsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(roomsPanelLayout.createSequentialGroup()
                        .addComponent(newRoomButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(joinRoomButton))
                    .addComponent(roomsLabel))
                .addContainerGap(38, Short.MAX_VALUE))
            .addComponent(roomsScroll)
        );
        roomsPanelLayout.setVerticalGroup(
            roomsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(roomsPanelLayout.createSequentialGroup()
                .addComponent(roomsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(roomsScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(roomsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newRoomButton)
                    .addComponent(joinRoomButton))
                .addContainerGap())
        );

        roomAndMembersSplit.setTopComponent(roomsPanel);

        membersLabel.setFont(membersLabel.getFont().deriveFont(membersLabel.getFont().getStyle() | java.awt.Font.BOLD, membersLabel.getFont().getSize()+2));
        membersLabel.setLabelFor(membersList);
        membersLabel.setText("Members");

        membersList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        membersScroll.setViewportView(membersList);

        javax.swing.GroupLayout membersPanelLayout = new javax.swing.GroupLayout(membersPanel);
        membersPanel.setLayout(membersPanelLayout);
        membersPanelLayout.setHorizontalGroup(
            membersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(membersScroll)
            .addGroup(membersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(membersLabel)
                .addContainerGap(95, Short.MAX_VALUE))
        );
        membersPanelLayout.setVerticalGroup(
            membersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(membersPanelLayout.createSequentialGroup()
                .addComponent(membersLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(membersScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
        );

        roomAndMembersSplit.setRightComponent(membersPanel);

        mainSplit.setLeftComponent(roomAndMembersSplit);

        mainChatSplit.setDividerLocation(560);
        mainChatSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        chatEditorPane.setEditable(false);
        chatScroll.setViewportView(chatEditorPane);

        javax.swing.GroupLayout chatPanelLayout = new javax.swing.GroupLayout(chatPanel);
        chatPanel.setLayout(chatPanelLayout);
        chatPanelLayout.setHorizontalGroup(
            chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chatScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 912, Short.MAX_VALUE)
        );
        chatPanelLayout.setVerticalGroup(
            chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chatScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
        );

        mainChatSplit.setTopComponent(chatPanel);

        sendButton.setText("jButton3");

        inputTextArea.setColumns(20);
        inputTextArea.setRows(5);
        inputScroll.setViewportView(inputTextArea);

        nicknameButton.setText("Nickname");
        nicknameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nicknameButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout inputPanelLayout = new javax.swing.GroupLayout(inputPanel);
        inputPanel.setLayout(inputPanelLayout);
        inputPanelLayout.setHorizontalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inputPanelLayout.createSequentialGroup()
                .addComponent(inputScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sendButton)
                    .addComponent(nicknameButton))
                .addGap(17, 17, 17))
        );

        inputPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nicknameButton, sendButton});

        inputPanelLayout.setVerticalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nicknameButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sendButton)
                .addContainerGap())
            .addComponent(inputScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
        );

        mainChatSplit.setRightComponent(inputPanel);

        mainSplit.setRightComponent(mainChatSplit);

        notificationLabel.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainSplit)
            .addComponent(notificationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainSplit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notificationLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void nicknameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nicknameButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nicknameButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane chatEditorPane;
    private javax.swing.JPanel chatPanel;
    private javax.swing.JScrollPane chatScroll;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JScrollPane inputScroll;
    private javax.swing.JTextArea inputTextArea;
    private javax.swing.JButton joinRoomButton;
    private javax.swing.JSplitPane mainChatSplit;
    private javax.swing.JSplitPane mainSplit;
    private javax.swing.JLabel membersLabel;
    private javax.swing.JList membersList;
    private javax.swing.JPanel membersPanel;
    private javax.swing.JScrollPane membersScroll;
    private javax.swing.JButton newRoomButton;
    private javax.swing.JButton nicknameButton;
    private javax.swing.JLabel notificationLabel;
    private javax.swing.JSplitPane roomAndMembersSplit;
    private javax.swing.JLabel roomsLabel;
    private javax.swing.JList roomsList;
    private javax.swing.JPanel roomsPanel;
    private javax.swing.JScrollPane roomsScroll;
    private javax.swing.JButton sendButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onNotification(Notification notif) {
        notificationLabel.setText(notif.type.name() + ": " + notif.message);
    }

    @Override
    public void change(Set<Prefs.PrefValue> changes, Prefs prefs) {
        if (!changes.isEmpty()) {
            if (changes.contains(Prefs.PrefValue.MSG_FONT) || changes.contains(Prefs.PrefValue.UI_FONT)) {
                updateFonts(prefs);
            }
            if (changes.contains(Prefs.PrefValue.SERVER)) {
                //XXX restart UI, call SwingClient.main()
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Dimension size = chatEditorPane.getSize();
                chatEditorPane.scrollRectToVisible(new Rectangle(0, size.height - 1, size.width, 1));
            }
        });
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
    }
}
