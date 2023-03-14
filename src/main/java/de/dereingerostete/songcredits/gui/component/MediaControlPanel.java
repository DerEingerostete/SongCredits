/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.gui.component;

import com.formdev.flatlaf.extras.components.FlatProgressBar;
import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.source.AudioSource;
import de.dereingerostete.songcredits.source.PlayingState;
import de.dereingerostete.songcredits.source.SourceException;
import de.dereingerostete.songcredits.util.Config;
import de.dereingerostete.songcredits.util.Logging;
import de.dereingerostete.songcredits.util.Utils;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MediaControlPanel extends JPanel {
    protected final FlatProgressBar progressBar;
    protected final IconButton backButton;
    protected final IconButton pauseButton;
    protected final IconButton nextButton;

    //Config values
    protected final long clickRefreshDelay;
    protected final boolean resetProgressOnStop;
    protected final boolean stoppedAnimation;

    @Setter
    protected @Nullable AudioSource source;

    public MediaControlPanel() {
        setLayout(null);

        UIManager.put("ProgressBar.cycleTime", 5000);
        progressBar = new FlatProgressBar();
        progressBar.setValue(0);
        progressBar.setSquare(true);
        progressBar.setBounds(10, 15, 525, 10);
        progressBar.setForeground(Color.gray);
        add(progressBar);

        int y = 30;
        backButton = new IconButton("mediaControls/skip-back", "Skip Back");
        backButton.setBounds(0, y, 36, 36);
        backButton.addActionListener(event -> {
            try {
                if (source != null) {
                    if (!source.previousSong()) throw new SourceException("Source returned false");
                    refreshSource();
                }
            } catch (SourceException exception) {
                Logging.warning("Failed to skip to previous song", exception);
                SongCredits.showError("skipping to previous song", exception, this);
            }
        });
        add(backButton);

        pauseButton = new IconButton("mediaControls/play", "Play Song");
        pauseButton.setBounds(0, y, 36, 36);
        pauseButton.addActionListener(event -> {
            try {
                if (source != null) {
                    PlayingState state = source.getCurrentState();
                    if (state == PlayingState.PLAYING) {
                        if (!source.pause()) throw new SourceException("Source returned false");
                        pauseButton.setIcon("mediaControls/play", "Play Song");
                    } else {
                        if (!source.resume()) throw new SourceException("Source returned false");
                        pauseButton.setIcon("mediaControls/pause", "Pause Song");
                    }
                    refreshSource();
                }
            } catch (SourceException exception) {
                Logging.warning("Failed to pause/resume song", exception);
                SongCredits.showError("pausing/resuming song", exception, this);
            }
        });
        add(pauseButton);

        nextButton = new IconButton("mediaControls/skip-forward", "Skip Forward");
        nextButton.setBounds(0, y, 36, 36);
        nextButton.addActionListener(event -> {
            try {
                if (source != null) {
                    if (!source.nextSong()) throw new SourceException("Source returned false");
                    refreshSource();
                }
            } catch (SourceException exception) {
                Logging.warning("Failed to skip to next song", exception);
                SongCredits.showError("skipping to next song", exception, this);
            }
        });
        add(nextButton);

        JSeparator separator = new JSeparator();
        separator.setBounds(0, 5, 545, 3);
        separator.setBorder(new LineBorder(separator.getForeground(), 3));
        add(separator);

        Config config = SongCredits.getConfig();
        clickRefreshDelay = config.getLong("clickRefreshDelay", 500L);
        resetProgressOnStop = config.getBoolean("resetProgressOnStop", true);
        stoppedAnimation = config.getBoolean("animationWhileStopped", true);
    }

    private void refreshSource() {
        Utils.runLater(() -> {
            try {
                if (source != null) source.refresh();
            } catch (SourceException exception) {
                Logging.warning("Failed to refresh source", exception);
            }
        }, clickRefreshDelay);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        backButton.setEnabled(enabled);
        nextButton.setEnabled(enabled);
        pauseButton.setEnabled(enabled);
        if (!enabled) {
            setSongProgress(0, 1);
            if (stoppedAnimation) setLoading(true);
        }
    }

    public void setLoading(boolean loading) {
        progressBar.setIndeterminate(loading);
    }

    public void setState(@NotNull PlayingState state) {
        if (state == PlayingState.STOPPED || state == PlayingState.PAUSED) {
            pauseButton.setIcon("mediaControls/play", "Play Song");
            if (resetProgressOnStop && state == PlayingState.STOPPED) {
                setSongProgress(0, 1);
                if (stoppedAnimation) setLoading(true);
            }
        } else pauseButton.setIcon("mediaControls/pause", "Pause Song");
    }

    public void setSongProgress(int current, int total) {
        progressBar.setMaximum(total);
        progressBar.setValue(current);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        updateLocation();
    }

    protected void updateLocation() {
        int buffer = 5;
        int x = (getWidth() - pauseButton.getWidth()) / 2;
        pauseButton.setLocation(x, pauseButton.getY());
        nextButton.setLocation(x + backButton.getWidth() + buffer, pauseButton.getY());
        backButton.setLocation(x - backButton.getWidth() - buffer, pauseButton.getY());
    }

}
