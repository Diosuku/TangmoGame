package model.entity;

import java.awt.Color;

public enum FruitType {
    CHERRY("เชอร์รี่", 18, 1, new Color(220, 30, 40), new Color(150, 10, 20)),
    STRAWBERRY("สตรอว์เบอร์รี", 26, 3, new Color(240, 60, 60), new Color(170, 20, 30)),
    GRAPE("องุ่น", 35, 6, new Color(150, 110, 210), new Color(95, 60, 150)),
    DEKOPON("ส้มจุก", 45, 10, new Color(255, 185, 40), new Color(220, 130, 10)),
    PERSIMMON("ลูกพลับ", 57, 15, new Color(250, 140, 30), new Color(215, 85, 10)),
    WATERMELON("แตงโม", 74, 50, new Color(60, 170, 60), new Color(20, 100, 30));

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
