package scene;

import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

import core.GameHost;
import core.GamePanel;
import core.PhysicsWorld;
import core.ScoreManager;
import map.Level;
import map.StageMap;
import model.entity.Fruit;
import model.entity.FruitType;
import model.object.Box;
import ui.Button;
import util.Art;
import util.Sfx;

public class MergeScene extends Scene {
    private static final int OX = (GamePanel.W - 600) / 2;
    private static final double DROP_COOLDOWN = 0.6, OVER_TIME = 1.2;

    private enum State {
        PLAYING,
        GAME_OVER
    }

    private final Level level;
    private final Box box = new Box(130, 470, 260, 680);
    private final PhysicsWorld world = new PhysicsWorld(box);
    private final ScoreManager score;
    private final Random rnd = new Random();
    private final Button againButton = new Button(230, 430, 140, 40, "Play again");
    private final Button menuButton;
    private final Button fightButton = new Button(225, 98, 150, 40, "FIGHT!");

    private State state = State.PLAYING;
    private FruitType current, next;
    private double mouseX = 300, cooldown = 0.3, overTimer, time;
    private Point mouse = new Point(), rawMouse = new Point();
    private final PauseMenu pause;

    public MergeScene(GameHost host, Level level) {
        super(host);
        this.level = level;
        this.score = host.getScoreManager();
        this.menuButton = new Button(230, 478, 140, 40, level == null ? "Menu" : "Map");
        this.pause = new PauseMenu(true, level != null);
        score.reset();
        current = FruitType.randomDroppable(rnd);
        next = FruitType.randomDroppable(rnd);
    }

    private boolean goalReached() {
        return level != null && score.getScore() >= level.mergeGoal();
    }

    private Point local(Point p) {
        return new Point(p.x - OX, p.y);
    }

    @Override
    public void mouseMoved(Point p) {
        rawMouse = p;
        mouse = local(p);
        mouseX = mouse.x;
    }

    @Override
    public void mousePressed(Point p) {
        if (state == State.PLAYING) {
            if (pause.isOpen()) {
                handle(pause.click(p));
                return;
            }

            if (pause.toggleHit(p)) {
                pause.open();
                return;
            }
        }

        Point l = local(p);

        if (state == State.GAME_OVER) {
            if (againButton.pressed(l)) host.goTo(new MergeScene(host, level));
            else if (menuButton.pressed(l)) leave();
            return;
        }

        if (goalReached() && fightButton.pressed(l)) {
            startFight();
            return;
        }

        if (cooldown <= 0) {
            world.add(new Fruit(current, dropX(), box.getLineY() - 20));
            Sfx.play(Sfx.Sound.DROP);
            current = next;
            next = FruitType.randomDroppable(rnd);
            cooldown = DROP_COOLDOWN;
        }
    }

    @Override
    public void keyPressed(int code) {
        if (code == java.awt.event.KeyEvent.VK_ESCAPE) {
            if (state == State.GAME_OVER) leave();
            else pause.toggle();
            return;
        }

        if (code == java.awt.event.KeyEvent.VK_ENTER
                && goalReached()
                && state == State.PLAYING
                && !pause.isOpen()) {
            Sfx.play(Sfx.Sound.CLICK);
            startFight();
        }
    }

    private void handle(PauseMenu.Action a) {
        if (a == null) return;

        switch (a) {
            case RESUME -> pause.close();
            case RESTART -> host.goTo(new MergeScene(host, level));
            case MAP -> host.goTo(new OverworldScene(host));
            case MAIN_MENU -> host.goTo(new MenuScene(host));
        }
    }

    private void leave() {
        host.goTo(level == null ? new MenuScene(host) : new OverworldScene(host));
    }

    private void startFight() {
        int s = score.getScore();
        host.goTo(new BossScene(host, level, level.bonusHearts(s), level.damageMultiplier(s)));
    }

    private double dropX() {
        double r = current.getRadius();
        return Math.max(box.getLeft() + r, Math.min(box.getRight() - r, mouseX));
    }

