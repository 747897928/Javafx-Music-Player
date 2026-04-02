package com.aquarius.wizard.player.server.support.metadata;

import com.aquarius.wizard.player.server.model.BinaryPayload;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

/**
 * Reads metadata and artwork from local audio files.
 */
@Component
public class AudioMetadataReader {

    public AudioTrackMetadata readMetadata(
        final Path audioFilePath,
        final String fallbackTitle,
        final String fallbackArtist,
        final String fallbackAlbum
    ) {
        String title = fallbackTitle;
        String artist = fallbackArtist;
        String album = fallbackAlbum;
        Duration duration = Duration.ZERO;
        boolean artworkAvailable = false;

        try {
            final AudioFile audioFile = AudioFileIO.read(audioFilePath.toFile());
            final Tag tag = audioFile.getTag();
            final AudioHeader audioHeader = audioFile.getAudioHeader();
            if (audioHeader != null && audioHeader.getTrackLength() > 0) {
                duration = Duration.ofSeconds(audioHeader.getTrackLength());
            }
            if (tag != null) {
                title = firstNonBlank(tag.getFirst(FieldKey.TITLE), title);
                artist = firstNonBlank(tag.getFirst(FieldKey.ARTIST), artist);
                album = firstNonBlank(tag.getFirst(FieldKey.ALBUM), album);
                artworkAvailable = tag.getFirstArtwork() != null;
            }
        } catch (Exception ignored) {
            duration = Duration.ZERO;
        }

        return new AudioTrackMetadata(title, artist, album, duration, artworkAvailable);
    }

    public Optional<BinaryPayload> loadArtwork(final Path audioFilePath) {
        try {
            final AudioFile audioFile = AudioFileIO.read(audioFilePath.toFile());
            final Tag tag = audioFile.getTag();
            if (tag == null || tag.getFirstArtwork() == null) {
                return Optional.empty();
            }
            final var artwork = tag.getFirstArtwork();
            final byte[] binaryData = artwork.getBinaryData();
            if (binaryData == null || binaryData.length == 0) {
                return Optional.empty();
            }
            final String mediaType = artwork.getMimeType() == null || artwork.getMimeType().isBlank()
                ? "image/jpeg"
                : artwork.getMimeType();
            return Optional.of(new BinaryPayload(binaryData, mediaType));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String firstNonBlank(final String candidate, final String fallback) {
        return candidate == null || candidate.isBlank() ? fallback : candidate;
    }
}

