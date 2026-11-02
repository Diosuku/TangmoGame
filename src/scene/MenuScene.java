package scene;

import java.awt.*;
import java.awt.geom.AffineTransform;

import core.GameHost;
import core.GamePanel;
import model.entity.Fruit;
import model.entity.FruitType;
import ui.Button;
import ui.Display;
import util.Art;

public class MenuScene extends Scene {
    private static final double QUIT_DELAY = 0.18;

    private final Button story = new Button(GamePanel.W / 2 - 100, 400, 200, 46, "Story Mode");
    private final Button classic = new Button(GamePanel.W / 2 - 100, 462, 200, 46, "Classic Mode");
    private final Button quit = new Button(GamePanel.W / 2 - 100, 524, 200, 46, "Quit");
    private Point mouse = new Point();
    private double quitTimer = -1;
    private double time;

    public MenuScene(GameHost host) {
        super(host);
    }

    @Override
    public void update(double dt) {
        time += dt;

        if (quitTimer >= 0) {
            quitTimer += dt;

            if (quitTimer > QUIT_DELAY) {
                quitTimer = -1;
                Display.quit();
            }
        }
    }

    @Override
    public void mouseMoved(Point p) {
        mouse = p;
    }

    @Override
    public void mousePressed(Point p) {
        if (story.pressed(p)) host.goTo(new OverworldScene(host));
        else if (classic.pressed(p)) host.goTo(new MergeScene(host, null));
        else if (quit.pressed(p) && quitTimer < 0) quitTimer = 0;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(210, 150, 90));
        g.fillRect(0, 0, GamePanel.W, GamePanel.H);
        double cx = GamePanel.W / 2.0, cy = 270;
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.rotate(Math.sin(time * 2) * 0.05);
        Fruit.paint(g, FruitType.WATERMELON, 0, 0, 80);
        g.setTransform(old);

        g.setFont(new Font("Tahoma", Font.BOLD, 46));
        Art.arcText(
                g,
                "Tangmo Game",
                cx,
                cy + 110,
                235,
                -0.07 + Math.sin(time * 2) * 0.02,
                Color.WHITE,
                new Color(120, 70, 30, 150));
        story.draw(g, mouse);
        classic.draw(g, mouse);
        quit.draw(g, mouse);
    }
}
