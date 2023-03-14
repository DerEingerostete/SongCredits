/*
 * Copyright (C) 2022 DerEingerostete
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/
 */

package de.dereingerostete.songcredits.source.spotify;

import de.dereingerostete.songcredits.SongCredits;
import de.dereingerostete.songcredits.gui.MainGui;
import de.dereingerostete.songcredits.gui.tab.SpotifyPanel;
import de.dereingerostete.songcredits.song.Song;
import de.dereingerostete.songcredits.song.Song.Metadata;
import de.dereingerostete.songcredits.source.AudioSource;
import de.dereingerostete.songcredits.source.PlayingState;
import de.dereingerostete.songcredits.source.SourceException;
import de.dereingerostete.songcredits.source.SourceType;
import de.dereingerostete.songcredits.util.Config;
import de.dereingerostete.songcredits.util.Logging;
import de.dereingerostete.songcredits.util.Utils;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.enums.Action;
import se.michaelthelin.spotify.enums.AuthorizationScope;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

public class SpotifySource extends AudioSource {
    protected final boolean countPausedAsStopped;
    protected final SpotifyApi spotifyApi;
    protected Timer timer;
    protected boolean timerRunning;

    private Song currentSong;
    private PlayingState currentState;
    private int totalLength = -1; //in ms
    private int currentPosition = -1; //in ms

    public SpotifySource(@NotNull SpotifyConfig config) throws SourceException {
        super(SourceType.SPOTIFY, config);
        if (!config.isComplete()) throw new SourceException("Config is incomplete");

        this.countPausedAsStopped = config.isCountPausedAsStopped();
        timer = new Timer("SpotifyAuthRefresh", true);
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(config.getClientId())
                .setClientSecret(config.getClientSecret())
                .setRedirectUri(SpotifyHttpManager.makeUri("http://localhost/"))
                .build();
    }

    @Nullable
    @Override
    public Song getCurrentSong() {
        return currentSong;
    }

    @NotNull
    @Override
    public PlayingState getCurrentState() {
        return Objects.requireNonNull(currentState, "State is not loaded");
    }

    @Override
    public void refresh() throws SourceException {
        try {
            CurrentlyPlaying playing = spotifyApi.getUsersCurrentlyPlayingTrack().build().execute();
            if (playing == null) { //Possible causes: Spotify is not open or Spotify was paused for too long
                //Logging.debug("Spotify: Response was null. Changing state to stopped");
                currentState = PlayingState.STOPPED;
                currentSong = null;
                return;
            }

            if (playing.getIs_playing() && playing.getItem() != null) {
                //Logging.debug("Spotify: Updating song and duration");
                IPlaylistItem item = playing.getItem();
                currentSong = getTrackById(item.getId());
                totalLength = item.getDurationMs();
                //Logging.debug("Spotify: Set song to: " + currentSong);
            } else {
                //Logging.debug("Spotify: Item is null. Clearing song");
                currentSong = null;
            }

            currentPosition = playing.getProgress_ms();
            if (playing.getIs_playing()) currentState = PlayingState.PLAYING;
            else if (countPausedAsStopped) currentState = PlayingState.STOPPED;
            else {
                //Is pausing is currently disabled the song was paused
                EnumSet<Action> actions = playing.getActions().getDisallows().getDisallowedActions();
                currentState = actions.contains(Action.PAUSING) ? PlayingState.PAUSED : PlayingState.STOPPED;
            }
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new SourceException("Failed to get current spotify playing track", exception);
        }
    }

    @NotNull
    private Song getTrackById(@NotNull String id) throws SourceException {
        try {
            Track track = spotifyApi.getTrack(id).build().execute();
            Song song = new Song();
            song.addMetadata(Metadata.TITLE, track.getName());

            String[] artists = Stream.of(track.getArtists())
                    .map(ArtistSimplified::getName).toArray(String[]::new);
            String artist = Utils.toString(artists, ", ");
            song.addMetadata(Metadata.ARTIST, artist);

            AlbumSimplified album = track.getAlbum();
            addDateMetadata(song, album.getReleaseDate());
            song.addMetadata(Metadata.ALBUM, album.getName());

            Image image = getBiggestImage(album);
            if (image != null) {
                String imageUrl = image.getUrl();
                song.addMetadata(Metadata.ARTWORK_URL, imageUrl);
                song.addMetadata(Metadata.COVER, imageUrl);
            }

            int duration = track.getDurationMs() / 1000;
            song.addMetadata(Metadata.DURATION, String.valueOf(duration));

            boolean explicit = track.getIsExplicit();
            song.addMetadata(Metadata.EXPLICIT, explicit ? "Yes" : "No");

            song.addMetadata(Metadata.DISC_NUMBER, track.getDiscNumber().toString());
            song.addMetadata(Metadata.TRACK_NUMBER, track.getTrackNumber().toString());
            song.addMetadata(Metadata.FILE_NAME, track.getName());
            return song;
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new SourceException("Failed to get song by id", exception);
        }
    }

    private void addDateMetadata(@NotNull Song song, @NotNull String releaseDate) throws ParseException {
        song.addMetadata(Metadata.RELEASE, releaseDate);

        String[] splitDate = releaseDate.split("-");
        if (splitDate.length == 0 || splitDate.length > 3)
            throw new ParseException("Date returns invalid value");

        song.addMetadata(Metadata.RELEASE_YEAR, splitDate[0]);
        if (splitDate.length >= 2) song.addMetadata(Metadata.RELEASE_MONTH, splitDate[1]);
        if (splitDate.length == 3) song.addMetadata(Metadata.RELEASE_DAY, splitDate[2]);
    }

