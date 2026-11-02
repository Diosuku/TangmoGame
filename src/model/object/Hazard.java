package model.object;

import java.awt.Graphics2D;

public interface Hazard {
    void update(double dt);

    boolean overlaps(double cx, double cy, double r);

    void draw(Graphics2D g);
}
