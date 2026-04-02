package com.aquarius.wizard.player.fx.local;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralizes local audio-tag read/write logic so local scans, embedded-artwork
 * loading, and backend-to-local downloads stay on the same metadata behavior.
 */
public final class LocalAudioMetadataUtils {

    private static final String USER_AGENT = "WizardMusicBox/1.0";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(12))
        .build();

    static {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        Logger.getLogger("org.jaudiotagger.audio").setLevel(Level.OFF);
    }

    private LocalAudioMetadataUtils() {
    }

    public static AudioMetadata readMetadata(
        final Path audioFilePath,
        final String fallbackTitle,
        final String fallbackArtist,
        final String fallbackAlbum
    ) {
        String title = firstNonBlank(fallbackTitle, "未知歌曲");
        String artist = fallbackArtist == null ? "" : fallbackArtist;
        String album = firstNonBlank(fallbackAlbum, "本地音乐");
        Duration duration = Duration.ZERO;

        if (audioFilePath == null || !Files.isRegularFile(audioFilePath)) {
            return new AudioMetadata(title, artist, album, duration);
        }

        try {
            final AudioFile audioFile = AudioFileIO.read(audioFilePath.toFile());
            final AudioHeader audioHeader = audioFile.getAudioHeader();
            if (audioHeader != null && audioHeader.getTrackLength() > 0) {
                duration = Duration.ofSeconds(audioHeader.getTrackLength());
            }
            final Tag tag = audioFile.getTag();
            if (tag != null) {
                title = firstNonBlank(tag.getFirst(FieldKey.TITLE), title);
                artist = firstNonBlank(tag.getFirst(FieldKey.ARTIST), artist);
                album = firstNonBlank(tag.getFirst(FieldKey.ALBUM), album);
            }
        } catch (Exception ignored) {
            return new AudioMetadata(title, artist, album, duration);
        }
        return new AudioMetadata(title, artist, album, duration);
    }

    public static EmbeddedArtwork readArtwork(final Path audioFilePath) {
        if (audioFilePath == null || !Files.isRegularFile(audioFilePath)) {
            return EmbeddedArtwork.empty();
        }
        try {
            final AudioFile audioFile = AudioFileIO.read(audioFilePath.toFile());
            final Tag tag = audioFile.getTag();
            if (tag == null || tag.getFirstArtwork() == null) {
                return EmbeddedArtwork.empty();
            }
            final Artwork artwork = tag.getFirstArtwork();
            final byte[] binaryData = artwork.getBinaryData();
            BufferedImage image = null;
            try {
                image = artwork.getImage();
            } catch (Exception ignored) {
                image = null;
            }
            return new EmbeddedArtwork(binaryData == null ? new byte[0] : binaryData.clone(), image);
        } catch (Exception ignored) {
            return EmbeddedArtwork.empty();
        }
    }

    public static AudioTagWriteResult writeMetadata(
        final Path audioFilePath,
        final String title,
        final String artist,
        final String album,
        final String artworkUrl
    ) {
        if (audioFilePath == null || !Files.isRegularFile(audioFilePath)) {
            return AudioTagWriteResult.failure("目标音频文件不存在。");
        }

        final String resolvedTitle = firstNonBlank(title, stripExtension(audioFilePath.getFileName().toString()));
        final String resolvedArtist = artist == null ? "" : artist.trim();
        final String resolvedAlbum = album == null ? "" : album.trim();
        String warningMessage = "";

        try {
            final AudioFile audioFile = AudioFileIO.read(audioFilePath.toFile());
            final Tag tag = audioFile.getTagOrCreateAndSetDefault();

            tag.setField(FieldKey.TITLE, resolvedTitle);
            if (!resolvedArtist.isBlank()) {
                tag.setField(FieldKey.ARTIST, resolvedArtist);
            }
            if (!resolvedAlbum.isBlank()) {
                tag.setField(FieldKey.ALBUM, resolvedAlbum);
            }

            if (artworkUrl != null && !artworkUrl.isBlank()) {
                Path artworkFile = null;
                try {
                    artworkFile = downloadArtwork(artworkUrl.trim());
                    try {
                        tag.deleteArtworkField();
                    } catch (Exception ignored) {
                    }
                    tag.setField(Artwork.createArtworkFromFile(artworkFile.toFile()));
                } catch (Exception exception) {
                    warningMessage = "音频封面未能写入：" + safeMessage(exception.getMessage());
                } finally {
                    if (artworkFile != null) {
                        try {
                            Files.deleteIfExists(artworkFile);
                        } catch (IOException ignored) {
                        }
                    }
                }
            }

            audioFile.commit();
            return warningMessage.isBlank()
                ? AudioTagWriteResult.ok()
                : AudioTagWriteResult.withWarning(warningMessage);
        } catch (Exception exception) {
            return AudioTagWriteResult.failure("写入音频标签失败：" + safeMessage(exception.getMessage()));
        }
    }

    private static Path downloadArtwork(final String artworkUrl) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(artworkUrl))
            .timeout(Duration.ofSeconds(15))
            .header("User-Agent", USER_AGENT)
            .header("Accept", "image/*,*/*;q=0.8")
            .GET()
            .build();
        final HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 400) {
            throw new IOException("封面下载失败，状态码：" + response.statusCode());
        }
        if (response.body() == null || response.body().length == 0) {
            throw new IOException("封面响应为空。");
        }
        final Path tempFile = Files.createTempFile(
            "wizard-artwork-",
            resolveImageExtension(response.headers().firstValue("Content-Type").orElse(""))
        );
        Files.write(tempFile, response.body());
        return tempFile;
    }

    private static String resolveImageExtension(final String contentType) {
        final String normalized = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (normalized.contains("png")) {
            return ".png";
        }
        if (normalized.contains("webp")) {
            return ".webp";
        }
        if (normalized.contains("gif")) {
            return ".gif";
        }
        return ".jpg";
    }

    private static String stripExtension(final String fileName) {
        final int lastDot = fileName == null ? -1 : fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : firstNonBlank(fileName, "unknown-track");
    }

    private static String firstNonBlank(final String candidate, final String fallback) {
        return candidate == null || candidate.isBlank() ? fallback : candidate;
    }

    private static String safeMessage(final String message) {
        return firstNonBlank(message, "未知原因");
    }

    public record AudioMetadata(
        String title,
        String artist,
        String album,
        Duration duration
    ) {
    }

    public record EmbeddedArtwork(
        byte[] binaryData,
        BufferedImage bufferedImage
    ) {

        public static EmbeddedArtwork empty() {
            return new EmbeddedArtwork(new byte[0], null);
        }

        public boolean isEmpty() {
            return (this.binaryData == null || this.binaryData.length == 0) && this.bufferedImage == null;
        }
    }

    public record AudioTagWriteResult(
        boolean success,
        String message
    ) {

        public static AudioTagWriteResult ok() {
            return new AudioTagWriteResult(true, "");
        }

        public static AudioTagWriteResult withWarning(final String message) {
            return new AudioTagWriteResult(true, firstNonBlank(message, ""));
        }

        public static AudioTagWriteResult failure(final String message) {
            return new AudioTagWriteResult(false, firstNonBlank(message, "写入音频标签失败。"));
        }
    }
}
