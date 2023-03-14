/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.gui.component.MediaControlPanel;
import de.dereingerostete.songcredits.gui.panel.LoadingPanel;
import de.dereingerostete.songcredits.gui.panel.TabbedPanel;
import de.dereingerostete.songcredits.gui.tab.AboutPanel;
import de.dereingerostete.songcredits.gui.tab.GeneralPanel;
import de.dereingerostete.songcredits.gui.tab.SpotifyPanel;
import de.dereingerostete.songcredits.gui.tab.VLCPanel;
import de.dereingerostete.songcredits.source.AudioSource;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.source.manager.SourceManager;
import de.dereingerostete.songcredits.util.GuiUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class MainGui {
    private static @Getter MainGui mainGui;
    private final @Getter JFrame frame;
    private final JTabbedPane tabbedPane;
    private final LoadingPanel loadingPanel;
    private final @Getter MediaControlPanel musicControlPanel;

    private @Getter GeneralPanel generalPanel;
    private @Getter SpotifyPanel spotifyPanel;

    public static void showMainFrame(Supplier<SourceManager> supplier) {
        EventQueue.invokeLater(() -> {
            mainGui = new MainGui();
            mainGui.setVisible(true);
            mainGui.setLoading(true);

            ExecutorService service = Executors.newFixedThreadPool(1);
            service.submit(() -> {
                SourceManager manager = supplier.get();
                AudioSource source = manager.getAudioSource();
                SourceType type = source == null ? SourceType.VLC : source.getType();
                mainGui.generalPanel.setActiveSource(type);

                mainGui.musicControlPanel.setSource(source);
                manager.start(mainGui.frame);
                mainGui.generalPanel.setRunningStatus(manager.isRunning());
                mainGui.setLoading(false);
            });
        });
    }

    public MainGui() {
        frame = new JFrame("SongCredits");
        frame.setResizable(false);
        frame.setBounds(100, 100, 580, 650);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(GuiUtil.buildWindowListener(frame, true));
        frame.setLayout(null);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(10, 0, 545, 530);
        frame.add(tabbedPane);

        loadingPanel = new LoadingPanel();
        loadingPanel.setBounds(10, 0, 545, 510);
        loadingPanel.setEnabled(false);
        loadingPanel.setVisible(false);
        frame.add(loadingPanel);

        musicControlPanel = new MediaControlPanel();
        musicControlPanel.setBounds(10, 530, 545, 75);
        musicControlPanel.setEnabled(false);
        frame.add(musicControlPanel);
    }

    public void loadTabs() {
        generalPanel = new GeneralPanel("General", SongCredits.getGeneralConfig());
        addTab(generalPanel);

        spotifyPanel = new SpotifyPanel("Spotify");
        addTab(spotifyPanel);

        addTab(new VLCPanel("VLC"));
        addTab(new AboutPanel("About"));
    }

    public void addTab(@NotNull TabbedPanel tabbedPanel) {
        tabbedPanel.init(frame);
        tabbedPane.addTab(tabbedPanel.getName(), tabbedPanel);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void setLoading(boolean loading) {
        loadingPanel.setVisible(loading);
        loadingPanel.setEnabled(loading);
        tabbedPane.setVisible(!loading);
        tabbedPane.setEnabled(!loading);
        musicControlPanel.setLoading(loading);
    }

    public static MediaControlPanel getMediaControl() {
        return getMainGui().getMusicControlPanel();
    }

    public static GeneralPanel getGeneral() {
        return getMainGui().getGeneralPanel();
    }

}
