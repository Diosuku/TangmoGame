package model.object;

import java.awt.*;
import java.awt.geom.*;

import scene.BossScene;

public final class Hazards {
    private Hazards() {}

    private static boolean circleRect(
            double cx, double cy, double r, double x, double y, double w, double h) {
        double nx = Math.max(x, Math.min(cx, x + w)), ny = Math.max(y, Math.min(cy, y + h));
        return Math.hypot(cx - nx, cy - ny) <= r;
    }

    public static class ThornBush implements Hazard {
        private final double x, w = 76, h = 42;

        public ThornBush(double x) {
            this.x = x;
        }

        @Override
        public void update(double dt) {}

        @Override
        public boolean overlaps(double cx, double cy, double r) {
            return circleRect(cx, cy, r - 3, x - w / 2 + 6, BossScene.GROUND - h, w - 12, h);
        }

        @Override
        public void draw(Graphics2D g) {
            double gy = BossScene.GROUND;
            g.setColor(new Color(60, 110, 40));
            g.fill(new Ellipse2D.Double(x - w / 2, gy - h, w, h * 1.3));
            g.setColor(new Color(235, 225, 190));

            for (int i = 0; i < 9; i++) {
                double tx = x - w / 2 + 6 + i * (w - 12) / 8,
                        ty = gy - h * (0.55 + 0.4 * Math.sin(i * 1.7) * Math.sin(i * 1.7));
                Path2D p = new Path2D.Double();
                p.moveTo(tx - 5, ty + 12);
                p.lineTo(tx, ty - 12);
                p.lineTo(tx + 5, ty + 12);
                g.fill(p);
            }
        }
    }

    public static class Geyser implements Hazard {
        private static final double WARN = 0.9, ERUPT = 0.7, HEIGHT = 300;
        private final double x, period;
        private double t;

        public Geyser(double x, double period, double offset) {
            this.x = x;
            this.period = period;
            this.t = offset;
        }

        @Override
        public void update(double dt) {
            t = (t + dt) % period;
        }

        private boolean warning() {
            return t >= period - WARN - ERUPT && t < period - ERUPT;
        }

        private boolean erupting() {
            return t >= period - ERUPT;
        }

        @Override
        public boolean overlaps(double cx, double cy, double r) {
            return erupting()
                    && circleRect(cx, cy, r - 3, x - 24, BossScene.GROUND - HEIGHT, 48, HEIGHT);
        }

        @Override
        public void draw(Graphics2D g) {
            double gy = BossScene.GROUND;
            g.setColor(new Color(30, 10, 10));
            g.fill(new Ellipse2D.Double(x - 34, gy - 6, 68, 16));
            g.setColor(new Color(255, 120, 30, warning() ? 255 : 140));
            g.fill(new Ellipse2D.Double(x - 26, gy - 3, 52, 9));

            if (warning()) {
                g.setColor(new Color(255, 200, 60));

                for (int i = 0; i < 4; i++) {
                    double bx = x - 18 + i * 12, by = gy - 8 - Math.abs(Math.sin(t * 14 + i)) * 24;
                    g.fill(new Ellipse2D.Double(bx - 4, by - 4, 8, 8));
                }
            }

            if (erupting()) {
                double k = Math.min(1, (t - (period - ERUPT)) / 0.12);
                double h = HEIGHT * k;
                g.setPaint(
                        new GradientPaint(
                                0,
                                (float) (gy - h),
                                new Color(255, 230, 100),
                                0,
                                (float) gy,
                                new Color(230, 60, 20)));
                g.fill(new RoundRectangle2D.Double(x - 22, gy - h, 44, h, 30, 30));
                g.setColor(new Color(255, 255, 200, 160));
                g.fill(new RoundRectangle2D.Double(x - 8, gy - h + 10, 16, h - 10, 12, 12));
            }
        }
    }

    public static class Icicle implements Hazard {
        private static final double WAIT_AFTER = 1.2, SHAKE = 0.9, FALL_SPEED = 950, LEN = 64;
        private final double x, period;
        private double timer, y;
        private int state;

        public Icicle(double x, double period, double offset) {
            this.x = x;
            this.period = period;
            this.timer = offset;
            this.y = -LEN * 0.4;
        }

        @Override
        public void update(double dt) {
            timer += dt;

            switch (state) {
                case 0 -> {
                    if (timer >= period) {
                        state = 1;
                        timer = 0;
                    }
                }
                case 1 -> {
                    if (timer >= SHAKE) {
                        state = 2;
                        timer = 0;
                    }
                }
                case 2 -> {
                    y += FALL_SPEED * dt;

                    if (y + LEN >= BossScene.GROUND) {
                        state = 0;
                        timer = Math.max(0, period - WAIT_AFTER);
                        y = -LEN * 0.4;
                    }
                }
            }
        }

        @Override
        public boolean overlaps(double cx, double cy, double r) {
            if (state != 2) return false;
            return Math.hypot(cx - x, cy - (y + LEN - 12)) <= r + 10
                    || Math.hypot(cx - x, cy - (y + LEN * 0.4)) <= r + 12;
        }

        @Override
        public void draw(Graphics2D g) {
            double sx = state == 1 ? Math.sin(timer * 60) * 2.5 : 0;
            double yy = state == 1 ? -LEN * 0.4 : y;
            Path2D p = new Path2D.Double();
            p.moveTo(x + sx - 14, yy);
            p.lineTo(x + sx, yy + LEN);
            p.lineTo(x + sx + 14, yy);
            p.closePath();
            g.setPaint(
                    new GradientPaint(
                            (float) x,
                            (float) yy,
                            new Color(235, 250, 255),
                            (float) x,
                            (float) (yy + LEN),
                            new Color(120, 190, 235)));
            g.fill(p);
            g.setColor(new Color(255, 255, 255, 190));
            g.fill(new Rectangle2D.Double(x + sx - 5, yy + 2, 3, LEN * 0.5));

            if (state == 1) {
                g.setColor(new Color(255, 80, 80, 110));
                g.fill(new Ellipse2D.Double(x - 22, BossScene.GROUND - 6, 44, 10));
            }
        }
    }
}
