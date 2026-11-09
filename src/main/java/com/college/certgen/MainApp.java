package com.college.certgen;

import com.college.certgen.database.DatabaseService;
import com.college.certgen.ui.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainApp {
    private static File logFile = null;

    public static void main(String[] args) {
        initLogging();
        log("=== College Certificate Management System Starting ===");
        log("Java Version: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        log("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
        log("User Home: " + System.getProperty("user.home"));
        log("Working Dir: " + System.getProperty("user.dir"));

        // Global uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logError("Uncaught Exception on thread [" + thread.getName() + "]", throwable);
            showErrorDialog("An unexpected error occurred in College Certificate Management System:\n\n"
                    + throwable.getClass().getSimpleName() + ": " + throwable.getMessage()
                    + "\n\nA diagnostic log has been written to:\n"
                    + (logFile != null ? logFile.getAbsolutePath() : "startup_crash.log"));
        });

        // macOS system menu bar & app title optimizations
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "College Certificate Management System");
            System.setProperty("apple.awt.application.appearance", "system");
        } catch (Throwable ignored) {}

        // High DPI font rendering
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Throwable ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Setup FlatLaf modern UI
                try {
                    FlatLightLaf.setup();
                    UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
                    UIManager.put("Button.arc", 8);
                    UIManager.put("Component.arc", 8);
                    UIManager.put("ProgressBar.arc", 8);
                    UIManager.put("TextComponent.arc", 8);
                    log("FlatLaf modern theme initialized successfully.");
                } catch (Throwable t) {
                    log("Warning: FlatLaf setup failed (" + t.getMessage() + "), falling back to system Look & Feel.");
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Throwable ignored) {}
                }

                // 2. Warm up DatabaseService (creates default tables & seeds initial sample data)
                log("Initializing database service...");
                try {
                    DatabaseService.getInstance();
                    log("Database service initialized successfully.");
                } catch (Throwable t) {
                    logError("Warning: Database initialization encountered an issue", t);
                }

                // 3. Launch Login Interface
                log("Launching LoginFrame interface...");
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                log("LoginFrame displayed successfully.");

            } catch (Throwable t) {
                logError("Fatal error during GUI initialization", t);
                showErrorDialog("Unable to start College Certificate Management System:\n\n"
                        + t.getClass().getSimpleName() + ": " + t.getMessage()
                        + "\n\nPlease check the log file:\n"
                        + (logFile != null ? logFile.getAbsolutePath() : "startup_crash.log"));
            }
        });
    }

    private static void initLogging() {
        try {
            // First try user home directory so it's always writable on Windows
            String userHome = System.getProperty("user.home");
            File appDir = new File(userHome, ".college_cert_gen");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            logFile = new File(appDir, "app_startup.log");
        } catch (Exception e) {
            try {
                logFile = new File("app_startup.log");
            } catch (Exception ignored) {}
        }
    }

    public static synchronized void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String entry = "[" + timestamp + "] " + message;
        System.out.println(entry);
        if (logFile != null) {
            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(entry);
            } catch (Exception ignored) {}
        }
    }

    public static synchronized void logError(String message, Throwable t) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String entry = "[" + timestamp + "] [ERROR] " + message;
        System.err.println(entry);
        if (t != null) {
            t.printStackTrace(System.err);
        }
        if (logFile != null) {
            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.println(entry);
                if (t != null) {
                    t.printStackTrace(pw);
                }
            } catch (Exception ignored) {}
        }
    }

    private static void showErrorDialog(String message) {
        try {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "College Certificate Management System - Startup Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Throwable ignored) {}
    }
}


