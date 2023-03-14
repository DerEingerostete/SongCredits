/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.log;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ConsoleLogger extends Logger {
    protected final ConciseFormatter formatter = new ConciseFormatter();

    public ConsoleLogger(String name, String resourceBundleName) throws IOException {
        super(name, resourceBundleName);

        Handler handler = new CustomHandler(formatter);
        handler.setLevel(Level.ALL);
        handler.setFormatter(formatter);

        addHandler(handler);
        setLevel(Level.ALL);
        setUseParentHandlers(false);
    }

    @NotNull
    public File getLogFile() {
        return formatter.getLogFile();
    }

    @RequiredArgsConstructor
    private static class CustomHandler extends Handler {
        protected final @NotNull ConciseFormatter formatter;

        @Override
        public void publish(LogRecord record) {
            System.out.print(getFormatter().format(record));
        }

        @Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void close() throws SecurityException {
            formatter.close();
        }

    }

}
