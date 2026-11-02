package map;

import java.util.List;

import model.entity.BossType;

public record Level(int number, String name, int mergeGoal, BossType boss, String obstacles) {
    public static final List<Level> ALL =
            List.of(
                    new Level(1, "สวนสับปะรด", 120, BossType.PINEAPPLE, "พุ่มหนาม + แท่นลอย"),
                    new Level(2, "ป่าทุเรียน", 250, BossType.DURIAN, "โคลนหนืด + แท่นเลื่อน"),
                    new Level(3, "ไร่พริกผี", 400, BossType.PEPPER, "ลาวาพุ่งจากพื้น"),
                    new Level(
                            4, "ถ้ำน้ำแข็ง", 550, BossType.BLUEBERRY, "พื้นลื่น + ลม + น้ำแข็งตก"));

    public int bonusHearts(int score) {
        return Math.max(0, Math.min(3, (score - mergeGoal) / (mergeGoal / 2)));
    }

    public double damageMultiplier(int score) {
        return 1.0 + Math.max(0, Math.min(1.0, (score - mergeGoal) * 0.5 / mergeGoal));
    }
}
