/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.audiocredits;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class VLCGetMediaTest {

    public static void main(@NotNull String[] args) throws Exception {
        byte[] responseBytes = doRequest("requests/status.json", args);
        String request = new String(responseBytes, StandardCharsets.UTF_8);
        System.out.println(request);

        responseBytes = doRequest("art", args);
        ByteArrayInputStream byteStream = new ByteArrayInputStream(responseBytes);
        BufferedImage image = ImageIO.read(byteStream);
        ImageIO.write(image, "PNG", new File("cover-test.png"));
    }

    private static byte[] doRequest(@NotNull String request, @NotNull String[] args) throws IOException {
        URL url = new URL("http://127.0.0.1:8080/" + request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        byte[] bytes = (":" + args[0]).getBytes(StandardCharsets.UTF_8);
        Base64.Encoder encoder = Base64.getEncoder();
        String authentication = encoder.encodeToString(bytes);
        connection.setRequestProperty("Authorization", "Basic " + authentication);
        return IOUtils.toByteArray(connection.getInputStream());
    }

}
