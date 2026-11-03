import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class CustomButton extends JButton {
    private Color normalColor = new Color(220, 200, 170);
    private Color hoverColor = new Color(240, 220, 190);
    private Color textColor = new Color(80, 50, 20);

    public CustomButton(String text) {
        super(text);
        initButton();
    }

    public CustomButton(String text, Dimension size) {
        super(text);
        setPreferredSize(size);
        initButton();
    }

    private void initButton() {
        setFont(new Font("Arial", Font.BOLD, 16));
        setForeground(textColor);
        setBackground(normalColor);
        setFocusPainted(false);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(normalColor);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

        g2.setColor(new Color(150, 110, 70, 180));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);

        g2.dispose();
        super.paintComponent(g);
    }
}