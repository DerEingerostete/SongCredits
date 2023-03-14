/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.vlc;

import de.dereingerostete.songcredits.song.Song;
import de.dereingerostete.songcredits.source.AudioSource;
import de.dereingerostete.songcredits.source.PlayingState;
import de.dereingerostete.songcredits.source.SourceException;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.util.Logging;
import de.dereingerostete.songcredits.util.Utils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class VLCSource extends AudioSource {
    protected final @NotNull String host;
    protected final @NotNull String authentication;
    protected final int port;
    protected final boolean resolveCover;
    protected final String coverResolution;

    private Song currentSong;
    private PlayingState currentState;
    private int totalLength = -1; //in ms
    private int currentPosition = -1; //in ms

    @SuppressWarnings("HttpUrlsUsage")
    public VLCSource(@NotNull VLCConfig config) throws SourceException {
        super(SourceType.VLC, config);
        port = config.getPort();
        resolveCover = config.isResolveCover();
        coverResolution = config.getResolution();

        String host = config.getHost();
        if (host == null) throw new SourceException("No host was set");
        if (!host.startsWith("http")) host = "http://" + host;
        if (host.endsWith("/")) host = host.substring(0, host.length() - 1);
        this.host = host;

        String password = config.getPassword();
        if (password == null) password = "";
        byte[] bytes = (":" + password).getBytes(StandardCharsets.UTF_8);
        Base64.Encoder encoder = Base64.getEncoder();
        authentication = "Basic " + encoder.encodeToString(bytes);
    }

    @Nullable
    @Override
    public Song getCurrentSong() {
        return currentSong;
    }

    @NotNull
    @Override
    public PlayingState getCurrentState() {
        return Objects.requireNonNull(currentState, "State is not loaded");
    }

    @Override
    public void refresh() throws SourceException {
        currentState = getCurrentState(null);

        try {
            JSONObject rootObject = doRequest("status.json");
            retrieveSong(rootObject);
            retrieveDurationAndLength(rootObject);
        } catch (IOException exception) {
            throw new SourceException("Failed to get current song", exception);
        }
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public int getCurrentLength() {
        return totalLength;
    }

    @NotNull
    private PlayingState getCurrentState(@Nullable String command) throws SourceException {
        try {
            String request = "status.json";
            if (command != null) request = request + "?command=" + command;
            JSONObject rootObject = doRequest(request);
            String state = rootObject.getString("state");
            return PlayingState.valueOf(state.toUpperCase());
        } catch (IOException | JSONException exception) {
            throw new SourceException("Failed to get current state", exception);
        } catch (IllegalStateException exception) {
            throw new SourceException("Current state is of unknown type", exception);
        }
    }

    private void retrieveSong(@NotNull JSONObject rootObject) throws SourceException {
        try {
            JSONObject informationObject = rootObject.optJSONObject("information");
            if (informationObject == null) return; //Remember this wen adding further lines

            JSONObject metaObject = informationObject
                    .getJSONObject("category")
                    .getJSONObject("meta");
            JSONObject lowercaseMetaObject = Utils.formatKeys(metaObject);

            Song song = new Song();
            for (String key : lowercaseMetaObject.keySet()) {
                String value = String.valueOf(lowercaseMetaObject.get(key));
                song.addMetadata(key, value);
            }

            //Manual if filename has no underline in meta
            String fileName = lowercaseMetaObject.optString("filename");
            if (fileName != null) song.addMetadata(Song.Metadata.FILE_NAME, fileName);

            //Load Artwork
            if (!song.hasMetadata(Song.Metadata.ARTWORK_URL) && resolveCover) {
                String track = song.getMetadata(Song.Metadata.TITLE);
                String artists = song.getMetadata(Song.Metadata.ARTIST);

                boolean validate = true;
                if (track == null || artists == null) {
                    track = song.getMetadata(Song.Metadata.FILE_NAME);
                    if (track != null) track = FilenameUtils.getBaseName(fileName);
                    validate = false;
                }

                try {
                    if (track != null) {
                        URL url = song.resolveArtwork(track, artists, coverResolution, validate);
                        String rawUrl = url.toString();
                        song.addMetadata(Song.Metadata.COVER, rawUrl);
                        song.addMetadata(Song.Metadata.ARTWORK_URL, rawUrl);
                    }
                } catch (IOException exception) {
                    Logging.warning("Failed to resolve artwork of song", exception);
                }
            }
            currentSong = song;
        } catch (JSONException exception) {
            throw new SourceException("Failed to get current song", exception);
        }
    }

    private void retrieveDurationAndLength(@NotNull JSONObject rootObject) throws SourceException {
        try {
            totalLength = rootObject.getInt("length") * 1000;
            currentPosition = (int) (rootObject.getDouble("position") * totalLength);
        } catch (JSONException exception) {
            throw new SourceException("Failed to get duration and length", exception);
        }
    }

    @Override
    public boolean nextSong() throws SourceException {
        return getCurrentState("pl_next") == PlayingState.PLAYING;
    }

    @Override
    public boolean previousSong() throws SourceException {
        return getCurrentState("pl_previous") == PlayingState.PLAYING;
    }

    @Override
    public boolean pause() throws SourceException {
        return getCurrentState("pl_pause") == PlayingState.PAUSED;
    }

    @Override
    public boolean resume() throws SourceException {
        return getCurrentState("pl_forceresume") == PlayingState.PLAYING;
    }

    @Override
    public void authorize() {}

    @Override
    public void close() {}

    @NotNull
    private JSONObject doRequest(@NotNull String request) throws IOException {
        URL url = new URL(host + ":" + port + "/requests/" + request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authentication);

        try {
            String response = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
            return new JSONObject(response);
        } catch (JSONException exception) {
            throw new IOException(exception);
        }
    }

}
