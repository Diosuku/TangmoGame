package scene;

import java.awt.Graphics2D;
import java.awt.Point;

import core.GameHost;

public abstract class Scene {
    protected final GameHost host;

    protected Scene(GameHost host) {
        this.host = host;
    }

    public void update(double dt) {}

    public abstract void draw(Graphics2D g);

    public void mouseMoved(Point p) {}

    public void mousePressed(Point p) {}

    public void keyPressed(int keyCode) {}

    public void keyReleased(int keyCode) {}
}
