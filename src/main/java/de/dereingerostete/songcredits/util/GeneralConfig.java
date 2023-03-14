/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.util;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.song.SongFormatter;
import de.dereingerostete.songcredits.source.SourceType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Contains the config values for the GeneralPanel
 */
@Data
public class GeneralConfig {
    protected final Config config;
    protected final JSONObject generalConfigObject;

    //Values
    protected boolean resolveCover;
    protected @NotNull String coverResolution;
    protected @NotNull SourceType activeType;

    protected boolean defaultPlaceholderCover;
    protected @NotNull String placeholderText;

    protected @Nullable File coverFile;
    protected @Nullable File placeholderCoverFile;
    protected @NotNull List<SongFormatter> formatters;

    public GeneralConfig() {
        config = SongCredits.getConfig();
        JSONObject object = config.getJSONObject("general");
        if (object == null) {
            object = new JSONObject();
            config.set("general", object);
        }

        this.generalConfigObject = object;
        coverResolution = generalConfigObject.optString("coverResolution","256x256");
        resolveCover = generalConfigObject.optBoolean("resolveCover", true);

        JSONObject sourcesObject = Objects.requireNonNull(config.getJSONObject("sources"));
        String active = sourcesObject.optString("active", null);
        if (active != null) activeType = Objects.requireNonNull(SourceType.fromConfigName(active));

        defaultPlaceholderCover = generalConfigObject.optBoolean("defaultPlaceholderCover", true);
        placeholderText = generalConfigObject.optString("placeholderText", "No Song playing");

        String filePath = generalConfigObject.optString("coverFile", null);
        if (filePath != null) coverFile = new File(filePath);
        filePath = generalConfigObject.optString("placeholderCoverFile", null);
        if (filePath != null) placeholderCoverFile = new File(filePath);

        formatters = new ArrayList<>();
        JSONArray formatterArray = generalConfigObject.optJSONArray("outputFormatters");
        if (formatterArray == null) {
            formatterArray = new JSONArray();
            generalConfigObject.put("outputFormatters", formatterArray);
        }

        formatterArray.forEach(obj -> {
            if (!(obj instanceof JSONObject)) return;
            JSONObject formatterObject = (JSONObject) obj;

            String path = formatterObject.optString("file", null);
            if (path == null) return;
            File file = new File(path);

            boolean append = formatterObject.optBoolean("append", false);
            String format = formatterObject.optString("format", null);
            if (format != null) formatters.add(new SongFormatter(file, format, placeholderText, append));
        });
    }

    public void setActiveType(@NotNull SourceType type) {
        JSONObject sourcesObject = Objects.requireNonNull(config.getJSONObject("sources"));
        sourcesObject.put("active", type.getConfigName());
        this.activeType = type;
    }

    public void setCoverResolution(@NotNull String resolution) {
        generalConfigObject.put("coverResolution", resolution);
        this.coverResolution = resolution;
    }

    public void setResolveCover(boolean resolveCover) {
        generalConfigObject.put("resolveCover", resolveCover);
        this.resolveCover = resolveCover;
    }

    public void setDefaultPlaceholderCover(boolean defaultPlaceholderCover) {
        generalConfigObject.put("defaultPlaceholderCover", defaultPlaceholderCover);
        this.defaultPlaceholderCover = defaultPlaceholderCover;
    }

    public void setPlaceholderText(@NotNull String placeholderText) {
        generalConfigObject.put("placeholderText", placeholderText);
        this.placeholderText = placeholderText;
    }

    public void addFormatter(@NotNull SongFormatter formatter) {
        JSONArray formatterArray = generalConfigObject.optJSONArray("outputFormatters");
        if (formatterArray != null) {
            formatterArray.put(formatter.toJson());
            formatters.add(formatter);
        }
    }

    //Check if already the same output exists
    public boolean isValidFormatter(@NotNull SongFormatter formatter) {
        JSONArray formatterArray = generalConfigObject.optJSONArray("outputFormatters");
        if (formatterArray == null) return true;

        String filePath = resolveFilePath(formatter.getFile());
        for (int i = 0; i < formatterArray.length(); i++) {
            JSONObject object = formatterArray.optJSONObject(i);
            if (object == null) continue;

            String currentPath = object.optString("file", null);
            if (filePath.equals(currentPath)) return false;
        }
        return true;
    }

    public void removeFormatter(@NotNull SongFormatter formatter) {
        JSONArray formatterArray = generalConfigObject.optJSONArray("outputFormatters");
        if (formatterArray == null) return;

        String filePath = resolveFilePath(formatter.getFile());
        for (int i = 0; i < formatterArray.length(); i++) {
            JSONObject object = formatterArray.optJSONObject(i);
            if (object == null) continue;

            String currentPath = object.optString("file", null);
            if (filePath.equals(currentPath)) {
                formatterArray.remove(i);
                formatters.removeIf(current -> current.getFile().equals(formatter.getFile()));
                return;
            }
        }
    }

    public void replaceFormatter(@NotNull SongFormatter originalFormatter, @NotNull SongFormatter formatter) {
        removeFormatter(originalFormatter);
        addFormatter(formatter);
    }

    public void setCoverFile(@NotNull File file) {
        generalConfigObject.put("coverFile", resolveFilePath(file));
        this.coverFile = file;
    }

    public void setPlaceholderCoverFile(@NotNull File file) {
        generalConfigObject.put("placeholderCoverFile", resolveFilePath(file));
        this.placeholderCoverFile = file;
    }

    @NotNull
    private String resolveFilePath(@NotNull File file) {
        String path;
        try {
            path = file.getCanonicalPath();
        } catch (IOException exception) {
            path = file.getAbsolutePath();
        }
        return path;
    }

    public boolean save() {
        return config.saveQuietly();
    }

}
