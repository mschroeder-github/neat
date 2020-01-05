package com.github.mschroeder.github.neat;

import java.util.function.Consumer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * An adapter that catches all changes: insert, remove and change.
 * @author Markus Schr&ouml;der
 */

public class DocumentListenerAdapter implements DocumentListener {

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
    
    public void documentChanged(DocumentEvent e) {
        
    }
    
    public static DocumentListenerAdapter lambda(Consumer<String> consumer) {
        return new DocumentListenerAdapter() {
            @Override
            public void documentChanged(DocumentEvent e) {
                try {
                    Document doc = e.getDocument();
                    consumer.accept(e.getDocument().getText(0, doc.getLength()));
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }
    
}
