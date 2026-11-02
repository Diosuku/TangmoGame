package map;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import model.object.Hazard;
import model.object.Hazards;
import model.object.Platform;
import scene.BossScene;

public class StageMap {
    private enum Deco {
        GARDEN,
        JUNGLE,
        VOLCANO,
        ICE
    }

    private final Deco deco;
    private final Color skyTop, skyBottom, ground, groundTop, dirt, platformColor, hudText, mergeBg;
    private final List<Platform> platforms = new ArrayList<>();
    private final List<Hazard> hazards = new ArrayList<>();
    private final List<Zone> zones = new ArrayList<>();
    private boolean icy;
    private double windAccel, windOn, windOff;
    private double time;

    private StageMap(
            Deco d,
            Color skyTop,
            Color skyBottom,
            Color ground,
            Color groundTop,
            Color dirt,
            Color platformColor,
            Color hudText,
            Color mergeBg) {
        this.deco = d;
        this.skyTop = skyTop;
        this.skyBottom = skyBottom;
        this.ground = ground;
        this.groundTop = groundTop;
        this.dirt = dirt;
        this.platformColor = platformColor;
        this.hudText = hudText;
        this.mergeBg = mergeBg;
    }

    public static StageMap create(int level) {
        StageMap m;

        switch (level) {
            case 1 -> {
                m =
                        new StageMap(
                                Deco.GARDEN,
                                new Color(255, 214, 150),
                                new Color(255, 160, 100),
                                new Color(110, 160, 60),
                                new Color(80, 120, 40),
                                new Color(130, 90, 50),
                                new Color(190, 130, 70),
                                new Color(60, 30, 10),
                                new Color(210, 150, 90));
                m.platforms.add(new Platform(190, 520, 120));
                m.platforms.add(new Platform(450, 470, 120));
                m.hazards.add(new Hazards.ThornBush(330));
                m.hazards.add(new Hazards.ThornBush(560));
            }
            case 2 -> {
                m =
                        new StageMap(
                                Deco.JUNGLE,
                                new Color(40, 90, 70),
                                new Color(150, 190, 90),
                                new Color(70, 110, 50),
                                new Color(50, 85, 35),
                                new Color(85, 60, 40),
                                new Color(120, 85, 50),
                                Color.WHITE,
                                new Color(90, 140, 90));
                m.zones.add(new Zone(200, 340, 0.45));
                m.zones.add(new Zone(420, 560, 0.45));
                m.platforms.add(new Platform(330, 500, 110, 130, 1.3, 0));
                m.platforms.add(new Platform(130, 480, 100));
            }
            case 3 -> {
                m =
                        new StageMap(
                                Deco.VOLCANO,
                                new Color(60, 20, 50),
                                new Color(230, 90, 50),
                                new Color(70, 45, 45),
                                new Color(40, 25, 28),
                                new Color(45, 28, 30),
                                new Color(110, 70, 70),
                                Color.WHITE,
                                new Color(160, 70, 60));
                m.hazards.add(new Hazards.Geyser(230, 4.2, 0.5));
                m.hazards.add(new Hazards.Geyser(400, 4.8, 2.6));
                m.hazards.add(new Hazards.Geyser(570, 5.4, 1.4));
                m.platforms.add(new Platform(315, 500, 110));
                m.platforms.add(new Platform(490, 440, 100));
            }
            default -> {
                m =
                        new StageMap(
                                Deco.ICE,
                                new Color(170, 215, 245),
                                new Color(225, 245, 255),
                                new Color(190, 225, 245),
                                new Color(235, 250, 255),
                                new Color(140, 190, 220),
                                new Color(150, 205, 235),
                                new Color(40, 70, 100),
                                new Color(100, 150, 190));
                m.icy = true;
                m.windAccel = 520;
                m.windOn = 2.2;
                m.windOff = 3.5;
                m.hazards.add(new Hazards.Icicle(210, 3.4, 0.0));
                m.hazards.add(new Hazards.Icicle(390, 4.0, 1.5));
                m.hazards.add(new Hazards.Icicle(560, 4.6, 2.8));
                m.platforms.add(new Platform(300, 500, 110));
            }
        }

        return m;
    }

