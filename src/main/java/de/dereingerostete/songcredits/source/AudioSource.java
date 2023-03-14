/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source;

import de.dereingerostete.songcredits.song.Song;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public abstract class AudioSource {
    protected final @NotNull @Getter SourceType type;
    protected final @NotNull @Getter SourceConfig config;

    @NotNull
    public String getDisplayName() {
        return type.getDisplayName();
    }
    @Nullable
    public abstract Song getCurrentSong();

    @NotNull
    public abstract PlayingState getCurrentState();

    public abstract boolean nextSong() throws SourceException;

    public abstract boolean previousSong() throws SourceException;

    public abstract boolean pause() throws SourceException;

    public abstract boolean resume() throws SourceException;

    public abstract void refresh() throws SourceException;
    public abstract int getCurrentPosition();

    public abstract int getCurrentLength();

    public abstract void authorize() throws SourceException;

    public abstract void close();

}
