import java.awt.*;
import javax.swing.*;

// หน้าเมนูหลัก: ชื่อเกม + ปุ่ม PLAY / EXIT
public class MainMenuPanel extends JPanel {

    public MainMenuPanel(CatGame parentFrame) {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 240, 225));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("NEKO PAW RESCUE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(new Color(80, 50, 20));

        JButton playBtn = new CustomButton("PLAY", new Dimension(240, 50));
        JButton customBtn = new CustomButton("CUSTOM MAP", new Dimension(240, 50));
        JButton exitBtn = new CustomButton("EXIT", new Dimension(240, 50));

        playBtn.addActionListener(e -> parentFrame.showLevelSelect()); // ไปหน้าเลือกด่าน
        customBtn.addActionListener(e -> parentFrame.showCustomMaps()); // ไปหน้าด่านที่สร้างเอง
        exitBtn.addActionListener(e -> System.exit(0));                // ปิดโปรแกรม

        gbc.gridy = 0; add(title, gbc);
        gbc.gridy = 1; add(playBtn, gbc);
        gbc.gridy = 2; add(customBtn, gbc);
        gbc.gridy = 3; add(exitBtn, gbc);
    }
}