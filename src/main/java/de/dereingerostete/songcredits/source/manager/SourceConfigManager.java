/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.manager;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.source.SourceConfig;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.util.Config;
import de.dereingerostete.songcredits.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class SourceConfigManager {
    protected final List<SourceConfig> configs;

    public SourceConfigManager() {
        configs = new ArrayList<>();
        reloadConfigs();
    }

    public void reloadConfigs() {
        configs.clear();
        Config config = SongCredits.getConfig();
        for (SourceType type : SourceType.values()) {
            JSONObject object = config.getSource(type.getConfigName());
            if (object == null) {
                Logging.warning("Empty source config '" + type.getDisplayName() + "'. Skipping");
                continue;
            }

            try {
                Logging.debug("Loading config for source '" + type.getDisplayName() + "'");
                Logging.debug("Reflective creation of config class '" + type.getConfigClass().getName() + "'");
                Class<? extends SourceConfig> configClass = type.getConfigClass();
                Constructor<? extends SourceConfig> constructor = configClass.getConstructor(JSONObject.class, Config.class);
                configs.add(constructor.newInstance(object, config));
            } catch (ReflectiveOperationException exception) {
                Logging.warning("Failed to load config '" + type.getDisplayName() + "'", exception);
            }
        }
    }

    @NotNull
    public SourceConfig getByType(@NotNull SourceType type) {
        return configs.stream()
                .filter(config -> config.getSourceType() == type)
                .findAny().orElseThrow(() -> new IllegalStateException(
                        "Failed to retrieve config by type " + type.getDisplayName()));
    }

}
