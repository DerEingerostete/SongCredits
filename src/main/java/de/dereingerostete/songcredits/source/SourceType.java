/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source;

import de.dereingerostete.songcredits.source.spotify.SpotifyConfig;
import de.dereingerostete.songcredits.source.spotify.SpotifySource;
import de.dereingerostete.songcredits.source.vlc.VLCConfig;
import de.dereingerostete.songcredits.source.vlc.VLCSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public enum SourceType {
    VLC("VLC", "vlc", VLCSource.class, VLCConfig.class),
    SPOTIFY("Spotify", "spotify", SpotifySource.class, SpotifyConfig.class);

    private final @NotNull String displayName;
    private final @NotNull String configName;
    private final @NotNull Class<? extends AudioSource> sourceClass;
    private final @NotNull Class<? extends SourceConfig> configClass;

    @Nullable
    public static SourceType fromName(@NotNull String displayName) {
        for (SourceType type : values()) {
            if (type.getDisplayName().equals(displayName)) return type;
        }
        return null;
    }

    @Nullable
    public static SourceType fromConfigName(@NotNull String configName) {
        for (SourceType type : values()) {
            if (type.getConfigName().equals(configName)) return type;
        }
        return null;
    }

}
