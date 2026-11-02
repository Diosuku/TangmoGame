package scene;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import core.GameHost;
import core.GamePanel;
import map.Level;
import map.StageMap;
import model.attack.Projectile;
import model.entity.Boss;
import model.entity.Player;
import model.object.Seed;
import ui.Button;
import util.Art;
import util.Sfx;

public class BossScene extends Scene {
    public static final int GROUND = 640;
    private static final double INTRO_TIME = 1.8, END_DELAY = 1.6;

    private enum Phase {
        INTRO,
        FIGHT,
        WON,
        LOST
    }

    private final Level level;
    private final int bonusHearts;
    private final double damage;
    private final Player player;
    private final Boss boss;
    private final StageMap map;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Seed> seeds = new ArrayList<>();
    private final Set<Integer> keys = new HashSet<>();
    private Button menu = new Button(GamePanel.W / 2 + 10, 430, 140, 44, "Map");
    private Button primaryBtn;
    private Phase phase = Phase.INTRO;
    private double t, endT, time, lastBossSfx = -1;
    private Point mouse = new Point();
    private final PauseMenu pause = new PauseMenu(true, true);

    public BossScene(GameHost host, Level level, int bonusHearts, double damage) {
        super(host);
        this.level = level;
        this.bonusHearts = bonusHearts;
        this.damage = damage;
        this.player = new Player(150, GROUND, 3 + bonusHearts);
        this.boss = new Boss(level.boss(), 745, GROUND);
        this.map = StageMap.create(level.number());
        primaryBtn = new Button(GamePanel.W / 2 - 150, 430, 140, 44, "");
    }

    public void addProjectile(Projectile p) {
        projectiles.add(p);

        if (time - lastBossSfx > 0.09) {
            Sfx.play(Sfx.Sound.BOSS_SHOOT);
            lastBossSfx = time;
        }
    }

    private void hurt() {
        if (player.hit(-1)) Sfx.play(Sfx.Sound.HURT);
    }

    public double getPlayerX() {
        return player.getX();
    }

    public double getPlayerY() {
        return player.getBodyY();
    }

