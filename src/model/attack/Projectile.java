package model.attack;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import core.GamePanel;

public class Projectile {
    private double x, y0, vx, vy, age;
    private final double r, waveAmp, waveFreq;
    private final Color color;

    public Projectile(double x, double y, double vx, double vy, double r, Color color) {
        this(x, y, vx, vy, r, color, 0, 0);
    }

    public Projectile(
            double x,
            double y,
            double vx,
            double vy,
            double r,
            Color color,
            double waveAmp,
            double waveFreq) {
        this.x = x;
        this.y0 = y;
        this.vx = vx;
        this.vy = vy;
        this.r = r;
        this.color = color;
        this.waveAmp = waveAmp;
        this.waveFreq = waveFreq;
    }

    public void update(double dt) {
        age += dt;
        x += vx * dt;
        y0 += vy * dt;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y0 + waveAmp * Math.sin(age * waveFreq);
    }

    public double getRadius() {
        return r;
    }

    public boolean isOffscreen() {
        return x < -60 || x > GamePanel.W + 60 || y0 > GamePanel.H + 60 || y0 < -80;
    }

    public void draw(Graphics2D g) {
        double y = getY();
        g.setColor(color.darker());
        g.fill(new Ellipse2D.Double(x - r - 2, y - r - 2, 2 * r + 4, 2 * r + 4));
        g.setColor(color);
        g.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
        g.setColor(new Color(255, 255, 255, 170));
        g.fill(new Ellipse2D.Double(x - r * 0.5, y - r * 0.6, r * 0.5, r * 0.4));
    }
}
