package com.timboudreau.scamper.chat.swing.client;

import com.mastfrog.scamper.chat.api.ListRoomsReply.RoomInfo;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author Tim Boudreau
 */
public class RoomInfoCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> jlist, Object o, int i, boolean bln, boolean bln1) {
        Component result = super.getListCellRendererComponent(jlist, o, i, bln, bln1);
        if (o instanceof RoomInfo) {
            RoomInfo info = (RoomInfo) o;
            if (!bln) {
                if (!info.hasPassword) {
                    setBackground(new Color(255, 255, 220));
                } else {
                    setBackground(Color.white);
                }
            }
            setText(info.name);
        }
        return result;
    }
}
