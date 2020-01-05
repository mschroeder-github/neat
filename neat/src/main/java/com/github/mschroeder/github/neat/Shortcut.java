package com.github.mschroeder.github.neat;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.StringJoiner;
import org.json.JSONObject;

/**
 * The shortcut to activate the plugin.
 * @author Markus Schr&ouml;der
 */
public class Shortcut {
    
    boolean alt;
    boolean ctrl;
    boolean shift;
    int keyCode;

    public Shortcut(boolean ctrl, boolean alt, boolean shift, int keyCode) {
        this.alt = alt;
        this.ctrl = ctrl;
        this.shift = shift;
        this.keyCode = keyCode;
    }
    
    public static Shortcut fromJSON(JSONObject json) {
        return new Shortcut(json.getBoolean("ctrl"), json.getBoolean("alt"), json.getBoolean("shift"), getKeyCode(json.getString("key")));
    }
    
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("ctrl", ctrl);
        obj.put("alt", alt);
        obj.put("shift", shift);
        obj.put("key", getKeyCodeName(keyCode));
        return obj;
    }
    
    public static String getKeyCodeName(int keycode) {
        for(Field f : KeyEvent.class.getDeclaredFields()) {
            if(f.getName().startsWith("VK_")) {
                int code;
                try {
                    code = (int) f.get(null);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                
                if(keycode == code) {
                    return f.getName().substring("VK_".length());
                }
            }
        }
        return null;
    }
    
    public static int getKeyCode(String name) {
        String keyCodeName = "VK_" + name.toUpperCase();
        int keyCode = 0;
        try {
            Field f = KeyEvent.class.getDeclaredField(keyCodeName);
            keyCode = (int) f.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        return keyCode;
    }
    
    public boolean match(KeyEvent evt) {
        return 
                evt.isControlDown() == ctrl &&
                evt.isAltDown() == alt &&
                evt.isShiftDown() == shift &&
                evt.getKeyCode() == keyCode;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("+");
        if(ctrl) {
            sj.add("Ctrl");
        }
        if(alt) {
            sj.add("Alt");
        }
        if(shift) {
            sj.add("Shift");
        }
        sj.add(getKeyCodeName(keyCode));
        return sj.toString();
    }
    
}
