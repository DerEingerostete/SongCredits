/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.util;

import com.formdev.flatlaf.FlatDarkLaf;
import de.dereingerostete.songcredits.SongCredits;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Objects;

public class GuiUtil {
    private static final Font DEFAULT_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    private static final Font LIGHT_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    @NotNull
    public static Font getDefaultFont() {
        return DEFAULT_FONT;
    }

    @NotNull
    public static Font getLightFont() {
        return LIGHT_FONT;
    }

    public static void updateLookAndFell() {
        try {
            EventQueue.invokeAndWait(() -> {
                try {
                    FlatDarkLaf darkLaf = new FlatDarkLaf();
                    UIManager.setLookAndFeel(darkLaf);
                    Logging.debug("Updated look and feel to " + darkLaf.getName());
                } catch (UnsupportedLookAndFeelException exception) {
                    Logging.warning("Failed to set LookAndFeel", exception);
                }
            });
        } catch (InterruptedException | InvocationTargetException exception) {
            Logging.warning("EventQueue was interrupted while setting LookAndFeel", exception);
        }
    }

    @Nullable
    public static ImageIcon loadIcon(@NotNull String key) {
        try {
            ClassLoader loader = GuiUtil.class.getClassLoader();
            URL url = loader.getResource("assets/icons/" + key);
            if (url == null) throw new IOException("Resource not found");
            return new ImageIcon(url);
        } catch (IOException exception) {
            Logging.warning("Failed to load icon '" + key + '\'', exception);
            return null;
        }
    }

    @SuppressWarnings("resource")
    @NotNull
    public static InputStream getInputStream(@NotNull String key) {
        ClassLoader loader = GuiUtil.class.getClassLoader();
        InputStream inputStream = loader.getResourceAsStream("assets/" + key);
        return Objects.requireNonNull(inputStream, "Resource '" + key + "' not found");
    }

    @NotNull
    public static URL getResourceAsURL(@NotNull String key) {
        ClassLoader loader = GuiUtil.class.getClassLoader();
        URL url = loader.getResource("assets/" + key);
        return Objects.requireNonNull(url, "Resource '" + key + "' not found");
    }

    @NotNull
    public static WindowListener buildWindowListener(@NotNull JFrame frame, boolean saveLocation) {
        return new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent event) {
                if (!saveLocation) return;
                Config config = SongCredits.getConfig();
                JSONObject object = new JSONObject();
                object.put("x", frame.getX());
                object.put("y", frame.getY());
                config.set("window", object);
                config.saveQuietly();
            }

            @Override
            public void windowOpened(WindowEvent event) {
                Config config = SongCredits.getConfig();
                JSONObject object = config.getJSONObject("window");
                if (object == null) return;
                int x = object.optInt("x", -1);
                int y = object.optInt("y", -1);
                if (x > 0 && y > 0) frame.setLocation(x, y);
            }

        };
    }

}
