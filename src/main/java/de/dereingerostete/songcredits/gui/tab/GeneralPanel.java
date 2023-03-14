/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.tab;

import com.formdev.flatlaf.extras.components.FlatComboBox;
import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.gui.MainGui;
import de.dereingerostete.songcredits.gui.OutputDialoge;
import de.dereingerostete.songcredits.gui.OutputDialoge.DefaultCloseAction;
import de.dereingerostete.songcredits.gui.component.FilteredFileChooser;
import de.dereingerostete.songcredits.gui.component.OutputTable;
import de.dereingerostete.songcredits.gui.component.PlaceholderSaver;
import de.dereingerostete.songcredits.gui.panel.TabbedPanel;
import de.dereingerostete.songcredits.song.SongFormatter;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.source.manager.SourceManager;
import de.dereingerostete.songcredits.util.GeneralConfig;
import de.dereingerostete.songcredits.util.GuiUtil;
import de.dereingerostete.songcredits.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

public class GeneralPanel extends TabbedPanel {
    private final JTextField coverPathField;
    private final OutputTable outputsTable;
    private final JTextField defaultCoverField;
    private final FlatComboBox<String> activeSourceList;
    private final JButton defaultCoverSelect;

    private final JLabel statusLabel;
    private final JButton startButton;
    private final GeneralConfig config;
    private boolean comboBoxFired;

