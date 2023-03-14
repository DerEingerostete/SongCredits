/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.song;

import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class SongFormatter {
    protected @NotNull File file;
    protected @NotNull String formatText;
    protected @NotNull String placeholderText;
    protected @NotNull Set<String> formatKeys;
    protected boolean append;

    public SongFormatter(@NotNull File file, @NotNull String format,
                         @NotNull String placeholderText, boolean append) {
        setFormat(format);
        this.placeholderText = placeholderText;
        this.append = append;
        this.file = file;
    }

    public void format(@NotNull Song song) throws IOException {
        Map<String, String> map = song.getMetadataIds();
        String formattedString = formatText;
        for (String key : formatKeys) {
            String[] split = key.split(":");
            key = split[0].toLowerCase(); //Note: To implement a "turn to uppercase mode" remove this line

            int maxChars = -1;
            try {
                if (split.length > 1) maxChars = Integer.parseInt(split[1]);
            } catch (NumberFormatException ignored) {}

            String value = map.get(key);
            if (value != null) {
                if (maxChars > 0 && maxChars < value.length()) {
                    value = value.substring(0, maxChars) + "...";
                    key = "{" + key + ':' + maxChars + "}";
                } else key = "{" + key + "}";
                formattedString = formattedString.replace(key, value);
            }
        }
        printToFile(formattedString);
    }

    public void usePlaceholder() throws IOException {
        printToFile(placeholderText);
    }

    protected void printToFile(@NotNull String text) throws IOException {
        String formattedString = text;
        formattedString = formattedString.replace("{new_line}", "\n");
        if (append && file.exists()) formattedString = formattedString + "\n";
        FileUtils.writeStringToFile(file, formattedString, StandardCharsets.UTF_8, append);
    }

    public void setFormat(@NotNull String format) {
        this.formatText = format;
        Pattern pattern = Pattern.compile("\\{(.+?)}");
        Matcher matcher = pattern.matcher(format);
        formatKeys = matcher.results().map(result -> {
            String string = result.group();
            return string.substring(1, string.length() - 1);
        }).collect(Collectors.toSet());
    }

    @NotNull
    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        object.put("append", append);
        object.put("format", formatText);

        String filePath;
        try {
            filePath = file.getCanonicalPath();
        } catch (IOException exception) {
            filePath = file.getAbsolutePath();
        }
        object.put("file", filePath);
        return object;
    }

}