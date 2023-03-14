/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package org.slf4j.impl;

import de.dereingerostete.songcredits.log.ConsoleLoggerAdapter;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class LoggerFactory implements ILoggerFactory {
    private static final ConsoleLoggerAdapter LOGGER_ADAPTER = new ConsoleLoggerAdapter();

    @Override
    public Logger getLogger(String name) {
        return LOGGER_ADAPTER;
    }

}