    @Override
    public void update(double dt) {
        if (pause.isOpen()) return;
        time += dt;
        if (state != State.PLAYING) return;
        if (cooldown > 0) cooldown -= dt;

        for (Fruit f : world.step(dt)) {
            score.add(f.getType().getScore());
            Sfx.playMerge(f.getType());
        }

        boolean over = false, danger = false;

        for (Fruit f : world.getFruits()) {
            if (box.isOutside(f)) over = true;
            if (f.getAge() > 1.0 && f.getY() - f.getRadius() < box.getDangerY()) danger = true;
        }

        overTimer = danger ? overTimer + dt : 0;

        if (over || overTimer > OVER_TIME) {
            state = State.GAME_OVER;
            Sfx.play(Sfx.Sound.LOSE);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(
                level == null ? new Color(210, 150, 90) : StageMap.mergeBackground(level.number()));
        g.fillRect(0, 0, GamePanel.W, GamePanel.H);
        AffineTransform old = g.getTransform();
        g.translate(OX, 0);

        box.draw(g, GamePanel.H);
        for (Fruit f : world.getFruits()) f.draw(g);

        if (state == State.PLAYING && cooldown <= 0) {
            double x = dropX();
            g.setColor(new Color(255, 255, 255, 90));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine((int) x, (int) box.getLineY(), (int) x, (int) box.getFloor());
            Fruit.paint(g, current, x, box.getLineY() - 20, current.getRadius());
        }

        badge(g, 60, 60, "Score", String.valueOf(score.getScore()));
        badge(
                g,
                60,
                150,
                level == null ? "Best" : "Goal",
                level == null
                        ? String.valueOf(score.getBest())
                        : String.valueOf(level.mergeGoal()));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Tahoma", Font.BOLD, 14));
        g.drawString("Next", 510, 40);
        g.fill(new Ellipse2D.Double(500, 50, 60, 60));
        Fruit.paint(g, next, 530, 80, Math.min(24, next.getRadius()));

        for (int i = 0; i < FruitType.values().length; i++) {
            FruitType t = FruitType.values()[i];
            Fruit.paint(g, t, 40, 250 + i * 50, Math.min(16, t.getRadius()));
        }

        if (level != null) drawLevelHud(g);
        if (state == State.GAME_OVER) drawGameOver(g);
        g.setTransform(old);
        if (state == State.PLAYING) pause.draw(g, rawMouse);
    }

    private void drawLevelHud(Graphics2D g) {
        g.setFont(new Font("Tahoma", Font.BOLD, 16));
        g.setColor(Color.WHITE);
        Art.centerText(
                g,
                "ด่าน " + level.number() + ": " + level.name() + "  vs  " + level.boss().getTitle(),
                300,
                30);

        int s = score.getScore(), goal = level.mergeGoal();
        double frac = Math.min(1.0, s / (double) goal);
        g.setColor(new Color(120, 70, 30));
        g.fillRoundRect(130, 186, 340, 14, 10, 10);
        g.setColor(goalReached() ? new Color(90, 200, 90) : new Color(255, 220, 90));
        g.fillRoundRect(130, 186, (int) (340 * frac), 14, 10, 10);
        g.setFont(new Font("Tahoma", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        Art.centerText(g, "พลังโจมตี", 300, 181);

        if (goalReached()) {
            double pulse = 1 + 0.05 * Math.sin(time * 8);
            AffineTransform t = g.getTransform();
            g.translate(300, 118);
            g.scale(pulse, pulse);
            g.translate(-300, -118);
            fightButton.draw(g, mouse);
            g.setTransform(t);
            g.setFont(new Font("Tahoma", Font.PLAIN, 13));
            g.setColor(Color.WHITE);
            Art.centerText(
                    g,
                    "พลัง: หัวใจ +"
                            + level.bonusHearts(s)
                            + "  เมล็ด x"
                            + String.format("%.1f", level.damageMultiplier(s))
                            + "  (รวมต่อเพื่อเพิ่มพลัง)",
                    300,
                    162);
        }
    }

    private void badge(Graphics2D g, int cx, int cy, String title, String value) {
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(cx - 40, cy - 40, 80, 80));
        g.setColor(new Color(150, 90, 40));
        g.setFont(new Font("Tahoma", Font.BOLD, 13));
        Art.centerText(g, title, cx, cy - 6);
        g.setFont(new Font("Tahoma", Font.BOLD, 20));
        Art.centerText(g, value, cx, cy + 20);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(-OX, 0, GamePanel.W, GamePanel.H);
        g.setColor(new Color(210, 130, 50));
        g.fill(new RoundRectangle2D.Double(180, 280, 240, 250, 24, 24));
        g.setColor(new Color(255, 240, 210));
        g.setStroke(new BasicStroke(5f));
        g.draw(new RoundRectangle2D.Double(180, 280, 240, 250, 24, 24));
        g.setStroke(new BasicStroke(1f));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Tahoma", Font.BOLD, 26));
        Art.centerText(g, "Game Over", 300, 325);
        g.setFont(new Font("Tahoma", Font.PLAIN, 16));
        Art.centerText(g, "Your Score", 300, 360);
        g.setFont(new Font("Tahoma", Font.BOLD, 30));
        Art.centerText(g, String.valueOf(score.getScore()), 300, 396);
        againButton.draw(g, mouse);
        menuButton.draw(g, mouse);
    }
}
