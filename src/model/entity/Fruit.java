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
        Object aa0 = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Stroke st0 = g.getStroke();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (t) {
            case GRAPE -> paintGrapeBunch(g, t, cx, cy, r);
            case STRAWBERRY -> paintStrawberry(g, t, cx, cy, r);
            default -> paintRound(g, t, cx, cy, r);
        }

        g.setStroke(st0);
        if (aa0 != null) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa0);
    }

    private static void paintGrapeBunch(Graphics2D g, FruitType t, double cx, double cy, double r) {
        double br = r * 0.33, sp = r * 0.62;
        // stalk + leaf behind the berries
        g.setColor(new Color(110, 80, 40));
        g.setStroke(new BasicStroke((float) Math.max(2, r * 0.11), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new QuadCurve2D.Double(cx, cy - r * 0.45, cx + r * 0.02, cy - r * 0.85, cx + r * 0.22, cy - r * 1.08));
        vineLeaf(g, cx + r * 0.12, cy - r * 0.95, r * 0.7, 0.5);

        // berries, back to front: bottom tip first, top row last
        double[][] pos = {
            {0, 0.70}, {-0.3, 0.18}, {0.3, 0.18}, {-1, -0.34}, {0, -0.34}, {1, -0.34}
        };
        for (double[] q : pos) {
            double bx = cx + (Math.abs(q[0]) == 1 ? q[0] * sp : q[0] * r), by = cy + q[1] * r;
            berry(g, t, bx, by, br);
        }

        // little stalk cap where the berries meet
        g.setColor(new Color(95, 70, 35));
        g.fill(new Ellipse2D.Double(cx - r * 0.07, cy - r * 0.68, r * 0.14, r * 0.12));

        shine(g, cx - r * 0.1, cy + r * 0.05, r * 0.85);
        face(g, t, cx, cy + r * 0.06, r);
    }

    private static void berry(Graphics2D g, FruitType t, double bx, double by, double br) {
        Shape b = new Ellipse2D.Double(bx - br, by - br, 2 * br, 2 * br);
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(bx - br * 0.3, by - br * 0.35),
                        (float) (br * 1.5),
                        new float[] {0f, 0.5f, 1f},
                        new Color[] {mix(t.getLight(), Color.WHITE, 0.3), t.getLight(), t.getDark()}));
        g.fill(b);
        g.setColor(new Color(235, 220, 255, 60));
        g.fill(new Ellipse2D.Double(bx - br * 0.2, by - br * 0.1, br * 1.0, br * 0.9));
        g.setColor(mix(t.getDark(), Color.BLACK, 0.4));
        g.setStroke(new BasicStroke((float) Math.max(1.0, br * 0.1)));
        g.draw(b);
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(new Ellipse2D.Double(bx - br * 0.55, by - br * 0.6, br * 0.42, br * 0.28));
    }

    private static void paintStrawberry(Graphics2D g, FruitType t, double cx, double cy, double r) {
        Path2D.Double body = new Path2D.Double();
        body.moveTo(cx, cy - r * 0.72);
        body.curveTo(cx + r * 0.45, cy - r * 0.95, cx + r * 0.95, cy - r * 0.78, cx + r * 0.9, cy - r * 0.4);
        body.curveTo(cx + r * 0.75, cy + r * 0.05, cx + r * 0.3, cy + r * 0.72, cx, cy + r);
        body.curveTo(cx - r * 0.3, cy + r * 0.72, cx - r * 0.75, cy + r * 0.05, cx - r * 0.9, cy - r * 0.4);
        body.curveTo(cx - r * 0.95, cy - r * 0.78, cx - r * 0.45, cy - r * 0.95, cx, cy - r * 0.72);
        body.closePath();

        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(cx - r * 0.3, cy - r * 0.35),
                        (float) (r * 1.5),
                        new float[] {0f, 0.45f, 1f},
                        new Color[] {mix(t.getLight(), Color.WHITE, 0.3), t.getLight(), t.getDark()}));
        g.fill(body);

        Shape oldClip = g.getClip();
        g.clip(body);
        // seeds, in staggered rows that narrow toward the tip
        double sw = Math.max(1.6, r * 0.07), sh = sw * 1.6;
        for (int row = 0; row < 6; row++) {
            double py = cy - r * 0.3 + row * r * 0.27;
            double half = Math.max(0, 0.78 - row * 0.14) * r;
            int n = Math.max(1, (int) Math.round(half / (r * 0.3)) + 1);
            for (int k = 0; k < n; k++) {
                double px = cx + (n == 1 ? 0 : (k - (n - 1) / 2.0) * (2 * half / (n - 1))) + (row % 2) * r * 0.08;
                if (py > cy - r * 0.2 && py < cy + r * 0.38 && Math.abs(px - cx) < r * 0.5) continue;
                if (!body.contains(px, py)) continue;
                g.setColor(new Color(150, 20, 25, 140));
                g.fill(new Ellipse2D.Double(px - sw, py - sh * 0.4, sw * 2, sh * 1.3));
                g.setColor(new Color(255, 236, 150));
                g.fill(new Ellipse2D.Double(px - sw * 0.8, py - sh * 0.5, sw * 1.6, sh));
            }
        }
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(cx, cy),
                        (float) r,
                        new float[] {0.7f, 1f},
                        new Color[] {new Color(0, 0, 0, 0), new Color(0, 0, 0, 75)}));
        g.fill(body);
        g.setClip(oldClip);

        g.setColor(mix(t.getDark(), Color.BLACK, 0.35));
        g.setStroke(new BasicStroke((float) Math.max(1.2, r * 0.045), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(body);

        calyx(g, cx, cy - r * 0.74, r * 0.72, new Color(70, 170, 60));
        shine(g, cx - r * 0.05, cy - r * 0.1, r * 0.95);
        face(g, t, cx, cy - r * 0.12, r);
    }

    private static void paintRound(Graphics2D g, FruitType t, double cx, double cy, double r) {
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke oldStroke = g.getStroke();
        Shape body = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);

        // parts that sit behind the body
        switch (t) {
            case CHERRY -> cherryStem(g, cx, cy, r);
            case DEKOPON -> {
                g.setPaint(
                        new GradientPaint(
                                0,
                                (float) (cy - r * 1.25),
                                t.getLight(),
                                0,
                                (float) (cy - r * 0.8),
                                t.getDark()));
                g.fill(
                        new RoundRectangle2D.Double(
                                cx - r * 0.36, cy - r * 1.24, r * 0.72, r * 0.6, r * 0.5, r * 0.5));
            }
            default -> {}
        }

        // body: 3-stop gradient for volume
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(cx - r * 0.32, cy - r * 0.38),
                        (float) (r * 1.45),
                        new float[] {0f, 0.45f, 1f},
                        new Color[] {
                            mix(t.getLight(), Color.WHITE, 0.35), t.getLight(), t.getDark()
                        }));
        g.fill(body);

        Shape oldClip = g.getClip();
        g.clip(body);

        // texture & markings (clipped to the body)
        switch (t) {
            case DEKOPON -> pores(g, cx, cy, r, new Color(255, 240, 160, 90), 26, 1.1);
            case PERSIMMON -> {
                pores(g, cx, cy, r, new Color(150, 50, 0, 40), 14, 1.4);
                g.setColor(new Color(190, 70, 5, 70));
                g.setStroke(new BasicStroke((float) Math.max(1, r * 0.04)));
                g.draw(new QuadCurve2D.Double(cx, cy - r, cx - r * 0.12, cy, cx, cy + r));
            }
            case WATERMELON -> melonStripes(g, cx, cy, r);
            default -> {}
        }

        // rim shade (adds roundness) + inner glow at the bottom
        g.setPaint(
                new RadialGradientPaint(
                        new Point2D.Double(cx, cy),
                        (float) r,
                        new float[] {0.72f, 1f},
                        new Color[] {new Color(0, 0, 0, 0), new Color(0, 0, 0, 70)}));
        g.fill(body);
        g.setClip(oldClip);

        // outline
        g.setColor(mix(t.getDark(), Color.BLACK, 0.35));
        g.setStroke(new BasicStroke((float) Math.max(1.2, r * 0.045)));
        g.draw(body);

        // parts that sit in front of the body
        switch (t) {
            case CHERRY -> {
                // little dimple where the stem enters
                g.setColor(new Color(110, 0, 10, 120));
                g.fill(new Ellipse2D.Double(cx - r * 0.16, cy - r * 0.97, r * 0.32, r * 0.16));
            }
            case DEKOPON -> {
                g.setColor(new Color(60, 130, 45));
                g.fill(new Ellipse2D.Double(cx - r * 0.22, cy - r * 1.04, r * 0.44, r * 0.2));
                vineLeaf(g, cx + r * 0.05, cy - r * 1.18, r * 0.5, 0.55);
            }
            case PERSIMMON -> calyx4(g, cx, cy - r * 0.9, r * 0.7);
            case WATERMELON -> {
                stem(g, cx, cy - r, r * 0.2, r * 0.22);
            }
            default -> {}
        }

        shine(g, cx, cy, r);
        face(g, t, cx, cy, r);
        g.setStroke(oldStroke);
        if (aa != null) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }

    private static Color mix(Color a, Color b, double k) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * k),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * k),
                a.getAlpha());
    }

    private static void shine(Graphics2D g, double cx, double cy, double r) {
        Path2D.Double arc = new Path2D.Double();
        double sx = cx - r * 0.5, sy = cy - r * 0.55;
        AffineTransform old = g.getTransform();
        g.translate(sx, sy);
        g.rotate(-0.7);
        g.setPaint(
                new GradientPaint(
                        (float) (-r * 0.3),
                        0f,
                        new Color(255, 255, 255, 215),
                        (float) (r * 0.3),
                        0f,
                        new Color(255, 255, 255, 60)));
        g.fill(new Ellipse2D.Double(-r * 0.3, -r * 0.13, r * 0.6, r * 0.26));
        g.setTransform(old);
        g.setColor(new Color(255, 255, 255, 190));
        double d = Math.max(1.6, r * 0.09);
        g.fill(new Ellipse2D.Double(cx - r * 0.2, cy - r * 0.72, d, d));
    }

    private static void face(Graphics2D g, FruitType t, double cx, double cy, double r) {
        double ey = cy + r * 0.12, ex = r * 0.3, er = Math.max(2.0, r * 0.11);

        // blush
        if (r > 20) {
            g.setColor(new Color(255, 120, 140, 95));
            double bw = r * 0.26, bh = r * 0.15;
            g.fill(new Ellipse2D.Double(cx - ex * 1.65 - bw / 2, ey + r * 0.12, bw, bh));
            g.fill(new Ellipse2D.Double(cx + ex * 1.65 - bw / 2, ey + r * 0.12, bw, bh));
        }

        // eyes with glints
        g.setColor(new Color(35, 20, 25));
        g.fill(new Ellipse2D.Double(cx - ex - er, ey - er * 1.2, er * 2, er * 2.5));
        g.fill(new Ellipse2D.Double(cx + ex - er, ey - er * 1.2, er * 2, er * 2.5));
        g.setColor(Color.WHITE);
        double gr = er * 0.7;
        g.fill(new Ellipse2D.Double(cx - ex - er * 0.1, ey - er * 0.95, gr, gr));
        g.fill(new Ellipse2D.Double(cx + ex - er * 0.1, ey - er * 0.95, gr, gr));

        // smile
        g.setColor(new Color(35, 20, 25));
        g.setStroke(new BasicStroke((float) Math.max(1.2, r * 0.05), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double mw = r * 0.26;
        g.draw(new Arc2D.Double(cx - mw / 2, ey + er * 0.9, mw, mw * 0.7, 200, 140, Arc2D.OPEN));
    }

    private static void cherryStem(Graphics2D g, double cx, double cy, double r) {
        g.setColor(new Color(85, 130, 45));
        g.setStroke(new BasicStroke((float) Math.max(2, r * 0.13), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(
                new CubicCurve2D.Double(
                        cx,
                        cy - r * 0.9,
                        cx + r * 0.1,
                        cy - r * 1.5,
                        cx + r * 0.5,
                        cy - r * 1.7,
                        cx + r * 0.85,
                        cy - r * 1.7));
        vineLeaf(g, cx + r * 0.8, cy - r * 1.68, r * 0.8, 0.0);
    }

    private static void vineLeaf(Graphics2D g, double x, double y, double s, double rot) {
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(-rot);
        Path2D.Double leaf = new Path2D.Double();
        leaf.moveTo(0, 0);
        leaf.curveTo(s * 0.3, -s * 0.55, s * 0.95, -s * 0.45, s * 1.1, 0);
        leaf.curveTo(s * 0.95, s * 0.3, s * 0.3, s * 0.2, 0, 0);
        g.setPaint(
                new GradientPaint(0f, (float) (-s * 0.4), new Color(130, 210, 80), 0f, (float) (s * 0.2), new Color(50, 140, 50)));
        g.fill(leaf);
        g.setColor(new Color(30, 100, 40, 200));
        g.setStroke(new BasicStroke(Math.max(0.8f, (float) (s * 0.05))));
        g.draw(leaf);
        g.draw(new Line2D.Double(0, 0, s * 0.9, -s * 0.08));
        g.setTransform(old);
    }

    private static void stem(Graphics2D g, double x, double y, double h, double w) {
        g.setColor(new Color(110, 80, 40));
        g.setStroke(new BasicStroke((float) Math.max(2, w * 0.4), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new QuadCurve2D.Double(x, y + h * 0.2, x + w * 0.1, y - h * 0.6, x + w * 0.5, y - h));
    }

    private static void calyx(Graphics2D g, double cx, double cy, double s, Color c) {
        g.setColor(c);
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        for (int i = 0; i < 5; i++) {
            double a = -Math.PI / 2 + (i - 2) * 0.62;
            Path2D.Double leaf = new Path2D.Double();
            leaf.moveTo(0, 0);
            double lx = Math.cos(a) * s, ly = Math.sin(a) * s * 0.55;
            leaf.quadTo(Math.cos(a - 0.5) * s * 0.7, Math.sin(a - 0.5) * s * 0.5 + s * 0.1, lx, ly);
            leaf.quadTo(Math.cos(a + 0.5) * s * 0.7, Math.sin(a + 0.5) * s * 0.5 + s * 0.1, 0, 0);
            g.setColor(i % 2 == 0 ? c : mix(c, Color.BLACK, 0.18));
            g.fill(leaf);
        }
        g.setColor(new Color(55, 120, 45));
        g.fill(new Ellipse2D.Double(-s * 0.16, -s * 0.12, s * 0.32, s * 0.26));
        g.setTransform(old);
    }

    private static void calyx4(Graphics2D g, double cx, double cy, double s) {
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        for (int i = 0; i < 4; i++) {
            double a = Math.PI * 0.2 + i * Math.PI * 0.2 + Math.PI * 0.1;
            double dir = (i - 1.5) * 0.52;
            Path2D.Double leaf = new Path2D.Double();
            leaf.moveTo(0, 0);
            double lx = Math.sin(dir) * s, ly = -Math.cos(dir) * s * 0.28 + s * 0.12;
            leaf.quadTo(lx * 0.4 - s * 0.2, ly - s * 0.25, lx, ly);
            leaf.quadTo(lx * 0.4 + s * 0.2, ly + s * 0.15, 0, 0);
            g.setColor(i % 2 == 0 ? new Color(105, 140, 50) : new Color(85, 120, 40));
            g.fill(leaf);
        }
        g.setColor(new Color(95, 70, 35));
        g.fill(new RoundRectangle2D.Double(-s * 0.09, -s * 0.3, s * 0.18, s * 0.32, s * 0.1, s * 0.1));
        g.setTransform(old);
    }

    private static void pores(Graphics2D g, double cx, double cy, double r, Color c, int n, double size) {
        g.setColor(c);
        double sz = Math.max(1.0, r * 0.035 * size);
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (int i = 1; i <= n * 3; i++) {
            double rad = r * 0.95 * Math.sqrt(i / (n * 3.0)), a = i * golden;
            double px = cx + Math.cos(a) * rad, py = cy + Math.sin(a) * rad;
            g.fill(new Ellipse2D.Double(px - sz, py - sz, sz * 2, sz * 2));
        }
    }

    private static void melonStripes(Graphics2D g, double cx, double cy, double r) {
        // lighter base stripes then dark wavy stripes
        for (int i = -4; i <= 4; i++) {
            double ox = cx + i * r * 0.26;
            Path2D.Double stripe = new Path2D.Double();
            double w = r * 0.085, bend = i * r * 0.2;
            stripe.moveTo(ox - w, cy - r);
            stripe.curveTo(ox - w + bend * 0.5, cy - r * 0.4, ox - w + bend, cy + r * 0.4, ox - w * 0.5 + bend * 0.2, cy + r);
            stripe.lineTo(ox + w * 1.3 + bend * 0.2, cy + r);
            stripe.curveTo(ox + w + bend, cy + r * 0.4, ox + w + bend * 0.5, cy - r * 0.4, ox + w, cy - r);
            stripe.closePath();
            g.setColor(new Color(20, 85, 35, 210));
            g.fill(stripe);
        }
        g.setColor(new Color(150, 220, 110, 70));
        g.fill(new Ellipse2D.Double(cx - r * 0.9, cy - r * 0.95, r * 1.2, r * 0.8));
    }
}
