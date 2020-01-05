package com.github.mschroeder.github.neat;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class EditorPluginContext {
    
    private JTextAreaEditor textarea;
    private EditorPanel editorPanel;
    private EditorFrame editorFrame;

    public JTextAreaEditor getTextArea() {
        return textarea;
    }
    
    public void setTextArea(JTextAreaEditor textarea) {
        this.textarea = textarea;
    }

    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    public void setEditorPanel(EditorPanel editorPanel) {
        this.editorPanel = editorPanel;
    }

    public boolean hasEditorPanel() {
        return editorPanel != null;
    }
    
    public EditorFrame getEditorFrame() {
        return editorFrame;
    }

    public void setEditorFrame(EditorFrame editorFrame) {
        this.editorFrame = editorFrame;
    }
    
    public boolean hasTextArea() {
        return textarea != null;
    }
    
}
