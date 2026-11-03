package model.entity;

import java.awt.Color;

public enum FruitType {
    CHERRY("เชอร์รี่", 18, 1, new Color(235, 45, 55), new Color(150, 10, 25)),
    STRAWBERRY("สตรอว์เบอร์รี", 26, 3, new Color(250, 85, 80), new Color(185, 25, 40)),
    GRAPE("องุ่น", 35, 6, new Color(165, 120, 225), new Color(90, 50, 150)),
    DEKOPON("ส้มจุก", 45, 10, new Color(255, 200, 60), new Color(235, 140, 15)),
    PERSIMMON("ลูกพลับ", 57, 15, new Color(255, 150, 50), new Color(220, 80, 15)),
    WATERMELON("แตงโม", 74, 50, new Color(95, 200, 85), new Color(30, 130, 50));

    private final String thaiName;
    private final int radius;
    private final int score;
    private final Color light;
    private final Color dark;

    FruitType(String thaiName, int radius, int score, Color light, Color dark) {
        this.thaiName = thaiName;
        this.radius = radius;
        this.score = score;
        this.light = light;
        this.dark = dark;
    }

    public String getThaiName() {
        return thaiName;
    }

    public int getRadius() {
        return radius;
    }

    public int getScore() {
        return score;
    }

    public Color getLight() {
        return light;
    }

    public Color getDark() {
        return dark;
    }

    public boolean isLargest() {
        return this == WATERMELON;
    }

    public FruitType next() {
        return values()[ordinal() + 1];
    }

    public static FruitType randomDroppable(java.util.Random rnd) {
        return values()[rnd.nextInt(3)];
    }
}
