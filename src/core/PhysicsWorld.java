package core;

import java.util.*;

import model.entity.Fruit;
import model.object.Box;

public class PhysicsWorld {
    private static final double GRAVITY = 900;
    private static final double RESTITUTION = 0.15;
    private static final int SUBSTEPS = 6;

    private static final int RELAX_ITERATIONS = 4;
    private static final double NEAR_VERTICAL = 0.12, SLIDE_KICK = 14, SLIDE_MAX_SPEED = 45;

    private final Box box;
    private final List<Fruit> fruits = new ArrayList<>();

    public PhysicsWorld(Box box) {
        this.box = box;
    }

    public List<Fruit> getFruits() {
        return fruits;
    }

    public void add(Fruit f) {
        fruits.add(f);
    }

    public void clear() {
        fruits.clear();
    }

    public List<Fruit> step(double dt) {
        List<Fruit> created = new ArrayList<>();
        double h = dt / SUBSTEPS;

        for (int s = 0; s < SUBSTEPS; s++) {
            for (Fruit f : fruits) f.update(h, GRAVITY);
            for (Fruit f : fruits) collideWalls(f);
            Set<Fruit> consumed = new HashSet<>();

            for (int i = 0; i < fruits.size(); i++) {
                for (int j = i + 1; j < fruits.size(); j++) {
                    Fruit a = fruits.get(i), b = fruits.get(j);
                    if (consumed.contains(a) || consumed.contains(b) || !a.touches(b)) continue;

                    if (a.getType() == b.getType() && !a.getType().isLargest()) {
                        consumed.add(a);
                        consumed.add(b);
                        double ma = a.getMass(), mb = b.getMass();
                        Fruit n =
                                new Fruit(
                                        a.getType().next(),
                                        (a.getX() * ma + b.getX() * mb) / (ma + mb),
                                        (a.getY() * ma + b.getY() * mb) / (ma + mb));
                        n.setVelocity((a.getVx() + b.getVx()) / 2, (a.getVy() + b.getVy()) / 2);
                        created.add(n);
                    } else {
                        resolve(a, b, true);
                    }
                }
            }

            if (!consumed.isEmpty()) {
                fruits.removeAll(consumed);
                fruits.addAll(
                        created.subList(created.size() - consumed.size() / 2, created.size()));
            }

            relax();
        }

        return created;
    }

    private void relax() {
        for (int it = 0; it < RELAX_ITERATIONS; it++) {
            for (Fruit f : fruits) collideWalls(f);

            for (int i = 0; i < fruits.size(); i++) {
                for (int j = i + 1; j < fruits.size(); j++) {
                    Fruit a = fruits.get(i), b = fruits.get(j);
                    if (!a.touches(b)) continue;
                    if (a.getType() == b.getType() && !a.getType().isLargest()) continue;
                    resolve(a, b, false);
                }
            }
        }

        for (Fruit f : fruits) collideWalls(f);
    }

    private void collideWalls(Fruit f) {
        double r = f.getRadius();

        if (f.getX() - r < box.getLeft()) {
            f.addPosition(box.getLeft() - (f.getX() - r), 0);
            if (f.getVx() < 0) f.setVelocity(-f.getVx() * RESTITUTION, f.getVy());
        } else if (f.getX() + r > box.getRight()) {
            f.addPosition(box.getRight() - (f.getX() + r), 0);
            if (f.getVx() > 0) f.setVelocity(-f.getVx() * RESTITUTION, f.getVy());
        }

        if (f.getY() + r > box.getFloor()) {
            f.addPosition(0, box.getFloor() - (f.getY() + r));
            if (f.getVy() > 0) f.setVelocity(f.getVx() * 0.98, -f.getVy() * RESTITUTION);
        }
    }

    private void resolve(Fruit a, Fruit b, boolean velocity) {
        double dx = b.getX() - a.getX(), dy = b.getY() - a.getY();
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < 1e-6) {
            dx = 0;
            dy = -1;
            dist = 1e-6;
        }

        double nx = dx / dist, ny = dy / dist;
        double overlap = a.getRadius() + b.getRadius() - dist;
        double ia = 1 / a.getMass(), ib = 1 / b.getMass();
        double k = overlap / (ia + ib);
        a.addPosition(-nx * k * ia, -ny * k * ia);
        b.addPosition(nx * k * ib, ny * k * ib);

        if (!velocity) return;

        if (Math.abs(nx) < NEAR_VERTICAL) {
            Fruit up = a.getY() < b.getY() ? a : b, low = up == a ? b : a;
            double off = up.getX() - low.getX();
            int dir = Math.abs(off) > 0.05 ? (off > 0 ? 1 : -1) : up.getSlideBias();
            if (Math.abs(up.getVx()) < SLIDE_MAX_SPEED) up.addVelocity(dir * SLIDE_KICK, 0);
        }

        double rvn = (b.getVx() - a.getVx()) * nx + (b.getVy() - a.getVy()) * ny;

        if (rvn < 0) {
            double j = -(1 + RESTITUTION) * rvn / (ia + ib);
            a.addVelocity(-j * nx * ia, -j * ny * ia);
            b.addVelocity(j * nx * ib, j * ny * ib);
        }
    }
}
