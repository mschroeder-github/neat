package com.github.mschroeder.github.neat;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * An editor panel is the context for the file and contains the {@link JTextAreaEditor}.
 * @author Markus Schr&ouml;der
 */
public class EditorPanel extends javax.swing.JPanel {

    public static final String LF = "\n";
    public static final String CRLF = "\r\n";
    
    private String id;
    
    private EditorFrame editorFrame;

    private UndoManager undoManager;
    
    private File file;
    private String encoding = "UTF-8";
    //TODO use line ending
    private String lineEnding = LF; //windows: \r\n, mac: \r old?, linux: \n
    private String mimetype = "text/plain";

    
    
    private DocumentListener documentListener;

    public EditorPanel(EditorFrame editorFrame) {
        this.id = RandomStringUtils.randomAlphabetic(6);
        this.editorFrame = editorFrame;
        initComponents();

        undoManager = new UndoManager();
        undoManager.setLimit(-1);
        reinstallUndoManager();

        documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentChanged(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentChanged(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentChanged(e);
            }
        };

        jTabbedPanePanels.addChangeListener((e) -> {
            updateEditorPanelDivider();
        });
        
        //https://tips4java.wordpress.com/2009/05/23/text-component-line-number/
        TextLineNumber tln = new TextLineNumber(jTextAreaMain);
        /*
            setBorderGap – a convenience method to adjust the left and right insets of the Border, while retaining the outer MatteBorder
            setCurrentLineForeground – the Color of the current line number
            setDigitAlignment – align the line numbers to the LEFT, CENTER or RIGHT
            setMinimumDisplayDigits – controls the minimum width of the component. The width will increase automatically as necessary.
            setUpdateFont – enables the automatic updating of the Font when the Font of the related text component changes.
        */
        tln.setForeground(Color.gray);
        tln.setCurrentLineForeground(Color.black);
        tln.setMinimumDisplayDigits(2);
        tln.setUpdateFont(true);
        jScrollPaneMain.setRowHeaderView(tln);
    }

