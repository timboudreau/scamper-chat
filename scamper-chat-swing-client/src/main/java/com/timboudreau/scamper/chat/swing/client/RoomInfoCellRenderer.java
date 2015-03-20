package com.timboudreau.scamper.chat.swing.client;

import com.mastfrog.scamper.chat.api.ListRoomsReply.RoomInfo;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.border.Border;

/**
 *
 * @author Tim Boudreau
 */
public class RoomInfoCellRenderer extends DefaultListCellRenderer {

    private final UIModels mdls;
    private final Border bdr = BorderFactory.createEmptyBorder(0, 12, 0, 0);

    RoomInfoCellRenderer(UIModels mdls) {
        this.mdls = mdls;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> jlist, Object o, int i, boolean bln, boolean bln1) {
        Component result = super.getListCellRendererComponent(jlist, o, i, bln, bln1);
        ((JComponent) result).setBorder(bdr);
        Font f = result.getFont();
        if (o instanceof RoomInfo) {
            RoomInfo info = (RoomInfo) o;
            if (mdls.isRoom(info.name)) {
                f = f.deriveFont(Font.BOLD);
                result.setFont(f);
            }
            if (!bln) {
                if (!info.hasPassword) {
                    setBackground(new Color(255, 255, 220));
                } else {
                    setBackground(Color.white);
                }
            } else {
                setBackground(new Color(230, 230, 255));
            }
            setText(info.name);
        } else if (o instanceof String) {
            if (mdls.isUserName(o.toString())) {
                f = f.deriveFont(Font.BOLD);
                result.setFont(f);
            }
            if (bln) {
                setBackground(new Color(230, 230, 255));
            }
        }
        return result;
    }
}
