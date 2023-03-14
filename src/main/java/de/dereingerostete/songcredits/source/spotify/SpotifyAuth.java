/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.spotify;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.dereingerostete.songcredits.util.GuiUtil;
import de.dereingerostete.songcredits.util.Logging;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class SpotifyAuth {
    protected final @NotNull String htmlTemplate;
    protected final @NotNull URI requestURI;
    protected final @NotNull HttpServer server;
    protected CompletableFuture<String> future;

    public SpotifyAuth(@NotNull URI uri) throws IOException {
        this.requestURI = uri;
        InputStream inputStream = GuiUtil.getInputStream("html-template.html");
        htmlTemplate = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        InetSocketAddress address = new InetSocketAddress(80);
        server = HttpServer.create(address, 0);
        server.createContext("/", new RootHandler(this));
        server.createContext("/success", new TextResponseHandler(htmlTemplate, "Success",
                "Successfully authenticated. You can close the window now", 200));
        server.createContext("/failed", new TextResponseHandler(htmlTemplate, "Error",
                "An unexpected error occurred during authentication. Please try again.", 500));
        server.setExecutor(null);
        server.start();

    }

    public Future<String> getCode() throws IOException {
        future = new CompletableFuture<>();
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(requestURI);
        return future;
    }

    public void shutdown() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                server.stop(1);
            }

        }, 3000);
    }

    @AllArgsConstructor
    private static class RootHandler implements HttpHandler {
        protected final @NotNull SpotifyAuth auth;

        @Override
        public void handle(@NotNull HttpExchange exchange) throws IOException {
            try {
                URI uri = exchange.getRequestURI();
                String code = getCodeFromURI(uri);
                if (code == null) throw new IllegalStateException("Code not found");
                else if (auth.future != null && !auth.future.isDone()) {
                    Logging.debug("Retrieved Spotify auth code");
                    auth.future.complete(code);
                }

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Location", "/success");
                exchange.sendResponseHeaders(302,-1);
            } catch (Throwable throwable) {
                Logging.warning("Failed to retrieve code from auth", throwable);
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Location", "/failed");
                exchange.sendResponseHeaders(301,-1);
            }
        }

        @Nullable
        private String getCodeFromURI(@NotNull URI uri) {
            String query = uri.getQuery();
            if (query == null) return null;

            Charset charset = StandardCharsets.UTF_8;
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), charset);
                if (key.equals("code")) {
                    String code = pair.substring(idx + 1);
                    return URLDecoder.decode(code, charset);
                }
            }
            return null;
        }

    }

    @AllArgsConstructor
    private static class TextResponseHandler implements HttpHandler {
        protected final @NotNull String htmlTemplate;
        protected final @NotNull String title;
        protected final @NotNull String message;
        protected final int responseCode;

        @Override
        public void handle(@NotNull HttpExchange exchange) throws IOException {
            String response = htmlTemplate.replace("{title}", title)
                    .replace("{message}", message)
                    .replace("{website-title}", title);

            exchange.sendResponseHeaders(responseCode, response.length());
            OutputStream outputStream = exchange.getResponseBody();
            IOUtils.write(response, outputStream, StandardCharsets.UTF_8);
        }

    }

}
