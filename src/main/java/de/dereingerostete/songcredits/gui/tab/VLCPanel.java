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
import de.dereingerostete.songcredits.gui.component.FilteredFileChooser;
import de.dereingerostete.songcredits.gui.component.SpinnerLongModel;
import de.dereingerostete.songcredits.gui.panel.TabbedPanel;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.source.manager.SourceConfigManager;
import de.dereingerostete.songcredits.source.vlc.VLCConfig;
import de.dereingerostete.songcredits.source.vlc.VLCSetup;
import de.dereingerostete.songcredits.util.GuiUtil;
import de.dereingerostete.songcredits.util.Logging;
import de.dereingerostete.songcredits.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class VLCPanel extends TabbedPanel {
    private final @NotNull VLCConfig config;
    private final JTextField hostField;
    private final FlatPasswordField passwordField;
    private final JTextField vlcPathField;
    private final JSpinner portSpinner;
    private final JSpinner refreshRateSpinner;
    private final JLabel vlcStatusLabel;

    public VLCPanel(@NotNull String name) {
        super(name);
        setLayout(null);

        SourceConfigManager manager = SongCredits.getConfigManager();
        this.config = (VLCConfig) manager.getByType(SourceType.VLC);

        JLabel hostLabel = new JLabel("Host:");
        hostLabel.setFont(GuiUtil.getDefaultFont());
        hostLabel.setBounds(10, 10, 80, 25);
        add(hostLabel);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(GuiUtil.getDefaultFont());
        passwordLabel.setBounds(10, 70, 80, 25);
        add(passwordLabel);

        hostField = new JTextField();
        hostField.setBounds(105, 10, 425, 25);
        hostField.setFont(GuiUtil.getDefaultFont());
        hostField.setText(config.getHost());
        add(hostField);

        passwordField = new FlatPasswordField();
        passwordField.setBounds(105, 70, 425, 25);
        passwordField.putClientProperty("FlatLaf.style", "showRevealButton: true");
        passwordField.setFont(GuiUtil.getDefaultFont());
        passwordField.setText(config.getPassword());
        add(passwordField);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(GuiUtil.getDefaultFont());
        portLabel.setBounds(10, 40, 90, 25);
        add(portLabel);

        portSpinner = new JSpinner();
        portSpinner.setModel(new SpinnerNumberModel(config.getPort(), 0, 65535, 1));
        portSpinner.setEditor(new JSpinner.NumberEditor(portSpinner, "0000"));
        portSpinner.setBounds(105, 40, 425, 25);
        add(portSpinner);

        long refreshRate = config.getRefreshRate();
        refreshRateSpinner = new JSpinner();
        refreshRateSpinner.setBounds(105, 100, 425, 25);
        refreshRateSpinner.setModel(new SpinnerLongModel(refreshRate, 1L, 3600000L, 1L));
        add(refreshRateSpinner);

        JLabel refreshRateLabel = new JLabel("Refresh rate:");
        refreshRateLabel.setFont(GuiUtil.getDefaultFont());
        refreshRateLabel.setBounds(10, 100, 90, 25);
        add(refreshRateLabel);

        JButton setupButton = new JButton("Configure VLC");
        setupButton.setBounds(245, 265, 120, 25);
        setupButton.addActionListener(event -> Utils.runAsync(this::configureVLC));
        add(setupButton);

        JButton validateButton = new JButton("Validate VLC Settings");
        validateButton.setBounds(370, 265, 160, 25);
        validateButton.addActionListener(event -> Utils.runAsync(() -> validateVLC(true)));
        add(validateButton);

        JButton saveButton = new JButton("Save changes");
        saveButton.setBounds(10, 130, 520, 25);
        saveButton.addActionListener(event -> Utils.runAsync(this::saveChanges));
        add(saveButton);

        JLabel vlcPathLabel = new JLabel("VLC Config:");
        vlcPathLabel.setFont(GuiUtil.getDefaultFont());
        vlcPathLabel.setBounds(10, 235, 70, 25);
        add(vlcPathLabel);

        vlcPathField = new JTextField("Failed to retrieve vlc config path");
        vlcPathField.setEditable(false);
        vlcPathField.setFont(GuiUtil.getLightFont());
        vlcPathField.setBounds(85, 235, 365, 25);
        add(vlcPathField);
        File vlcConfig = config.getConfigFile();
        if (vlcConfig != null) vlcPathField.setText(vlcConfig.getAbsolutePath());

        JSeparator separator = new JSeparator();
        separator.setBounds(10, 160, 520, 2);
        add(separator);

        JLabel vlcStatusInfoLabel = new JLabel("VLC Status:");
        vlcStatusInfoLabel.setFont(GuiUtil.getDefaultFont());
        vlcStatusInfoLabel.setBounds(10, 265, 70, 25);
        add(vlcStatusInfoLabel);

        String configuredText;
        try {
            configuredText = VLCSetup.isValid(config) ? "Configured" : "Not Configured";
        } catch (IOException exception) {
            configuredText = "Not Configured";
        }
        vlcStatusLabel = new JLabel(configuredText);
        vlcStatusLabel.setFont(GuiUtil.getLightFont());
        vlcStatusLabel.setBounds(85, 265, 100, 25);
        add(vlcStatusLabel);

        JLabel vlcSetup = new JLabel("VLC Interfaces Setup");
        vlcSetup.setFont(GuiUtil.getDefaultFont());
        vlcSetup.setBounds(10, 200, 150, 25);
        add(vlcSetup);

        JSeparator setupSeparator = new JSeparator();
        setupSeparator.setBounds(10, 225, 520, 2);
        add(setupSeparator);

        JButton selectButton = new JButton("Select");
        selectButton.setBounds(455, 235, 75, 25);
        selectButton.addActionListener(event -> Utils.runAsync(this::selectConfigFile));
        add(selectButton);
    }

    private void selectConfigFile() {
        try {
            FilteredFileChooser chooser = new FilteredFileChooser(FilteredFileChooser.FileType.VLC_CONFIG);
            File file = config.getConfigFile();
            if (file != null) chooser.setCurrentDirectory(file.getParentFile());

            int result = chooser.openDialoge(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            file = chooser.getSelectedFile();
            if (file != null) {
                config.setConfigFile(file);
                config.save();
                vlcPathField.setText(file.getAbsolutePath());
                validateVLC(false);
            }
        } catch (IOException exception) {
            Logging.warning("Failed to set vlc config path", exception);
            SongCredits.showError("setting vlc config path", exception, this);
        }
    }

    public void saveChanges() {
        try {
            MainGui.getMainGui().setLoading(true);
            config.setHost(hostField.getText());
            hostField.setText(config.getHost());

            char[] password = passwordField.getPassword();
            config.setPassword(new String(password));
            config.setPort((int) portSpinner.getValue());
            config.setRefreshRate((long) refreshRateSpinner.getValue());
            config.save();

            validateVLC(false);
            SongCredits.getSourceManager().restart(this);
            JOptionPane.showMessageDialog(this, "Changes were saved successfully",
                    "Saved changes", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            MainGui.getMainGui().setLoading(false);
            Logging.warning("Failed to save vlc config", exception);
            SongCredits.showError("saving vlc config", exception, this);
        }
    }

    public void validateVLC(boolean showDialog) {
        try {
            if (config.getConfigFile() == null) {
                showMissingConfigMessage();
                return;
            }

            MainGui.getMainGui().setLoading(true);
            boolean valid = VLCSetup.isValid(config);
            vlcStatusLabel.setText(valid ? "Configured" : "Not Configured");
            MainGui.getMainGui().setLoading(false);
            if (showDialog) {
                String message;
                if (valid) message = "VLC is configured and can be used with the current settings";
                else message = "VLC is not configured. Press 'Configure VLC' to configure";
                JOptionPane.showMessageDialog(this, message,
                        "Validate Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException exception) {
            MainGui.getMainGui().setLoading(false);
            Logging.warning("Failed to validate vlc config", exception);
            SongCredits.showError("validating vlc", exception, this);
        }
    }

    public void configureVLC() {
        try {
            if (config.getConfigFile() == null) {
                showMissingConfigMessage();
                return;
            }

            MainGui.getMainGui().setLoading(true);
            boolean valid = VLCSetup.isValid(config);
            if (valid) {
                MainGui.getMainGui().setLoading(false);
                JOptionPane.showMessageDialog(this, "VLC is already configured",
                        "VLC configured", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            VLCSetup.configureVLC(config);
            validateVLC(false);
            MainGui.getMainGui().setLoading(false);
            JOptionPane.showMessageDialog(this,
                    "VLC was successfully configured. You can now use VLC as a source.\n" +
                            "Note: If VLC was already open, it needs to be restarted",
                    "VLC configured", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException exception) {
            MainGui.getMainGui().setLoading(false);
            Logging.warning("Failed to configure vlc", exception);
            SongCredits.showError("configuration vlc", exception, this);
        }
    }

    private void showMissingConfigMessage() {
        JOptionPane.showMessageDialog(this,
                "SongCredits could not find your VLC config file. " +
                        "\nPlease enter it manually by pressing 'Select'.\n" +
                        "The config of VLC is generally called 'vlcrc'.",
                "Missing config file", JOptionPane.WARNING_MESSAGE);
    }

}
