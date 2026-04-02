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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads artwork for local songs and backend compat responses while keeping
 * resource fallback handling in one place.
 */
public final class ArtworkImageLoader {

    private static final String DEFAULT_ARTWORK_RESOURCE = "/images/topandbottom/pandefault.png";
    private static final String LOGO_RESOURCE = "/images/topandbottom/logo.png";

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public Image loadSongArtwork(final SongSummary songSummary) {
        if (songSummary == null) {
            return loadFallbackArtwork();
        }
        final String cacheKey = buildSongCacheKey(songSummary);
        return this.imageCache.computeIfAbsent(cacheKey, ignored -> resolveSongArtwork(songSummary));
    }

    public Image loadPlaylistArtwork(final FxSampleData.PlaylistDetail playlistDetail) {
        if (playlistDetail == null) {
            return loadFallbackArtwork();
        }
        final String cacheKey = "playlist:" + Objects.toString(playlistDetail.sourceType(), "")
            + ":" + Objects.toString(playlistDetail.sourceId(), "")
            + ":" + Objects.toString(playlistDetail.coverImageUrl(), "");
        return this.imageCache.computeIfAbsent(cacheKey, ignored -> resolvePlaylistArtwork(playlistDetail));
    }

    public Image loadLogo() {
        return this.imageCache.computeIfAbsent("logo", ignored -> loadResourceImage(LOGO_RESOURCE));
    }

    public Image loadFallbackArtwork() {
        return this.imageCache.computeIfAbsent("fallback-artwork", ignored -> loadResourceImage(DEFAULT_ARTWORK_RESOURCE));
    }

    private Image resolveSongArtwork(final SongSummary songSummary) {
        if (songSummary.isLocalSource()) {
            final Image localArtwork = loadLocalEmbeddedArtwork(songSummary);
            if (localArtwork != null) {
                return localArtwork;
            }
        }
        if (songSummary.artworkUrl() != null && !songSummary.artworkUrl().isBlank()) {
            return new Image(songSummary.artworkUrl(), true);
        }
        return loadFallbackArtwork();
    }

    private Image resolvePlaylistArtwork(final FxSampleData.PlaylistDetail playlistDetail) {
        if (playlistDetail.coverImageUrl() != null && !playlistDetail.coverImageUrl().isBlank()) {
            return new Image(playlistDetail.coverImageUrl(), true);
        }
        if (playlistDetail.songs() != null && !playlistDetail.songs().isEmpty()) {
            return loadSongArtwork(playlistDetail.songs().get(0));
        }
        return loadFallbackArtwork();
    }

    private Image loadLocalEmbeddedArtwork(final SongSummary songSummary) {
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
                final Image image = new Image(inputStream);
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

    private String buildSongCacheKey(final SongSummary songSummary) {
        final StringBuilder builder = new StringBuilder("song:")
            .append(Objects.toString(songSummary.sourceType(), ""))
            .append(':')
            .append(Objects.toString(songSummary.sourceId(), ""))
            .append(':')
            .append(Objects.toString(songSummary.mediaSource(), ""))
            .append(':')
            .append(Objects.toString(songSummary.artworkUrl(), ""));
        if (songSummary.isLocalSource()) {
            final Path audioFilePath = resolveLocalSongPath(songSummary);
            if (audioFilePath != null) {
                try {
                    builder.append(':').append(Files.getLastModifiedTime(audioFilePath).toMillis());
                    builder.append(':').append(Files.size(audioFilePath));
                } catch (Exception ignored) {
                    builder.append(":local");
                }
            }
        }
        return builder.toString();
    }

    private Image loadResourceImage(final String resourcePath) {
        try (InputStream inputStream = ArtworkImageLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return null;
            }
            return new Image(inputStream);
        } catch (Exception ignored) {
            return null;
        }
    }
}