    @Nullable
    private Image getBiggestImage(@NotNull AlbumSimplified album) {
        Image[] images = album.getImages();
        if (images.length == 0) return null;

        Image biggestImage = null;
        int biggestSize = 0;
        for (Image image : images) {
            int height = image.getHeight();
            if (height > biggestSize) {
                biggestImage = image;
                biggestSize = height;
            }
        }
        return biggestImage;
    }

    @Override
    public boolean nextSong() throws SourceException {
        try {
            spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
            currentPosition = 0;
            totalLength = 0;
            return true;
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new SourceException("Failed to skip to next song", exception);
        }
    }

    @Override
    public boolean previousSong() throws SourceException {
        try {
            spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute();
            currentPosition = 0;
            totalLength = 0;
            return true;
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new SourceException("Failed to return to previous song", exception);
        }
    }

    @Override
    public boolean pause() throws SourceException {
        try {
            spotifyApi.pauseUsersPlayback().build().execute();
            return true;
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new SourceException("Failed to pause the current song", exception);
        }
    }

    @Override
    public boolean resume() throws SourceException {
        try {
            spotifyApi.startResumeUsersPlayback().build().execute();
            return true;
        } catch (IOException | SpotifyWebApiException | ParseException exception) {
            throw new SourceException("Failed to resume the current song", exception);
        }
    }

    @Override
    public int getCurrentLength() {
        return totalLength;
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public void close() {
        timer.cancel();
    }

    @Override
    public void authorize() throws SourceException {
        try {
            Config mainConfig = SongCredits.getConfig();
            JSONObject object = mainConfig.getSource("spotify");
            if (object == null) {
                object = new JSONObject();
                object.put("refreshToken", login());
                mainConfig.set("spotify", object);
                mainConfig.save();
                return;
            }

            String refreshToken = object.optString("refreshToken", null);
            if (refreshToken != null) {
                spotifyApi.setRefreshToken(refreshToken);

                long currentTime = System.currentTimeMillis();
                long expireDate = object.optLong("accessTokenExpire", -1);
                if (expireDate <= currentTime) {
                    refreshAuthorization();
                    return;
                }

                String accessToken = object.optString("accessToken", null);
                if (accessToken != null) {
                    AuthorizationCodeCredentials.Builder builder = new AuthorizationCodeCredentials.Builder();
                    builder.setAccessToken(accessToken);
                    builder.setExpiresIn((int) ((expireDate - currentTime) / 1000));
                    refreshAuthorization(builder.build());
                } else refreshAuthorization();
            } else {
                refreshToken = login();
                object.put("refreshToken", refreshToken);
                mainConfig.save();
            }
        } catch (Exception exception) {
            throw new SourceException("Failed to authorize with the spotify servers", exception);
        }
    }

    @NotNull
    public String login() throws Exception {
        URI uri = spotifyApi.authorizationCodeUri()
                .scope(AuthorizationScope.USER_MODIFY_PLAYBACK_STATE,
                        AuthorizationScope.USER_READ_CURRENTLY_PLAYING)
                .build().execute();

        SpotifyAuth auth = new SpotifyAuth(uri);
        String code = auth.getCode().get();
        auth.shutdown();

        if (code == null) throw new IOException("Code could not be extracted from uri");
        Logging.debug("User logged in with Spotify");

        AuthorizationCodeCredentials credentials = spotifyApi.authorizationCode(code).build().execute();
        spotifyApi.setRefreshToken(credentials.getRefreshToken());
        refreshAuthorization(credentials);
        return credentials.getRefreshToken();
    }

    private void refreshAuthorization() {
        try {
            AuthorizationCodeCredentials credentials = spotifyApi.authorizationCodeRefresh().build().execute();
            refreshAuthorization(credentials);
        } catch (ParseException | IOException | SpotifyWebApiException exception) {
            Logging.warning("Failed to refresh spotify auth", exception);
            if (timerRunning) {
                timer.cancel();
                timer = new Timer("SpotifyAuthRefresh", true);
            }
            timerRunning = true;
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    refreshAuthorization();
                }

            }, 5000L);
        }
    }

    private void refreshAuthorization(@NotNull AuthorizationCodeCredentials credentials) {
        String accessToken = credentials.getAccessToken();
        spotifyApi.setAccessToken(accessToken);
        SpotifyPanel panel = MainGui.getMainGui().getSpotifyPanel();
        if (panel != null) panel.setAccessToken(accessToken);

        long expiresIn = Math.max(credentials.getExpiresIn() - 5, 1) * 1000L;
        if (timerRunning) {
            timer.cancel();
            timer = new Timer("SpotifyAuthRefresh", true);
        }
        timerRunning = true;
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                refreshAuthorization();
            }

        }, expiresIn);

        Utils.runAsync(() -> {
            Config mainConfig = SongCredits.getConfig();
            JSONObject spotifyObject = mainConfig.getSource("spotify");
            if (spotifyObject == null) return;

            spotifyObject.put("accessToken", credentials.getAccessToken());
            spotifyObject.put("accessTokenExpire", System.currentTimeMillis() + expiresIn);
            mainConfig.saveQuietly();
        });
        Logging.debug("Refreshed Spotify authorization. Schedule new refresh in " + expiresIn + "ms");
    }

}
