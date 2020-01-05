package com.github.mschroeder.github.neat;

import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_DELETE;
import javax.swing.JFrame;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class EditorPluginDialog extends javax.swing.JDialog {

    private EditorPlugin plugin;
    
    public EditorPluginDialog(JFrame frame, EditorPlugin plugin) {
        super(frame, true);
        initComponents();
        this.plugin = plugin;
        setLocationRelativeTo(frame);
        jListAltNames.setModel(new ListModel<String>() {
            @Override
            public int getSize() {
                return plugin.getAltNames().size();
            }

            @Override
            public String getElementAt(int index) {
                return plugin.getAltNames().get(index);
            }

            @Override
            public void addListDataListener(ListDataListener l) {
            }

            @Override
            public void removeListDataListener(ListDataListener l) {
            }
        });
        
        Binding.bind(jTextFieldName, str -> ((MethodBasedEditorPlugin)plugin).setName(str), () -> plugin.getName());
        Binding.bind(jTextAreaDesc, str -> ((MethodBasedEditorPlugin)plugin).setDescription(str), () -> plugin.getDesc());
        
        if(plugin.hasShortcut()) {
            jCheckBoxCtrl.setSelected(plugin.getShortcut().ctrl);
            jCheckBoxAlt.setSelected(plugin.getShortcut().alt);
            jCheckBoxShift.setSelected(plugin.getShortcut().shift);
            jTextFieldShortcut.setText(Shortcut.getKeyCodeName(plugin.getShortcut().keyCode));
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldName = new JTextFieldPlaceholder("Name");
        jTextFieldAltNames = new JTextFieldPlaceholder("Alt. Name");
        jScrollPane1 = new javax.swing.JScrollPane();
        jListAltNames = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaDesc = new JTextAreaPlaceholder("Description");
        jCheckBoxCtrl = new javax.swing.JCheckBox();
        jCheckBoxAlt = new javax.swing.JCheckBox();
        jCheckBoxShift = new javax.swing.JCheckBox();
        jTextFieldShortcut = new JTextFieldPlaceholder("Shortcut");
        jButtonResetShortcut = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTextFieldAltNames.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldAltNamesKeyPressed(evt);
            }
        });

        jListAltNames.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jListAltNamesKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jListAltNames);

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setRows(5);
        jScrollPane2.setViewportView(jTextAreaDesc);

        jCheckBoxCtrl.setText("Ctrl");
        jCheckBoxCtrl.setEnabled(false);

        jCheckBoxAlt.setText("Alt");
        jCheckBoxAlt.setEnabled(false);

        jCheckBoxShift.setText("Shift");
        jCheckBoxShift.setEnabled(false);

        jTextFieldShortcut.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldShortcutKeyPressed(evt);
            }
        });

        jButtonResetShortcut.setText("x");
        jButtonResetShortcut.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonResetShortcut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetShortcutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(jTextFieldName)
                    .addComponent(jTextFieldAltNames)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBoxCtrl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBoxAlt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBoxShift)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextFieldShortcut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonResetShortcut)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldAltNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxCtrl)
                    .addComponent(jCheckBoxAlt)
                    .addComponent(jCheckBoxShift)
                    .addComponent(jTextFieldShortcut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonResetShortcut, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldAltNamesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldAltNamesKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            if(!jTextFieldAltNames.getText().isEmpty()) {
                plugin.getAltNames().add(jTextFieldAltNames.getText());
                jTextFieldAltNames.setText("");
                jListAltNames.updateUI();
            }
        }
    }//GEN-LAST:event_jTextFieldAltNamesKeyPressed

    private void jListAltNamesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListAltNamesKeyPressed
        if(evt.getKeyCode() == VK_DELETE) {
            plugin.getAltNames().removeAll(jListAltNames.getSelectedValuesList());
            jListAltNames.clearSelection();
            jListAltNames.updateUI();
        }
    }//GEN-LAST:event_jListAltNamesKeyPressed

    private void jTextFieldShortcutKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldShortcutKeyPressed
        evt.consume();
        String keyCodeName = Shortcut.getKeyCodeName(evt.getKeyCode());
        if(keyCodeName.equals("WINDOWS"))
            return;
        
        jCheckBoxCtrl.setSelected(evt.isControlDown());
        jCheckBoxAlt.setSelected(evt.isAltDown());
        jCheckBoxShift.setSelected(evt.isShiftDown());
        SwingUtilities.invokeLater(() -> {
            jTextFieldShortcut.setText(keyCodeName);
            plugin.setShortcut(new Shortcut(jCheckBoxCtrl.isSelected(), jCheckBoxAlt.isSelected(), jCheckBoxShift.isSelected(), Shortcut.getKeyCode(jTextFieldShortcut.getText())));
        });
    }//GEN-LAST:event_jTextFieldShortcutKeyPressed

    private void jButtonResetShortcutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetShortcutActionPerformed
        jCheckBoxCtrl.setSelected(false);
        jCheckBoxAlt.setSelected(false);
        jCheckBoxShift.setSelected(false);
        jTextFieldShortcut.setText("");
        plugin.setShortcut(null);
    }//GEN-LAST:event_jButtonResetShortcutActionPerformed

    public static void showGUI(JFrame frame, EditorPlugin plugin) {
        EditorPluginDialog dialog = new EditorPluginDialog(frame, plugin);
        dialog.setVisible(true);
    }
    
    /*
    public static void main(String[] args) throws Exception {
        EditorFrame.addPluginClassPath("/home/otaku/.neat/plugins");
        EditorPluginRegistry.getInstance().load(new File("/home/otaku/.neat/plugins"));
        EditorPlugin p = EditorPluginRegistry.getInstance().getPlugins().get(0);
        showGUI(null, p);
    }
    */
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonResetShortcut;
    private javax.swing.JCheckBox jCheckBoxAlt;
    private javax.swing.JCheckBox jCheckBoxCtrl;
    private javax.swing.JCheckBox jCheckBoxShift;
    private javax.swing.JList<String> jListAltNames;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextFieldAltNames;
    private javax.swing.JTextField jTextFieldName;
    private javax.swing.JTextField jTextFieldShortcut;
    // End of variables declaration//GEN-END:variables
}
