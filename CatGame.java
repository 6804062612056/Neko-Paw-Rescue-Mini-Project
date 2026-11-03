import javax.swing.*;

public class CatGame extends JFrame {

    private MainMenuPanel mainMenuPanel;
    private LevelSelectPanel levelSelectPanel;
    private GamePanel gamePanel;
    private CustomMapPanel customMapPanel;
    private MapEditorPanel mapEditorPanel;

    private JPanel currentScreen; // หน้าจอที่กำลังแสดงอยู่

    public CatGame() {
        setTitle("Neko Paw Rescue");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 750);
        setResizable(false);
        setLocationRelativeTo(null);

        // สร้างทุกหน้าจอไว้ก่อน
        mainMenuPanel = new MainMenuPanel(this);
        levelSelectPanel = new LevelSelectPanel(this);
        gamePanel = new GamePanel(this);
        customMapPanel = new CustomMapPanel(this);
        mapEditorPanel = new MapEditorPanel(this);

        // เริ่มเกมที่หน้าเมนูหลัก
        changeScreen(mainMenuPanel);
    }

    // เปลี่ยนหน้าจอ
    private void changeScreen(JPanel newScreen) {
        if (currentScreen != null) {
            remove(currentScreen);
        }
        add(newScreen);
        currentScreen = newScreen;

        revalidate();
        repaint();
        newScreen.requestFocusInWindow();
    }

    public void showMainMenu() {
        changeScreen(mainMenuPanel);
    }

    public void showLevelSelect() {
        levelSelectPanel.refreshLevelButtons();
        changeScreen(levelSelectPanel);
    }

    public void showGame(int levelIndex) {
        gamePanel.loadLevel(levelIndex);
        changeScreen(gamePanel);
    }

    // ---------- Custom Map ----------
    public void showCustomMaps() {
        customMapPanel.refreshList();
        changeScreen(customMapPanel);
    }

    public void showMapEditor() {
        mapEditorPanel.startNewMap();
        changeScreen(mapEditorPanel);
    }

    public void showCustomGame(CustomMapStore.Entry entry) {
        gamePanel.setCustomLevel(entry.name, entry.lines);
        gamePanel.loadLevel(GamePanel.CUSTOM_LEVEL);
        changeScreen(gamePanel);
    }
}