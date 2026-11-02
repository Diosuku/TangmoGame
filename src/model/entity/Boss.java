package model.entity;

import java.awt.*;
import java.util.List;

import model.attack.Attack;
import scene.BossScene;
import util.BossArt;

public class Boss {
    private final BossType type;
    private final double x, baseY, hurtRadius = 125;
    private final List<Attack> attacks;
    private double hp, time, flash;

    public Boss(BossType type, double x, double groundY) {
        this.type = type;
        this.x = x;
        this.baseY = groundY - 150;
        this.hp = type.getMaxHp();
        this.attacks = type.createAttacks();
    }

    public BossType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return baseY + Math.sin(time * 1.6) * 10;
    }

    public double getHurtRadius() {
        return hurtRadius;
    }

    public double getHp() {
        return hp;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public double getMouthX() {
        return x - hurtRadius * 0.5;
    }

    public double getMouthY() {
        return getY() + hurtRadius * 0.4;
    }

    public int getPhase() {
        double f = hp / type.getMaxHp();
        return f > 0.66 ? 0 : f > 0.33 ? 1 : 2;
    }

    public void damage(double d) {
        hp = Math.max(0, hp - d);
        flash = 1;
    }

    public void update(double dt, BossScene scene) {
        time += dt;
        flash = Math.max(0, flash - dt * 8);
        if (isDead()) return;
        for (Attack a : attacks) a.update(dt, this, scene);
    }

    public void draw(Graphics2D g, double deathScale) {
        BossArt.paint(g, type, x, getY(), hurtRadius * deathScale, getPhase() >= 1, flash);
    }
}
