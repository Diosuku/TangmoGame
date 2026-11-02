package scene;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.HashSet;
import java.util.Set;

import core.GameHost;
import core.GamePanel;
import map.Level;
import map.Progress;
import map.WorldMap;
import model.entity.Fruit;
import model.entity.FruitType;
import util.Art;
import util.BossArt;
import util.Sfx;

public class OverworldScene extends Scene {
    private static final double SPEED = 200, RADIUS = 11, INTERACT_DIST = 60;
    private static final Color[] ZONE = {
        new Color(255, 190, 90, 150),
        new Color(30, 90, 55, 175),
        new Color(150, 45, 40, 185),
        new Color(210, 240, 255, 215)
    };

    private final WorldMap map = new WorldMap();
    private final Set<Integer> keys = new HashSet<>();
    private double px, py, anim, time;
    private int facing = 1;
    private boolean moving;
    private Point mouse = new Point();
    private final PauseMenu pause = new PauseMenu(false, false);

    public OverworldScene(GameHost host) {
        super(host);
        int last = host.getProgress().getLastNode();

        if (last >= 0) {
            WorldMap.Node n = map.getNodes().get(last);
            px = n.x();
            py = n.y() + 40;

            if (!map.canStand(px, py, RADIUS, host.getProgress())) {
                px = n.x();
                py = n.y();
            }
        } else {
            px = map.getStart()[0] + 20;
            py = map.getStart()[1];
        }
    }

    @Override
    public void keyPressed(int code) {
        boolean fresh = keys.add(code);

        if (code == KeyEvent.VK_ESCAPE) {
            pause.toggle();
            return;
        }

        if (fresh
                && !pause.isOpen()
                && (code == KeyEvent.VK_Z
                        || code == KeyEvent.VK_ENTER
                        || code == KeyEvent.VK_SPACE)) {
            WorldMap.Node n = nearestNode();

            if (n != null) {
                Sfx.play(Sfx.Sound.CLICK);
                host.getProgress().setLastNode(n.levelIndex());
                host.goTo(new MergeScene(host, Level.ALL.get(n.levelIndex())));
            }
        }
    }

    @Override
    public void keyReleased(int code) {
        keys.remove(code);
    }

    @Override
    public void mouseMoved(Point p) {
        mouse = p;
    }

    @Override
    public void mousePressed(Point p) {
        if (pause.isOpen()) {
            PauseMenu.Action a = pause.click(p);
            if (a == PauseMenu.Action.MAIN_MENU) host.goTo(new MenuScene(host));
        } else if (pause.toggleHit(p)) pause.open();
    }

    private boolean down(int... codes) {
        for (int c : codes) if (keys.contains(c)) return true;
        return false;
    }

    private WorldMap.Node nearestNode() {
        for (WorldMap.Node n : map.getNodes()) {
            if (host.getProgress().isUnlocked(n.levelIndex())
                    && Math.hypot(px - n.x(), py - n.y()) < INTERACT_DIST) return n;
        }

        return null;
    }

    private WorldMap.Gate nearestLockedGate() {
        for (WorldMap.Gate g : map.getGates()) {
            if (!host.getProgress().isUnlocked(g.levelIndex())
                    && Math.hypot(px - g.x(), py - g.y()) < g.radius() + 45) return g;
        }

        return null;
    }

    @Override
    public void update(double dt) {
        if (pause.isOpen()) return;
        time += dt;
        double dx =
                (down(KeyEvent.VK_RIGHT, KeyEvent.VK_D) ? 1 : 0)
                        - (down(KeyEvent.VK_LEFT, KeyEvent.VK_A) ? 1 : 0);
        double dy =
                (down(KeyEvent.VK_DOWN, KeyEvent.VK_S) ? 1 : 0)
                        - (down(KeyEvent.VK_UP, KeyEvent.VK_W) ? 1 : 0);
        moving = dx != 0 || dy != 0;
        if (!moving) return;
        double len = Math.hypot(dx, dy);
        dx = dx / len * SPEED * dt;
        dy = dy / len * SPEED * dt;
        if (dx != 0) facing = dx > 0 ? 1 : -1;
        anim += dt;
        Progress pr = host.getProgress();

        if (map.canStand(px + dx, py + dy, RADIUS, pr)) {
            px += dx;
            py += dy;
        } else if (map.canStand(px + dx, py, RADIUS, pr)) px += dx;
        else if (map.canStand(px, py + dy, RADIUS, pr)) py += dy;
    }

