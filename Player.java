import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class Player {
    private int gridX, gridY;
    private int renderX, renderY;
    private int startX, startY;
    private int targetX, targetY;
    private boolean isMoving = false;
    private float moveProgress = 0.0f;

    private static final float MOVE_SPEED = 0.1f;

    private int direction = 0;
    private int currentFrame = 0;
    private int animationTimer = 0;

    private BufferedImage spriteSheet;
    private int spriteWidth = 32;
    private int spriteHeight = 32;

    public Player(String spritePath) {
        // อ่านรูปจาก classpath เพื่อให้ใช้ได้ทั้งตอนรันปกติและใน jar
        try (InputStream is = Resources.open(spritePath)) {
            if (is != null) {
                spriteSheet = ImageIO.read(is);
            }
            if (spriteSheet != null) {
                spriteWidth = spriteSheet.getWidth() / 3;
                spriteHeight = spriteSheet.getHeight() / 4;
            } else {
                System.err.println("Could not find player sprite: " + spritePath);
            }
        } catch (IOException e) {
            System.err.println("Could not load player sprite image from: " + spritePath);
            spriteSheet = null;
        }
    }

    public void setGridPosition(int gridX, int gridY, int tileSize) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.startX = gridX * tileSize;
        this.startY = gridY * tileSize;
        this.renderX = startX;
        this.renderY = startY;
        this.targetX = startX;
        this.targetY = startY;
        this.isMoving = false;
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

            animationTimer++;
            if (animationTimer % 6 == 0) {
                currentFrame = (currentFrame + 1) % 3;
            }

            if (moveProgress >= 1.0f) {
                moveProgress = 1.0f;
                isMoving = false;
                renderX = targetX;
                renderY = targetY;
                currentFrame = 0;
            } else {
                renderX = (int) (startX + (targetX - startX) * moveProgress);
                renderY = (int) (startY + (targetY - startY) * moveProgress);
            }
        }
    }

    public void draw(Graphics g, int offsetX, int offsetY, int tileSize) {
        int x = offsetX + renderX;
        int y = offsetY + renderY;

        if (spriteSheet != null) {
            int srcX = currentFrame * spriteWidth;
            int srcY = direction * spriteHeight;

            int displaySize = tileSize + 10;

            int xOffsetAdjustment = (displaySize - tileSize) / 2;
            int yOffsetAdjustment = (displaySize - tileSize);

            g.drawImage(spriteSheet,
                        x - xOffsetAdjustment, y - yOffsetAdjustment,
                        x + displaySize - xOffsetAdjustment, y + displaySize - yOffsetAdjustment,
                        srcX, srcY,
                        srcX + spriteWidth, srcY + spriteHeight,
                        null);
        } else {
            g.setColor(Color.ORANGE);
            g.fillOval(x, y, tileSize, tileSize);
        }
    }

    // วาดแมวท่านิ่ง (หันหน้าลง) ลงในช่องขนาด size — ใช้ในหน้าสร้างแมพ
    public void drawIdle(Graphics g, int x, int y, int size) {
        if (spriteSheet == null) {
            g.setColor(Color.ORANGE);
            g.fillOval(x + 2, y + 2, size - 4, size - 4);
            return;
        }
        g.drawImage(spriteSheet,
                    x, y, x + size, y + size,
                    0, 0, spriteWidth, spriteHeight,
                    null);
    }

    public void resetState() {
        this.isMoving = false;
        this.currentFrame = 0;
        this.direction = 0;
    }

    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isMoving() { return isMoving; }
    public void setDirection(int dir) { this.direction = dir; }
}
