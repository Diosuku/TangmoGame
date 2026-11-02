package util;

import java.awt.*;
import java.awt.geom.*;

public final class Art {
    private Art() {}

    public static void heart(Graphics2D g, double cx, double cy, double s, Color c) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx, cy + s * 0.9);
        p.curveTo(cx - s * 1.6, cy - s * 0.1, cx - s * 0.7, cy - s * 1.3, cx, cy - s * 0.4);
        p.curveTo(cx + s * 0.7, cy - s * 1.3, cx + s * 1.6, cy - s * 0.1, cx, cy + s * 0.9);
        g.setColor(c);
        g.fill(p);
    }

    public static void centerText(Graphics2D g, String s, int cx, int y) {
        g.drawString(s, cx - g.getFontMetrics().stringWidth(s) / 2, y);
    }

    public static void arcText(
            Graphics2D g,
            String s,
            double cx,
            double cy,
            double radius,
            double tilt,
            Color fill,
            Color shadow) {
        FontMetrics fm = g.getFontMetrics();
        double total = 0;
        double[] w = new double[s.length()];

        for (int i = 0; i < s.length(); i++) {
            w[i] = fm.charWidth(s.charAt(i));
            total += w[i];
        }

        double start = -total / 2 / radius + tilt;
        double run = 0;
        java.awt.geom.AffineTransform old = g.getTransform();

        for (int i = 0; i < s.length(); i++) {
            double phi = start + (run + w[i] / 2) / radius;
            run += w[i];
            double x = cx + radius * Math.sin(phi), y = cy - radius * Math.cos(phi);
            g.setTransform(old);
            g.translate(x, y);
            g.rotate(phi);
            String ch = String.valueOf(s.charAt(i));

            if (shadow != null) {
                g.setColor(shadow);
                g.drawString(ch, (float) (-w[i] / 2 + 2), 3f);
            }

            g.setColor(fill);
            g.drawString(ch, (float) (-w[i] / 2), 0f);
        }

        g.setTransform(old);
    }

    public static void outlinedText(Graphics2D g, String s, int cx, int y, Color fill) {
        int x = cx - g.getFontMetrics().stringWidth(s) / 2;
        g.setColor(new Color(60, 30, 10));
        for (int dx = -2; dx <= 2; dx += 2)
            for (int dy = -2; dy <= 2; dy += 2) g.drawString(s, x + dx, y + dy);
        g.setColor(fill);
        g.drawString(s, x, y);
    }
}
