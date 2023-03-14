/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.manager;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.gui.MainGui;
import de.dereingerostete.songcredits.gui.component.MediaControlPanel;
import de.dereingerostete.songcredits.gui.tab.GeneralPanel;
import de.dereingerostete.songcredits.song.Song;
import de.dereingerostete.songcredits.song.SongFormatter;
import de.dereingerostete.songcredits.source.*;
import de.dereingerostete.songcredits.util.*;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.ConnectException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static de.dereingerostete.songcredits.SongCredits.showError;

public class SourceManager {
    protected final MediaControlPanel controlPanel = MainGui.getMediaControl();
    protected final GeneralConfig config = SongCredits.getGeneralConfig();
    protected @Getter @Nullable AudioSource audioSource;
    protected Timer timer;

    //Values while active
    protected PlayingState lastState;
    protected @Getter boolean running;
    protected Song oldSong;

    public SourceManager() {
        loadSource();
    }

    public boolean start() throws SourceException {
        if (audioSource == null) {
            return false;
        }


        try {
            lastState = null;
            audioSource.refresh();
        } catch (SourceException exception) {
            Throwable cause = exception.getCause();
            if (!(cause instanceof ConnectException) && audioSource.getType() != SourceType.VLC)
                throw exception;
        }

        MediaControlPanel panel = MainGui.getMediaControl();
        panel.setEnabled(true);
        panel.setLoading(false);
        panel.setSource(audioSource);

        GeneralPanel generalPanel = MainGui.getGeneral();
        generalPanel.setRunningStatus(true);

        SourceConfig config = audioSource.getConfig();
        long refreshRate = config.getRefreshRate();
        Logging.debug("Starting source manager timer with a refresh rate of " + refreshRate + "ms");
        timer = new Timer("SourceManager");
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                onTick();
            }

        }, 20L, refreshRate);

        running = true;
        return true;
    }

    public void updateRefreshRate(long refreshRate) {
        if (!running) return;
        Logging.debug("Reloading source manager timer with a refresh rate of " + refreshRate + "ms");
        timer.cancel();
        timer = new Timer("SourceManager");
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                onTick();
            }

        }, 20L, refreshRate);
    }

    public void start(@Nullable Component component) {
        try {
            if (!start()) {
                Logging.warning("Failed to start source manager");
                JOptionPane.showMessageDialog(component, "Could not start SongCredits. " +
                                "Please check the settings of your active source",
                        "Failed to start", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SourceException exception) {
            Logging.warning("Failed to start source manager", exception);
            SongCredits.showError("starting source manager", exception, component);
        }
    }

    public void stop() {
        Logging.debug("Stopping manager");
        timer.cancel();
        lastState = null;
        running = false;

        MediaControlPanel panel = MainGui.getMediaControl();
        panel.setEnabled(false);
        panel.setSource(null);

        GeneralPanel generalPanel = MainGui.getGeneral();
        generalPanel.setRunningStatus(false);
    }

    //Called in intervals
    protected void onTick() {
        try {
            if (audioSource == null) return;
            audioSource.refresh();

            PlayingState state = audioSource.getCurrentState();
            Song song = audioSource.getCurrentSong();
            boolean newSong = song != null && !song.equals(oldSong);
            if (newSong) oldSong = song;

            PlayingState lastStateSave = lastState;
            Utils.runAsync(() -> {
                List<SongFormatter> formatters = config.getFormatters();
                formatters.forEach(formatter -> {
                    try {
                        if (newSong) {
                            formatter.format(song);
                            updateCover(song);
                        } else if (state == PlayingState.STOPPED && lastStateSave != state) {
                            formatter.usePlaceholder();
                            updateCover(null);
                        }
                    } catch (IOException exception) {
                        Logging.warning("Failed to format song", exception);
                    }
                });
            });

            if (lastState != state) {
                if (lastState == PlayingState.STOPPED) controlPanel.setLoading(false);
                lastState = state;
                controlPanel.setState(state);
            }

            if (state == PlayingState.PLAYING) {
                int totalLength = audioSource.getCurrentLength();
                int current = audioSource.getCurrentPosition();
                controlPanel.setSongProgress(current, totalLength);
            }
        } catch (SourceException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof ConnectException && audioSource.getType() == SourceType.VLC) return;
            Logging.debug("Failed to refresh source", exception);
        }
    }

    protected void updateCover(@Nullable Song song) throws IOException {
        File coverDestination = config.getCoverFile();
        if (coverDestination == null) return;
        if (song == null) {
            if (config.isDefaultPlaceholderCover()) {
                InputStream stream = GuiUtil.getInputStream("placeholder.png");
                FileUtils.copyInputStreamToFile(stream, coverDestination);
            }
        } else {
            String urlValue = song.getMetadata(Song.Metadata.COVER);
            if (urlValue == null) urlValue = song.getMetadata(Song.Metadata.ARTWORK_URL);
            if (urlValue != null) {
                URL url = new URL(urlValue);
                FileUtils.copyURLToFile(url, coverDestination);
            } else updateCover(null);
        }
    }

    public void close() {
        if (audioSource != null) {
            audioSource.close();
            Logging.debug("Closed audio source");
        }
    }

    public void switchSource(@NotNull SourceType type, @NotNull Component component) {
        MainGui.getMainGui().setLoading(true);
        boolean wasRunning = isRunning();
        if (wasRunning) stop();

        loadSource(type);
        if (wasRunning) start(component);
        MainGui.getMainGui().setLoading(false);
    }

    @Nullable
    private JSONObject resolveSourcesObject() {
        Config config = SongCredits.getConfig();
        JSONObject sourcesObject = config.getJSONObject("sources");
        if (sourcesObject == null) {
            MainGui.getMainGui().setLoading(false);
            showError("loading active source", null, null);
            Logging.warning("Failed to load active source: Config is missing sources entry");
            return null;
        } else return sourcesObject;
    }

    private void loadSource() {
        JSONObject sourcesObject = resolveSourcesObject();
        if (sourcesObject == null) return;

        String active = sourcesObject.optString("active", null);
        if (active == null) {
            Logging.debug("No active source found");
            MainGui.getMainGui().setLoading(false);
            return;
        }

        SourceType type = SourceType.fromConfigName(active);
        if (type == null) {
            Logging.warning("Invalid source found: " + active + ". Skipping");
            MainGui.getMainGui().setLoading(false);
        } else loadSource(type);
    }

    private void loadSource(@NotNull SourceType type) {
        try {
            SourceConfigManager manager = SongCredits.getConfigManager();
            SourceConfig sourceConfig = manager.getByType(type);

            Logging.debug("Reflective creation of source class '" + type.getSourceClass().getName() + "'");
            Class<? extends AudioSource> sourceClass = type.getSourceClass();
            Constructor<? extends AudioSource> sourceConstructor = sourceClass.getConstructor(sourceConfig.getClass());
            audioSource = sourceConstructor.newInstance(sourceConfig);
        } catch (ReflectiveOperationException exception) {
            Logging.warning("Failed to load source", exception);
            return;
        }

        try {
            Logging.debug("Authorizing source");
            audioSource.authorize();
            audioSource.refresh();
        } catch (SourceException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof ConnectException && audioSource.getType() == SourceType.VLC) return;
            Logging.warning("Failed to authorize source", exception);
            showError("authorizing source", exception, null);
        }
    }

    public void restart(@NotNull Component component) {
        boolean wasRunning = isRunning();
        if (wasRunning) stop();
        switchSource(SourceType.VLC, component);
        if (wasRunning) start(component);
    }

}
