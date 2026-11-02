package model.attack;

import java.awt.Color;
import java.util.Random;

import model.entity.Boss;
import scene.BossScene;

public final class Attacks {
    private Attacks() {}

    abstract static class Timed implements Attack {
        protected static final Random RND = new Random();
        private final double interval;
        private final int minPhase;
        private double timer;

        Timed(double interval, int minPhase) {
            this.interval = interval;
            this.minPhase = minPhase;
            this.timer = interval * 0.6;
        }

        @Override
        public void update(double dt, Boss b, BossScene s) {
            if (b.getPhase() < minPhase) return;
            timer -= dt * (1 + 0.3 * b.getPhase());

            if (timer <= 0) {
                timer = interval;
                fire(b, s);
            }
        }

        abstract void fire(Boss b, BossScene s);
    }

    public static class AimedShot extends Timed {
        private final double speed;

        public AimedShot(double interval, double speed, int minPhase) {
            super(interval, minPhase);
            this.speed = speed;
        }

        @Override
        void fire(Boss b, BossScene s) {
            double dx = s.getPlayerX() - b.getMouthX(), dy = s.getPlayerY() - b.getMouthY();
            double d = Math.max(1, Math.hypot(dx, dy));
            s.addProjectile(
                    new Projectile(
                            b.getMouthX(),
                            b.getMouthY(),
                            dx / d * speed,
                            dy / d * speed,
                            14,
                            new Color(255, 90, 60)));
        }
    }

    public static class GroundRoller extends Timed {
        public GroundRoller(double interval, int minPhase) {
            super(interval, minPhase);
        }

        @Override
        void fire(Boss b, BossScene s) {
            s.addProjectile(
                    new Projectile(
                            b.getX() - 100,
                            BossScene.GROUND - 18,
                            -360 - 30 * b.getPhase(),
                            0,
                            18,
                            new Color(230, 160, 40)));
        }
    }

    public static class FanShot extends Timed {
        private final int count;
        private final double spread;

        public FanShot(double interval, int count, double spread, int minPhase) {
            super(interval, minPhase);
            this.count = count;
            this.spread = spread;
        }

        @Override
        void fire(Boss b, BossScene s) {
            double base = Math.PI + 0.15;

            for (int i = 0; i < count; i++) {
                double a = base + (i - (count - 1) / 2.0) * spread;
                s.addProjectile(
                        new Projectile(
                                b.getMouthX(),
                                b.getMouthY(),
                                Math.cos(a) * 380,
                                Math.sin(a) * 380,
                                11,
                                new Color(255, 150, 50)));
            }
        }
    }

    public static class SkyRain extends Timed {
        public SkyRain(double interval, int minPhase) {
            super(interval, minPhase);
        }

        @Override
        void fire(Boss b, BossScene s) {
            s.addProjectile(
                    new Projectile(
                            40 + RND.nextDouble() * (b.getX() - 200),
                            -20,
                            0,
                            300 + RND.nextDouble() * 120,
                            12,
                            new Color(150, 220, 80)));
        }
    }

    public static class SineWave extends Timed {
        public SineWave(double interval, int minPhase) {
            super(interval, minPhase);
        }

        @Override
        void fire(Boss b, BossScene s) {
            double y = BossScene.GROUND - 120 - RND.nextDouble() * 120;
            s.addProjectile(
                    new Projectile(b.getMouthX(), y, -310, 0, 13, new Color(255, 70, 110), 70, 5));
        }
    }
}
