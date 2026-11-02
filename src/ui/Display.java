package ui;

import java.awt.*;
import javax.swing.*;

public final class Display {
    private static JFrame frame;
    private static Component focusTarget;
    private static boolean fullscreen;
    private static Rectangle windowBounds;
    private static Runnable exitAction = () -> System.exit(0);

    private Display() {}

    public static void attach(JFrame f, Component focus) {
        frame = f;
        focusTarget = focus;
    }

    public static void setExitAction(Runnable r) {
        exitAction = r;
    }

    public static void quit() {
        try {
            if (frame != null) {
                GraphicsDevice gd =
                        frame.getGraphicsConfiguration() != null
                                ? frame.getGraphicsConfiguration().getDevice()
                                : GraphicsEnvironment.getLocalGraphicsEnvironment()
                                        .getDefaultScreenDevice();
                if (gd.getFullScreenWindow() == frame) gd.setFullScreenWindow(null);
                fullscreen = false;
                frame.dispose();
            }
        } catch (RuntimeException e) {
            System.err.println("[Display] Error while closing window: " + e.getMessage());
        }

        exitAction.run();
    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static void toggleFullscreen() {
        if (frame == null) return;
        GraphicsDevice gd =
                frame.getGraphicsConfiguration() != null
                        ? frame.getGraphicsConfiguration().getDevice()
                        : GraphicsEnvironment.getLocalGraphicsEnvironment()
                                .getDefaultScreenDevice();

        try {
            if (!fullscreen) {
                windowBounds = frame.getBounds();
                frame.dispose();
                frame.setUndecorated(true);

                if (gd.isFullScreenSupported()) {
                    gd.setFullScreenWindow(frame);
                } else {
                    frame.setBounds(gd.getDefaultConfiguration().getBounds());
                    frame.setVisible(true);
                }

                fullscreen = true;
            } else {
                if (gd.getFullScreenWindow() == frame) gd.setFullScreenWindow(null);
                frame.dispose();
                frame.setUndecorated(false);
                frame.setBounds(windowBounds);
                frame.setVisible(true);
                fullscreen = false;
            }
        } catch (RuntimeException e) {
            System.err.println("[Display] Cannot switch fullscreen mode: " + e.getMessage());
        }

        if (focusTarget != null) SwingUtilities.invokeLater(focusTarget::requestFocusInWindow);
    }
}
