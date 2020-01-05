package com.github.mschroeder.github.neat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class MethodBasedEditorPlugin extends EditorPlugin {

    private String name;
    private String description;
    private List<String> altNames;
    
    private String method;
    private List<Object> args;
    
    private Consumer<EditorPluginContext> consumer;
    
    public MethodBasedEditorPlugin(String name, Consumer<EditorPluginContext> consumer) {
        this.name = name;
        this.consumer = consumer;
        this.altNames = new ArrayList<>();
        this.args = new ArrayList<>();
    }
    
    public MethodBasedEditorPlugin(String name, Consumer<EditorPluginContext> consumer, Shortcut shortcut) {
        this(name, consumer);
        this.shortcut = shortcut;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public void run(EditorPluginContext ctx) {
        consumer.accept(ctx);
    }

    @Override
    public String getDesc() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<String> getAltNames() {
        return altNames;
    }

    public String getDescription() {
        return description;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getMethod() {
        return method;
    }

    public List<Object> getArgs() {
        return args;
    }
    
}
