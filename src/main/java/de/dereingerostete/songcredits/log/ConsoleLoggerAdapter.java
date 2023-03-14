/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.log;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.util.Config;
import de.dereingerostete.songcredits.util.Logging;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class ConsoleLoggerAdapter implements Logger {
    protected final boolean spotifyDebug;

    public ConsoleLoggerAdapter() {
        Config config = SongCredits.getConfig();
        spotifyDebug = config.getBoolean("spotifyDebug", false);
    }

    @Override
    public String getName() {
        return ConsoleLogger.class.getCanonicalName();
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return spotifyDebug;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return spotifyDebug;
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public void trace(String msg) {}

    @Override
    public void trace(String format, Object arg) {}

    @Override
    public void trace(String format, Object arg1, Object arg2) {}

    @Override
    public void trace(String format, Object... arguments) {}

    @Override
    public void trace(String msg, Throwable t) {}

    @Override
    public void trace(Marker marker, String msg) {}

    @Override
    public void trace(Marker marker, String format, Object arg) {}

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {}

    @Override
    public void trace(Marker marker, String format, Object... argArray) {}

    @Override
    public void trace(Marker marker, String msg, Throwable t) {}

    @Override
    public void debug(String msg) {
        if (spotifyDebug) Logging.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        if (spotifyDebug) Logging.debug(String.format(format, arg));
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (spotifyDebug) Logging.debug(String.format(format, arg1, arg2));
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (spotifyDebug) Logging.debug(String.format(format, arguments));
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (spotifyDebug) Logging.debug(msg, t);
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (spotifyDebug) debug(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (spotifyDebug) debug(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (spotifyDebug) debug(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        if (spotifyDebug) debug(format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (spotifyDebug) debug(msg, t);
    }

    @Override
    public void info(String msg) {
        Logging.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        Logging.info(String.format(format, arg));
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        Logging.info(String.format(format, arg1, arg2));
    }

    @Override
    public void info(String format, Object... arguments) {
        Logging.info(String.format(format, arguments));
    }

    @Override
    public void info(String msg, Throwable t) {
        String cause = t == null ? "Unknown" : t.getMessage();
        Logging.info(msg + ": " + cause);
    }

    @Override
    public void info(Marker marker, String msg) {
        info(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        info(format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        info(msg, t);
    }

    @Override
    public void warn(String msg) {
        Logging.warning(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        Logging.warning(String.format(format, arg));
    }

    @Override
    public void warn(String format, Object... arguments) {
        Logging.warning(String.format(format, arguments));
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        Logging.warning(String.format(format, arg1, arg2));
    }

    @Override
    public void warn(String msg, Throwable t) {
        Logging.warning(msg, t);
    }

    @Override
    public void warn(Marker marker, String msg) {
        warn(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        warn(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        warn(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        warn(format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        warn(msg, t);
    }

    @Override
    public void error(String msg) {
        Logging.severe(msg);
    }

    @Override
    public void error(String format, Object arg) {
        Logging.severe(String.format(format, arg));
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        Logging.severe(String.format(format, arg1, arg2));
    }

    @Override
    public void error(String format, Object... arguments) {
        Logging.severe(String.format(format, arguments));
    }

    @Override
    public void error(String msg, Throwable t) {
        Logging.severe(msg, t);
    }

    @Override
    public void error(Marker marker, String msg) {
        error(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        error(format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        error(msg, t);
    }

}
