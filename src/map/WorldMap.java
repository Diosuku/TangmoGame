package map;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class WorldMap {

    public record Node(int levelIndex, double x, double y) {}

    public record Gate(int levelIndex, double x, double y, double radius) {}

    public static final double[][] PATH = {
        {90, 630}, {250, 630}, {430, 630}, {640, 630}, {730, 540},
        {730, 420}, {600, 330}, {420, 330}, {250, 260}, {180, 140}
    };
    private static final double WALK_HALF = 62, LAND_HALF = 105, PLAZA = 85;

    private final List<Node> nodes = new ArrayList<>();
    private final List<Gate> gates = new ArrayList<>();
    private final Area walkable, land;

    public WorldMap() {
        nodes.add(new Node(0, PATH[2][0], PATH[2][1]));
        nodes.add(new Node(1, PATH[5][0], PATH[5][1]));
        nodes.add(new Node(2, PATH[7][0], PATH[7][1]));
        nodes.add(new Node(3, PATH[9][0], PATH[9][1]));
        gates.add(new Gate(1, 685, 585, 68));
        gates.add(new Gate(2, 665, 375, 68));
        gates.add(new Gate(3, 335, 295, 68));

        Path2D line = new Path2D.Double();
        line.moveTo(PATH[0][0], PATH[0][1]);
        for (int i = 1; i < PATH.length; i++) line.lineTo(PATH[i][0], PATH[i][1]);
        walkable =
                new Area(
                        new BasicStroke(
                                        (float) (WALK_HALF * 2),
                                        BasicStroke.CAP_ROUND,
                                        BasicStroke.JOIN_ROUND)
                                .createStrokedShape(line));
        land =
                new Area(
                        new BasicStroke(
                                        (float) (LAND_HALF * 2),
                                        BasicStroke.CAP_ROUND,
                                        BasicStroke.JOIN_ROUND)
                                .createStrokedShape(line));

        for (double[] p : new double[][] {PATH[0], PATH[2], PATH[5], PATH[7], PATH[9]}) {
            walkable.add(
                    new Area(
                            new Ellipse2D.Double(
                                    p[0] - PLAZA, p[1] - PLAZA, PLAZA * 2, PLAZA * 2)));
            land.add(
                    new Area(
                            new Ellipse2D.Double(
                                    p[0] - PLAZA - 25,
                                    p[1] - PLAZA - 25,
                                    (PLAZA + 25) * 2,
                                    (PLAZA + 25) * 2)));
        }
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public Area getLand() {
        return land;
    }

    public Area getWalkable() {
        return walkable;
    }

    public double[] getStart() {
        return PATH[0];
    }

    public static double distToPath(double x, double y) {
        double best = Double.MAX_VALUE;

        for (int i = 0; i + 1 < PATH.length; i++) {
            double ax = PATH[i][0], ay = PATH[i][1], bx = PATH[i + 1][0], by = PATH[i + 1][1];
            double dx = bx - ax,
                    dy = by - ay,
                    t = ((x - ax) * dx + (y - ay) * dy) / (dx * dx + dy * dy);
            t = Math.max(0, Math.min(1, t));
            best = Math.min(best, Math.hypot(x - (ax + t * dx), y - (ay + t * dy)));
        }

        return best;
    }

    public boolean canStand(double x, double y, double r, Progress progress) {
        double[][] pts = {{0, 0}, {r, 0}, {-r, 0}, {0, r}, {0, -r}};
        for (double[] o : pts) if (!walkable.contains(x + o[0], y + o[1])) return false;

        for (Gate g : gates) {
            if (!progress.isUnlocked(g.levelIndex())
                    && Math.hypot(x - g.x(), y - g.y()) < g.radius() + r * 0.5) return false;
        }

        return true;
    }
}
