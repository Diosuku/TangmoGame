package model.object;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import core.GamePanel;

public class Seed {
    private double x, y;
    private final double vx, damage;

    public Seed(double x, double y, double vx, double damage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.damage = damage;
    }

    public void update(double dt) {
        x += vx * dt;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDamage() {
        return damage;
    }

    public boolean isOffscreen() {
        return x < -30 || x > GamePanel.W + 30;
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(30, 20, 20));
        g.fill(new Ellipse2D.Double(x - 9, y - 5, 18, 10));
        g.setColor(new Color(255, 255, 255, 180));
        g.fill(new Ellipse2D.Double(x - 4, y - 3, 6, 3));
    }
}