    //==========================================================================
    //tool panel (interactive tool like search)
    public <T  extends JPanel> T openPanel(String name, Class<T> editorPanelClass) {
        //select if already available
        for (int i = 0; i < jTabbedPanePanels.getTabCount(); i++) {
            if (jTabbedPanePanels.getTitleAt(i).equals(name)) {
                jTabbedPanePanels.setSelectedIndex(i);
                updateEditorPanelDivider();
                return (T) jTabbedPanePanels.getComponentAt(i);
            }
        }

        //create class
        JPanel panel;
        try {
            panel = editorPanelClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        EditorPluginPanel epp = (EditorPluginPanel) panel;
        
        
        epp.init(getContext()); //getEditorTextArea(), this, editorFrame);

        //add and select
        jTabbedPanePanels.addTab(name, panel);
        jTabbedPanePanels.setSelectedIndex(jTabbedPanePanels.getTabCount() - 1);

        updateEditorPanelDivider();
        
        return (T) panel;
    }
    
    public void closePanel(String name) {
        //select if already available
        for (int i = 0; i < jTabbedPanePanels.getTabCount(); i++) {
            if (jTabbedPanePanels.getTitleAt(i).equals(name)) {

                EditorPluginPanel epp = (EditorPluginPanel) jTabbedPanePanels.getComponentAt(i);
                epp.deinit(getContext());

                jTabbedPanePanels.removeTabAt(i);
                updateEditorPanelDivider();
                return;
            }
        }
    }

    public void closeSelectedPanel() {
        int selected = jTabbedPanePanels.getSelectedIndex();
        if (selected >= 0) {
            EditorPluginPanel epp = (EditorPluginPanel) jTabbedPanePanels.getComponentAt(selected);
            epp.deinit(getContext());

            jTabbedPanePanels.removeTabAt(selected);
        }
        updateEditorPanelDivider();
    }

    public void updateEditorPanelDivider() {
        JPanel panel = (JPanel) jTabbedPanePanels.getSelectedComponent();
        if (panel == null) {
            //closed
            jSplitPane1.setDividerLocation(getHeight());
            return;
        }

        //EditorPluginPanel epp = (EditorPluginPanel) panel;
        int loc = getHeight() - panel.getPreferredSize().height - 50;

        jSplitPane1.setDividerLocation(loc);
    }

    private EditorPluginContext getContext() {
        EditorPluginContext ctx = new EditorPluginContext();
        //TODO set getEditorTextArea(), this, editorFrame
        return ctx;
    }
    
    //==========================================================================
    //document listener
    private void installListener() {
        jTextAreaMain.getDocument().addDocumentListener(documentListener);
    }

    private void deinstallListener() {
        jTextAreaMain.getDocument().removeDocumentListener(documentListener);
    }

    private void documentChanged(DocumentEvent e) {
        editorFrame.unsavedState(this);
    }

    private void installUndoManager() {
        jTextAreaMain.getDocument().addUndoableEditListener(undoManager);
    }

    private void deinstallUndoManager() {
        jTextAreaMain.getDocument().removeUndoableEditListener(undoManager);
        undoManager.discardAllEdits();
    }
    
    private void reinstallUndoManager() {
        deinstallUndoManager();
        installUndoManager();
    }

    //==========================================================================
    //load save
    public void loadTextFromFile() {
        try {
            jTextAreaMain.setText(FileUtils.readFileToString(file, encoding));
            jTextAreaMain.setCaretPosition(0);
            editorFrame.savedState(this);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void saveTextToFile() {
        try {
            FileWriterWithEncoding fw = new FileWriterWithEncoding(file, encoding);
                
            for(int i = 0; i < jTextAreaMain.getLineCount(); i++) {
                int s = jTextAreaMain.getLineStartOffset(i);
                int e = jTextAreaMain.getLineEndOffset(i);
                
                String line = jTextAreaMain.getText(s, e - s);
                
                fw.write(line);
                fw.write(lineEnding);
            }
       
            FileUtils.writeStringToFile(file, jTextAreaMain.getText(), encoding);
            editorFrame.savedState(this);
        } catch (IOException | BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setFile(File file, boolean loadText) {
        this.file = file;

        if (file != null) {
            if(loadText) {
                loadTextFromFile();
            }
            installListener();
            installUndoManager();
        } else {
            deinstallUndoManager();
            deinstallListener();
            jTextAreaMain.setText("");
        }
    }

    //==========================================================================
    //get
    public JTextAreaEditor getEditorTextArea() {
        return (JTextAreaEditor) jTextAreaMain;
    }

    public File getFile() {
        return file;
    }

    public boolean hasFile() {
        return this.file != null;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public String getId() {
        return id;
    }

    //to overwrite it when from backup
    public void setId(String id) {
        this.id = id;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getLineEnding() {
        return lineEnding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
        editorFrame.unsavedState(this);
    }

    public void setLineEnding(String lineEnding) {
        this.lineEnding = lineEnding;
        editorFrame.unsavedState(this);
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }
    
    public String getMimeType() {
        return mimetype;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooserSave = new javax.swing.JFileChooser();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPaneMain = new javax.swing.JScrollPane();
        jTextAreaMain = new JTextAreaEditor();
        jTabbedPanePanels = new javax.swing.JTabbedPane();

        jFileChooserSave.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(100);
        jSplitPane1.setDividerSize(0);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTextAreaMain.setColumns(20);
        jTextAreaMain.setFont(new java.awt.Font("DialogInput", 0, 12)); // NOI18N
        jTextAreaMain.setRows(5);
        jTextAreaMain.setTabSize(4);
        jScrollPaneMain.setViewportView(jTextAreaMain);

        jSplitPane1.setLeftComponent(jScrollPaneMain);

        jTabbedPanePanels.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPanePanels.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jSplitPane1.setRightComponent(jTabbedPanePanels);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        updateEditorPanelDivider();
    }//GEN-LAST:event_formComponentShown


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser jFileChooserSave;
    private javax.swing.JScrollPane jScrollPaneMain;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPanePanels;
    private javax.swing.JTextArea jTextAreaMain;
    // End of variables declaration//GEN-END:variables
}
