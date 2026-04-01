package cn.gxust.player.fx.ui;

import cn.gxust.player.domain.model.SongSummary;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads artwork for local and legacy-online songs while keeping resource
 * fallback handling in one place.
 */
public final class ArtworkImageLoader {

    private static final String DEFAULT_ARTWORK_RESOURCE = "/images/topandbottom/pandefault.png";
    private static final String LOGO_RESOURCE = "/images/topandbottom/logo.png";

    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public Image loadSongArtwork(final SongSummary songSummary) {
        if (songSummary == null) {
            return loadFallbackArtwork();
        }
        final String cacheKey = "song:" + Objects.toString(songSummary.sourceType(), "")
            + ":" + Objects.toString(songSummary.sourceId(), "")
            + ":" + Objects.toString(songSummary.mediaSource(), "")
            + ":" + Objects.toString(songSummary.artworkUrl(), "");
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
        try {
            final AudioFile audioFile = AudioFileIO.read(audioFilePath.toFile());
            final Tag tag = audioFile.getTag();
            if (tag == null || tag.getFirstArtwork() == null) {
                return null;
            }
            final BufferedImage artworkImage = tag.getFirstArtwork().getImage();
            return artworkImage == null ? null : SwingFXUtils.toFXImage(artworkImage, null);
        } catch (Exception ignored) {
            return null;
        }
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
