/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.tab;

import com.formdev.flatlaf.extras.components.FlatPasswordField;
import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.gui.MainGui;
import de.dereingerostete.songcredits.gui.component.SpinnerLongModel;
import de.dereingerostete.songcredits.gui.panel.TabbedPanel;
import de.dereingerostete.songcredits.source.AudioSource;
import de.dereingerostete.songcredits.source.manager.SourceConfigManager;
import de.dereingerostete.songcredits.source.SourceException;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.source.manager.SourceManager;
import de.dereingerostete.songcredits.source.spotify.SpotifyConfig;
import de.dereingerostete.songcredits.source.spotify.SpotifySource;
import de.dereingerostete.songcredits.util.GuiUtil;
import de.dereingerostete.songcredits.util.Logging;
import de.dereingerostete.songcredits.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

public class SpotifyPanel extends TabbedPanel {
    private final @NotNull SpotifyConfig config;
    private final @NotNull FlatPasswordField clientIdField;
    private final @NotNull FlatPasswordField clientSecretField;
    private final @NotNull FlatPasswordField accessTokenField;
    private final @NotNull JSpinner refreshRateSpinner;

    public SpotifyPanel(@NotNull String name) {
        super(name);
        setLayout(null);

        SourceConfigManager manager = SongCredits.getConfigManager();
        this.config = (SpotifyConfig) manager.getByType(SourceType.SPOTIFY);

        JLabel clientIdLabel = new JLabel("Client Id:");
        clientIdLabel.setFont(GuiUtil.getDefaultFont());
        clientIdLabel.setBounds(10, 10, 80, 25);
        add(clientIdLabel);

        JLabel clientSecretLabel = new JLabel("Client secret:");
        clientSecretLabel.setFont(GuiUtil.getDefaultFont());
        clientSecretLabel.setBounds(10, 40, 80, 25);
        add(clientSecretLabel);

        clientIdField = new FlatPasswordField();
        clientIdField.setBounds(105, 10, 425, 25);
        clientIdField.putClientProperty("FlatLaf.style", "showRevealButton: true");
        clientIdField.setFont(GuiUtil.getDefaultFont());
        clientIdField.setText(config.getClientId() == null ? "" : config.getClientId());
        add(clientIdField);

        clientSecretField = new FlatPasswordField();
        clientSecretField.setBounds(105, 40, 425, 25);
        clientSecretField.putClientProperty("FlatLaf.style", "showRevealButton: true");
        clientSecretField.setFont(GuiUtil.getDefaultFont());
        clientSecretField.setText(config.getClientSecret() == null ? "" : config.getClientSecret());
        add(clientSecretField);

        JLabel accessTokenLabel = new JLabel("Access token:");
        accessTokenLabel.setFont(GuiUtil.getDefaultFont());
        accessTokenLabel.setBounds(10, 70, 90, 25);
        add(accessTokenLabel);

        accessTokenField = new FlatPasswordField();
        accessTokenField.setEditable(false);
        accessTokenField.setFont(GuiUtil.getDefaultFont());
        accessTokenField.setBounds(105, 70, 425, 25);
        accessTokenField.putClientProperty("FlatLaf.style", "showRevealButton: true");
        accessTokenField.setText(config.getAccessToken());
        add(accessTokenField);

        JLabel refreshTokenLabel = new JLabel("Refresh token:");
        refreshTokenLabel.setFont(GuiUtil.getDefaultFont());
        refreshTokenLabel.setBounds(10, 100, 90, 25);
        add(refreshTokenLabel);

        FlatPasswordField refreshTokenField = new FlatPasswordField();
        refreshTokenField.setEditable(false);
        refreshTokenField.setFont(GuiUtil.getDefaultFont());
        refreshTokenField.setBounds(105, 100, 425, 25);
        refreshTokenField.setText(config.getRefreshToken());
        add(refreshTokenField);

        JLabel refreshRateLabel = new JLabel("Refresh rate:");
        refreshRateLabel.setFont(GuiUtil.getDefaultFont());
        refreshRateLabel.setBounds(10, 135, 90, 25);
        add(refreshRateLabel);

        long refreshRate = config.getRefreshRate();
        refreshRateSpinner = new JSpinner();
        refreshRateSpinner.setModel(new SpinnerLongModel(refreshRate, 1L, 3600000L, 1L));
        refreshRateSpinner.setBounds(105, 135, 425, 25);
        add(refreshRateSpinner);

        JButton connectButton = new JButton("Connect with Spotify Account");
        connectButton.addActionListener(event -> Utils.runAsync(this::login));
        connectButton.setBounds(10, 170, 520, 25);
        add(connectButton);

        JButton refreshButton = new JButton("Refresh authorization");
        refreshButton.addActionListener(event -> Utils.runAsync(this::refreshAuthorization));
        refreshButton.setBounds(10, 200, 520, 25);
        add(refreshButton);

        JButton saveButton = new JButton("Save changes");
        saveButton.addActionListener(event -> Utils.runAsync(this::saveChanges));
        saveButton.setBounds(10, 230, 520, 25);
        add(saveButton);
    }

    public void login() {
        MainGui.getMainGui().setLoading(true);
        try {
            SourceManager manager = SongCredits.getSourceManager();
            AudioSource source = manager.getAudioSource();
            if (source instanceof SpotifySource) {
                ((SpotifySource) source).login();
                MainGui.getMainGui().setLoading(false);
                return;
            }

            SourceConfigManager configManager = SongCredits.getConfigManager();
            SpotifyConfig spotifyConfig = (SpotifyConfig) configManager.getByType(SourceType.SPOTIFY);
            SpotifySource spotifySource = new SpotifySource(spotifyConfig);
            spotifySource.login();
            spotifySource.close();
        } catch (Exception exception) {
            Logging.warning("Failed to login", exception);
            SongCredits.showError("logging in", exception, this);
        }
        MainGui.getMainGui().setLoading(false);
    }

    public void refreshAuthorization() {
        try {
            MainGui.getMainGui().setLoading(true);
            SourceManager manager = SongCredits.getSourceManager();
            manager.stop();

            AudioSource source = manager.getAudioSource();
            if (source != null) source.authorize();

            manager.start(this);
            MainGui.getMainGui().setLoading(false);
        } catch (SourceException exception) {
            Logging.warning("Failed to refresh audio source", exception);
            SongCredits.showError("refreshing authorization", exception, this);
        }
    }

    public void saveChanges() {
        String clientId = new String(clientIdField.getPassword());
        config.setClientId(clientId);

        String clientSecret = new String(clientSecretField.getPassword());
        config.setClientSecret(clientSecret);

        long refreshRate = (int) refreshRateSpinner.getValue();
        config.setRefreshRate(refreshRate);

        try {
            MainGui.getMainGui().setLoading(true);
            config.save();
            SourceManager manager = SongCredits.getSourceManager();
            manager.updateRefreshRate(refreshRate);
            MainGui.getMainGui().setLoading(false);
            SongCredits.getSourceManager().restart(this);
            JOptionPane.showMessageDialog(this, "Changes were saved successfully",
                    "Saved changes", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            Logging.warning("Failed to save spotify config", exception);
            SongCredits.showError("saving spotify changes", exception, this);
        }
    }

    public void setAccessToken(@NotNull String accessToken) {
        accessTokenField.setText(accessToken);
    }

}
