package model.entity;

import java.awt.*;
import java.awt.geom.*;

import core.GamePanel;
import map.StageMap;
import model.object.Platform;
import model.object.Seed;

public class Player {
    private static final double SPEED = 330, GRAVITY = 2400, JUMP_V = -880, DASH_SPEED = 760;
    private static final double DASH_TIME = 0.2,
            DASH_COOLDOWN = 0.7,
            SHOOT_INTERVAL = 0.12,
            INVULN_TIME = 1.3;
    private double x, y;
    private double vx, vy, shootCd, dashT, dashCd, invuln, anim;
    private int facing = 1, hp;
    private final int maxHp;
    private boolean onGround;
    private Platform standingOn;
    private double dropT;

    public Player(double x, double groundY, int maxHp) {
        this.x = x;
        this.y = groundY;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    public double getX() {
        return x;
    }

    public double getBodyY() {
        return y - 26;
    }

    public double getHitRadius() {
        return 19;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public boolean isDead() {
        return hp <= 0;
    }

    public boolean isDashing() {
        return dashT > 0;
    }

    public boolean isInvulnerable() {
        return invuln > 0 || dashT > 0;
    }

    public int getFacing() {
        return facing;
    }

    public void jump() {
        if (onGround && dashT <= 0) {
            vy = JUMP_V;
            onGround = false;
        }
    }

    public void dropDown() {
        if (standingOn != null) {
            dropT = 0.25;
            standingOn = null;
            onGround = false;
        }
    }

    public void cutJump() {
        if (vy < -300) vy *= 0.45;
    }

    public void dash() {
        if (dashCd <= 0 && dashT <= 0) {
            dashT = DASH_TIME;
            dashCd = DASH_COOLDOWN;
            vy = 0;
        }
    }

    public boolean hit(double knockDir) {
        if (isInvulnerable()) return false;
        hp--;
        invuln = INVULN_TIME;
        vy = -450;
        onGround = false;
        vx = knockDir * 250;
        return true;
    }

    public Seed update(
            double dt,
            boolean left,
            boolean right,
            boolean shoot,
            double groundY,
            double damage,
            StageMap map) {
        anim += dt;
        invuln = Math.max(0, invuln - dt);
        dashCd = Math.max(0, dashCd - dt);
        shootCd = Math.max(0, shootCd - dt);
        dropT = Math.max(0, dropT - dt);
        int dir = (right ? 1 : 0) - (left ? 1 : 0);
        if (dir != 0) facing = dir;

        if (standingOn != null) x += standingOn.getDx();

        if (dashT > 0) {
            dashT -= dt;
            vx = facing * DASH_SPEED;
            vy = 0;
        } else {
            boolean atFloor = onGround;
            double mul = atFloor && standingOn == null ? map.speedMultiplierAt(x) : 1.0;
            double accel = atFloor && map.isIcy() ? 2.2 : 14;
            vx += (dir * SPEED * mul - vx) * Math.min(1, dt * accel);
            vx += map.getWindAccel() * dt;
            vy += GRAVITY * dt;
        }

        double prevY = y;
        x += vx * dt;
        y += vy * dt;
        x = Math.max(30, Math.min(GamePanel.W - 30, x));

        onGround = false;
        standingOn = null;

        if (vy >= 0 && dropT <= 0) {
            for (Platform pl : map.getPlatforms()) {
                if (x >= pl.getLeft() - 8
                        && x <= pl.getRight() + 8
                        && prevY <= pl.getTop() + 3
                        && y >= pl.getTop()) {
                    y = pl.getTop();
                    vy = 0;
                    onGround = true;
                    standingOn = pl;
                }
            }
        }

        if (y >= groundY) {
            y = groundY;
            vy = 0;
            onGround = true;
            standingOn = null;
        }

        if (shoot && shootCd <= 0 && dashT <= 0) {
            shootCd = SHOOT_INTERVAL;
            return new Seed(x + facing * 30, y - 28, facing * 780, damage);
        }

        return null;
    }

    public void draw(Graphics2D g) {
        if (invuln > 0 && dashT <= 0 && ((int) (anim * 20)) % 2 == 0) return;
        double r = 26;
        double bob = onGround && Math.abs(vx) > 40 ? Math.abs(Math.sin(anim * 14)) * 4 : 0;
        double cy = y - r - bob;
        Composite old = g.getComposite();
        if (dashT > 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));

        g.setColor(new Color(60, 30, 20));
        double step = onGround ? Math.sin(anim * 14) * 6 * (Math.abs(vx) > 40 ? 1 : 0) : 0;
        g.fill(new Ellipse2D.Double(x - 14 + step, y - 6, 14, 8));
        g.fill(new Ellipse2D.Double(x + 1 - step, y - 6, 14, 8));
        Fruit.paint(g, FruitType.WATERMELON, x, cy, r);

        g.setColor(new Color(90, 50, 30));
        g.fill(
                new RoundRectangle2D.Double(
                        facing > 0 ? x + r - 6 : x - r - 8, cy - 2, 14, 9, 4, 4));
        g.setComposite(old);
    }
}
