/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.timboudreau.scamper.chat.swing.client;

import com.timboudreau.scamper.chat.swing.client.Prefs.PrefValue;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Tim Boudreau
 */
public class PrefsPanel extends javax.swing.JPanel implements DocumentListener {

    private final Prefs prefs;

    /**
     * Creates new form PrefsPanel
     */
    public PrefsPanel(Prefs prefs) {
        this.prefs = prefs;
        initComponents();
        DefaultComboBoxModel uim = new DefaultComboBoxModel();
        DefaultComboBoxModel mim = new DefaultComboBoxModel();
        for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            uim.addElement(s);
            mim.addElement(s);
        }
        uiFontCombo.setModel(uim);
        messageFontCombo.setModel(mim);
        FontRenderer r = new FontRenderer();
        messageFontCombo.setRenderer(r);
        uiFontCombo.setRenderer(r);
        uiFontCombo.setSelectedItem(prefs.getUiFontName());
        messageFontCombo.setSelectedItem(prefs.getMessageFontName());
        messageFontSize.setValue(prefs.getMessageFontSize());
        uiFontSize.setValue(prefs.getUiFontSize());
        serverField.setText(prefs.getHost());
        portSpinner.setValue(prefs.getPort());
        changed = false;
        serverField.getDocument().addDocumentListener(this);
    }

    void save(Set<PrefValue> s) {
        for (PrefValue p : s) {
            switch (p) {
                case MSG_FONT:
                    prefs.setMessageFont((String) messageFontCombo.getSelectedItem(), (int) messageFontSize.getValue());
                    break;
                case UI_FONT:
                    prefs.setUiFont((String) uiFontCombo.getSelectedItem(), (int) uiFontSize.getValue());
                    break;
                case SERVER:
                    try {
                        prefs.setHost(serverField.getText().trim());
                        prefs.setPort((int) portSpinner.getValue());
                    } catch (IllegalArgumentException iae) {
                        JOptionPane.showMessageDialog(null, iae.getMessage());
                    }
                    break;
            }
        }
    }

    Set<PrefValue> changes() {
        Set<PrefValue> result = EnumSet.noneOf(PrefValue.class);
        if (!serverField.getText().equals(prefs.getHost())) {
            result.add(PrefValue.SERVER);
        }
        int pt = (int) portSpinner.getValue();
        if (pt != prefs.getPort()) {
            result.add(PrefValue.SERVER);
        }
        if (!messageFontCombo.getSelectedItem().equals(prefs.getMessageFontName())) {
            result.add(PrefValue.MSG_FONT);
        }
        int mfs = (int) messageFontSize.getValue();
        if (mfs != prefs.getMessageFontSize()) {
            result.add(PrefValue.MSG_FONT);
        }
        if (!uiFontCombo.getSelectedItem().equals(prefs.getUiFontName())) {
            result.add(PrefValue.UI_FONT);
        }
        int ufs = (int) uiFontSize.getValue();
        if (ufs != prefs.getUiFontSize()) {
            result.add(PrefValue.UI_FONT);
        }
        return result;
    }

    public void showDialog() {
        final JOptionPane jp = new JOptionPane(this, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dlg = jp.createDialog(null, "New Room");
        jp.setInitialValue(serverField);

        PropertyChangeListener pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
            }
        };
        addPropertyChangeListener("ok", pcl);
        try {
            dlg.setVisible(true);
            Object sel = jp.getValue();
            System.out.println("SEL IS " + sel);
            if (sel != null && sel == (Integer) 0) {
                Set<PrefValue> changes = changes();
                if (!changes.isEmpty()) {
                    save(changes);
                    prefs.change(changes);
                }
            }
        } finally {
            removePropertyChangeListener(pcl);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        change();
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        change();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        change();
    }

    static class FontRenderer extends DefaultListCellRenderer {

        private final Map<String, Font> m = new HashMap<>();

        @Override
        public Component getListCellRendererComponent(JList<?> jlist, Object o, int i, boolean bln, boolean bln1) {
            Component result = super.getListCellRendererComponent(jlist, o, i, bln, bln1);
            Font orig = result.getFont();
            int size = orig.getSize();
            Font nue = m.get((String) o);
            if (nue == null) {
                nue = new Font((String) o, Font.PLAIN, size);
                m.put((String) o, nue);
            }
            result.setFont(nue);
            return result;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        serverField = new javax.swing.JTextField();
        portSpinner = new javax.swing.JSpinner();
        jSeparator1 = new javax.swing.JSeparator();
        messageFontLabel = new javax.swing.JLabel();
        messageFontCombo = new javax.swing.JComboBox();
        messageFontSize = new javax.swing.JSpinner();
        uiFontLabel = new javax.swing.JLabel();
        uiFontCombo = new javax.swing.JComboBox();
        uiFontSize = new javax.swing.JSpinner();

        serverLabel.setLabelFor(serverField);
        serverLabel.setText("Server");

        portLabel.setLabelFor(portSpinner);
        portLabel.setText("Port");

        serverField.setText("jTextField1");

        portSpinner.setModel(new javax.swing.SpinnerNumberModel(8007, 1, 65535, 1));
        portSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerCh(evt);
            }
        });

        messageFontLabel.setLabelFor(messageFontCombo);
        messageFontLabel.setText("Message Font");

        messageFontCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        messageFontCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageFontComboActionPerformed(evt);
            }
        });

        messageFontSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerChange(evt);
            }
        });

        uiFontLabel.setLabelFor(uiFontCombo);
        uiFontLabel.setText("UI Font");

        uiFontCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        uiFontCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uiFontComboActionPerformed(evt);
            }
        });

        uiFontSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerChange(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverLabel)
                            .addComponent(portLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverField)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(messageFontLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(messageFontCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(uiFontLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(uiFontCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(uiFontSize, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                            .addComponent(messageFontSize))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {messageFontCombo, uiFontCombo});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverLabel)
                    .addComponent(serverField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageFontLabel)
                    .addComponent(messageFontCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(uiFontLabel)
                    .addComponent(uiFontCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(uiFontSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void messageFontComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageFontComboActionPerformed
        change();
    }//GEN-LAST:event_messageFontComboActionPerformed

    private void uiFontComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uiFontComboActionPerformed
        change();
    }//GEN-LAST:event_uiFontComboActionPerformed

    private void spinnerChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerChange
        change();
    }//GEN-LAST:event_spinnerChange

    private void spinnerCh(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerCh
        change();
    }//GEN-LAST:event_spinnerCh

    boolean changed;

    private void change() {
        changed = true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox messageFontCombo;
    private javax.swing.JLabel messageFontLabel;
    private javax.swing.JSpinner messageFontSize;
    private javax.swing.JLabel portLabel;
    private javax.swing.JSpinner portSpinner;
    private javax.swing.JTextField serverField;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JComboBox uiFontCombo;
    private javax.swing.JLabel uiFontLabel;
    private javax.swing.JSpinner uiFontSize;
    // End of variables declaration//GEN-END:variables
}
