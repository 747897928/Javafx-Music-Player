package com.aquarius.wizard.player.fx.ui;

import com.aquarius.wizard.player.model.SongSummary;
import com.aquarius.wizard.player.fx.local.LocalAudioMetadataUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Loads artwork for local songs and backend compat responses while keeping
 * resource fallback handling in one place.
 *
 * <p>The same song can be rendered in multiple places at once: playlist rows,
 * current-playing card, mini mode, and lyrics drawer. This loader caches
 * resolved images so JavaFX does not repeatedly decode the same cover on every
 * repaint.</p>
 *
 * <p>The cache is intentionally shared across loader instances and capped with
 * a small LRU policy. Without that cap, every screen-level component would keep
 * its own growing map and long sessions with many songs could accumulate a large
 * number of decoded images.</p>
 */
public final class ArtworkImageLoader {

    private static final String DEFAULT_ARTWORK_RESOURCE = "/images/topandbottom/pandefault.png";
    private static final String LOGO_RESOURCE = "/images/topandbottom/logo.png";
    private static final int MAX_CACHE_ENTRIES = 256;

    private static final Map<String, Image> IMAGE_CACHE = Collections.synchronizedMap(
        new LinkedHashMap<>(MAX_CACHE_ENTRIES, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<String, Image> eldest) {
                return size() > MAX_CACHE_ENTRIES;
            }
        }
    );

    public Image loadSongArtwork(final SongSummary songSummary) {
        if (songSummary == null) {
            return loadFallbackArtwork();
        }
        final String cacheKey = buildSongCacheKey(songSummary);
        return loadCachedImage(cacheKey, () -> resolveSongArtwork(songSummary));
    }

    public Image loadPlaylistArtwork(final FxSampleData.PlaylistDetail playlistDetail) {
        if (playlistDetail == null) {
            return loadFallbackArtwork();
        }
        final String cacheKey = "playlist:" + Objects.toString(playlistDetail.sourceType(), "")
            + ":" + Objects.toString(playlistDetail.sourceId(), "")
            + ":" + Objects.toString(playlistDetail.coverImageUrl(), "");
        return loadCachedImage(cacheKey, () -> resolvePlaylistArtwork(playlistDetail));
    }

    public Image loadLogo() {
        return loadCachedImage("logo", () -> loadResourceImage(LOGO_RESOURCE));
    }

    public Image loadFallbackArtwork() {
        return loadCachedImage("fallback-artwork", () -> loadResourceImage(DEFAULT_ARTWORK_RESOURCE));
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
                // Prefer constructing the JavaFX Image directly from the raw
                // bytes. This avoids an extra BufferedImage conversion when the
                // embedded artwork bytes are already valid for JavaFX.
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
                    // Local artwork can change after the app has started when a
                    // user rewrites tags or redownloads a cover. Including file
                    // timestamp and size gives us cheap cache invalidation
                    // without rescanning audio tags on every render.
                    builder.append(':').append(Files.getLastModifiedTime(audioFilePath).toMillis());
                    builder.append(':').append(Files.size(audioFilePath));
                } catch (Exception ignored) {
                    builder.append(":local");
                }
            }
        }
        return builder.toString();
    }

    private Image loadCachedImage(final String cacheKey, final java.util.function.Supplier<Image> imageSupplier) {
        synchronized (IMAGE_CACHE) {
            final Image cachedImage = IMAGE_CACHE.get(cacheKey);
            if (cachedImage != null) {
                return cachedImage;
            }
            final Image resolvedImage = imageSupplier.get();
            if (resolvedImage != null) {
                IMAGE_CACHE.put(cacheKey, resolvedImage);
            }
            return resolvedImage;
        }
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


