/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {
    private static final Timer TIMER = new Timer("SongCredits-Utils");
    private static final ExecutorService SERVICE = Executors.newCachedThreadPool();

    @NotNull
    public static String toString(Object @NotNull [] array, String connector) {
        int iMax = array.length - 1;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; ; i++) {
            builder.append(array[i]);
            if (i == iMax) break;
            builder.append(connector);
        }
        return builder.toString();
    }

    @NotNull //Makes all keys lowercase
    public static JSONObject formatKeys(@NotNull JSONObject object) {
        JSONObject resultObject = new JSONObject();
        object.keySet().forEach(key -> {
            Object obj = object.get(key);
            resultObject.put(key.toLowerCase(), obj);
        });
        return resultObject;
    }

    @NotNull
    public static String getCause(@Nullable Throwable throwable) {
        if (throwable == null) return "Unknown";
        Throwable lastCause;
        Throwable finalCause = throwable;
        do {
            lastCause = finalCause;
            finalCause = lastCause.getCause();
        } while (finalCause != null);
        String message = lastCause.getMessage();
        return message == null ? "Unknown" : message;
    }

    public static void runLater(@NotNull Runnable runnable, long delay) {
        TIMER.schedule(new TimerTask() {

            @Override
            public void run() {
                runnable.run();
            }

        }, delay);
    }

    public static void runAsync(@NotNull Runnable runnable) {
        SERVICE.execute(runnable);
    }

}
