package com.github.mschroeder.github.neat;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.text.JTextComponent;

/**
 * To bind JTextField or JTextArea one way to an object.
 * @author Markus Schr&ouml;der
 */
public class Binding {
    
    public static void bind(JTextComponent textComponent, Consumer<String> setter, Supplier<String> getter) {
        textComponent.setText(getter.get());
        textComponent.getDocument().addDocumentListener(
                DocumentListenerAdapter.lambda(setter)
        );
    }
    
}
