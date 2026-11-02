package ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import util.Sfx;

public class Button {
    private final Rectangle bounds;
    private String label;
    private boolean locked;

    public Button(int x, int y, int w, int h, String label) {
        this.bounds = new Rectangle(x, y, w, h);
        this.label = label;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean contains(Point p) {
        return !locked && bounds.contains(p);
    }

    public boolean pressed(Point p) {
        if (!contains(p)) return false;
        Sfx.play(Sfx.Sound.CLICK);
        return true;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void draw(Graphics2D g, Point mouse) {
        boolean hover = contains(mouse);
        g.setColor(
                locked ? new Color(190, 170, 150) : hover ? new Color(255, 245, 225) : Color.WHITE);
        g.fill(
                new RoundRectangle2D.Double(
                        bounds.x, bounds.y, bounds.width, bounds.height, 16, 16));
        g.setColor(locked ? new Color(130, 110, 90) : new Color(150, 90, 40));
        g.setFont(new Font("Tahoma", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(
                label,
                bounds.x + (bounds.width - fm.stringWidth(label)) / 2,
                bounds.y + (bounds.height + fm.getAscent() - fm.getDescent()) / 2);
    }
}
