package com.aquarius.wizard.player.fx.ui;

import com.aquarius.wizard.player.model.SongSummary;
import com.aquarius.wizard.player.fx.local.LocalAudioMetadataUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Loads artwork for local songs and backend compat responses while keeping
 * resource fallback handling in one place.
 *
 * <p>The loader now prefers low memory usage over aggressive reuse. Dynamic song
 * and playlist artwork is loaded on demand at the approximate display size
 * instead of being kept in a long-lived cache. Only tiny static resources such
 * as the logo and fallback cover stay cached for the full app session.</p>
 */
public final class ArtworkImageLoader {

    private static final String DEFAULT_ARTWORK_RESOURCE = "/images/topandbottom/pandefault.png";
    private static final String LOGO_RESOURCE = "/images/topandbottom/logo.png";
    private static volatile Image logoImage;
    private static volatile Image fallbackArtwork;

    public Image loadSongArtwork(final SongSummary songSummary) {
        return loadSongArtwork(songSummary, 256.0, 256.0);
    }

    public Image loadSongArtwork(
        final SongSummary songSummary,
        final double requestedWidth,
        final double requestedHeight
    ) {
        if (songSummary == null) {
            return loadFallbackArtwork();
        }
        return resolveSongArtwork(songSummary, requestedWidth, requestedHeight);
    }

    public Image loadPlaylistArtwork(final FxSampleData.PlaylistDetail playlistDetail) {
        return loadPlaylistArtwork(playlistDetail, 256.0, 256.0);
    }

    public Image loadPlaylistArtwork(
        final FxSampleData.PlaylistDetail playlistDetail,
        final double requestedWidth,
        final double requestedHeight
    ) {
        if (playlistDetail == null) {
            return loadFallbackArtwork();
        }
        return resolvePlaylistArtwork(playlistDetail, requestedWidth, requestedHeight);
    }

    public Image loadLogo() {
        Image cachedLogo = logoImage;
        if (cachedLogo != null) {
            return cachedLogo;
        }
        synchronized (ArtworkImageLoader.class) {
            if (logoImage == null) {
                logoImage = loadResourceImage(LOGO_RESOURCE, 96.0, 96.0);
            }
            return logoImage;
        }
    }

    public Image loadFallbackArtwork() {
        Image cachedFallback = fallbackArtwork;
        if (cachedFallback != null) {
            return cachedFallback;
        }
        synchronized (ArtworkImageLoader.class) {
            if (fallbackArtwork == null) {
                fallbackArtwork = loadResourceImage(DEFAULT_ARTWORK_RESOURCE, 256.0, 256.0);
            }
            return fallbackArtwork;
        }
    }

    private Image resolveSongArtwork(
        final SongSummary songSummary,
        final double requestedWidth,
        final double requestedHeight
    ) {
        if (songSummary.isLocalSource()) {
            final Image localArtwork = loadLocalEmbeddedArtwork(songSummary, requestedWidth, requestedHeight);
            if (localArtwork != null) {
                return localArtwork;
            }
        }
        if (songSummary.artworkUrl() != null && !songSummary.artworkUrl().isBlank()) {
            return loadRemoteImage(songSummary.artworkUrl(), requestedWidth, requestedHeight);
        }
        return loadFallbackArtwork();
    }

    private Image resolvePlaylistArtwork(
        final FxSampleData.PlaylistDetail playlistDetail,
        final double requestedWidth,
        final double requestedHeight
    ) {
        if (playlistDetail.coverImageUrl() != null && !playlistDetail.coverImageUrl().isBlank()) {
            return loadRemoteImage(playlistDetail.coverImageUrl(), requestedWidth, requestedHeight);
        }
        if (playlistDetail.songs() != null && !playlistDetail.songs().isEmpty()) {
            return loadSongArtwork(playlistDetail.songs().get(0), requestedWidth, requestedHeight);
        }
        return loadFallbackArtwork();
    }

    private Image loadLocalEmbeddedArtwork(
        final SongSummary songSummary,
        final double requestedWidth,
        final double requestedHeight
    ) {
        final Path audioFilePath = resolveLocalSongPath(songSummary);
        if (audioFilePath == null || !Files.isRegularFile(audioFilePath)) {
            return null;
        }
        final LocalAudioMetadataUtils.EmbeddedArtwork embeddedArtwork = LocalAudioMetadataUtils.readArtwork(audioFilePath);
        if (embeddedArtwork.isEmpty()) {
            return null;
        }
        final byte[] binaryData = embeddedArtwork.binaryData();
        if (binaryData != null && binaryData.length > 0) {
            try (InputStream inputStream = new ByteArrayInputStream(binaryData)) {
                // Decode embedded artwork near the UI target size instead of
                // always materializing the original file-size cover in memory.
                final Image image = new Image(
                    inputStream,
                    requestedWidth,
                    requestedHeight,
                    true,
                    true
                );
                if (!image.isError()) {
                    return image;
                }
            } catch (Exception ignored) {
            }
        }
        final BufferedImage artworkImage = embeddedArtwork.bufferedImage();
        return artworkImage == null ? null : SwingFXUtils.toFXImage(artworkImage, null);
    }

    private Path resolveLocalSongPath(final SongSummary songSummary) {
        if (songSummary.mediaSource() == null || !songSummary.mediaSource().startsWith("file:")) {
            return null;
        }
        try {
            return Path.of(URI.create(songSummary.mediaSource())).normalize();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Image loadRemoteImage(
        final String imageUrl,
        final double requestedWidth,
        final double requestedHeight
    ) {
        return new Image(imageUrl, requestedWidth, requestedHeight, true, true, true);
    }

    private Image loadResourceImage(
        final String resourcePath,
        final double requestedWidth,
        final double requestedHeight
    ) {
        try (InputStream inputStream = ArtworkImageLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }
            return new Image(inputStream, requestedWidth, requestedHeight, true, true);
        } catch (Exception ignored) {
            return null;
        }
    }
}


