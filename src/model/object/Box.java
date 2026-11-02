package model.object;

import java.awt.*;

import model.entity.Fruit;

public class Box {
    private final double left, right, floor, lineY;

    public Box(double left, double right, double top, double floor) {
        this.left = left;
        this.right = right;
        this.floor = floor;
        this.lineY = top;
    }

    public double getLeft() {
        return left;
    }

    public double getRight() {
        return right;
    }

    public double getFloor() {
        return floor;
    }

    public double getLineY() {
        return lineY;
    }

    public double getWidth() {
        return right - left;
    }

    public boolean isOutside(Fruit f) {
        return f.getX() < left - 5 || f.getX() > right + 5 || f.getY() > floor + 5;
    }

    public void draw(Graphics2D g, int panelHeight) {
        g.setColor(new Color(255, 232, 178));
        g.fillRect((int) left, (int) lineY, (int) getWidth(), (int) (floor - lineY));
        g.setColor(new Color(250, 215, 140));
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) left, (int) lineY, (int) left, (int) floor);
        g.drawLine((int) right, (int) lineY, (int) right, (int) floor);
        g.drawLine((int) left, (int) floor, (int) right, (int) floor);

        g.setColor(new Color(220, 70, 50, 170));
        g.setStroke(
                new BasicStroke(
                        2f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10f,
                        new float[] {8f, 6f},
                        0f));
        g.drawLine((int) left, (int) lineY + 40, (int) right, (int) lineY + 40);
    }

    public double getDangerY() {
        return lineY + 40;
    }
}
