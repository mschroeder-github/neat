package com.github.mschroeder.github.neat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A set of plugins from an author.
 *
 * @author Markus Schr&ouml;der
 */
public class EditorPluginPackage {

    private File file;

    private String name;
    private String author;

    private final List<EditorPlugin> plugins = new ArrayList<>();
    private final List<EditorPlugin> hidden = new ArrayList<>();

    /**
     * Loads from a *.json file all pugins.
     *
     * @param configFile
     */
    public void load(File configFile) {
        plugins.clear();
        this.file = configFile;

        String content;
        try {
            content = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        JSONObject json = new JSONObject(content);
        this.name = json.getString("name");
        this.author = json.getString("author");

        JSONArray visible = json.getJSONArray("visible");

        System.out.println("load " + visible.length() + " plugins from package " + name);

        for (int i = 0; i < visible.length(); i++) {
            JSONObject pluginObj = visible.getJSONObject(i);

            EditorPlugin plugin = loadPlugin(pluginObj);

            if (plugin != null) {
                plugins.add(plugin);
            }
        }

    }

    /**
     * Loads a plugin from json.
     *
     * @param pluginObj
     * @return
     */
    private EditorPlugin loadPlugin(JSONObject pluginObj) {
        MethodBasedEditorPlugin plugin = null;

        String methodClasspath = pluginObj.getString("method");
        int lastDot = methodClasspath.lastIndexOf('.');
        String classpath = methodClasspath.substring(0, lastDot);
        String methodName = methodClasspath.substring(lastDot + 1, methodClasspath.length());

        Method method = null;
        try {
            Class<?> c = EditorPluginRegistry.class.getClassLoader().loadClass(classpath);

            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }

            if (method == null) {
                throw new NoSuchMethodException(methodName);
            }

        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            ex.printStackTrace();
            return null;
        }

        JSONArray argsArray = pluginObj.getJSONArray("args");
        Object[] args = new Object[argsArray.length() + 1]; //+1 because ctx is always first
        for (int j = 0; j < argsArray.length(); j++) {
            args[j + 1] = argsArray.get(j);
        }

        Method finalMethod = method;
        plugin = new MethodBasedEditorPlugin(pluginObj.getString("prefLabel"), ctx -> {
            args[0] = ctx;
            try {
                finalMethod.invoke(null, args);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        });

        //shortcut setting
        if (pluginObj.has("shortcut")) {
            plugin.setShortcut(Shortcut.fromJSON(pluginObj.getJSONObject("shortcut")));
        }

        JSONArray altArray = pluginObj.getJSONArray("altLabels");
        for (int j = 0; j < altArray.length(); j++) {
            plugin.getAltNames().add(altArray.getString(j));
        }

        plugin.setDescription(pluginObj.getString("description"));

        plugin.setMethod(methodClasspath);

        plugin.getArgs().addAll(pluginObj.getJSONArray("args").toList());

        return plugin;
    }

    /**
     * After change you can save it.
     */
    public void save() {
        if (file == null) {
            return;
        }

        /*
        {
            "name": "Package Name",
            "author": "Author Name",

            "visible": [
                ...
            ],

            "hidden": [
                ...
            ]
        }
         */
        JSONObject packObj = new JSONObject();
        packObj.put("name", name);
        packObj.put("author", author);

        JSONArray visible = new JSONArray();
        toJSON(plugins, visible);
        packObj.put("visible", visible);

        JSONArray hiddenArray = new JSONArray();
        toJSON(hidden, hiddenArray);
        packObj.put("hidden", hiddenArray);

        try {
            FileUtils.writeStringToFile(file, packObj.toString(2), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void toJSON(List<EditorPlugin> plugins, JSONArray array) {
        /*
         {
            "prefLabel": "Prefered Name",
            "altLabels": ["Alternate Name", ...],
            "description": "A long text description.",
            "shortcut" : { "ctrl": true, "alt": false, "shift": false, "key": "L" },
            "method" : "org.example.MyPluginClass.myPluginMethod2",
            "args": [true, 5, "str"]
        },
         */
        for (EditorPlugin plugin : plugins) {
            JSONObject obj = new JSONObject();

            obj.put("prefLabel", plugin.getName());
            obj.put("altLabels", new JSONArray(plugin.getAltNames()));
            obj.put("description", plugin.getDesc());
            if (plugin.hasShortcut()) {
                obj.put("shortcut", plugin.getShortcut().toJSON());
            }

            if (plugin instanceof MethodBasedEditorPlugin) {
                MethodBasedEditorPlugin mbep = (MethodBasedEditorPlugin) plugin;
                obj.put("method", mbep.getMethod());
                obj.put("args", new JSONArray(mbep.getArgs()));
            }

            array.put(obj);
        }
    }

    public List<EditorPlugin> getPlugins() {
        return plugins;
    }

    public List<EditorPlugin> getHidden() {
        return hidden;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

}