    public GeneralPanel(@NotNull String name, @NotNull GeneralConfig config) {
        super(name);
        setLayout(null);
        this.config = config;

        JLabel coverPathLabel = new JLabel("Song cover path");
        coverPathLabel.setFont(GuiUtil.getDefaultFont());
        coverPathLabel.setBounds(10, 10, 100, 20);
        add(coverPathLabel);

        coverPathField = new JTextField();
        coverPathField.setEditable(false);
        coverPathField.setFont(GuiUtil.getDefaultFont());
        coverPathField.setBounds(120, 10, 310, 20);
        coverPathField.setText(config.getCoverFile() == null ? "" : config.getCoverFile().getAbsolutePath());
        coverPathField.setColumns(1);
        add(coverPathField);

        JButton coverSelect = new JButton("Select");
        coverSelect.setFont(GuiUtil.getDefaultFont());
        coverSelect.setBounds(440, 10, 90, 20);
        coverSelect.addActionListener(event -> handleCoverSelect(file -> {
            coverPathField.setText(file.getAbsolutePath());
            config.setCoverFile(file);
        }));
        add(coverSelect);

        JCheckBox downloadCoverCheckbox = new JCheckBox("Search for missing cover on Itunes with resolution");
        downloadCoverCheckbox.setFont(GuiUtil.getDefaultFont());
        downloadCoverCheckbox.setSelected(config.isResolveCover());
        downloadCoverCheckbox.setBounds(10, 40, 330, 20);
        downloadCoverCheckbox.addActionListener(event -> handleCoverHandle(downloadCoverCheckbox.isSelected()));
        add(downloadCoverCheckbox);

        FlatComboBox<String> coverSizeList = new FlatComboBox<>();
        coverSizeList.setModel(new DefaultComboBoxModel<>(new String[] {"512x512", "256x256", "64x64", "16x16"}));
        coverSizeList.setFont(GuiUtil.getDefaultFont());
        coverSizeList.setSelectedItem(config.getCoverResolution());
        coverSizeList.setBounds(360, 40, 165, 20);
        coverSizeList.addActionListener(event -> handleCoverSize(coverSizeList.getSelectedItem()));
        coverSizeList.setRoundRect(true);
        add(coverSizeList);

        JPanel outputsInfoPanel = new RoundedPanel();
        outputsInfoPanel.setBounds(10, 70, 520, 280);
        outputsInfoPanel.setLayout(null);
        add(outputsInfoPanel);

        JLabel infoLabel = new JLabel("Song info outputs");
        infoLabel.setFont(GuiUtil.getDefaultFont());
        infoLabel.setBounds(10, 11, 130, 20);
        outputsInfoPanel.add(infoLabel);

        JButton editButton = new JButton("Edit");
        editButton.setFont(GuiUtil.getDefaultFont());
        editButton.setBounds(10, 245, 90, 25);
        editButton.addActionListener(event -> handleEdit());
        outputsInfoPanel.add(editButton);

        JButton removeButton = new JButton("Remove");
        removeButton.setFont(GuiUtil.getDefaultFont());
        removeButton.setBounds(110, 246, 90, 25);
        removeButton.addActionListener(event -> handleRemove());
        outputsInfoPanel.add(removeButton);

        JButton addButton = new JButton("Add");
        addButton.setFont(GuiUtil.getDefaultFont());
        addButton.setBounds(210, 246, 90, 25);
        addButton.addActionListener(event -> handleAddOutput());
        outputsInfoPanel.add(addButton);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(10, 40, 500, 200);
        outputsInfoPanel.add(scrollPane);

        outputsTable = new OutputTable(config.getFormatters());
        outputsTable.setFont(GuiUtil.getDefaultFont());
        scrollPane.setViewportView(outputsTable);

        JLabel placeholderLabel = new JLabel("Placeholder Text");
        placeholderLabel.setFont(GuiUtil.getDefaultFont());
        placeholderLabel.setBounds(10, 360, 100, 20);
        add(placeholderLabel);

        JTextField placeholderField = new JTextField();
        placeholderField.setFont(GuiUtil.getDefaultFont());
        placeholderField.setColumns(10);
        placeholderField.setBounds(120, 360, 410, 20);
        placeholderField.setText(config.getPlaceholderText());
        placeholderField.addKeyListener(new PlaceholderSaver(config, placeholderField));
        add(placeholderField);

        JLabel imageLabel = new JLabel("Placeholder Cover");
        imageLabel.setFont(GuiUtil.getDefaultFont());
        imageLabel.setBounds(10, 390, 110, 20);
        add(imageLabel);

        defaultCoverField = new JTextField();
        defaultCoverField.setEditable(false);
        defaultCoverField.setFont(GuiUtil.getDefaultFont());
        defaultCoverField.setBounds(130, 390, 300, 20);
        defaultCoverField.setColumns(1);
        add(defaultCoverField);

        String defaultCoverText;
        if (config.isDefaultPlaceholderCover()) defaultCoverText = "DEFAULT";
        else {
            File placeholderFile = config.getPlaceholderCoverFile();
            defaultCoverText = placeholderFile == null ? "" : placeholderFile.getAbsolutePath();
        }
        defaultCoverField.setText(defaultCoverText);

        defaultCoverSelect = new JButton("Select");
        defaultCoverSelect.setFont(GuiUtil.getDefaultFont());
        defaultCoverSelect.setEnabled(!config.isDefaultPlaceholderCover());
        defaultCoverSelect.setBounds(440, 390, 90, 20);
        defaultCoverSelect.addActionListener(event -> handleCoverSelect(file -> {
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "The selected file does not exist!",
                        "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            defaultCoverField.setText(file.getAbsolutePath());
            config.setPlaceholderCoverFile(file);
        }));
        add(defaultCoverSelect);

        JCheckBox useDefaultCheckbox = new JCheckBox("Use default placeholder cover (instead of custom path)");
        useDefaultCheckbox.setFont(GuiUtil.getDefaultFont());
        useDefaultCheckbox.setBounds(10, 420, 520, 20);
        useDefaultCheckbox.setSelected(config.isDefaultPlaceholderCover());
        useDefaultCheckbox.addActionListener(event -> handleDefaultCover(useDefaultCheckbox.isSelected()));
        add(useDefaultCheckbox);

        JLabel activeSourceLabel = new JLabel("Active Source");
        activeSourceLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        activeSourceLabel.setBounds(10, 445, 100, 20);
        add(activeSourceLabel);

        String[] sourcesArray = Arrays.stream(SourceType.values())
                .map(SourceType::getDisplayName).toArray(String[]::new);
        activeSourceList = new FlatComboBox<>();
        activeSourceList.setModel(new DefaultComboBoxModel<>(sourcesArray));
        activeSourceList.setRoundRect(true);
        activeSourceList.setSelectedItem(config.getActiveType().getDisplayName());
        activeSourceList.setFont(GuiUtil.getDefaultFont());
        activeSourceList.setBounds(365, 445, 165, 20);
        activeSourceList.addActionListener(event -> {
            if (!comboBoxFired) {
                comboBoxFired = true;
                return;
            }
            Utils.runAsync(this::updateSource);
        });
        add(activeSourceList);

        startButton = new JButton("Start");
        startButton.setFont(GuiUtil.getDefaultFont());
        startButton.setBounds(440, 470, 90, 25);
        startButton.addActionListener(event -> handeStartButton());
        add(startButton);

        statusLabel = new JLabel("SongCredits is stopped");
        statusLabel.setFont(GuiUtil.getDefaultFont());
        statusLabel.setBounds(10, 470, 155, 25);
        add(statusLabel);
    }

