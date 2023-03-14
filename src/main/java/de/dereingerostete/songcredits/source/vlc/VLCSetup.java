/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.vlc;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class VLCSetup {
    private static final @NotNull String INTERFACE = "http";

    public static boolean isValid(@NotNull VLCConfig config) throws IOException {
        File configFile = config.getConfigFile();
        if (configFile == null) return false;

        VLCProperties properties = new VLCProperties(configFile);
        String password = properties.getProperty("http-password");
        String interfaces = properties.getProperty("extraintf");

        int port = properties.getInt("http-port", 8080);
        String host = properties.getProperty("http-host");

        boolean validHost = host == null ? isSelf(config) : host.equals(config.getHost());
        return password != null && password.equals(config.getPassword())
                && interfaces != null && interfaces.contains(INTERFACE)
                && port == config.getPort() && validHost;
    }

    public static void configureVLC(@NotNull VLCConfig config) throws IOException {
        File configFile = config.getConfigFile();
        if (configFile == null) return;

        boolean[] saved = new boolean[4]; //1. Password, 2. Interface, 3. Port 4. Host
        List<String> lines = FileUtils.readLines(configFile, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.startsWith("http-password=")) {
                lines.set(i, "http-password=" + config.getPassword());
                saved[0] = true;
            } else if (line.startsWith("extraintf=")) {
                String value = line.split("=", 2)[1];
                if (value.isEmpty()) value = "http";
                else if (value.contains("http")) value = value + ":http";
                lines.set(i, "extraintf=" + value);
                saved[1] = true;
            } else if (line.startsWith("http-port=")) {
                lines.set(i, "http-port=" + config.getPort());
                saved[2] = true;
            } else if (line.startsWith("http-host=")) {
                lines.set(i, "http-host=" + config.getHost());
                saved[3] = true;
            }
        }

        //Check if lines were edited
        if (!saved[0]) lines.add("http-password=" + config.getPassword());
        if (!saved[1]) lines.add("extraintf=http");
        if (!saved[2]) lines.add("http-port=" + config.getPort());
        if (!saved[3]) lines.add("http-host=" + config.getHost());
        FileUtils.writeLines(configFile, StandardCharsets.UTF_8.name(), lines, false);
    }

    private static boolean isSelf(@NotNull VLCConfig config) throws UnknownHostException {
        try {
            String configHost = config.getHost();
            if (configHost == null) return false;

            InetAddress address = InetAddress.getByName(configHost);
            return address.isAnyLocalAddress() || address.isLoopbackAddress() ||
                    NetworkInterface.getByInetAddress(address) != null;
        } catch (SocketException exception) {
            return false;
        }
    }

}
