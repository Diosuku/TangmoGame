package model.entity;

import java.util.List;

import model.attack.Attack;
import model.attack.Attacks;

public enum BossType {
    PINEAPPLE("กัปตันสับปะรด", 100),
    DURIAN("บารอนทุเรียน", 150),
    PEPPER("ราชินีพริกผี", 210),
    BLUEBERRY("ราชาบลูเบอร์รี่", 260);

    private final String title;
    private final int maxHp;

    BossType(String title, int maxHp) {
        this.title = title;
        this.maxHp = maxHp;
    }

    public String getTitle() {
        return title;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public List<Attack> createAttacks() {
        return switch (this) {
            case PINEAPPLE ->
                    List.of(
                            new Attacks.AimedShot(1.9, 400, 0),
                            new Attacks.GroundRoller(3.4, 0),
                            new Attacks.FanShot(4.2, 3, 0.35, 1),
                            new Attacks.SkyRain(1.2, 2));
            case DURIAN ->
                    List.of(
                            new Attacks.FanShot(3.4, 4, 0.36, 0),
                            new Attacks.SkyRain(1.4, 0),
                            new Attacks.AimedShot(2.3, 470, 1),
                            new Attacks.GroundRoller(3.0, 2));
            case PEPPER ->
                    List.of(
                            new Attacks.SineWave(1.5, 0),
                            new Attacks.AimedShot(1.3, 520, 0),
                            new Attacks.GroundRoller(2.8, 1),
                            new Attacks.SkyRain(0.7, 1),
                            new Attacks.FanShot(3.0, 7, 0.25, 2));
            case BLUEBERRY ->
                    List.of(
                            new Attacks.AimedShot(1.7, 480, 0),
                            new Attacks.FanShot(3.6, 5, 0.28, 0),
                            new Attacks.GroundRoller(3.0, 1),
                            new Attacks.SineWave(2.0, 1),
                            new Attacks.SkyRain(1.1, 2));
        };
    }
}
