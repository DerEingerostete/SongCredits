/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.vlc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class VLCProperties {
    protected final @NotNull Map<String, String> map;
    protected final @NotNull File file;

    public VLCProperties(@NotNull File file) throws IOException {
        this.map = new HashMap<>();
        this.file = file;
        BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.startsWith("#") || !line.contains("=")) continue;
            String[] split = line.split("=", 2);
            if (split.length != 2) throw new IOException("Invalid line '" + line + "'");
            map.put(split[0], split[1]);
        }
    }

    @NotNull
    public String getProperty(@NotNull String key, @NotNull String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Nullable
    public String getProperty(@NotNull String key) {
        return map.get(key);
    }

    public int getInt(@NotNull String key, int defaultValue) {
        String property = getProperty(key);
        return property == null ? defaultValue : Integer.parseInt(property);
    }

}
