/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.vlc;

import de.dereingerostete.songcredits.source.SourceConfig;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.util.Config;
import de.dereingerostete.songcredits.util.SystemUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Data
@Builder(builderClassName = "Builder")
@AllArgsConstructor
public class VLCConfig implements SourceConfig {
    protected final @NotNull JSONObject vlcObject;
    protected final @NotNull Config config;

    protected @Nullable String host;
    protected int port;
    protected @Nullable String password;
    protected long refreshRate;
    protected @Nullable File configFile;

    protected boolean resolveCover;
    protected @Nullable String resolution;

    public VLCConfig(@NotNull JSONObject vlcObject, @NotNull Config config) {
        this.vlcObject = vlcObject;
        this.config = config;

        this.port = vlcObject.optInt("port", 8080);
        this.password = vlcObject.optString("password", null);

        String host = vlcObject.optString("host", null);
        this.host = host == null ? "localhost" : formatHost(host);

        refreshRate = vlcObject.optLong("refreshRate",1000L);
        String configFilePath = vlcObject.optString("configFile", null);
        if (configFilePath != null) this.configFile = new File(configFilePath);
        else {
            File vlcConfig = SystemUtils.getOS().resolveVLCConfig();
            if (vlcConfig != null) {
                setConfigFile(vlcConfig);
                config.saveQuietly();
            }
        }

        JSONObject generalObject = config.getJSONObject("general");
        if (generalObject == null) throw new IllegalStateException("General Config cannot be null");
        resolveCover = generalObject.optBoolean("resolveCover", false);
        resolution = generalObject.optString("coverResolution", null);
    }

    @Override
    public boolean isComplete() {
        return password != null && host != null && resolution != null;
    }

    @NotNull
    @Override
    public SourceType getSourceType() {
        return SourceType.VLC;
    }

    public void setHost(@NotNull String host) {
        this.host = formatHost(host);
        vlcObject.put("host", this.host);
    }

    public void setPort(int port) {
        this.port = port;
        vlcObject.put("port", port);
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
        vlcObject.put("password", password);
    }

    public void setRefreshRate(long refreshRate) {
        this.refreshRate = refreshRate;
        vlcObject.put("refreshRate", refreshRate);
    }

    public void setConfigFile(@NotNull File configFile) {
        this.configFile = configFile;

        String path;
        try {
            path = configFile.getCanonicalPath();
        } catch (IOException exception) {
            path = configFile.getAbsolutePath();
        }
        vlcObject.put("configFile", path);
    }

    public void save() throws IOException {
        config.save();
    }

    @NotNull
    protected String formatHost(@NotNull String host) {
        try {
            String formattedHost = host;
            if (host.contains("://")) {
                int index = host.indexOf("://") + 3;
                formattedHost = formattedHost.substring(index);
            }
            return new URL(formattedHost).getHost();
        } catch (IOException exception) {
            return "localhost";
        }
    }

}
