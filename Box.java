import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Box {
    private int gridX, gridY;
    private int renderX, renderY;
    private int startX, startY;
    private int targetX, targetY;
    private boolean isMoving = false;
    private float moveProgress = 0.0f;
    
    private static final float MOVE_SPEED = 0.1f;

    public Box(int gridX, int gridY, int tileSize) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.startX = gridX * tileSize;
        this.startY = gridY * tileSize;
        this.renderX = startX;
        this.renderY = startY;
        this.targetX = startX;
        this.targetY = startY;
    }

    public void moveToGrid(int newGridX, int newGridY, int tileSize) {
        this.gridX = newGridX;
        this.gridY = newGridY;
        this.startX = this.renderX;
        this.startY = this.renderY;
        this.targetX = newGridX * tileSize;
        this.targetY = newGridY * tileSize;
        this.isMoving = true;
        this.moveProgress = 0.0f;
    }

    public void update() {
        if (isMoving) {
            moveProgress += MOVE_SPEED;
            if (moveProgress >= 1.0f) {
                moveProgress = 1.0f;
                isMoving = false;
                renderX = targetX;
                renderY = targetY;
            } else {
                renderX = (int) (startX + (targetX - startX) * moveProgress);
                renderY = (int) (startY + (targetY - startY) * moveProgress);
            }
        }
    }

    public void draw(Graphics g, int offsetX, int offsetY, int tileSize, boolean isOnTarget) {
        drawCrate(g, offsetX + renderX, offsetY + renderY, tileSize, isOnTarget);
    }

    // วาดกล่องไม้ (ใช้ร่วมกันทั้งตอนเล่นและตอนสร้างแมพ เพื่อให้หน้าตาเหมือนกัน)
    public static void drawCrate(Graphics g0, int x, int y, int size, boolean isOnTarget) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color base = isOnTarget ? new Color(80, 200, 120) : new Color(200, 135, 60);
        Color light = isOnTarget ? new Color(130, 230, 160) : new Color(228, 170, 95);
        Color dark = isOnTarget ? new Color(40, 140, 80) : new Color(130, 75, 30);

        int m = Math.max(2, size / 12);          // ระยะขอบ
        int bx = x + m, by = y + m, bs = size - 2 * m;
        int arc = Math.max(4, size / 6);

        // เงาใต้กล่อง
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRoundRect(bx + 1, by + 2, bs, bs, arc, arc);

        // ตัวกล่อง
        g.setColor(base);
        g.fillRoundRect(bx, by, bs, bs, arc, arc);

        // แผ่นไม้ด้านใน
        int pad = Math.max(3, size / 7);
        g.setColor(light);
        g.fillRoundRect(bx + pad, by + pad, bs - 2 * pad, bs - 2 * pad, arc / 2, arc / 2);

        // ไม้พาดแนวทแยง
        g.setColor(dark);
        g.setStroke(new BasicStroke(Math.max(2f, size / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(bx + pad, by + pad, bx + bs - pad, by + bs - pad);
        g.drawLine(bx + bs - pad, by + pad, bx + pad, by + bs - pad);

        // ขอบกล่อง
        g.setStroke(new BasicStroke(Math.max(2f, size / 12f)));
        g.drawRoundRect(bx, by, bs, bs, arc, arc);

        // ตะปูมุมกล่อง
        int n = Math.max(2, size / 14);
        g.setColor(dark.darker());
        int o = pad / 2;
        g.fillOval(bx + o, by + o, n, n);
        g.fillOval(bx + bs - o - n, by + o, n, n);
        g.fillOval(bx + o, by + bs - o - n, n, n);
        g.fillOval(bx + bs - o - n, by + bs - o - n, n, n);
        g.dispose();
    }

    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isMoving() { return isMoving; }
}