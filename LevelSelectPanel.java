import java.awt.*;
import javax.swing.*;

// หน้าเลือกด่าน
public class LevelSelectPanel extends JPanel {

    private static final int TOTAL_LEVELS = 8;

    private CatGame parentFrame;
    private JPanel gridPanel;

    public LevelSelectPanel(CatGame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 240, 225));

        // ส่วนหัว
        JLabel title = new JLabel("SELECT LEVEL", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(new Color(80, 50, 20));
        title.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // ส่วนกลาง
        gridPanel = new JPanel(new GridLayout(2, 4, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        add(gridPanel, BorderLayout.CENTER);

        // ส่วนท้าย
        JButton backBtn = new CustomButton("BACK", new Dimension(140, 40));
        backBtn.addActionListener(e -> parentFrame.showMainMenu());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);

        refreshLevelButtons();
    }

    // อัปเดตปุ่มทุกครั้งที่เข้า
    public void refreshLevelButtons() {
        gridPanel.removeAll();

        for (int i = 0; i < TOTAL_LEVELS; i++) {
            int levelIndex = i;
            boolean unlocked = LevelProgress.isLevelUnlocked(levelIndex);
            boolean cleared = LevelProgress.isLevelCleared(levelIndex);

            JButton btn = createLevelButton(levelIndex, unlocked, cleared);

            if (unlocked) {
                btn.addActionListener(e -> parentFrame.showGame(levelIndex));
            }
            gridPanel.add(btn);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton createLevelButton(int levelIndex, boolean unlocked, boolean cleared) {
        JButton btn = new JButton();
        btn.setFocusable(false);
        btn.setLayout(new BorderLayout());

        if (unlocked) {
            // ด่านที่เล่นได้: แสดงเลขด่าน + PLAY / CLEARED
            btn.setBackground(new Color(240, 225, 195));
            btn.setBorder(BorderFactory.createLineBorder(new Color(160, 110, 60), 3, true));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel number = new JLabel(String.valueOf(levelIndex + 1), SwingConstants.CENTER);
            number.setFont(new Font("Arial", Font.BOLD, 28));
            number.setForeground(new Color(80, 50, 20));

            JLabel status = new JLabel(cleared ? "CLEARED" : "PLAY", SwingConstants.CENTER);
            status.setFont(new Font("Arial", Font.BOLD, 12));
            status.setForeground(cleared ? new Color(40, 140, 50) : new Color(140, 90, 40));

            btn.add(number, BorderLayout.CENTER);
            btn.add(status, BorderLayout.SOUTH);
        } else {
            // ด่านที่ยังล็อก: กดไม่ได้
            btn.setBackground(new Color(200, 195, 185));
            btn.setBorder(BorderFactory.createLineBorder(new Color(150, 145, 135), 3, true));
            btn.setEnabled(false);

            JLabel lock = new JLabel("🔒", SwingConstants.CENTER);
            lock.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

            JLabel text = new JLabel("LOCKED", SwingConstants.CENTER);
            text.setFont(new Font("Arial", Font.BOLD, 11));
            text.setForeground(new Color(110, 105, 95));

            btn.add(lock, BorderLayout.CENTER);
            btn.add(text, BorderLayout.SOUTH);
        }

        return btn;
    }
}