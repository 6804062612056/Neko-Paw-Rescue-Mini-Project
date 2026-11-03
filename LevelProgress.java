public class LevelProgress {
    private static int maxUnlockedLevel = 0;
    private static boolean[] levelCleared = new boolean[8];

    public static boolean isLevelUnlocked(int levelIndex) {
        return levelIndex <= maxUnlockedLevel;
    }

    public static void unlockNextLevel(int completedLevelIndex) {
        levelCleared[completedLevelIndex] = true;
        if (completedLevelIndex + 1 > maxUnlockedLevel && completedLevelIndex + 1 < 8) {
            maxUnlockedLevel = completedLevelIndex + 1;
        }
    }

    public static boolean isLevelCleared(int levelIndex) {
        return levelCleared[levelIndex];
    }
}