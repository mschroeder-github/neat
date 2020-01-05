package com.github.mschroeder.github.neat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A singelton containing all loaded packages.
 * It is used by this app.
 * @author Markus Schr&ouml;der
 */
public class EditorPluginRegistry {

    private final List<EditorPluginPackage> packages = new ArrayList<>();

    public List<EditorPlugin> getPlugins() {
        List<EditorPlugin> registry = new ArrayList<>();
        for(EditorPluginPackage p : packages) {
            registry.addAll(p.getPlugins());
        }
        return registry;
    }
    
    //load all *.json files
    //used at the start of the app
    public void load(File folder) {
        List<File> files = new ArrayList<>(Arrays.asList(folder.listFiles((f) -> {
            return f.getName().endsWith("json");
        })));
        files.sort((a,b) -> {
            return a.getName().compareTo(b.getName());
        });
        
        for (File f : files) {
            EditorPluginPackage pack = new EditorPluginPackage();
            pack.load(f);
            packages.add(pack);
        }
    }
    
    //save all plugin settings from loaded files
    public void save() {
        packages.forEach(pack -> pack.save());
    }
    
    //singleton ================================================================
    
    private static EditorPluginRegistry singleton = new EditorPluginRegistry();

    private EditorPluginRegistry() {
        
    }

    public static EditorPluginRegistry getInstance() {
        return singleton;
    }

}
