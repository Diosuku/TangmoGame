package util;

import java.awt.*;
import java.awt.geom.*;

import model.entity.BossType;

public final class BossArt {
    private BossArt() {}

    public static void paint(
            Graphics2D g, BossType t, double cx, double cy, double r, boolean angry, double flash) {
        switch (t) {
            case PINEAPPLE -> pineapple(g, cx, cy, r);
            case DURIAN -> durian(g, cx, cy, r);
            case PEPPER -> pepper(g, cx, cy, r);
            case BLUEBERRY -> blueberry(g, cx, cy, r);
        }

        face(g, cx, cy, r, angry);

        if (flash > 0) {
            g.setColor(new Color(255, 255, 255, (int) (140 * Math.min(1, flash))));
            g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        }
    }

    private static void leaves(Graphics2D g, double cx, double top, double r, Color c) {
        g.setColor(c);

        for (int i = -3; i <= 3; i++) {
            Path2D p = new Path2D.Double();
            p.moveTo(cx + i * r * 0.07, top + r * 0.05);
            p.lineTo(cx + i * r * 0.28 - r * 0.07, top - r * (0.55 - Math.abs(i) * 0.08));
            p.lineTo(cx + i * r * 0.28 + r * 0.07, top + r * 0.05);
            g.fill(p);
        }
    }

    private static void pineapple(Graphics2D g, double cx, double cy, double r) {
        leaves(g, cx, cy - r, r, new Color(50, 150, 50));
        Shape body = new Ellipse2D.Double(cx - r * 0.85, cy - r, r * 1.7, r * 2);
        g.setPaint(
                new GradientPaint(
                        (float) cx,
                        (float) (cy - r),
                        new Color(255, 215, 70),
                        (float) cx,
                        (float) (cy + r),
                        new Color(230, 150, 20)));
        g.fill(body);
        Shape old = g.getClip();
        g.setClip(body);
        g.setColor(new Color(160, 100, 10, 120));
        g.setStroke(new BasicStroke(3f));

        for (int i = -8; i <= 8; i++) {
            g.draw(new Line2D.Double(cx + i * r * 0.22 - r, cy - r, cx + i * r * 0.22 + r, cy + r));
            g.draw(new Line2D.Double(cx + i * r * 0.22 + r, cy - r, cx + i * r * 0.22 - r, cy + r));
        }

        g.setClip(old);
    }

    private static void durian(Graphics2D g, double cx, double cy, double r) {
        g.setColor(new Color(110, 140, 40));

        for (int i = 0; i < 28; i++) {
            double a = i * Math.PI * 2 / 28, a1 = a - 0.1, a2 = a + 0.1;
            Path2D p = new Path2D.Double();
            p.moveTo(cx + Math.cos(a1) * r * 0.9, cy + Math.sin(a1) * r * 0.9);
            p.lineTo(cx + Math.cos(a) * r * 1.12, cy + Math.sin(a) * r * 1.12);
            p.lineTo(cx + Math.cos(a2) * r * 0.9, cy + Math.sin(a2) * r * 0.9);
            g.fill(p);
        }

        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(cx - r * 0.3, cy - r * 0.35),
                        (float) r * 1.4f,
                        new float[] {0f, 1f},
                        new Color[] {new Color(200, 210, 80), new Color(120, 150, 40)}));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
    }

    private static void pepper(Graphics2D g, double cx, double cy, double r) {
        g.setColor(new Color(50, 140, 50));
        g.setStroke(
                new BasicStroke((float) (r * 0.14), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(
                new QuadCurve2D.Double(
                        cx, cy - r * 0.9, cx + r * 0.1, cy - r * 1.3, cx + r * 0.5, cy - r * 1.35));
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(cx - r * 0.3, cy - r * 0.3),
                        (float) r * 1.4f,
                        new float[] {0f, 1f},
                        new Color[] {new Color(255, 80, 70), new Color(150, 10, 30)}));
        g.fill(new Ellipse2D.Double(cx - r * 0.9, cy - r * 0.95, r * 1.8, r * 1.95));
        g.setColor(new Color(255, 210, 60, 200));

        for (int i = -2; i <= 2; i++) {
            Path2D f = new Path2D.Double();
            f.moveTo(cx + i * r * 0.3 - r * 0.1, cy - r * 0.9);
            f.lineTo(cx + i * r * 0.3, cy - r * (1.25 - Math.abs(i) * 0.12));
            f.lineTo(cx + i * r * 0.3 + r * 0.1, cy - r * 0.9);
            g.fill(f);
        }
    }

    private static void blueberry(Graphics2D g, double cx, double cy, double r) {
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(cx - r * 0.3, cy - r * 0.35),
                        (float) r * 1.4f,
                        new float[] {0f, 1f},
                        new Color[] {new Color(120, 140, 230), new Color(40, 50, 140)}));
        g.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        g.setColor(new Color(230, 245, 255, 90));
        g.fill(new Ellipse2D.Double(cx - r * 0.7, cy - r * 0.85, r * 0.9, r * 0.45));

        g.setColor(new Color(200, 235, 255));

        for (int i = -2; i <= 2; i++) {
            Path2D c = new Path2D.Double();
            double bx = cx + i * r * 0.22;
            c.moveTo(bx - r * 0.1, cy - r * 0.92);
            c.lineTo(bx, cy - r * (1.35 - Math.abs(i) * 0.12));
            c.lineTo(bx + r * 0.1, cy - r * 0.92);
            g.fill(c);
        }
    }

    private static void face(Graphics2D g, double cx, double cy, double r, boolean angry) {
        double ex = r * 0.3, ey = cy - r * 0.1, er = r * 0.14;
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(cx - ex - er, ey - er, er * 2, er * 2.2));
        g.fill(new Ellipse2D.Double(cx + ex - er, ey - er, er * 2, er * 2.2));
        g.setColor(new Color(30, 10, 10));
        g.fill(new Ellipse2D.Double(cx - ex - er * 0.9, ey - er * 0.1, er * 0.9, er * 1.1));
        g.fill(new Ellipse2D.Double(cx + ex - er * 0.1, ey - er * 0.1, er * 0.9, er * 1.1));
        g.setStroke(
                new BasicStroke((float) (r * 0.05), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double dy = angry ? r * 0.12 : 0;
        g.draw(
                new Line2D.Double(
                        cx - ex - er * 1.4,
                        ey - er * 1.5 - dy,
                        cx - ex + er * 1.2,
                        ey - er * 1.1 + dy));
        g.draw(
                new Line2D.Double(
                        cx + ex + er * 1.4,
                        ey - er * 1.5 - dy,
                        cx + ex - er * 1.2,
                        ey - er * 1.1 + dy));

        double mw = r * 0.5, mh = angry ? r * 0.28 : r * 0.12, my = cy + r * 0.3;
        g.setColor(new Color(60, 10, 20));
        g.fill(new RoundRectangle2D.Double(cx - mw / 2, my, mw, mh, 12, 12));
        g.setColor(Color.WHITE);
        for (int i = 0; i < 4; i++)
            g.fill(
                    new Rectangle2D.Double(
                            cx - mw / 2 + 4 + i * (mw - 8) / 4, my, (mw - 8) / 4 - 2, mh * 0.4));
    }
}
