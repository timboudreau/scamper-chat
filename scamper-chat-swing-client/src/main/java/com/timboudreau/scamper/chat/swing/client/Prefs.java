package com.timboudreau.scamper.chat.swing.client;

import com.mastfrog.scamper.chat.base.ScamperClient;
import java.awt.Font;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.inject.Singleton;

/**
 *
 * @author Tim Boudreau
 */
@Singleton
public class Prefs {

    private final Preferences prefs = Preferences.userNodeForPackage(Prefs.class);
    public static final String PREFS_KEY_UI_FONT_NAME = "uifont";
    public static final String PREFS_KEY_MSG_FONT_NAME = "messagefont";
    public static final String PREFS_KEY_UI_FONT_SIZE = "uifontsize";
    public static final String PREFS_KEY_MSG_FONT_SIZE = "messagefontsize";
    private final List<Reference<PrefsListener>> listeners = new LinkedList<>();

    Prefs() {

    }

    void addListener(PrefsListener l) {
        listeners.add(new WeakReference<PrefsListener>(l));
    }

    void setUserName(String userName) {
        prefs.put("userName", userName);
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Prefs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    String getUserName() {
        return prefs.get("userName", "Me");
    }

    public String getHost() {
        return prefs.get("host", ScamperClient.DEFAULT_HOST);
    }

    public int getPort() {
        return prefs.getInt("port", ScamperClient.DEFAULT_PORT);
    }

    public void setPort(int val) {
        if (val < 1 || val > 65536) {
            throw new IllegalArgumentException("Port must be between 1 and 65535 but was " + val);
        }
        prefs.putInt("port", val);
    }

    public void setHost(String host) {
        try {
            InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host " + host);
        }
        prefs.put("host", host);
    }

    public String getUiFontName() {
        return prefs.get(PREFS_KEY_UI_FONT_NAME, "Dialog");
    }

    public int getUiFontSize() {
        return prefs.getInt(PREFS_KEY_UI_FONT_SIZE, 14);
    }

    public Font getUIFont() {
        String ufName = getUiFontName();
        if ("Dialog".equals(ufName)) {
            return null;
        }
        return new Font(getUiFontName(), Font.PLAIN, getUiFontSize());
    }

    public String getMessageFontName() {
        return prefs.get(PREFS_KEY_MSG_FONT_NAME, "Dialog");
    }

    public int getMessageFontSize() {
        return prefs.getInt(PREFS_KEY_MSG_FONT_SIZE, 14);
    }

    public Font getMessageFont() {
        String ufName = getMessageFontName();
        if ("Dialog".equals(ufName)) {
            return null;
        }
        return new Font(getMessageFontName(), Font.PLAIN, getMessageFontSize());
    }

    public void setMessageFont(String name, int size) {
        prefs.put(PREFS_KEY_MSG_FONT_NAME, name);
        prefs.putInt(PREFS_KEY_MSG_FONT_SIZE, size);
    }

    public void setUiFont(String name, int size) {
        prefs.put(PREFS_KEY_UI_FONT_NAME, name);
        prefs.putInt(PREFS_KEY_UI_FONT_SIZE, size);
    }

    public interface PrefsListener {

        public void change(Set<PrefValue> set, Prefs src);
    }

    void change(Set<PrefValue> s) {
        for (Iterator<Reference<PrefsListener>> it = listeners.iterator(); it.hasNext();) {
            Reference<PrefsListener> r = it.next();
            PrefsListener p = r.get();
            if (p == null) {
                it.remove();
            } else {
                p.change(s, this);
            }
        }
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Prefs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public enum PrefValue {
        UI_FONT,
        MSG_FONT,
        SERVER
    }

}
