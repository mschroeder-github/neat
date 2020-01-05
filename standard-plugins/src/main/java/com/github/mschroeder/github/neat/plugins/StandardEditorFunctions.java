package com.github.mschroeder.github.neat.plugins;

import com.github.mschroeder.github.neat.EditorFrame;
import com.github.mschroeder.github.neat.EditorPanel;
import com.github.mschroeder.github.neat.EditorPluginContext;
import com.github.mschroeder.github.neat.JTextAreaEditor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static java.util.stream.Collectors.joining;
import javax.swing.JFileChooser;
import javax.swing.text.BadLocationException;
import org.apache.commons.text.WordUtils;

/**
 * Typical editor functions.
 * @author Markus Schr&ouml;der
 */
public class StandardEditorFunctions {
    
    //plugin management
    
    public static void searchPlugin(EditorPluginContext ctx) {
        ctx.getEditorFrame().getPluginSearch().setText("");
        ctx.getEditorFrame().getPluginSearch().requestFocus();
    }
    
    //import & export
    
    public static void save(EditorPluginContext ctx) {
        if(ctx.getEditorPanel().hasFile()) {
            ctx.getEditorPanel().saveTextToFile();
        } else {
            File folder = new File(System.getProperty("user.home"));
            JFileChooser fileChooser = new JFileChooser();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            fileChooser.setSelectedFile(new File(folder, sdf.format(new Date()) + ".txt"));
            
            //TODO move caret to right position
            
            if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    fileChooser.getSelectedFile().createNewFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                ctx.getEditorPanel().setFile(fileChooser.getSelectedFile(), false);
                ctx.getEditorPanel().saveTextToFile();
            }
        }
    }
    
    public static void open(EditorPluginContext ctx) {
        File folder = new File(System.getProperty("user.home"));
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(folder);
        fileChooser.setMultiSelectionEnabled(true);

        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            for(File file : fileChooser.getSelectedFiles()) {
                ctx.getEditorFrame().createOrFocusTabFor(file);
            }
        }
    }
    
    //tab management
    
    public static void newTab(EditorPluginContext ctx) {
        EditorPanel tab = ctx.getEditorFrame().createOrFocusTabFor(null);
        ctx.getEditorFrame().getTabbedPaneMain().setSelectedComponent(tab);
        tab.getEditorTextArea().requestFocus();
    }
    
    public static void switchTab(EditorPluginContext ctx, boolean right) {
        EditorFrame editorFrame = ctx.getEditorFrame();
        
        int count = editorFrame.getTabbedPaneMain().getTabCount();
        if(count == 0)
            return;
        
        int dir = right ? 1 : -1;
        int sel = editorFrame.getTabbedPaneMain().getSelectedIndex();
        sel += dir;
        sel %= count;
        if(sel < 0) {
            sel = count + sel;
        }
        editorFrame.getTabbedPaneMain().setSelectedIndex(sel);
    }
    
    public static void closeTab(EditorPluginContext ctx) {
        int index = ctx.getEditorFrame().getIndexOf(ctx.getEditorPanel());
        
        //TODO check if have to be saved
        
        if(index >= 0) {
            ctx.getEditorFrame().getTabbedPaneMain().remove(index);
        }
    }
    
    //panel
    
    public static void closePanel(EditorPluginContext ctx) {
        if(!ctx.hasEditorPanel())
            return;
        
        ctx.getEditorPanel().closeSelectedPanel();
    }
    
    //text view and history
    
    public static void undoredo(EditorPluginContext ctx, boolean undo) {
        EditorPanel editorPanel = ctx.getEditorPanel();
        
        if(undo) {
            if(editorPanel.getUndoManager().canUndo()) {
                editorPanel.getUndoManager().undo();
            }
        } else {
            if(editorPanel.getUndoManager().canRedo()) {
                editorPanel.getUndoManager().redo();
            }
        }
    }
    
    public static void toggleWrap(EditorPluginContext ctx) {
        if(!ctx.hasTextArea())
            return;
        
        boolean b = ctx.getTextArea().getLineWrap();
        ctx.getTextArea().setLineWrap(!b);
        ctx.getTextArea().setWrapStyleWord(!b);
    }
    
    //text manipulation (always on selection)
    
    public static void sort(EditorPluginContext ctx, boolean desc) {
        if(!ctx.hasTextArea())
            return;
        
        JTextAreaEditor textarea = ctx.getTextArea();
        
        List<String> s = Arrays.asList(textarea.getSelectedText().split("\n"));
        
        Collections.sort(s);
        if(desc) {
            Collections.reverse(s);
        }
        
        textarea.replaceSelection(s.stream().collect(joining("\n")), true);
    }
    
    public static void changeCase(EditorPluginContext ctx, String targetCase) {
        if(!ctx.hasTextArea())
            return;
            
        JTextAreaEditor textarea = ctx.getTextArea();
        
        String text = textarea.getSelectedText();
        
        switch(targetCase) {
            case "Lowercase": text = text.toLowerCase(); break;
            case "Uppercase": text = text.toUpperCase(); break;
            case "Propercase": text = WordUtils.capitalizeFully(text); break;
        }
        
        textarea.replaceSelection(text, true);
    }
    
    //text insertion
    
    public static void insertToday(EditorPluginContext ctx, String pattern) {
        if(!ctx.hasTextArea())
            return;
        
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String date = sdf.format(new Date());
        
        ctx.getTextArea().insert(date, ctx.getTextArea().getCaretPosition());
    }
    
    //text removal
    
    public static void removeLine(EditorPluginContext ctx) {
        if(!ctx.hasTextArea())
            return;
        
        JTextAreaEditor textarea = ctx.getTextArea();
        
        try {
            int line = textarea.getLineOfOffset(textarea.getCaretPosition());
            
            int start = textarea.getLineStartOffset(line);
            int end = textarea.getLineEndOffset(line);

            textarea.setSelectionStart(start);
            textarea.setSelectionEnd(end);
            textarea.replaceSelection("");
            
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void moveLineToClipboard(EditorPluginContext ctx) {
        if(!ctx.hasTextArea())
            return;
        
        JTextAreaEditor textarea = ctx.getTextArea();
        
        try {
            int line = textarea.getLineOfOffset(textarea.getCaretPosition());
            
            int start = textarea.getLineStartOffset(line);
            int end = textarea.getLineEndOffset(line);

            textarea.setSelectionStart(start);
            textarea.setSelectionEnd(end);
            
            StringSelection selection = new StringSelection(textarea.getSelectedText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            
            textarea.replaceSelection("");
            
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    //text selection
    
    public static void selectLine(EditorPluginContext ctx) {
        if(!ctx.hasTextArea())
            return;
        
        JTextAreaEditor textarea = ctx.getTextArea();
        
        try {
            int line = textarea.getLineOfOffset(textarea.getCaretPosition());
            
            int start = textarea.getLineStartOffset(line);
            int end = textarea.getLineEndOffset(line);

            textarea.requestFocus();
            textarea.setSelectionStart(start);
            textarea.setSelectionEnd(end);
            
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
