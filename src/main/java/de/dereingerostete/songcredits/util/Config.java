/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.util;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Config {
    private final JSONObject object;
    private final File file;

    public Config(File file) throws IOException {
        this(file, file.getName());
    }

    public Config(@NotNull File file, @NotNull String resourceKey) throws IOException {
        this.file = file;

        if (!file.exists()) {
            ClassLoader loader = getClass().getClassLoader();
            InputStream input = loader.getResourceAsStream(resourceKey);
            if (input == null) throw new IOException("Resource '" + resourceKey + "' not found");
            FileUtils.copyInputStreamToFile(input, file);
        }

        Charset charset = StandardCharsets.UTF_8;
        String fileContent = FileUtils.readFileToString(file, charset);
        object = new JSONObject(fileContent);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return object.optBoolean(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return object.optInt(key, defaultValue);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        return object.optString(key, defaultValue);
    }

    @Nullable
    public JSONObject getJSONObject(@NotNull String key) {
        return object.optJSONObject(key);
    }

    public Object getObject(String key) {
        return object.opt(key);
    }

    public JSONArray getJSONArray(String key) {
        return object.optJSONArray(key);
    }

    public UUID getUUID(String key) {
        String string = getString(key);
        if (string == null) return null;
        return UUID.fromString(string);
    }

    public JSONObject getRootObject() {
        return object;
    }

    public long getLong(String key, long defaultValue) {
        return object.optLong(key, defaultValue);
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public <T extends Enum<T>> T getEnum(String key, T defaultValue, Class<T> enumClass) {
        return object.optEnum(enumClass, key, defaultValue);
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass) {
        return getEnum(key, null, enumClass);
    }

    public void set(String key, Object object) {
        this.object.put(key, object);
    }

    @Nullable
    public JSONObject getSource(@NotNull String source) throws IllegalStateException {
        JSONObject sourcesObject = getJSONObject("sources");
        if (sourcesObject == null) throw new IllegalStateException("Sources not defined in config");
        return sourcesObject.optJSONObject(source);
    }

    public void save() throws IOException {
        String string = object.toString(4);
        Charset charset = StandardCharsets.UTF_8;
        FileUtils.writeStringToFile(file, string, charset, false);
    }

    /**
     * Saves the config
     * If an error occurs the error will only be
     * logged in the log file but will not be shown
     */
    public boolean saveQuietly() {
        try {
            save();
            return true;
        } catch (IOException exception) {
            Logging.warning("Failed to save config", exception);
            return false;
        }
    }

}
