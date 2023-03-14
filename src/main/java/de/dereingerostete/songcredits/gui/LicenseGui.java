/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.util.GuiUtil;
import de.dereingerostete.songcredits.util.Logging;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LicenseGui {
    private static LicenseGui gui;
    protected final List<Dependency> dependencies;
    protected final JFrame frame;
    protected final JTable table;

    public static void showLicenseGui() {
        EventQueue.invokeLater(() -> {
            try {
                if (gui != null && gui.isOpen()) return;
                gui = new LicenseGui();
                gui.setVisible(true);
            } catch (IOException exception) {
                Logging.warning("Failed to open license gui", exception);
                SongCredits.showError("opening the license window", exception, null);
            }
        });
    }

    public LicenseGui() throws IOException {
        dependencies = Dependency.loadDependencies();
        frame = new JFrame("Third-party licenses");
        frame.setBounds(100, 100, 490, 425);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(GuiUtil.buildWindowListener(frame, false));
        frame.setLayout(null);
        frame.setResizable(false);

        JEditorPane thanksPane = new JEditorPane();
        thanksPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        thanksPane.setText("A big thank you to all the open source projects that made this tool possible. " +
                "Their names and licenses are listed below.<br>" +
                "<br>Additionally, a big thank you to the programmer of the OBS plugin Tuna universallp, " +
                "who allowed me to base my tools GUI on his plugin. " +
                "<a href=\"https://obsproject.com/forum/resources/tuna.843/\">(Link)</a>");
        thanksPane.setBounds(10, 10, frame.getWidth() - 36, 100);
        thanksPane.setFont(GuiUtil.getLightFont());
        thanksPane.setEditable(false);
        thanksPane.addHyperlinkListener(event -> {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                openUrl(event.getURL().toString());
        });
        frame.add(thanksPane);

        table = new JTable();
        table.setModel(Dependency.createModel(dependencies));
        table.getColumnModel().getColumn(0).setResizable(true);
        table.setShowGrid(false);
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent event) {
                if (!SwingUtilities.isLeftMouseButton(event)) return;
                Point point = event.getPoint();
                int row = table.rowAtPoint(point);
                int colum = table.columnAtPoint(point);
                if (row < 0 || colum < 0 || dependencies.size() - 1 < row) return;

                Dependency dependency = dependencies.get(row);
                String urlString = colum == 0 ? dependency.getProjectUrl() : dependency.getLicenseUrl();
                openUrl(urlString);
            }

        });

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 120, frame.getWidth() - 36, 230);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(table);
        frame.add(scrollPane);

        JLabel clickInfoLabel = new JLabel("Left click on an entry to open the url in a browser");
        clickInfoLabel.setBounds(10, 355, 350, 25);
        clickInfoLabel.setFont(GuiUtil.getLightFont());
        frame.add(clickInfoLabel);

        JButton closeButton = new JButton("Close");
        closeButton.setBounds(scrollPane.getWidth() - 80, 355, 90, 25);
        closeButton.addActionListener(event -> frame.dispose());
        frame.add(closeButton);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public boolean isOpen() {
        return frame.isDisplayable();
    }

    protected void openUrl(@NotNull String urlString) {
        try {
            if (!Desktop.isDesktopSupported()) throw new IOException("Desktop not supported");
            URL url = new URL(urlString);
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(url.toURI());
        } catch (IOException | URISyntaxException exception) {
            Logging.warning("Failed to open url in browser: " + urlString, exception);
            JOptionPane.showMessageDialog(frame,
                    "Link could not be opened in browser!\nLink: " + urlString,
                    "Unexpected error", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Data
    public static class Dependency {
        protected final @NotNull String name;
        protected final @NotNull String license;
        protected final @NotNull String licenseUrl;
        protected final @NotNull String projectUrl;

        @NotNull
        public static Dependency fromJson(@NotNull JSONObject object) {
            String name = object.getString("name");
            String license = object.getString("license");
            String licenseUrl = object.getString("licenseUrl");
            String projectUrl = object.getString("projectUrl");
            return new Dependency(name, license, licenseUrl, projectUrl);
        }

        @NotNull
        public static List<Dependency> loadDependencies() throws IOException {
            List<Dependency> dependencies = new ArrayList<>();
            String string = IOUtils.toString(GuiUtil.getInputStream("licenses.json"), StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(string);
            array.forEach(object -> {
                try {
                    if (!(object instanceof JSONObject)) return;
                    Dependency dependency = fromJson((JSONObject) object);
                    dependencies.add(dependency);
                } catch (JSONException exception) {
                    Logging.warning("Failed to load dependency: " + object, exception);
                }
            });
            return dependencies;
        }

        @NotNull
        public static TableModel createModel(@NotNull List<Dependency> dependencies) {
            Object[][] data = new Object[dependencies.size()][2];
            for (int i = 0; i < dependencies.size(); i++) {
                Dependency dependency = dependencies.get(i);
                data[i][0] = dependency.getName();
                data[i][1] = dependency.getLicense();
            }

            String[] names = {"Software", "License"};
            return new DefaultTableModel(data, names) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

            };
        }

    }

}