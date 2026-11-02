package main;

import javax.swing.*;

import core.GamePanel;
import ui.Display;
import util.Sfx;

public class Main {
    public static void main(String[] args) {
        Thread loader = new Thread(Sfx::preload, "sfx-preload");
        loader.setDaemon(true);
        loader.start();
        SwingUtilities.invokeLater(
                () -> {
                    JFrame f = new JFrame("Tangmo Game");
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    GamePanel panel = new GamePanel();
                    f.setContentPane(panel);
                    Display.attach(f, panel);
                    f.pack();
                    f.setResizable(true);
                    f.setMinimumSize(new java.awt.Dimension(450, 360));
                    f.setLocationRelativeTo(null);
                    f.setVisible(true);
                    f.getContentPane().requestFocusInWindow();
                });
    }
}