    @Override
    public void keyPressed(int code) {
        boolean fresh = keys.add(code);

        if (code == KeyEvent.VK_ESCAPE) {
            if (phase == Phase.WON || phase == Phase.LOST) host.goTo(new OverworldScene(host));
            else pause.toggle();
            return;
        }

        if (phase != Phase.FIGHT || !fresh || pause.isOpen()) return;
        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_W || code == KeyEvent.VK_UP)
            player.jump();
        if (code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_X) player.dash();
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) player.dropDown();
    }

    @Override
    public void keyReleased(int code) {
        keys.remove(code);
        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_W || code == KeyEvent.VK_UP)
            player.cutJump();
    }

    @Override
    public void mouseMoved(Point p) {
        mouse = p;
    }

    @Override
    public void mousePressed(Point p) {
        if (phase == Phase.INTRO || phase == Phase.FIGHT) {
            if (pause.isOpen()) {
                handle(pause.click(p));
                return;
            }

            if (pause.toggleHit(p)) {
                pause.open();
                return;
            }
        }

        if (phase == Phase.WON && endT > END_DELAY) {
            if (primaryBtn.pressed(p)) host.goTo(new OverworldScene(host));
            else if (menu.pressed(p)) host.goTo(new MenuScene(host));
        } else if (phase == Phase.LOST && endT > END_DELAY) {
            if (primaryBtn.pressed(p)) host.goTo(new BossScene(host, level, bonusHearts, damage));
            else if (menu.pressed(p)) host.goTo(new OverworldScene(host));
        }
    }

    private void handle(PauseMenu.Action a) {
        if (a == null) return;

        switch (a) {
            case RESUME -> pause.close();
            case RESTART -> host.goTo(new BossScene(host, level, bonusHearts, damage));
            case MAP -> host.goTo(new OverworldScene(host));
            case MAIN_MENU -> host.goTo(new MenuScene(host));
        }
    }

    private boolean down(int... codes) {
        for (int c : codes) if (keys.contains(c)) return true;
        return false;
    }

    @Override
    public void update(double dt) {
        if (pause.isOpen()) return;
        time += dt;
        if (phase != Phase.INTRO) map.update(dt);

        switch (phase) {
            case INTRO -> {
                t += dt;
                if (t > INTRO_TIME) phase = Phase.FIGHT;
            }
            case FIGHT -> fight(dt);
            case WON, LOST -> {
                endT += dt;
                if (phase == Phase.WON) boss.update(dt, this);
            }
        }
    }

    private void fight(double dt) {
        Seed s =
                player.update(
                        dt,
                        down(KeyEvent.VK_LEFT, KeyEvent.VK_A),
                        down(KeyEvent.VK_RIGHT, KeyEvent.VK_D),
                        down(KeyEvent.VK_Z, KeyEvent.VK_J, KeyEvent.VK_C),
                        GROUND,
                        damage,
                        map);

        if (s != null) {
            seeds.add(s);
            Sfx.play(Sfx.Sound.SHOOT);
        }

        boss.update(dt, this);

        for (Iterator<Seed> it = seeds.iterator(); it.hasNext(); ) {
            Seed sd = it.next();
            sd.update(dt);

            if (Math.hypot(sd.getX() - boss.getX(), sd.getY() - boss.getY())
                    <= boss.getHurtRadius() * 0.9) {
                boss.damage(sd.getDamage());
                it.remove();
            } else if (sd.isOffscreen()) it.remove();
        }

        for (Iterator<Projectile> it = projectiles.iterator(); it.hasNext(); ) {
            Projectile p = it.next();
            p.update(dt);

            if (p.isOffscreen()) {
                it.remove();
                continue;
            }

            if (!player.isInvulnerable()
                    && Math.hypot(p.getX() - player.getX(), p.getY() - player.getBodyY())
                            <= p.getRadius() + player.getHitRadius()) {
                hurt();
                it.remove();
            }
        }

        if (!player.isInvulnerable()
                && map.hazardHits(player.getX(), player.getBodyY(), player.getHitRadius())) hurt();

        if (Math.hypot(boss.getX() - player.getX(), boss.getY() - player.getBodyY())
                <= boss.getHurtRadius() * 0.85 + player.getHitRadius()) {
            hurt();
        }

        if (boss.isDead()) {
            phase = Phase.WON;
            endT = 0;
            projectiles.clear();
            seeds.clear();
            Sfx.play(Sfx.Sound.WIN);
            host.getProgress().clear(level.number() - 1);
            primaryBtn = new Button(GamePanel.W / 2 - 150, 430, 140, 44, "Continue");
            menu = new Button(GamePanel.W / 2 + 10, 430, 140, 44, "Menu");
        } else if (player.isDead()) {
            phase = Phase.LOST;
            endT = 0;
            Sfx.play(Sfx.Sound.LOSE);
            primaryBtn = new Button(GamePanel.W / 2 - 150, 430, 140, 44, "Retry");
        }
    }

    @Override
    public void draw(Graphics2D g) {
        int W = GamePanel.W, H = GamePanel.H;
        map.drawBackground(g, W, H, GROUND);
        map.drawWorld(g);

        double scale = phase == Phase.WON ? Math.max(0, 1 - Math.max(0, endT - 0.2) / 1.3) : 1;
        if (scale > 0) boss.draw(g, scale);
        for (Seed s : seeds) s.draw(g);
        for (Projectile p : projectiles) p.draw(g);
        if (phase != Phase.LOST || endT < 0.3) player.draw(g);
        map.drawForeground(g, W, H);

        drawHud(g);
        drawOverlay(g);
        if (phase == Phase.INTRO || phase == Phase.FIGHT) pause.draw(g, mouse);
    }

    private void drawHud(Graphics2D g) {

        for (int i = 0; i < player.getMaxHp(); i++) {
            Art.heart(
                    g,
                    40 + i * 38,
                    685,
                    13,
                    i < player.getHp() ? new Color(235, 50, 70) : new Color(80, 50, 40, 160));
        }

        g.setFont(new Font("Tahoma", Font.BOLD, 13));
        g.setColor(map.getHudText());
        g.drawString("เมล็ด x" + String.format("%.1f", damage), 40, 712);

        double f = boss.getHp() / boss.getType().getMaxHp();
        g.setColor(new Color(60, 30, 10, 200));
        g.fillRoundRect(GamePanel.W / 2 - 230, 20, 460, 22, 12, 12);
        g.setColor(f > 0.33 ? new Color(240, 90, 60) : new Color(255, 40, 40));
        g.fillRoundRect(GamePanel.W / 2 - 226, 24, (int) (452 * f), 14, 8, 8);
        g.setFont(new Font("Tahoma", Font.BOLD, 14));
        g.setColor(map.getHudText());
        Art.centerText(g, level.boss().getTitle(), GamePanel.W / 2, 60);

        g.setFont(new Font("Tahoma", Font.PLAIN, 12));
        g.drawString(
                "←/→ เดิน  Space กระโดด  ↓ ลงจากแท่น  Z ยิง  Shift ดัช  Esc เมนู",
                GamePanel.W - 440,
                712);
    }

    private void drawOverlay(Graphics2D g) {
        int W = GamePanel.W;

        if (phase == Phase.INTRO) {
            g.setFont(new Font("Tahoma", Font.BOLD, 72));
            Art.outlinedText(
                    g, t < 1.0 ? "READY?" : "FIGHT!", W / 2, 330, new Color(255, 240, 200));
            return;
        }

        if (phase == Phase.WON || phase == Phase.LOST) {
            if (endT < 0.3) return;
            boolean won = phase == Phase.WON;
            g.setFont(new Font("Tahoma", Font.BOLD, 72));
            Art.outlinedText(
                    g,
                    won ? "KNOCKOUT!" : "YOU DIED",
                    W / 2,
                    300,
                    won ? new Color(255, 230, 90) : new Color(255, 110, 100));

            if (endT > END_DELAY) {
                g.setFont(new Font("Tahoma", Font.PLAIN, 18));
                g.setColor(map.getHudText());
                Art.centerText(
                        g,
                        won ? "ชนะ " + level.boss().getTitle() + " แล้ว!" : "ลองใหม่อีกครั้ง",
                        W / 2,
                        385);
                primaryBtn.draw(g, mouse);
                menu.draw(g, mouse);
            }
        }
    }
}
