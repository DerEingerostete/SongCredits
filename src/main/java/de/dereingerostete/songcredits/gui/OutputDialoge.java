/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.gui.component.FilteredFileChooser;
import de.dereingerostete.songcredits.gui.tab.GeneralPanel;
import de.dereingerostete.songcredits.song.Song.Metadata;
import de.dereingerostete.songcredits.song.SongFormatter;
import de.dereingerostete.songcredits.util.GeneralConfig;
import de.dereingerostete.songcredits.util.GuiUtil;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static de.dereingerostete.songcredits.SongCredits.showWarning;

public class OutputDialoge extends JDialog {
    private final JTextField infoPathField;
    private final JTextField formatField;
    private final JCheckBox appendCheckBox;
    protected final List<Metadata> sortedMetadata;
    private @Getter @Nullable File outputFile;
    private @Nullable SongFormatter originalFormatter;

    public OutputDialoge(@NotNull Frame owner, @NotNull Function<OutputDialoge, Boolean> onClose,
                         @NotNull Runnable onCancel) {
        super(owner, "Output editor", true);
        sortedMetadata = new ArrayList<>(List.of(Metadata.values()));
        sortedMetadata.sort(Comparator.comparing(Metadata::getDisplayName));

        setBounds(100, 100, 460, 530);
        setLocationRelativeTo(owner);
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        setVisible(false);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JLabel infoPathLabel = new JLabel("Song info path");
        infoPathLabel.setFont(GuiUtil.getDefaultFont());
        infoPathLabel.setBounds(10, 10, 425, 20);
        contentPanel.add(infoPathLabel);

        infoPathField = new JTextField();
        infoPathField.setEditable(false);
        infoPathField.setColumns(1);
        infoPathField.setBounds(10, 40, 340, 25);
        contentPanel.add(infoPathField);

        JButton selectButton = new JButton("Select");
        selectButton.setBounds(364,  40, 70, 25);
        selectButton.setFont(GuiUtil.getDefaultFont());
        selectButton.addActionListener(event -> {
            FilteredFileChooser chooser = new FilteredFileChooser(FilteredFileChooser.FileType.TEXT);
            chooser.openDialoge(this);
            outputFile = chooser.getSelectedFile();
            if (outputFile != null) infoPathField.setText(outputFile.getAbsolutePath());
        });
        contentPanel.add(selectButton);

        JTextArea textPane = new JTextArea();
        textPane.setWrapStyleWord(true);
        textPane.setLineWrap(true);
        textPane.setSize(425, 60);
        textPane.setLocation(10, 100);
        textPane.setEditable(false);
        textPane.setText("Note:\n- Not every source supports all format options" +
                "\n- Appending :<n> will limit the variable to <n> characters (e.g. {title:10})");
        textPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textPane.setOpaque(false);
        contentPanel.add(textPane);

        formatField = new JTextField();
        formatField.setColumns(1);
        formatField.setBounds(10, 410, 425, 25);
        contentPanel.add(formatField);

        appendCheckBox = new JCheckBox("Append to file");
        appendCheckBox.setBounds(10, 440, 424, 20);
        appendCheckBox.setFont(GuiUtil.getDefaultFont());
        contentPanel.add(appendCheckBox);

        JLabel formatLabel = new JLabel("Song format");
        formatLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        formatLabel.setBounds(10, 75, 425, 20);
        contentPanel.add(formatLabel);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 165, 425, 235);
        contentPanel.add(scrollPane);

        JTable variablesTable = new JTable();
        variablesTable.setFont(GuiUtil.getDefaultFont());
        variablesTable.setModel(createModel());
        variablesTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.getClickCount() != 2 || !SwingUtilities.isLeftMouseButton(event)) return;
                Point point = event.getPoint();
                int row = variablesTable.rowAtPoint(point);
                if (row < 0 || sortedMetadata.size() < row) return;

                String text = formatField.getText();
                if (row < sortedMetadata.size()) {
                    Metadata metadata = sortedMetadata.get(row);
                    text = text + '{' + metadata.getId() + '}';
                } else text = text + "{new_line}";
                formatField.setText(text);
            }

        });
        scrollPane.setViewportView(variablesTable);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(buttonPane, BorderLayout.SOUTH);

        JButton okButton = new JButton("OK");
        okButton.setFont(GuiUtil.getDefaultFont());
        okButton.setActionCommand("OK");
        okButton.addActionListener(event -> {
            if (onClose.apply(this)) dispose();
        });
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(GuiUtil.getDefaultFont());
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(event -> {
            onCancel.run();
            dispose();
        });
        buttonPane.add(cancelButton);
    }

    public void openDialoge(@Nullable SongFormatter formatter) {
        if (formatter != null) {
            File file = formatter.getFile();
            infoPathField.setText(file.getAbsolutePath());
            formatField.setText(formatter.getFormatText());
            appendCheckBox.setSelected(formatter.isAppend());
            this.outputFile = file;
            this.originalFormatter = formatter;
        }
        setVisible(true);
    }

    @Nullable
    public String getFormatText() {
        String text = formatField.getText();
        return text.isEmpty() ? null : text;
    }

    public boolean isAppend() {
        return appendCheckBox.isSelected();
    }

    @Nullable
    public SongFormatter toFormatter() {
        String formatText = getFormatText();
        if (formatText == null || outputFile == null) return null;

        GeneralConfig config = SongCredits.getGeneralConfig();
        return new SongFormatter(outputFile, formatText, config.getPlaceholderText(), isAppend());
    }

    @NotNull
    protected TableModel createModel() {
        Object[][] data = new Object[sortedMetadata.size() + 1][2];
        for (int i = 0; i < sortedMetadata.size(); i++) {
            Metadata metadata = sortedMetadata.get(i);
            data[i][0] = metadata.getDisplayName();
            data[i][1] = '{' + metadata.getId() + '}';
        }

        int lastEntry = data.length - 1;
        data[lastEntry][0] = "New line";
        data[lastEntry][1] = "{new_line}";

        String[] names = {"Name", "Id"};
        return new DefaultTableModel(data, names) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

        };
    }

    @Data
    public static class DefaultCloseAction implements Function<OutputDialoge, Boolean> {
        protected final @NotNull GeneralPanel generalPanel;
        protected final @NotNull GeneralConfig config;
        protected final boolean add; //If False it's edit mode

        @Override
        public Boolean apply(@NotNull OutputDialoge result) {
            SongFormatter formatter = result.toFormatter();
            if (formatter == null) {
                showWarning(generalPanel, "The output is incomplete.\n" +
                        "Please fill in all the fields or click Cancel");
                return false;
            }

            if (add) {
                if (!config.isValidFormatter(formatter)) {
                    showWarning(generalPanel, "There already exists an output with the same file path");
                    return false;
                }
                config.addFormatter(formatter);
            } else {
                if (result.getOutputFile() == null || result.getFormatText() == null) {
                    showWarning(generalPanel, "The output is incomplete.\n" +
                            "Please fill in all the fields or click Cancel");
                    return false;
                }

                SongFormatter original = result.originalFormatter;
                if (original == null) return true;
                config.replaceFormatter(original, formatter);
            }

            if (!config.save()) {
                showWarning(generalPanel, "Failed to save the output. Please try again or cancel");
                return false;
            }

            generalPanel.updateOutputsTable();
            return true;
        }

    }

}
