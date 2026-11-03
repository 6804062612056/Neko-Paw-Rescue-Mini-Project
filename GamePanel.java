import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;

public class GamePanel extends JPanel {

    private static final int TILE = 42;   // ขนาดช่อง 1 ช่อง
    private static final int MAX = 30;    // แผนที่ใหญ่สุด 30x30

    // ข้อมูลเกม
    private char[][] map = new char[MAX][MAX]; //สร้างตัวแปรเก็บข้อมูลด่าน
    private Player player;
    private List<Box> boxes = new CopyOnWriteArrayList<>();
    private int mapWidth, mapHeight;
    private int currentLevel = 0;
    public static final int CUSTOM_LEVEL = -1;   // ใช้เป็นเลขด่านสำหรับด่านที่ผู้เล่นสร้างเอง
    private List<String> customLines;            // แผนที่ของด่านที่สร้างเอง
    private String customName = "";
    private int moves = 0;
    private volatile boolean isPaused = false;
    private volatile boolean levelCompleted = false;

    private CatGame parentFrame;
    private JPanel pauseMenu;

    // Thread 60 fps (16 ms)
    private Thread gameThread = new Thread(new Runnable() {
        public void run() {
            while (true) {
                if (!isPaused) {
                    player.update();
                    for (Box box : boxes) {
                        box.update();
                    }
                    checkWin();
                }
                repaint();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                }
            }
        }
    });

    public GamePanel(CatGame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setPreferredSize(new Dimension(900, 750));

        player = new Player("pictures/pipo-nekonin004.png");

        createPauseMenu();
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
            }
        });

        gameThread.setDaemon(true); // ปิดหน้าต่างแล้ว thread หยุดตาม
        gameThread.start();
    }

    // รับปุ่มคีย์บอร์ด
    private void handleKey(int key) {
        if (key == KeyEvent.VK_ESCAPE) {
            togglePause();
            return;
        }
        // ห้ามเดินตอนหยุดเกม / กำลังเคลื่อนไหว / ผ่านด่านแล้ว
        if (isPaused || isAnimating() || levelCompleted) return;

        if (key == KeyEvent.VK_R) {
            loadLevel(currentLevel);
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            player.setDirection(3);
            tryMove(0, -1);
        } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            player.setDirection(0);
            tryMove(0, 1);
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            player.setDirection(1);
            tryMove(-1, 0);
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            player.setDirection(2);
            tryMove(1, 0);
        }
    }

    // เดิน / ดันกล่อง
    private void tryMove(int dx, int dy) {
        int newX = player.getGridX() + dx;
        int newY = player.getGridY() + dy;

        if (isWall(newX, newY)) return; // ชนกำแพง เดินไม่ได้

        Box box = getBoxAt(newX, newY);
        if (box == null) {
            // ไม่มีกล่อง -> เดินได้
            player.moveToGrid(newX, newY, TILE);
            moves++;
        } else {
            // มีกล่อง -> ดูช่องหลังกล่องว่างไหม
            int boxNewX = newX + dx;
            int boxNewY = newY + dy;
            if (!isWall(boxNewX, boxNewY) && getBoxAt(boxNewX, boxNewY) == null) {
                box.moveToGrid(boxNewX, boxNewY, TILE);
                player.moveToGrid(newX, newY, TILE);
                moves++;
            }
        }
    }

    private boolean isWall(int x, int y) {
        if (x < 0 || x >= MAX || y < 0 || y >= MAX) return true;
        return map[y][x] == '#';
    }

    private Box getBoxAt(int x, int y) {
        for (Box box : boxes) {
            if (box.getGridX() == x && box.getGridY() == y) return box;
        }
        return null;
    }

    private boolean isAnimating() {
        if (player.isMoving()) return true;
        for (Box box : boxes) {
            if (box.isMoving()) return true;
        }
        return false;
    }

    // ตั้งค่าด่านที่สร้างเอง (แล้วเรียก loadLevel(CUSTOM_LEVEL) เพื่อเริ่มเล่น)
    public void setCustomLevel(String name, List<String> lines) {
        customName = name;
        customLines = new ArrayList<>(lines);
    }

    // โหลดด่านจากไฟล์ (อ่านจาก classpath เพื่อให้ใช้ได้ทั้งตอนรันปกติและใน jar)
    // w = กำแพง, t = เป้าหมาย, b = กล่อง, p = ผู้เล่น
    public void loadLevel(int levelIndex) {
        currentLevel = levelIndex;
        moves = 0;
        levelCompleted = false;
        isPaused = false;
        pauseMenu.setVisible(false);
        player.resetState();
        boxes.clear();

        // ล้างแผนที่ให้เป็นพื้นว่าง
        for (int r = 0; r < MAX; r++) {
            for (int c = 0; c < MAX; c++) {
                map[r][c] = ' ';
            }
        }

        // อ่านไฟล์เก็บไว้ใน list
        List<String> lines = new ArrayList<>();
        String path = "/levels/level" + (levelIndex + 1) + ".txt";
        if (levelIndex == CUSTOM_LEVEL) {
            // ด่านที่สร้างเอง: ใช้แผนที่ที่ส่งเข้ามา ไม่ต้องอ่านไฟล์
            lines.addAll(customLines);
        } else {
            try (InputStream is = Resources.open(path)) {
                if (is == null) {
                    System.err.println("Level file not found: " + path);
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = reader.readLine()) != null && lines.size() < MAX) {
                        lines.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Cannot read level file: " + e.getMessage());
            }
        }

        mapHeight = lines.size();
        mapWidth = 0;

        // แปลงตัวอักษรเป็นแผนที่ / กล่อง / ผู้เล่น
        for (int r = 0; r < lines.size(); r++) {
            String line = lines.get(r);
            mapWidth = Math.max(mapWidth, line.length());

            for (int c = 0; c < line.length() && c < MAX; c++) {
                char ch = line.charAt(c);

                if (ch == 'w') map[r][c] = '#';
                if (ch == 't') map[r][c] = '.';
                if (ch == 'b') boxes.add(new Box(c, r, TILE));
                if (ch == 'p') player.setGridPosition(c, r, TILE);
            }
        }

        requestFocusInWindow();
    }

    // เช็คชนะ: กล่องทุกใบต้องอยู่บนช่องที่กำหนด
    private void checkWin() {
        if (boxes.isEmpty() || isAnimating() || levelCompleted) return;

        for (Box box : boxes) {
            if (map[box.getGridY()][box.getGridX()] != '.') return; // มีกล่องที่ยังไม่อยู่บนช่องที่กำหนด
        }

        levelCompleted = true;
        if (currentLevel != CUSTOM_LEVEL) LevelProgress.unlockNextLevel(currentLevel);

        // แสดงข้อความผ่านด่าน
        SwingUtilities.invokeLater(() -> {
            if (currentLevel == CUSTOM_LEVEL) {
                JOptionPane.showMessageDialog(this, customName + " Cleared in " + moves + " moves!");
                parentFrame.showCustomMaps();
            } else {
                JOptionPane.showMessageDialog(this, "Level " + (currentLevel + 1) + " Cleared in " + moves + " moves!");
                parentFrame.showLevelSelect();
            }
        });
    }

    // เมนู Pause
    private void createPauseMenu() {
        pauseMenu = new JPanel(new GridLayout(4, 1, 0, 10));
        pauseMenu.setBackground(new Color(240, 230, 210));
        pauseMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 50, 20), 3),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        pauseMenu.setBounds(310, 240, 280, 260);
        pauseMenu.setVisible(false);

        JLabel title = new JLabel("PAUSED", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(80, 50, 20));

        JButton resumeBtn = new CustomButton("Resume");
        JButton restartBtn = new CustomButton("Restart");
        JButton exitBtn = new CustomButton("Exit to Level Select");

        resumeBtn.addActionListener(e -> togglePause());
        restartBtn.addActionListener(e -> loadLevel(currentLevel));
        exitBtn.addActionListener(e -> {
            isPaused = false;
            pauseMenu.setVisible(false);
            if (currentLevel == CUSTOM_LEVEL) parentFrame.showCustomMaps();
            else parentFrame.showLevelSelect();
        });

        pauseMenu.add(title);
        pauseMenu.add(resumeBtn);
        pauseMenu.add(restartBtn);
        pauseMenu.add(exitBtn);
        add(pauseMenu);
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseMenu.setVisible(isPaused);
        if (!isPaused) requestFocusInWindow();
    }

    // วาดหน้าจอ
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // พื้นหลัง
        g.setColor(new Color(245, 240, 225));
        g.fillRect(0, 0, getWidth(), getHeight());

        // ข้อความด้านบน
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString((currentLevel == CUSTOM_LEVEL ? customName : "Level: " + (currentLevel + 1)) + "  |  Moves: " + moves, 20, 30);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("WASD / Arrow Keys  |  R = Restart  |  ESC = Pause", 20, 50);

        // จัดแผนที่ให้อยู่กลางจอ
        int offsetX = (getWidth() - mapWidth * TILE) / 2;
        int offsetY = (getHeight() - mapHeight * TILE) / 2 + 20;

        // วาดแผนที่
        for (int r = 0; r < mapHeight; r++) {
            for (int c = 0; c < mapWidth; c++) {
                int x = offsetX + c * TILE;
                int y = offsetY + r * TILE;

                if (map[r][c] == '#') {
                    g.setColor(new Color(100, 80, 70));
                    g.fill3DRect(x, y, TILE, TILE, true);
                } else {
                    g.setColor(new Color(220, 210, 195));
                    g.drawRect(x, y, TILE, TILE);
                    if (map[r][c] == '.') {
                        g.setColor(new Color(230, 70, 70));
                        g.fillOval(x + TILE / 3, y + TILE / 3, TILE / 3, TILE / 3);
                    }
                }
            }
        }

        // วาดกล่อง (เปลี่ยนสีถ้าอยู่บนช่องที่กำหนด)
        for (Box box : boxes) {
            boolean onTarget = (map[box.getGridY()][box.getGridX()] == '.');
            box.draw(g, offsetX, offsetY, TILE, onTarget);
        }

        // วาดผู้เล่น
        player.draw(g, offsetX, offsetY, TILE);

        // ทำจอมืดตอน Pause (วาดก่อนเมนู เพราะ Swing วาดลูกทีหลัง)
        if (isPaused) {
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
