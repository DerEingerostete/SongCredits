/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.spotify;

import de.dereingerostete.songcredits.source.SourceConfig;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.util.Config;
import lombok.Data;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;

@Data
public class SpotifyConfig implements SourceConfig {
    protected final @NotNull JSONObject spotifyObject;
    protected final @NotNull Config config;

    protected @Nullable String clientId;
    protected @Nullable String clientSecret;
    protected boolean countPausedAsStopped;
    protected long refreshRate;

    public SpotifyConfig(@NotNull JSONObject object, @NotNull Config config) {
        this.spotifyObject = object;
        this.config = config;

        this.clientId = object.optString("clientId", null);
        this.clientSecret = object.optString("clientSecret", null);
        this.countPausedAsStopped = object.optBoolean("countPausedAsStopped", true);
        this.refreshRate = object.optLong("refreshRate", 2000L);
    }

    @Override
    public boolean isComplete() {
        return clientId != null && clientSecret != null;
    }

    @NotNull
    @Override
    public SourceType getSourceType() {
        return SourceType.SPOTIFY;
    }

    public void setClientId(@Nullable String clientId) {
        this.clientId = clientId;
        spotifyObject.put("clientId", clientId);
    }

    public void setClientSecret(@Nullable String clientSecret) {
        this.clientSecret = clientSecret;
        spotifyObject.put("clientSecret", clientSecret);
    }

    public void setCountPausedAsStopped(boolean countPausedAsStopped) {
        this.countPausedAsStopped = countPausedAsStopped;
        spotifyObject.put("countPausedAsStopped", countPausedAsStopped);
    }

    public void setRefreshRate(long refreshRate) {
        this.refreshRate = refreshRate;
        spotifyObject.put("refreshRate", refreshRate);
    }

    public void save() throws IOException {
        config.save();
    }

    @ApiStatus.Internal
    @NotNull
    public String getAccessToken() {
        return spotifyObject.optString("accessToken", "");
    }

    @ApiStatus.Internal
    @NotNull
    public String getRefreshToken() {
        return spotifyObject.optString("refreshToken", "");
    }

}
