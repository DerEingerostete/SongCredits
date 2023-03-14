/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.song;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.dereingerostete.songcredits.util.GuiUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ToString
@EqualsAndHashCode
public class Song {
    private static final Cache<String, URL> artworkCache = Caffeine.newBuilder()
            .maximumSize(5000).expireAfterAccess(3L, TimeUnit.HOURS).build();
    protected final @NotNull Map<Metadata, String> mainMetadata;
    protected final @NotNull Map<String, String> additionalMetadata;

    public Song() {
        mainMetadata = new HashMap<>();
        additionalMetadata = new HashMap<>();
    }

    public void addMetadata(@NotNull Metadata metadata, @NotNull String value) {
        mainMetadata.put(metadata, value);
    }

    public void addMetadata(@NotNull String key, @NotNull String value) {
        Metadata metadata = resolveMetadata(key);
        if (metadata != null) addMetadata(metadata, value);
        else additionalMetadata.put(key, value);
    }

    @Nullable
    public String getMetadata(@NotNull String key) {
        Metadata metadata = resolveMetadata(key);
        if (metadata != null) return mainMetadata.get(metadata);
        else return additionalMetadata.get(key);
    }

    @Nullable
    public String getMetadata(@NotNull Metadata metadata) {
        return mainMetadata.get(metadata);
    }

    public boolean hasMetadata(@NotNull Metadata metadata) {
        return mainMetadata.containsKey(metadata);
    }

    @NotNull
    public Map<String, String> getMetadataIds() {
        Map<String, String> map = new HashMap<>(additionalMetadata);
        mainMetadata.forEach((metadata, value) -> map.put(metadata.getId(), value));
        return map;
    }

    @Nullable
    private Metadata resolveMetadata(@NotNull String key) {
        for (Metadata current : Metadata.values()) {
            if (current.getId().equals(key)) {
                return current;
            }
        }
        return null;
    }

    @NotNull
    public URL resolveArtwork(@NotNull String track, @Nullable String artist,
                              String resolution, boolean validate) throws IOException {
        String searchTerm = track;
        if (artist != null) searchTerm = artist + " - " + track;
        URL cachedUrl = artworkCache.getIfPresent(searchTerm + "#" + resolution);
        if (cachedUrl != null) return cachedUrl;

        String encoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        String urlRaw = "https://itunes.apple.com/search?term=" + encoded + "&media=music&entity=musicTrack";
        URL url = new URL(urlRaw);
        try {
            String response = IOUtils.toString(url, StandardCharsets.UTF_8);
            JSONObject rootObject = new JSONObject(response);

            String artworkUrl = null;
            JSONArray array = rootObject.getJSONArray("results");
            for (int i = 0; i < array.length(); i++) {
                Object object = array.get(i);
                if (!(object instanceof JSONObject)) continue;
                JSONObject resultObject = (JSONObject) object;

                if (validate) {
                    String artistName = resultObject.getString("artistName");
                    String trackName = resultObject.getString("trackName");
                    if ((artist != null && !artistName.equalsIgnoreCase(artist)) ||
                            !trackName.equalsIgnoreCase(track)) continue;
                }

                artworkUrl = resultObject.getString("artworkUrl100").replace("100x100", resolution);
                break;
            }

            URL resultUrl;
            if (artworkUrl != null) resultUrl = new URL(artworkUrl);
            else resultUrl = GuiUtil.getResourceAsURL("placeholder.png");
            artworkCache.put(searchTerm, resultUrl);
            return resultUrl;
        } catch (JSONException exception) {
            throw new IOException("Failed to resolve artwork from json", exception);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Metadata {
        TITLE("title", "Title"),
        ARTIST("artists", "Artist"),
        ALBUM("album", "Album"),
        RELEASE("release_date", "Release Date"),
        RELEASE_DAY("release_day", "Day of release"),
        RELEASE_MONTH("release_month", "Month of release"),
        RELEASE_YEAR("release_year", "Year of release"),
        COVER("cover_path", "Path to the cover"),
        LYRICS("lyrics", "Lyrics"),
        DURATION("duration", "Duration"),
        EXPLICIT("explicit", "Explicit"),
        DISC_NUMBER("disc_number", "Disc Number"),
        TRACK_NUMBER("track_number", "Track Number"),
        STATUS("status_id", "Status Id"),
        LABEL("record_label", "Label"),
        FILE_NAME("file_name", "Filename"),

        /* VLC source specific */
        GENRE("genre", "Genre"),
        COPYRIGHT("copyright", "Copyright"),
        DESCRIPTION("description", "Description"),
        RATING("rating", "Rating"),
        DATE("date", "Date"),
        URL("url", "URL"),
        LANGUAGE("language", "Language"),
        PUBLISHER("publisher", "Publisher"),
        ARTWORK_URL("artwork_url", "Artwork URL"),
        TRACK_TOTAL("track_total", "Track Total"),
        ACTORS("actors", "Actors"),
        DISC_TOTAL("disc_total", "Disc Total"),
        ENCODED_BY("encoded_by", "Encoded by");

        private final @NotNull String id;
        private final @NotNull String displayName;

    }

}

