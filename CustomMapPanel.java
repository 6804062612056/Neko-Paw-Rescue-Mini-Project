import java.awt.*;
import java.util.List;
import javax.swing.*;

// หน้า Custom Map: แสดงรายการด่านที่สร้างเอง (เล่น / ลบ / เพิ่ม) สูงสุด 5 ด่าน
public class CustomMapPanel extends JPanel {

    private static final Color BROWN = new Color(80, 50, 20);

    private CatGame parentFrame;
    private JPanel listPanel;
    private JLabel countLabel;

    public CustomMapPanel(CatGame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 240, 225));

        // ส่วนหัว: ชื่อหน้า + จำนวนด่าน (เช่น 2 / 5)
        JLabel title = new JLabel("CUSTOM MAPS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(BROWN);

        countLabel = new JLabel("", SwingConstants.RIGHT);
        countLabel.setFont(new Font("Arial", Font.BOLD, 18));
        countLabel.setForeground(BROWN);
        countLabel.setPreferredSize(new Dimension(130, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(25, 20, 10, 30));
        header.add(javax.swing.Box.createHorizontalStrut(130), BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        header.add(countLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ส่วนกลาง: กรอบรายการด่าน
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(225, 205, 170));
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BROWN, 4),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(10, 80, 10, 80));
        center.add(listPanel, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ส่วนท้าย: BACK / ADD MAP
        JButton backBtn = new CustomButton("BACK", new Dimension(140, 40));
        JButton addBtn = new CustomButton("ADD MAP", new Dimension(140, 40));
        backBtn.addActionListener(e -> parentFrame.showMainMenu());
        addBtn.addActionListener(e -> {
            if (CustomMapStore.isFull()) {
                JOptionPane.showMessageDialog(this,
                        "You can have up to " + CustomMapStore.MAX_MAPS + " custom maps.\nDelete one first.");
            } else {
                parentFrame.showMapEditor();
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        footer.add(backBtn);
        footer.add(addBtn);
        add(footer, BorderLayout.SOUTH);

        refreshList();
    }

    // อัปเดตรายการทุกครั้งที่เข้าหน้านี้
    public void refreshList() {
        listPanel.removeAll();
        List<CustomMapStore.Entry> maps = CustomMapStore.loadAll();
        countLabel.setText(maps.size() + " / " + CustomMapStore.MAX_MAPS);

        if (maps.isEmpty()) {
            JLabel empty = new JLabel("No custom maps yet. Press ADD MAP to create one!", SwingConstants.CENTER);
            empty.setFont(new Font("Arial", Font.PLAIN, 16));
            empty.setForeground(BROWN);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(javax.swing.Box.createVerticalGlue());
            listPanel.add(empty);
            listPanel.add(javax.swing.Box.createVerticalGlue());
        } else {
            for (CustomMapStore.Entry entry : maps) {
                listPanel.add(createRow(entry));
                listPanel.add(javax.swing.Box.createVerticalStrut(8));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // แถวของด่าน 1 ด่าน: ชื่อ | PLAY / DELETE
    private JPanel createRow(CustomMapStore.Entry entry) {
        JPanel row = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 225, 195));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(new Color(160, 110, 60));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        row.setPreferredSize(new Dimension(0, 80));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ชื่อด่าน
        JLabel name = new JLabel(entry.name);
        name.setFont(new Font("Arial", Font.BOLD, 22));
        name.setForeground(BROWN);
        name.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        row.add(name, BorderLayout.CENTER);

        // ปุ่ม
        JButton playBtn = new CustomButton("PLAY", new Dimension(100, 40));
        JButton deleteBtn = new CustomButton("DELETE", new Dimension(100, 40));
        playBtn.addActionListener(e -> parentFrame.showCustomGame(entry));
        deleteBtn.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Delete \"" + entry.name + "\"?", "Delete map",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                CustomMapStore.delete(entry.slot);
                refreshList();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        buttons.setOpaque(false);
        buttons.add(playBtn);
        buttons.add(deleteBtn);
        row.add(buttons, BorderLayout.EAST);

        return row;
    }
}
