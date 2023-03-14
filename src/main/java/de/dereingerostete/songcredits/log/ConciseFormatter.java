/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.log;

import de.dereingerostete.songcredits.SongCredits;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ConciseFormatter extends Formatter {
    protected final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    protected final PrintStream printStream;
    protected final @NotNull @Getter File logFile;

    public ConciseFormatter() throws IOException {
        Date date = new Date();
        DateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dayFormat.format(date);

        File dataFolder = SongCredits.getDataFolder();
        File directory = new File(dataFolder, "logs");
        if (!directory.exists() && !directory.mkdir())
            throw new IOException("Failed to create log folder:" + directory);

        long folderSize = 0;
        File[] oldLogFiles = directory.listFiles();
        if (oldLogFiles != null)
            for (File file : oldLogFiles)
                folderSize += file.length();
        if (folderSize >= 10000000) {
            int response = JOptionPane.showConfirmDialog(null,
                    "The log folder is larger than 10MB!\n" +
                            "It is recommended to delete old and unused logs\nwhen they become too large. " +
                            "Do you want to delete all old logs?", "Logs löschen",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION)
                for (File file : oldLogFiles) FileUtils.deleteQuietly(file);
        }

        int i = 0;
        File file;
        do {
            if (i == 0) file = new File(directory, formattedDate + ".log");
            else file = new File(directory, formattedDate + '-' + i + ".log");
            i++;
        } while (file.exists());
        this.logFile = file;
        printStream = new PrintStream(file, StandardCharsets.UTF_8.name());
    }

    @Override
    public String format(@NotNull LogRecord record) {
        StringBuilder builder = new StringBuilder();

        builder.append('[');
        builder.append(dateFormat.format(record.getMillis()));
        builder.append("]");
        builder.append(" [");
        builder.append(record.getLevel().getName());
        builder.append("] ");
        builder.append(formatMessage(record));
        builder.append('\n');

        if (record.getThrown() != null) {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(writer));
            builder.append(writer);
        }

        String string = builder.toString();
        printStream.print(string);
        printStream.flush();
        return string;
    }

    public void close() {
        printStream.close();
    }

}
