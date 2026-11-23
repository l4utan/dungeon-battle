package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class CustomButton extends JButton {
    private Color color;

    public CustomButton(Color color, String tooltip) {
        this.color = color;
        setToolTipText(tooltip);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setPreferredSize(new Dimension(20, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
        g2.dispose();
    }
}