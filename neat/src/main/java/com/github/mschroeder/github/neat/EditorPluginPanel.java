package com.github.mschroeder.github.neat;

/**
 *
 * @author Markus Schr&ouml;der
 */
public interface EditorPluginPanel {
    
    public void init(EditorPluginContext ctx);
    
    public void deinit(EditorPluginContext ctx);
    
}
