package model.entity;

import java.awt.*;
import java.awt.geom.*;

public class Fruit {
    private final FruitType type;
    private double x, y, vx, vy;
    private double age;
    private final int slideBias = Math.random() < 0.5 ? -1 : 1;

    public Fruit(FruitType type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public FruitType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getRadius() {
        return type.getRadius();
    }

    public double getMass() {
        return getRadius() * getRadius();
    }

    public double getAge() {
        return age;
    }

    public int getSlideBias() {
        return slideBias;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public void addPosition(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public void addVelocity(double dx, double dy) {
        vx += dx;
        vy += dy;
    }

    public void update(double dt, double gravity) {
        age += dt;
        vy += gravity * dt;
        vx *= 0.999;
        x += vx * dt;
        y += vy * dt;
    }

    public boolean touches(Fruit o) {
        double dx = o.x - x, dy = o.y - y, r = getRadius() + o.getRadius();
        return dx * dx + dy * dy <= r * r;
    }

    public void draw(Graphics2D g) {
        draw(g, x, y, getRadius());
    }

    public void draw(Graphics2D g, double cx, double cy, double r) {
        paint(g, type, cx, cy, r);
    }

    public static void paint(Graphics2D g, FruitType t, double cx, double cy, double r) {
        Shape body = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(cx - r * 0.3, cy - r * 0.35),
                        (float) r * 1.3f,
                        new float[] {0f, 1f},
                        new Color[] {t.getLight().brighter(), t.getDark()}));
        g.fill(body);

        switch (t) {
            case CHERRY -> {
                g.setColor(new Color(60, 120, 40));
                g.setStroke(new BasicStroke(2f));
                g.draw(
                        new QuadCurve2D.Double(
                                cx,
                                cy - r,
                                cx + r * 0.2,
                                cy - r * 1.5,
                                cx + r * 0.6,
                                cy - r * 1.6));
            }
            case STRAWBERRY -> {
                g.setColor(new Color(255, 230, 120));

                for (int i = 0; i < 7; i++) {
                    double a = i * 0.9, d = r * (0.25 + 0.12 * (i % 3));
                    g.fill(
                            new Ellipse2D.Double(
                                    cx + Math.cos(a) * d - 1,
                                    cy + r * 0.2 + Math.sin(a) * d * 0.7 - 1,
                                    2.5,
                                    3.5));
                }

                leaf(g, cx, cy - r, r * 0.5);
            }
            case GRAPE -> {
                g.setColor(new Color(120, 80, 180, 140));

                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3;
                    g.fill(
                            new Ellipse2D.Double(
                                    cx + Math.cos(a) * r * 0.6 - r * 0.25,
                                    cy + Math.sin(a) * r * 0.6 - r * 0.25,
                                    r * 0.5,
                                    r * 0.5));
                }

                leaf(g, cx, cy - r, r * 0.35);
            }
            case DEKOPON, PERSIMMON -> {
                g.setColor(new Color(70, 140, 50));
                g.fill(new Ellipse2D.Double(cx - r * 0.25, cy - r - r * 0.08, r * 0.5, r * 0.2));
            }
            case WATERMELON -> {
                Shape old = g.getClip();
                g.setClip(body);
                g.setColor(new Color(15, 80, 25, 200));
                g.setStroke(new BasicStroke((float) (r * 0.14)));

                for (int i = -3; i <= 3; i++) {
                    double ox = cx + i * r * 0.3;
                    g.draw(new QuadCurve2D.Double(ox, cy - r, ox + i * r * 0.18, cy, ox, cy + r));
                }

                g.setClip(old);
            }
        }

        double ey = cy + r * 0.05, ex = r * 0.3, er = Math.max(1.8, r * 0.09);
        g.setColor(new Color(40, 20, 20));
        g.fill(new Ellipse2D.Double(cx - ex - er, ey - er, er * 2, er * 2.4));
        g.fill(new Ellipse2D.Double(cx + ex - er, ey - er, er * 2, er * 2.4));

        if (r > 18) {
            g.setStroke(new BasicStroke(1.5f));
            g.draw(
                    new Arc2D.Double(
                            cx - r * 0.12, ey + r * 0.1, r * 0.24, r * 0.16, 200, 140, Arc2D.OPEN));
        }
    }

    private static void leaf(Graphics2D g, double x, double y, double s) {
        g.setColor(new Color(60, 150, 50));
        g.fill(new Ellipse2D.Double(x - s, y - s * 0.4, s * 2, s * 0.8));
    }
}
