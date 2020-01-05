package com.github.mschroeder.github.neat;

/**
 *
 * @author Markus Schr&ouml;der
 */
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class JTextAreaPlaceholder extends JTextArea {

    private String ph;

    public JTextAreaPlaceholder(String ph) {
        this.ph = ph;
    }

    public JTextAreaPlaceholder() {
        this.ph = null;
    }

    /**
     * Gets text, returns placeholder if nothing specified
     * @return 
     */
    /*
    @Override
    public String getText() {
        String text = super.getText();

        if (text.trim().length() == 0 && ph != null) {
            text = ph;
        }

        return text;
    }
    */

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (super.getText().length() > 0 || ph == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(super.getDisabledTextColor());
        g2.drawString(ph, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
    }
}
