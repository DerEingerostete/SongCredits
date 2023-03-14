/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.tab;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.gui.LicenseGui;
import de.dereingerostete.songcredits.gui.panel.TabbedPanel;
import de.dereingerostete.songcredits.util.GuiUtil;
import de.dereingerostete.songcredits.util.Logging;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class AboutPanel extends TabbedPanel {

    @SneakyThrows(IOException.class)
    public AboutPanel(@NotNull String name) {
        super(name);
        InputStream inputStream = GuiUtil.getInputStream("about.html");
        String htmlDocument = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        setLayout(null);
        JLabel titleLabel = new JLabel("SongCredits");
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(10, 11, 520, 35);
        add(titleLabel);

        JSeparator separator = new JSeparator();
        separator.setBorder(new LineBorder(separator.getForeground(), 5));
        separator.setBounds(10, 50, 520, 2);
        add(separator);

        JTextPane textPane = new JTextPane();
        textPane.setOpaque(false);
        textPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        textPane.setText(htmlDocument);
        textPane.setEditable(false);
        textPane.setBounds(10, 60, 520, 150);
        textPane.addHyperlinkListener(this::openHyperlink);
        add(textPane);

        JButton licensesButton = new JButton("Third-party licenses and special thanks");
        licensesButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        licensesButton.setBounds(126, 450, 290, 35);
        licensesButton.addActionListener(event -> LicenseGui.showLicenseGui());
        add(licensesButton);

        JButton configButton = new JButton("Open config folder");
        configButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        configButton.setBounds(126, 405, 140, 35);
        configButton.addActionListener(event -> openFile(SongCredits.getDataFolder(), "data folder"));
        add(configButton);

        JButton showLogButton = new JButton("View latest log");
        showLogButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showLogButton.setBounds(276, 405, 140, 35);
        showLogButton.addActionListener(event -> openFile(Logging.getLogFile(), "log file"));
        add(showLogButton);
    }

    private void openHyperlink(@NotNull HyperlinkEvent event) {
        if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
        else if (!Desktop.isDesktopSupported()) {
            Logging.warning("Failed to open url: Desktop API is not supported");
            SongCredits.showWarning(this, "Failed to open url: Desktop API is not supported");
            return;
        }

        try {
            URL url = event.getURL();
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(url.toURI());
        } catch (IOException | URISyntaxException exception) {
            Logging.warning("Failed to open url", exception);
            SongCredits.showError("opening url", exception, this);
        }
    }

    private void openFile(@NotNull File file, @NotNull String action) {
        if (!Desktop.isDesktopSupported()) {
            Logging.warning("Failed to open " + action + ": Desktop API is not supported");
            SongCredits.showWarning(this, "Failed to open " +
                    action + ": Desktop API is not supported");
            return;
        }

        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(file);
        } catch (IOException exception) {
            Logging.warning("Failed to open file with default editor", exception);
            SongCredits.showError("opening " + action, exception, this);
        }
    }

}