    @Override
    public void draw(Graphics2D g) {
        int W = GamePanel.W, H = GamePanel.H;
        drawSea(g, W, H);
        drawLand(g);
        drawProps(g);
        drawTrail(g);
        drawGates(g);
        drawNodes(g);
        drawStartHouse(g);
        drawPlayer(g);
        drawHud(g);
        pause.draw(g, mouse);
    }

    private void drawSea(Graphics2D g, int W, int H) {
        g.setPaint(new GradientPaint(0, 0, new Color(60, 150, 190), 0, H, new Color(35, 100, 160)));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(255, 255, 255, 60));
        g.setStroke(new BasicStroke(2f));

        for (int i = 0; i < 26; i++) {
            double x = (i * 137 + Math.sin(time + i) * 14) % (W + 40) - 20, y = (i * 89) % H;
            g.draw(new Arc2D.Double(x, y, 30, 10, 200, 140, Arc2D.OPEN));
        }
    }

    private void drawLand(Graphics2D g) {
        Area land = map.getLand();
        g.setColor(new Color(240, 215, 150));
        AffineTransform old = g.getTransform();
        g.fill(land);
        Shape clip = g.getClip();

        Area inner = new Area(new BasicStroke(10f).createStrokedShape(land));
        Area grass = new Area(land);
        grass.subtract(inner);
        g.setColor(new Color(125, 185, 95));
        g.fill(grass);
        g.setClip(grass);

        for (int i = 0; i < map.getNodes().size(); i++) {
            WorldMap.Node n = map.getNodes().get(i);
            g.setPaint(
                    new RadialGradientPaint(
                            new Point2D.Double(n.x(), n.y()),
                            230f,
                            new float[] {0f, 0.55f, 1f},
                            new Color[] {
                                ZONE[i],
                                ZONE[i],
                                new Color(
                                        ZONE[i].getRed(), ZONE[i].getGreen(), ZONE[i].getBlue(), 0)
                            }));
            g.fill(new Ellipse2D.Double(n.x() - 230, n.y() - 230, 460, 460));
        }

        g.setClip(clip);
        g.setTransform(old);
    }

    private void drawTrail(Graphics2D g) {
        Path2D line = new Path2D.Double();
        line.moveTo(WorldMap.PATH[0][0], WorldMap.PATH[0][1]);
        for (int i = 1; i < WorldMap.PATH.length; i++)
            line.lineTo(WorldMap.PATH[i][0], WorldMap.PATH[i][1]);
        g.setColor(new Color(200, 160, 105, 200));
        g.setStroke(new BasicStroke(46f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(line);
        g.setColor(new Color(225, 190, 135, 160));
        g.setStroke(new BasicStroke(30f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(line);
    }

    private void drawProps(Graphics2D g) {
        double[][] P = WorldMap.PATH;
        int count = 0;

        for (int i = 0; i + 1 < P.length; i++) {
            double ax = P[i][0],
                    ay = P[i][1],
                    dx = P[i + 1][0] - ax,
                    dy = P[i + 1][1] - ay,
                    len = Math.hypot(dx, dy);
            double nx = -dy / len, ny = dx / len;

            for (double s = 20; s < len; s += 46) {
                for (int side = -1; side <= 1; side += 2) {
                    double x = ax + dx / len * s + nx * side * 92,
                            y = ay + dy / len * s + ny * side * 92;
                    double d = WorldMap.distToPath(x, y);
                    if (d < 84 || !map.getLand().contains(x, y) || (++count % 3 == 0)) continue;
                    prop(g, zoneOf(x, y), x, y, count);
                }
            }
        }
    }

    private int zoneOf(double x, double y) {
        int best = 0;
        double bd = Double.MAX_VALUE;

        for (int i = 0; i < map.getNodes().size(); i++) {
            WorldMap.Node n = map.getNodes().get(i);
            double d = Math.hypot(x - n.x(), y - n.y());

            if (d < bd) {
                bd = d;
                best = i;
            }
        }

        return best;
    }

    private void prop(Graphics2D g, int zone, double x, double y, int seed) {
        double s = 0.8 + (seed % 4) * 0.12;

        switch (zone) {
            case 0 -> {
                g.setColor(new Color(60, 140, 50));
                for (int i = -1; i <= 1; i++)
                    g.fill(new Ellipse2D.Double(x + i * 7 * s - 4, y - 22 * s, 8, 20 * s));
                g.setColor(new Color(240, 190, 50));
                g.fill(new Ellipse2D.Double(x - 11 * s, y - 14 * s, 22 * s, 24 * s));
            }
            case 1 -> {
                g.setColor(new Color(90, 60, 35));
                g.fill(new Rectangle2D.Double(x - 4, y - 8, 8, 20 * s));
                g.setColor(new Color(25, 85, 45));
                g.fill(new Ellipse2D.Double(x - 20 * s, y - 32 * s, 40 * s, 34 * s));
            }
            case 2 -> {
                g.setColor(new Color(50, 30, 32));
                g.fill(new Ellipse2D.Double(x - 16 * s, y - 12 * s, 32 * s, 24 * s));
                g.setColor(new Color(255, 120, 40, 200 + (int) (50 * Math.sin(time * 3 + seed))));
                g.setStroke(new BasicStroke(2f));
                g.draw(new Line2D.Double(x - 6 * s, y - 6 * s, x + 4 * s, y + 4 * s));
            }
            default -> {
                Path2D c = new Path2D.Double();
                c.moveTo(x, y - 30 * s);
                c.lineTo(x + 11 * s, y);
                c.lineTo(x, y + 8);
                c.lineTo(x - 11 * s, y);
                c.closePath();
                g.setColor(new Color(150, 205, 240));
                g.fill(c);
                g.setColor(new Color(255, 255, 255, 170));
                g.fill(new Rectangle2D.Double(x - 3, y - 22 * s, 3, 14 * s));
            }
        }
    }

    private void drawStartHouse(Graphics2D g) {
        double x = map.getStart()[0] - 28, y = map.getStart()[1] - 60;
        g.setColor(new Color(170, 110, 60));
        g.fill(new Rectangle2D.Double(x - 24, y - 6, 48, 36));
        Path2D roof = new Path2D.Double();
        roof.moveTo(x - 30, y - 6);
        roof.lineTo(x, y - 34);
        roof.lineTo(x + 30, y - 6);
        roof.closePath();
        g.setColor(new Color(190, 60, 50));
        g.fill(roof);
        g.setColor(new Color(90, 55, 30));
        g.fill(new Rectangle2D.Double(x - 6, y + 8, 12, 22));
    }

    private void drawGates(Graphics2D g) {
        for (WorldMap.Gate gt : map.getGates()) {
            if (host.getProgress().isUnlocked(gt.levelIndex())) continue;
            double x = gt.x(), y = gt.y();
            g.setColor(new Color(0, 0, 0, 50));
            g.fill(new Ellipse2D.Double(x - 70, y + 20, 140, 34));
            double[][] rocks = {
                {-48, 8, 28},
                {-18, 22, 30},
                {20, 18, 32},
                {48, 6, 26},
                {-30, -16, 30},
                {12, -20, 34},
                {40, -26, 24},
                {-4, 0, 36}
            };

            for (double[] r : rocks) {
                g.setColor(new Color(110, 108, 115));
                g.fill(
                        new Ellipse2D.Double(
                                x + r[0] - r[2], y + r[1] - r[2] * 0.8, r[2] * 2, r[2] * 1.6));
                g.setColor(new Color(150, 148, 155));
                g.fill(
                        new Ellipse2D.Double(
                                x + r[0] - r[2] * 0.7,
                                y + r[1] - r[2] * 0.7,
                                r[2] * 1.0,
                                r[2] * 0.6));
            }

            g.setColor(new Color(240, 200, 60));
            g.fill(new RoundRectangle2D.Double(x - 14, y - 6, 28, 22, 6, 6));
            g.setColor(new Color(240, 200, 60));
            g.setStroke(new BasicStroke(4f));
            g.draw(new Arc2D.Double(x - 9, y - 22, 18, 22, 0, 180, Arc2D.OPEN));
            g.setColor(new Color(90, 60, 10));
            g.fill(new Ellipse2D.Double(x - 3, y, 6, 6));
        }
    }

    private void drawNodes(Graphics2D g) {
        for (WorldMap.Node n : map.getNodes()) {
            Level lv = Level.ALL.get(n.levelIndex());
            boolean open = host.getProgress().isUnlocked(n.levelIndex());
            boolean cleared = host.getProgress().isCleared(n.levelIndex());
            double x = n.x(), y = n.y();

            g.setColor(new Color(150, 100, 55, open ? 220 : 120));
            g.fill(new Ellipse2D.Double(x - 44, y - 22, 88, 46));
            g.setColor(new Color(245, 225, 175, open ? 255 : 130));
            g.fill(new Ellipse2D.Double(x - 38, y - 18, 76, 36));

            g.setColor(new Color(110, 70, 35));
            g.fill(new Rectangle2D.Double(x - 3, y - 70, 6, 66));

            double bob = open ? Math.sin(time * 2.4 + n.levelIndex()) * 4 : 0;

            if (open) {
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(x - 32, y - 112 + bob, 64, 64));
                BossArt.paint(g, lv.boss(), x, y - 80 + bob, 24, true, 0);
            } else {
                g.setColor(new Color(150, 150, 150, 200));
                g.fill(new Ellipse2D.Double(x - 32, y - 112, 64, 64));
                g.setColor(new Color(90, 90, 90));
                g.setFont(new Font("Tahoma", Font.BOLD, 36));
                Art.centerText(g, "?", (int) x, (int) y - 69);
            }

            g.setFont(new Font("Tahoma", Font.BOLD, 14));
            Art.outlinedText(g, "ด่าน " + lv.number(), (int) x, (int) y + 42, Color.WHITE);

            if (cleared) {
                g.setColor(new Color(255, 215, 60));
                g.fill(starShape(x + 40, y - 70, 14));
            }
        }
    }

    private static Shape starShape(double cx, double cy, double r) {
        Path2D p = new Path2D.Double();

        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5, rr = i % 2 == 0 ? r : r * 0.45;
            if (i == 0) p.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
            else p.lineTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
        }

        p.closePath();
        return p;
    }

    private void drawPlayer(Graphics2D g) {
        double bob = moving ? Math.abs(Math.sin(anim * 14)) * 4 : 0;
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new Ellipse2D.Double(px - 14, py + 8, 28, 10));
        double r = 17;
        g.setColor(new Color(60, 30, 20));
        double step = moving ? Math.sin(anim * 14) * 5 : 0;
        g.fill(new Ellipse2D.Double(px - 12 + step, py + 5, 11, 7));
        g.fill(new Ellipse2D.Double(px + 1 - step, py + 5, 11, 7));
        Fruit.paint(g, FruitType.WATERMELON, px, py - 6 - bob, r);
    }

    private void drawHud(Graphics2D g) {
        Progress pr = host.getProgress();
        int cleared = 0;
        for (int i = 0; i < Level.ALL.size(); i++) if (pr.isCleared(i)) cleared++;
        g.setFont(new Font("Tahoma", Font.BOLD, 20));
        Art.outlinedText(
                g,
                "แผนที่ผลไม้  ผ่านแล้ว " + cleared + "/" + Level.ALL.size(),
                740,
                40,
                Color.WHITE);
        g.setFont(new Font("Tahoma", Font.PLAIN, 13));
        g.setColor(Color.WHITE);
        g.drawString("←↑↓→ / WASD เดิน    Z / Enter เข้าด่าน    Esc เมนู", 30, 700);

        WorldMap.Node n = nearestNode();
        String msg = null;

        if (n != null) {
            Level lv = Level.ALL.get(n.levelIndex());
            msg = "กด Z เพื่อสู้กับ " + lv.boss().getTitle() + " (ด่าน " + lv.number() + ")";
        } else if (nearestLockedGate() != null) {
            msg = "ทางถูกปิดอยู่ ต้องชนะบอสก่อนหน้าก่อน";
        }

        if (msg != null) {
            g.setFont(new Font("Tahoma", Font.BOLD, 15));
            int w = g.getFontMetrics().stringWidth(msg) + 28;
            int bx = (int) Math.max(10, Math.min(GamePanel.W - w - 10, px - w / 2.0)),
                    by = (int) py - 70;
            g.setColor(new Color(255, 250, 230));
            g.fill(new RoundRectangle2D.Double(bx, by, w, 30, 14, 14));
            g.setColor(new Color(150, 90, 40));
            g.drawString(msg, bx + 14, by + 20);
        }

        if (pr.isAllClear()) {
            g.setFont(new Font("Tahoma", Font.BOLD, 20));
            Art.outlinedText(g, "ผ่านครบทุกด่านแล้ว! เก่งมาก", 700, 80, new Color(255, 230, 90));
        }
    }
}
