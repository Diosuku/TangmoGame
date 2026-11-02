package scene;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import core.GamePanel;
import ui.Button;
import ui.Display;
import util.Art;
import util.Sfx;

public class PauseMenu {
    public enum Action {
        RESUME,
        RESTART,
        MAP,
        SOUND,
        FULLSCREEN,
        MAIN_MENU
    }

    private final Button toggle = new Button(12, 12, 92, 36, "Menu");
    private final List<Action> actions = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private boolean open;

    public PauseMenu(boolean hasRestart, boolean hasMap) {
        actions.add(Action.RESUME);
        if (hasRestart) actions.add(Action.RESTART);
        if (hasMap) actions.add(Action.MAP);
        actions.add(Action.SOUND);
        actions.add(Action.FULLSCREEN);
        actions.add(Action.MAIN_MENU);
        int y = 250;

        for (Action a : actions) {
            buttons.add(new Button(GamePanel.W / 2 - 100, y, 200, 44, label(a)));
            y += 58;
        }
    }

    private static String label(Action a) {
        return switch (a) {
            case RESUME -> "Resume";
            case RESTART -> "Restart";
            case MAP -> "Map";
            case SOUND -> soundLabel();
            case FULLSCREEN -> fullscreenLabel();
            case MAIN_MENU -> "Main Menu";
        };
    }

    private static String fullscreenLabel() {
        return Display.isFullscreen() ? "Fullscreen: ON" : "Fullscreen: OFF";
    }

    private static String soundLabel() {
        return Sfx.isEnabled() ? "Sound: ON" : "Sound: OFF";
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        open = true;
    }

    public void close() {
        open = false;
    }

    public void toggle() {
        open = !open;
    }

    public boolean toggleHit(Point p) {
        return !open && toggle.pressed(p);
    }

    public Action click(Point p) {
        for (int i = 0; i < buttons.size(); i++) {
            if (buttons.get(i).pressed(p)) {
                Action a = actions.get(i);
                if (a == Action.RESUME) close();
                if (a == Action.FULLSCREEN) Display.toggleFullscreen();
                if (a == Action.SOUND) Sfx.toggle();
                return a;
            }
        }

        return null;
    }

    public void draw(Graphics2D g, Point mouse) {
        if (!open) {
            toggle.draw(g, mouse);
            return;
        }

        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, GamePanel.W, GamePanel.H);
        int h = 100 + buttons.size() * 58;
        g.setColor(new Color(210, 130, 50));
        g.fill(new RoundRectangle2D.Double(GamePanel.W / 2.0 - 140, 170, 280, h, 24, 24));
        g.setColor(Color.WHITE);
        g.setFont(new Font("Tahoma", Font.BOLD, 28));
        Art.centerText(g, "Paused", GamePanel.W / 2, 218);

        for (int i = 0; i < buttons.size(); i++) {
            if (actions.get(i) == Action.SOUND) buttons.get(i).setLabel(soundLabel());
            if (actions.get(i) == Action.FULLSCREEN) buttons.get(i).setLabel(fullscreenLabel());
            buttons.get(i).draw(g, mouse);
        }
    }
}
