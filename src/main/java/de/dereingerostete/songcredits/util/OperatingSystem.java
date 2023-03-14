/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@RequiredArgsConstructor
public enum OperatingSystem {
    WINDOWS("%appdata%/SongCredits", "%appdata%/vlc/vlcrc"),
    LINUX("%home%/.config/SongCredits", "%home%/.config/vlc/vlcrc"),
    MAC("%home%/Library/Preferences/de.dereingerostete.songcredits/SongCredits",
            "%home%/Library/Preferences/org.videolan.vlc/vlcrc"),
    SUN_OS(null, "%appdata%/vlc/vlcrc"),
    FREE_BSD(null, "%appdata%/vlc/vlcrc"),
    OTHER(null, null),
    UNKNOWN(null, null);

    private final @Nullable String configDirectory;
    private final @Nullable String vlcConfigPath;

    @NotNull
    public File resolveConfigDirectory() {
        if (configDirectory == null) {
            File directory = new File("SongCredits");
            createDirectory(directory);
            return directory;
        }

        File directory = resolveFile(configDirectory);
        createDirectory(directory);
        return directory;
    }

    @Nullable
    public File resolveVLCConfig() {
        if (vlcConfigPath == null) return null;
        return resolveFile(vlcConfigPath);
    }

    @NotNull
    private File resolveFile(@NotNull String filePath) {
        Pattern pattern = Pattern.compile("(%.+?%)");
        Matcher matcher = pattern.matcher(filePath);
        if (!matcher.find()) {
            File directory = new File(filePath);
            createDirectory(directory);
            return directory;
        }

        String path = filePath;
        for (int i = 0; i < matcher.groupCount(); i++) {
            String group = matcher.group(i);
            String name = group.substring(1, group.length() - 1);
            path = path.replace(group, System.getenv(name));
        }
        return new File(path);
    }

    private void createDirectory(@NotNull File file) {
        if (file.exists()) {
            if (file.isDirectory()) return;
            else FileUtils.deleteQuietly(file);
        }

        if (!file.mkdirs() && !file.exists())
            throw new IllegalStateException("Failed to create directory '" + file.getName() + "'");
    }

}