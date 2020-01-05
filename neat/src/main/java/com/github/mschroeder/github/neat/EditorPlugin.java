package com.github.mschroeder.github.neat;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * A plugin of the editor has a name, a shortcut and an implemented run method.
 *
 * @author Markus Schr&ouml;der
 */
public abstract class EditorPlugin {

    //protected List<EditorPluginParam> params;

    protected Shortcut shortcut;

    public EditorPlugin() {
        
    }

    /*
    public final EditorPlugin addParam(String name, String desc, Object defaultValue, Object type) {
        params.add(new EditorPluginParam(name, desc, defaultValue, type));
        return this;
    }
*/
    
    public String getName() {
        String n = this.getClass().getSimpleName();
        return n.substring(0, n.length() - "Plugin".length());
    }

    public List<String> getAltNames() {
        return Arrays.asList();
    }
    
    public String getDesc() {
        InputStream is = EditorFrame.class.getResourceAsStream("/de/dfki/sds/markus/editor/plugins/" + getName() + ".txt");
        String desc = "";
        if (is != null) {
            try {
                desc = IOUtils.toString(is, "UTF-8");
            } catch (IOException ex) {
                
            }
        }
        return desc;
    }
    
    @Override
    public String toString() {
        return getName();
    }

    /*
    public EditorPluginParam getParam(String name) {
        for (EditorPluginParam p : params) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
    */
    
    /*
    public <T> T getParamValue(String name, Class<T> type) {
        EditorPluginParam param = getParam(name);
        if (param.getValue() == null) {
            return (T) param.getDefaultValue();
        }
        return (T) param.getValue();
    }

    public void setParamValues(Object... paramValues) {
        for (int i = 0; i < paramValues.length; i += 2) {
            String name = (String) paramValues[i];
            Object value = paramValues[i + 1];
            EditorPluginParam param = getParam(name);
            if (param != null) {
                param.setValue(value);
            }
        }
    }
    */
    
    public Shortcut getShortcut() {
        return shortcut;
    }

    public boolean hasShortcut() {
        return shortcut != null;
    }

    public boolean isShortcutMatching(KeyEvent evt) {
        if (!hasShortcut()) {
            return false;
        }
        return getShortcut().match(evt);
    }

    public EditorPlugin setShortcut(Shortcut shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    /*
    public List<EditorPluginParam> getParams() {
        return params;
    }
    */
    
    public abstract void run(EditorPluginContext ctx);

}
