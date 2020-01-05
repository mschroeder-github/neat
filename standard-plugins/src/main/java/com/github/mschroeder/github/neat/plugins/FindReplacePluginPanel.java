package com.github.mschroeder.github.neat.plugins;

import static com.github.mschroeder.github.neat.EditorFrame.withLambda;
import com.github.mschroeder.github.neat.EditorPlugin;
import com.github.mschroeder.github.neat.EditorPluginContext;
import com.github.mschroeder.github.neat.EditorPluginPanel;
import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class FindReplacePluginPanel extends javax.swing.JPanel implements EditorPluginPanel {
    
    public static class Plugin extends EditorPlugin {

        public static final String TAB_TITLE = "Find & Replace";

        @Override
        public String getName() {
            return TAB_TITLE;
        }
        
        @Override
        public void run(EditorPluginContext ctx) {
            FindReplacePluginPanel panel = ctx.getEditorPanel().openPanel(TAB_TITLE, FindReplacePluginPanel.class);
            panel.focusFind();
            //} else {
            //    editorPanel.closePanel(TAB_TITLE);
            //}*/
        }

    }
    
    private EditorPluginContext ctx;
    
    private final Color selectionColor = new Color(184, 207, 229);
    
    public FindReplacePluginPanel() {
        initComponents();
        
        jTextFieldFind.getDocument().addDocumentListener(withLambda(this::findChanged));
        jTextFieldReplace.getDocument().addDocumentListener(withLambda(this::replaceChanged));
    }
    
    @Override
    public void init(EditorPluginContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void deinit(EditorPluginContext ctx) {
        this.ctx = null;
    }
    
    public void focusFind() {
        jTextFieldFind.requestFocus();
        jTextFieldFind.selectAll();
    }
    
    public void focusReplace() {
        jTextFieldReplace.requestFocus();
    }
    
    private void findChanged(DocumentEvent e) {
        if(jTextFieldFind.getText().length() >= 3) {
            //TODO here use a highlighter to indicate what will be found
        }
    }
    
    private void replaceChanged(DocumentEvent e) {
        
    }
    
    private void findNext(boolean reverse) {
        JTextArea textarea = ctx.getTextArea();
        
        int s = textarea.getSelectionStart();
        int e = textarea.getSelectionEnd();
        
        String pattern = jTextFieldFind.getText();
        String t = textarea.getText();
        int foundIndex = t.indexOf(pattern, e);
        
        int hlnum = textarea.getHighlighter().getHighlights().length;
        System.out.println(foundIndex);
        System.out.println(hlnum);
        
        if(foundIndex != -1) {
            try {
                
                Object tag = textarea.getHighlighter().addHighlight(
                        foundIndex, 
                        foundIndex + pattern.length(),
                        new DefaultHighlighter.DefaultHighlightPainter(selectionColor)
                );
                
                textarea.setCaretPosition(foundIndex);
                
                System.out.println(tag);
                //textarea.requestFocus();
                //textarea.setCaretPosition(foundIndex);
                //textarea.select(foundIndex, foundIndex + pattern.length());
                //textarea.repaint();
            } catch (BadLocationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    private void findAll() {
        
    }
    
    private void replaceNext(boolean reverse) {
        
    }
    
    private void replaceAll() {
        
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldFind = new javax.swing.JTextField();
        jTextFieldReplace = new javax.swing.JTextField();
        jLabelStatus = new javax.swing.JLabel();
        jToggleButtonRegex = new javax.swing.JToggleButton();
        jToggleButtonMatchCase = new javax.swing.JToggleButton();
        jToggleButtonSelection = new javax.swing.JToggleButton();
        jToggleButtonWholeWord = new javax.swing.JToggleButton();

        jTextFieldFind.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldFindKeyPressed(evt);
            }
        });

        jLabelStatus.setText(" ");

        jToggleButtonRegex.setText("a");
        jToggleButtonRegex.setMargin(new java.awt.Insets(2, 2, 2, 2));

        jToggleButtonMatchCase.setText("a");
        jToggleButtonMatchCase.setMargin(new java.awt.Insets(2, 2, 2, 2));

        jToggleButtonSelection.setText("a");
        jToggleButtonSelection.setMargin(new java.awt.Insets(2, 2, 2, 2));

        jToggleButtonWholeWord.setText("a");
        jToggleButtonWholeWord.setMargin(new java.awt.Insets(2, 2, 2, 2));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 237, Short.MAX_VALUE)
                        .addComponent(jToggleButtonRegex, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonMatchCase, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToggleButtonWholeWord, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextFieldReplace)
                    .addComponent(jTextFieldFind, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelStatus)
                    .addComponent(jToggleButtonRegex)
                    .addComponent(jToggleButtonMatchCase)
                    .addComponent(jToggleButtonSelection)
                    .addComponent(jToggleButtonWholeWord))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTextFieldFind, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldFindKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldFindKeyPressed
        //TODO should be a setting
        boolean reverse = evt.isShiftDown();
        if(evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_N || evt.getKeyCode() == KeyEvent.VK_ENTER) {
            findNext(reverse);
        }
    }//GEN-LAST:event_jTextFieldFindKeyPressed

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JTextField jTextFieldFind;
    private javax.swing.JTextField jTextFieldReplace;
    private javax.swing.JToggleButton jToggleButtonMatchCase;
    private javax.swing.JToggleButton jToggleButtonRegex;
    private javax.swing.JToggleButton jToggleButtonSelection;
    private javax.swing.JToggleButton jToggleButtonWholeWord;
    // End of variables declaration//GEN-END:variables
}
