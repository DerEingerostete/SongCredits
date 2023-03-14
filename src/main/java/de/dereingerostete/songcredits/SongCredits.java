/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits;

import de.dereingerostete.songcredits.gui.MainGui;
import de.dereingerostete.songcredits.log.ConsoleLogger;
import de.dereingerostete.songcredits.source.manager.SourceConfigManager;
import de.dereingerostete.songcredits.source.manager.SourceManager;
import de.dereingerostete.songcredits.util.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SongCredits {
    public static final String VERSION = "1.0";
    private static @Getter File dataFolder;
    private static @Getter SourceManager sourceManager;

    private static @Getter Config config;
    private static @Getter GeneralConfig generalConfig;
    private static @Getter SourceConfigManager configManager;

    public static void main(String[] args) {
        long startMillis = System.currentTimeMillis();
        loadLogger();
        Logging.info("=====================");
        Logging.info("SongCredits");
        Logging.info("By DerEingerostete");
        Logging.info("Version " + VERSION);
        Logging.info("=====================");
        Logging.info("");
        SystemUtils.printSystemInfo();
        Logging.info("");

        try {
            Logging.info("Set data folder to " + dataFolder.getAbsolutePath());
            File file = new File(dataFolder, "config.json");
            config = new Config(file);

            Logging.setDebug(config.getBoolean("debug"));
            Logging.debug("Loaded config " + file.getPath());
            generalConfig = new GeneralConfig();
        } catch (IOException exception) {
            Logging.severe("Failed to load config", exception);
            showError("while loading the config", exception, null);
            System.exit(1);
        }

        loadShutdownThread();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logging.warning("[Thread-" + thread.getName() + "] Unexpectedly thrown a exception", throwable);
            showError(throwable, null);
        });
        Logging.debug("Exception handler set up");

        Logging.debug("Loading GUI components");
        GuiUtil.updateLookAndFell();
        UIManager.put("TabbedPane.showTabSeparators", true);
        MainGui.showMainFrame(() -> {
            Logging.info("");
            Logging.info("Running async setup tasks");
            Logging.info("Loading source configs");
            configManager = new SourceConfigManager();

            Logging.info("Loading source manager");
            sourceManager = new SourceManager();
            MainGui.getMainGui().loadTabs();
            return sourceManager;
        });

        double duration = (System.currentTimeMillis() - startMillis) / 1000.0;
        Logging.info("Startup completed (Took: " + duration + "s)");
    }

    private static void loadLogger() {
        dataFolder = SystemUtils.getOS().resolveConfigDirectory();
        String folderPath = dataFolder.getAbsolutePath();

        if (!dataFolder.exists() && !dataFolder.mkdir()) {
            System.err.println("Failed to create dataFolder: " + dataFolder.getPath());
            JOptionPane.showMessageDialog(null, "Failed to create config folder\n" +
                            "Please make sure that the program has access\n" +
                            " to the following folder or create it yourself:\n"
                            + folderPath.replaceAll("/", "\\\\"),
                    "Error during startup", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        try {
            ConsoleLogger logger = new ConsoleLogger("SongCredits", null);
            Logging.init(logger);
        } catch (IOException exception) {
            System.err.println("Failed to create logger");
            exception.printStackTrace();
        }
    }

    private static void loadShutdownThread() {
        Thread thread = new Thread(() -> {
            Logging.debug("Performing closing actions");
            sourceManager.close();

            Logging.info("Goodbye");
        }, "ShutdownThread");
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public static void showError(@Nullable Throwable throwable, @Nullable Component component) {
        JOptionPane.showMessageDialog(component, "An unexpected error occurred!\n" +
                        "Error: " + Utils.getCause(throwable),
                "Unexpected error", JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(@NotNull String message, @Nullable Throwable throwable, @Nullable Component component) {
        JOptionPane.showMessageDialog(component, "An unexpected error occurred while " + message +
                        "\nError: " + Utils.getCause(throwable),
                "Unexpected error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(@NotNull Component component, @NotNull String message) {
        JOptionPane.showMessageDialog(component, message, "Error", JOptionPane.WARNING_MESSAGE);
    }

}
