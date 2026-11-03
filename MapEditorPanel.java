import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

// หน้าสร้างด่านใหม่: คลิก/ลากเพื่อวาง กำแพง / เป้าหมาย / กล่อง / แมว (คลิกขวา = ลบ)
public class MapEditorPanel extends JPanel {

    private static final int COLS = 20;
    private static final int ROWS = 15;
    private static final int CELL = 32;
    private static final Color BROWN = new Color(80, 50, 20);

    // ชนิดของที่วางได้ (ตรงกับตัวอักษรในไฟล์ด่าน)
    private static final char WALL = 'w', TARGET = 't', BOX = 'b', PLAYER = 'p', EMPTY = ' ';

    private CatGame parentFrame;
    private char[][] grid = new char[ROWS][COLS];
    private char tool = WALL;
    private JTextField nameField;
    private List<ToolButton> toolButtons = new ArrayList<>();
    private Player catPreview = new Player("pictures/pipo-nekonin004.png"); // รูปแมวสำหรับวางในตาราง

    public MapEditorPanel(CatGame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 240, 225));

        // ส่วนหัว: ชื่อหน้า + ช่องใส่ชื่อด่าน
        JLabel title = new JLabel("CREATE MAP", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(BROWN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(BROWN);
        nameField = new JTextField(16);
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        nameRow.setOpaque(false);
        nameRow.add(nameLabel);
        nameRow.add(nameField);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        north.setBorder(BorderFactory.createEmptyBorder(15, 0, 8, 0));
        north.add(title);
        north.add(javax.swing.Box.createVerticalStrut(8));
        north.add(nameRow);

        JLabel hint = new JLabel("Left click / drag = place     Right click = erase", SwingConstants.CENTER);
        hint.setFont(new Font("Arial", Font.PLAIN, 12));
        hint.setForeground(new Color(140, 90, 40));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        north.add(javax.swing.Box.createVerticalStrut(6));
        north.add(hint);
        add(north, BorderLayout.NORTH);

        // ส่วนกลาง: ตารางวาดด่าน
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(new GridCanvas());
        add(center, BorderLayout.CENTER);

        // ส่วนท้าย: แถวเครื่องมือ + แถวปุ่ม
        JPanel toolRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        toolRow.setOpaque(false);
        addToolButton(toolRow, "WALL", WALL);
        addToolButton(toolRow, "TARGET", TARGET);
        addToolButton(toolRow, "BOX", BOX);
        addToolButton(toolRow, "CAT", PLAYER);
        addToolButton(toolRow, "ERASE", EMPTY);
        selectTool(WALL);

        JButton clearBtn = new CustomButton("CLEAR", new Dimension(130, 38));
        JButton saveBtn = new CustomButton("SAVE", new Dimension(130, 38));
        JButton cancelBtn = new CustomButton("CANCEL", new Dimension(130, 38));
        clearBtn.addActionListener(e -> {
            clearGrid();
            repaint();
        });
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> parentFrame.showCustomMaps());

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 4));
        actionRow.setOpaque(false);
        actionRow.add(clearBtn);
        actionRow.add(saveBtn);
        actionRow.add(cancelBtn);

        JPanel south = new JPanel(new GridLayout(2, 1));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        south.add(toolRow);
        south.add(actionRow);
        add(south, BorderLayout.SOUTH);

        clearGrid();
    }

    // เรียกทุกครั้งที่เปิดหน้านี้: เริ่มด่านใหม่ว่าง ๆ
    public void startNewMap() {
        clearGrid();
        nameField.setText("Custom Map " + CustomMapStore.nextFreeSlot());
        selectTool(WALL);
        repaint();
    }

    private void clearGrid() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) grid[r][c] = EMPTY;
        }
    }

    private void addToolButton(JPanel row, String text, char toolChar) {
        ToolButton btn = new ToolButton(text, toolChar);
        btn.addActionListener(e -> selectTool(toolChar));
        toolButtons.add(btn);
        row.add(btn);
    }

    private void selectTool(char newTool) {
        tool = newTool;
        for (ToolButton b : toolButtons) {
            b.selected = (b.toolChar == newTool);
            b.repaint();
        }
    }

    // วางของลงช่อง
    private void place(int c, int r, char what) {
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) return;
        if (what == PLAYER) {
            // มีแมวได้ตัวเดียว: ลบตัวเก่าก่อน
            for (int rr = 0; rr < ROWS; rr++) {
                for (int cc = 0; cc < COLS; cc++) {
                    if (grid[rr][cc] == PLAYER) grid[rr][cc] = EMPTY;
                }
            }
        }
        grid[r][c] = what;
    }

    // ตรวจและบันทึกด่าน
    private void save() {
        if (CustomMapStore.isFull()) {
            JOptionPane.showMessageDialog(this, "You already have " + CustomMapStore.MAX_MAPS + " custom maps.");
            return;
        }

        int players = 0, boxes = 0, targets = 0, px = -1, py = -1;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                switch (grid[r][c]) {
                    case PLAYER: players++; px = c; py = r; break;
                    case BOX: boxes++; break;
                    case TARGET: targets++; break;
                    default: break;
                }
            }
        }

        String problem = null;
        if (players != 1) {
            problem = "Place exactly one cat.";
        } else if (boxes == 0) {
            problem = "Place at least one box.";
        } else if (boxes != targets) {
            problem = "Boxes and targets must be equal.\n(Boxes: " + boxes + ", Targets: " + targets + ")";
        } else if (canEscape(px, py)) {
            problem = "The map must be closed by walls.\nThe cat can walk out of the map.";
        }
        if (problem != null) {
            JOptionPane.showMessageDialog(this, problem, "Cannot save", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        if (name.isEmpty()) name = "Custom Map " + CustomMapStore.nextFreeSlot();
        if (name.length() > 20) name = name.substring(0, 20);

        if (CustomMapStore.add(name, cropLines())) {
            parentFrame.showCustomMaps();
        } else {
            JOptionPane.showMessageDialog(this, "Could not save the map file.\nFolder: " + CustomMapStore.folder(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // แมวเดินออกขอบตารางได้ไหม (ถ้าได้ แปลว่ากำแพงไม่ปิด)
    private boolean canEscape(int startC, int startR) {
        boolean[][] seen = new boolean[ROWS][COLS];
        java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
        queue.add(new int[]{startC, startR});
        seen[startR][startC] = true;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            if (cur[0] == 0 || cur[1] == 0 || cur[0] == COLS - 1 || cur[1] == ROWS - 1) return true;
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] d : dirs) {
                int nc = cur[0] + d[0], nr = cur[1] + d[1];
                if (!seen[nr][nc] && grid[nr][nc] != WALL) {
                    seen[nr][nc] = true;
                    queue.add(new int[]{nc, nr});
                }
            }
        }
        return false;
    }

    // ตัดขอบว่างทิ้ง แล้วแปลงเป็นบรรทัดข้อความ (เพื่อให้ด่านอยู่กลางจอตอนเล่น)
    private List<String> cropLines() {
        int minR = ROWS, maxR = -1, minC = COLS, maxC = -1;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r][c] != EMPTY) {
                    minR = Math.min(minR, r);
                    maxR = Math.max(maxR, r);
                    minC = Math.min(minC, c);
                    maxC = Math.max(maxC, c);
                }
            }
        }
        List<String> lines = new ArrayList<>();
        for (int r = minR; r <= maxR; r++) {
            lines.add(new String(grid[r], minC, maxC - minC + 1));
        }
        return lines;
    }

    // ปุ่มเครื่องมือ (ไฮไลต์ปุ่มที่เลือกอยู่)
    private static class ToolButton extends CustomButton {
        final char toolChar;
        boolean selected = false;

        ToolButton(String text, char toolChar) {
            super(text, new Dimension(116, 36));
            this.toolChar = toolChar;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (selected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BROWN);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                g2.dispose();
            }
        }
    }

    // ตารางวาดด่าน
    private class GridCanvas extends JPanel {
        GridCanvas() {
            setPreferredSize(new Dimension(COLS * CELL, ROWS * CELL));
            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { paintAt(e); }

                @Override
                public void mouseDragged(MouseEvent e) { paintAt(e); }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        // คลิกซ้าย = วางตามเครื่องมือ, คลิกขวา = ลบ
        private void paintAt(MouseEvent e) {
            char what = SwingUtilities.isRightMouseButton(e) ? EMPTY : tool;
            place(e.getX() / CELL, e.getY() / CELL, what);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(235, 228, 210));
            g.fillRect(0, 0, getWidth(), getHeight());

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int x = c * CELL;
                    int y = r * CELL;
                    g.setColor(new Color(220, 210, 195));
                    g.drawRect(x, y, CELL, CELL);

                    switch (grid[r][c]) {
                        case WALL:
                            g.setColor(new Color(100, 80, 70));
                            g.fill3DRect(x, y, CELL, CELL, true);
                            break;
                        case TARGET:
                            g.setColor(new Color(230, 70, 70));
                            g.fillOval(x + CELL / 3, y + CELL / 3, CELL / 3, CELL / 3);
                            break;
                        case BOX:
                            Box.drawCrate(g, x, y, CELL, false);
                            break;
                        case PLAYER:
                            catPreview.drawIdle(g, x, y, CELL);
                            break;
                        default:
                            break;
                    }
                }
            }
            g.setColor(BROWN);
            g.drawRect(0, 0, COLS * CELL - 1, ROWS * CELL - 1);
        }
    }
}
