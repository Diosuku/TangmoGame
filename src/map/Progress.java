package map;

public class Progress {
    private int unlocked = 1;

    public boolean isUnlocked(int levelIndex) {
        return levelIndex < unlocked;
    }

    public void clear(int levelIndex) {
        cleared[levelIndex] = true;
        unlocked = Math.max(unlocked, Math.min(Level.ALL.size(), levelIndex + 2));
        if (levelIndex == Level.ALL.size() - 1) allClear = true;
    }

    private boolean allClear;
    private final boolean[] cleared = new boolean[Level.ALL.size()];
    private int lastNode = -1;

    public boolean isCleared(int levelIndex) {
        return cleared[levelIndex];
    }

    public int getLastNode() {
        return lastNode;
    }

    public void setLastNode(int n) {
        lastNode = n;
    }

    public boolean isAllClear() {
        return allClear;
    }
}
