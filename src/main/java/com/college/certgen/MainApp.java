package com.college.certgen;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class MainApp {
    public static void main(String[] args) {
        // macOS system menu bar & app title optimizations
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "College Certificate Management System");
        System.setProperty("apple.awt.application.appearance", "system");

        // High DPI font rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Setup FlatLaf modern UI
                FlatLightLaf.setup();

                // Global font configuration for clean modern appearance
                UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
                UIManager.put("Button.arc", 8);
                UIManager.put("Component.arc", 8);
                UIManager.put("ProgressBar.arc", 8);
                UIManager.put("TextComponent.arc", 8);
            } catch (Exception e) {
                System.err.println("Could not initialize FlatLaf theme, falling back to system LAF.");
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
            }

            // Warm up DatabaseService (creates default tables & seeds initial sample data)
            DatabaseService.getInstance();

            // Launch Login Interface
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

