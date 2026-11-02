package core;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import map.Progress;
import scene.MenuScene;
import scene.Scene;
import ui.Display;

public class GamePanel extends JPanel implements ActionListener, GameHost {
    public static final int W = 900, H = 720;

    private final ScoreManager scoreManager = new ScoreManager();
    private final Progress progress = new Progress();
    private Scene scene;
    private long last = System.nanoTime();

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        scene = new MenuScene(this);

        MouseAdapter ma =
                new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        scene.mouseMoved(toGame(e.getPoint()));
                    }

                    @Override
                    public void mouseDragged(MouseEvent e) {
                        scene.mouseMoved(toGame(e.getPoint()));
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        requestFocusInWindow();
                        scene.mousePressed(toGame(e.getPoint()));
                    }
                };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        boolean altEnter = e.getKeyCode() == KeyEvent.VK_ENTER && e.isAltDown();

                        if (e.getKeyCode() == KeyEvent.VK_F11 || altEnter) {
                            Display.toggleFullscreen();
                            return;
                        }

                        scene.keyPressed(e.getKeyCode());
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        scene.keyReleased(e.getKeyCode());
                    }
                });
        new Timer(16, this).start();
    }

    @Override
    public void goTo(Scene s) {
        scene = s;
    }

    @Override
    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    @Override
    public Progress getProgress() {
        return progress;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = Math.min(0.033, (now - last) / 1e9);
        last = now;
        scene.update(dt);
        repaint();
    }

    private double scale() {
        return Math.max(0.1, Math.min(getWidth() / (double) W, getHeight() / (double) H));
    }

    private double offsetX() {
        return (getWidth() - W * scale()) / 2;
    }

    private double offsetY() {
        return (getHeight() - H * scale()) / 2;
    }

    Point toGame(Point p) {
        return new Point(
                (int) Math.round((p.x - offsetX()) / scale()),
                (int) Math.round((p.y - offsetY()) / scale()));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.translate(offsetX(), offsetY());
        g.scale(scale(), scale());
        g.setClip(0, 0, W, H);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        scene.draw(g);
    }
}
