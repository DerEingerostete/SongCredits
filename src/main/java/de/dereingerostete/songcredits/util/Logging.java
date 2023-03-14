/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.util;

import de.dereingerostete.songcredits.log.ConsoleLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Level;

public class Logging {
    private static ConsoleLogger logger;
    private static boolean debug;

    public static void init(@Nullable ConsoleLogger newLogger) {
        logger = newLogger;
    }

    @NotNull
    public static File getLogFile() {
        return logger.getLogFile();
    }

    public static void setDebug(boolean enableDebug) {
        debug = enableDebug;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void debug(String message) {
        if (debug) info("[DEBUG] " + message);
    }

    public static void debug(String message, Throwable throwable) {
        if (debug) warning("[DEBUG] " + message, throwable);
    }

    public static void warning(String message) {
        if (logger != null) logger.log(Level.WARNING, message);
        else System.err.println("[WARNING] " + message);
    }

    public static void info(String message) {
        if (logger != null) logger.info(message);
        else System.out.println("[INFO] " + message);
    }

    public static void warning(String message, Throwable throwable) {
        if (logger != null) logger.log(Level.WARNING, message, throwable);
        else {
            System.err.println("[WARNING] " + message);
            throwable.printStackTrace();
        }
    }

    public static void severe(String message) {
        if (logger != null) logger.log(Level.SEVERE, message);
        else System.err.println("[SEVERE] " + message);
    }

    public static void severe(String message, Throwable throwable) {
        if (logger != null) logger.log(Level.SEVERE, message, throwable);
        else {
            System.err.println("[SEVERE] " + message);
            throwable.printStackTrace();
        }
    }

}
