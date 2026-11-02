package core;

public class ScoreManager {
    private int score;
    private int best;

    public void add(int points) {
        score += points;
        if (score > best) best = score;
    }

    public void reset() {
        score = 0;
    }

    public int getScore() {
        return score;
    }

    public int getBest() {
        return best;
    }
}
