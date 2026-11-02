package map;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Zone {
    private final double x1, x2, speedMultiplier;

    public Zone(double x1, double x2, double speedMultiplier) {
        this.x1 = x1;
        this.x2 = x2;
        this.speedMultiplier = speedMultiplier;
    }

    public boolean contains(double x) {
        return x >= x1 && x <= x2;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void draw(Graphics2D g, double groundY, double time) {
        g.setColor(new Color(90, 60, 35));
        g.fill(new Ellipse2D.Double(x1, groundY - 7, x2 - x1, 20));
        g.setColor(new Color(130, 90, 55));
        g.fill(new Ellipse2D.Double(x1 + 6, groundY - 5, x2 - x1 - 12, 12));
        g.setColor(new Color(170, 125, 80));

        for (int i = 0; i < 4; i++) {
            double bx = x1 + 20 + ((i * 57 + time * 12) % Math.max(10, x2 - x1 - 40));
            double r = 3 + (Math.sin(time * 3 + i) + 1) * 2;
            g.fill(new Ellipse2D.Double(bx, groundY - 5 - r, r * 2, r * 2));
        }
    }
}
