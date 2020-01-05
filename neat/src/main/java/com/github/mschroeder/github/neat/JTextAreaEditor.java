package com.github.mschroeder.github.neat;

import javax.swing.JTextArea;

/**
 *
 * @author Markus Schr&ouml;der
 */
public class JTextAreaEditor extends JTextArea {

    public JTextAreaEditor() {
        
    }

    public void replaceSelection(String s, boolean selectAgain) {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        
        super.replaceSelection(s);
        
        if(selectAgain) {
            setSelectionStart(start);
            setSelectionEnd(end);
        }
    }
    
}