    public static Color mergeBackground(int level) {
        return create(level).mergeBg;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<Hazard> getHazards() {
        return hazards;
    }

    public Color getHudText() {
        return hudText;
    }

    public boolean isIcy() {
        return icy;
    }

    public double speedMultiplierAt(double x) {
        for (Zone z : zones) if (z.contains(x)) return z.getSpeedMultiplier();
        return 1.0;
    }

    private boolean gusting() {
        if (windAccel == 0) return false;
        return (time % (windOn + windOff)) < windOn;
    }

    public double getWindAccel() {
        return gusting() ? -windAccel : 0;
    }

    public void update(double dt) {
        time += dt;
        for (Platform p : platforms) p.update(dt);
        for (Hazard h : hazards) h.update(dt);
    }

    public boolean hazardHits(double cx, double cy, double r) {
        for (Hazard h : hazards) if (h.overlaps(cx, cy, r)) return true;
        return false;
    }

    public void drawBackground(Graphics2D g, int w, int h, int groundY) {
        g.setPaint(new GradientPaint(0, 0, skyTop, 0, groundY, skyBottom));
        g.fillRect(0, 0, w, h);

        switch (deco) {
            case GARDEN -> {
                g.setColor(new Color(255, 255, 255, 140));

                for (int i = 0; i < 5; i++) {
                    double cx = ((i * 230 + time * (8 + i * 3)) % (w + 200)) - 100,
                            cy = 90 + i * 45;
                    g.fill(new Ellipse2D.Double(cx, cy, 120, 36));
                    g.fill(new Ellipse2D.Double(cx + 30, cy - 16, 70, 40));
                }

                g.setColor(new Color(230, 140, 70, 150));
                for (int i = 0; i < 5; i++)
                    g.fill(new Ellipse2D.Double(i * 220 - 80, groundY - 90, 300, 180));
            }
            case JUNGLE -> {
                g.setColor(new Color(30, 55, 35));

                for (int i = 0; i < 6; i++) {
                    double x = i * 175 + 20;
                    g.fill(new Rectangle2D.Double(x, 0, 46, groundY));
                    g.fill(new Ellipse2D.Double(x - 70, 20 + (i % 2) * 40, 190, 120));
                }

                g.setColor(new Color(60, 110, 50));
                g.setStroke(new BasicStroke(4f));

                for (int i = 0; i < 9; i++) {
                    double x = 60 + i * 100, len = 80 + (i * 37) % 90;
                    g.draw(
                            new QuadCurve2D.Double(
                                    x, 0, x + Math.sin(time + i) * 10, len / 2, x, len));
                }

                g.setColor(new Color(255, 245, 150, 200));

                for (int i = 0; i < 18; i++) {
                    double x = (i * 83 + Math.sin(time * 0.7 + i) * 30) % w,
                            y = 150 + (i * 47) % 380 + Math.cos(time + i) * 14;
                    g.fill(new Ellipse2D.Double(x, y, 4, 4));
                }
            }
            case VOLCANO -> {
                Path2D v = new Path2D.Double();
                v.moveTo(120, groundY);
                v.lineTo(360, 210);
                v.lineTo(420, 210);
                v.lineTo(680, groundY);
                v.closePath();
                g.setColor(new Color(35, 15, 30));
                g.fill(v);
                g.setPaint(
                        new RadialGradientPaint(
                                new Point2D.Double(390, 205),
                                90f,
                                new float[] {0f, 1f},
                                new Color[] {
                                    new Color(255, 200, 60, 230), new Color(255, 80, 30, 0)
                                }));
                g.fill(new Ellipse2D.Double(300, 120, 180, 180));
                g.setColor(new Color(60, 50, 55, 170));

                for (int i = 0; i < 45; i++) {
                    double x = (i * 97 + time * 18) % w, y = (i * 53 + time * 42) % groundY;
                    g.fill(new Ellipse2D.Double(x, y, 3, 3));
                }
            }
            case ICE -> {
                g.setColor(new Color(190, 225, 245));

                for (int i = 0; i < 4; i++) {
                    Path2D p = new Path2D.Double();
                    double x = i * 260 - 60;
                    p.moveTo(x, groundY);
                    p.lineTo(x + 170, 240 + (i % 2) * 70);
                    p.lineTo(x + 340, groundY);
                    p.closePath();
                    g.fill(p);
                }

                g.setColor(new Color(150, 195, 225));
                g.fillRect(0, 0, w, 14);
                g.setColor(Color.WHITE);

                for (int i = 0; i < 60; i++) {
                    double x = (i * 71 + Math.sin(time + i) * 25 + time * 20) % w,
                            y = (i * 43 + time * 38) % groundY;
                    g.fill(new Ellipse2D.Double(x, y, 3, 3));
                }
            }
        }

        g.setColor(ground);
        g.fillRect(0, groundY, w, h - groundY);
        g.setColor(groundTop);
        g.fillRect(0, groundY, w, 8);
        g.setColor(dirt);
        g.fillRect(0, groundY + 40, w, h);

        if (deco == Deco.VOLCANO) {
            g.setColor(new Color(255, 120, 30, 200));
            g.fillRect(0, groundY + 40, w, 6);
        }
    }

    public void drawWorld(Graphics2D g) {
        for (Zone z : zones) z.draw(g, BossScene.GROUND, time);
        for (Platform p : platforms) p.draw(g, platformColor);
        for (Hazard hz : hazards) hz.draw(g);
    }

    public void drawForeground(Graphics2D g, int w, int h) {
        if (!gusting()) return;
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 0; i < 16; i++) {
            double x = w - ((time * 900 + i * 137) % (w + 200)) + 100, y = 120 + (i * 61) % 480;
            g.draw(new Line2D.Double(x, y, x + 70, y));
        }
    }
}
