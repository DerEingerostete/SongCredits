/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.component;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;

public class FilteredFileChooser {
    protected final JFileChooser chooser;

    public FilteredFileChooser(@NotNull FileType type) {
        chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(type.toFilter());

        FileSystemView view = FileSystemView.getFileSystemView();
        File homeDirectory = view.getHomeDirectory();
        chooser.setCurrentDirectory(homeDirectory);
        chooser.setAcceptAllFileFilterUsed(false);
    }

    public void setCurrentDirectory(@NotNull File directory) {
        chooser.setCurrentDirectory(directory);
    }

    public int openDialoge(@Nullable Component parent) {
        return chooser.showSaveDialog(parent);
    }

    @Nullable
    public File getSelectedFile() {
        return chooser.getSelectedFile();
    }

    public enum FileType {
        IMAGES("Image files", "bmp", "tga", "png", "jpeg", "jpg", "gif"),
        TEXT("Text files", "txt"),
        VLC_CONFIG("VLC Config");

        @Getter
        private final @NotNull String description;

        @Getter
        private final @NotNull String[] fileExtensions;

        FileType(@NotNull String description, @NotNull String... fileExtensions) {
            this.description = description;
            this.fileExtensions = fileExtensions;
        }

        @NotNull
        public FileFilter toFilter() {
            if (fileExtensions.length == 0) return new FileFilter() {

                @Override
                public boolean accept(File file) {
                    return file.isFile();
                }

                @Override
                public String getDescription() {
                    return description;
                }
            };
            return new FileNameExtensionFilter(description, fileExtensions);
        }

    }

}
