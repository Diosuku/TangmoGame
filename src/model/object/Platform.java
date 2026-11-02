package model.object;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Platform {
    private static final double THICK = 16;
    private final double cx0, top, width, amplitude, speed, phase;
    private double cx, dx, time;

    public Platform(double cx, double top, double width) {
        this(cx, top, width, 0, 0, 0);
    }

    public Platform(
            double cx, double top, double width, double amplitude, double speed, double phase) {
        this.cx0 = cx;
        this.cx = cx;
        this.top = top;
        this.width = width;
        this.amplitude = amplitude;
        this.speed = speed;
        this.phase = phase;
    }

    public void update(double dt) {
        time += dt;
        double nx = cx0 + amplitude * Math.sin(time * speed + phase);
        dx = nx - cx;
        cx = nx;
    }

    public double getTop() {
        return top;
    }

    public double getLeft() {
        return cx - width / 2;
    }

    public double getRight() {
        return cx + width / 2;
    }

    public double getDx() {
        return dx;
    }

    public void draw(Graphics2D g, Color base) {
        g.setColor(base.darker());
        g.fill(new RoundRectangle2D.Double(getLeft(), top + 3, width, THICK, 10, 10));
        g.setColor(base);
        g.fill(new RoundRectangle2D.Double(getLeft(), top, width, THICK, 10, 10));
        g.setColor(new Color(255, 255, 255, 70));
        g.fillRect((int) getLeft() + 6, (int) top + 2, (int) width - 12, 3);
    }
}