    private void handleRemove() {
        SongFormatter selectedFormatter = outputsTable.getSelected();
        if (selectedFormatter == null) return;

        config.removeFormatter(selectedFormatter);
        config.save();
        updateOutputsTable();
    }

    private void handleEdit() {
        SongFormatter selectedFormatter = outputsTable.getSelected();
        if (selectedFormatter == null) return;

        Frame frame = MainGui.getMainGui().getFrame();
        DefaultCloseAction action = new DefaultCloseAction(this, config, false);
        OutputDialoge dialoge = new OutputDialoge(frame, action, () -> {});
        dialoge.openDialoge(selectedFormatter);
    }

    private void handleAddOutput() {
        JFrame frame = MainGui.getMainGui().getFrame();
        DefaultCloseAction action = new DefaultCloseAction(this, config, true);
        OutputDialoge dialoge = new OutputDialoge(frame, action, () -> {});
        dialoge.openDialoge(null);
    }

    private void handleCoverSelect(@NotNull Consumer<File> consumer) {
        FilteredFileChooser chooser = new FilteredFileChooser(FilteredFileChooser.FileType.IMAGES);
        int result = chooser.openDialoge(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (file != null) Utils.runAsync(() -> consumer.accept(file));
    }

    private void handleCoverSize(@Nullable Object selectedItem) {
        if (!(selectedItem instanceof String)) return;
        Utils.runAsync(() -> {
            config.setCoverResolution((String) selectedItem);
            config.save();
        });
    }

    private void handleCoverHandle(boolean selected) {
        Utils.runAsync(() -> {
            config.setResolveCover(selected);
            config.save();
        });
    }

    private void handleDefaultCover(boolean selected) {
        Utils.runAsync(() -> {
            if (selected) {
                defaultCoverField.setText("DEFAULT");
            } else {
                File file = config.getPlaceholderCoverFile();
                if (file == null) defaultCoverField.setText("");
                else defaultCoverField.setText(file.getAbsolutePath());
            }
            defaultCoverSelect.setEnabled(!selected);
            config.setDefaultPlaceholderCover(selected);
            config.save();
        });
    }

    private void updateSource() {
        Object selectedItem = activeSourceList.getSelectedItem();
        if (!(selectedItem instanceof String)) return;
        SourceType type = SourceType.fromName((String) selectedItem);
        if (type == null) return;

        SongCredits.getSourceManager().switchSource(type, this);
        config.setActiveType(type);
        config.save();
    }

    public void setActiveSource(@NotNull SourceType type) {
        activeSourceList.setSelectedItem(type.getDisplayName());
    }

    public void setRunningStatus(boolean running) {
        if (running) {
            statusLabel.setText("SongCredits is running");
            startButton.setText("Stop");
        } else {
            statusLabel.setText("SongCredits is stopped");
            startButton.setText("Start");
        }
    }

    private void handeStartButton() {
        SourceManager manager = SongCredits.getSourceManager();
        if (manager.isRunning()) manager.stop();
        else manager.start(this);
        setRunningStatus(manager.isRunning());
    }

    public void updateOutputsTable() {
        outputsTable.updateModel(config.getFormatters());
    }

    private static class RoundedPanel extends JPanel {
        protected final Dimension arcs = new Dimension(20, 20);
        protected final int strokeSize = 1;

        public RoundedPanel() {
            super();
            setOpaque(false);
            setForeground(Color.GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //Draws the rounded opaque panel with borders.
            graphics.setColor(getBackground());
            graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
            graphics.setColor(getForeground());
            graphics.setStroke(new BasicStroke(strokeSize));
            graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);

            //Sets strokes to default, is better.
            graphics.setStroke(new BasicStroke());
        }

    }

}
