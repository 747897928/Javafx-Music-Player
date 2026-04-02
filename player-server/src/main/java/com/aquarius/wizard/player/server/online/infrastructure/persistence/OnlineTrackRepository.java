package com.aquarius.wizard.player.server.online.infrastructure.persistence;

import com.aquarius.wizard.player.infra.storage.StorageLayout;
import com.aquarius.wizard.player.server.online.domain.model.CatalogSong;
import com.aquarius.wizard.player.server.online.infrastructure.persistence.entity.OnlineTrackEntity;
import com.aquarius.wizard.player.server.online.infrastructure.persistence.mapper.OnlineTrackMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Repository facade over MyBatis-Plus for online track queries.
 */
@Repository
public class OnlineTrackRepository {

    private final OnlineTrackMapper onlineTrackMapper;
    private final StorageLayout storageLayout;

    public OnlineTrackRepository(final OnlineTrackMapper onlineTrackMapper, final StorageLayout storageLayout) {
        this.onlineTrackMapper = Objects.requireNonNull(onlineTrackMapper, "onlineTrackMapper must not be null");
        this.storageLayout = Objects.requireNonNull(storageLayout, "storageLayout must not be null");
    }

    public List<CatalogSong> findAll() {
        return mapSongs(this.onlineTrackMapper.selectList(
            Wrappers.<OnlineTrackEntity>query()
                .orderByAsc("title")
                .orderByAsc("file_name")
        ));
    }

    public List<CatalogSong> findRecent(final int limit) {
        return mapSongs(this.onlineTrackMapper.selectList(
            Wrappers.<OnlineTrackEntity>query()
                .orderByDesc("last_modified_epoch_millis")
                .orderByAsc("file_name")
                .last("LIMIT " + Math.max(1, limit))
        ));
    }

    public List<CatalogSong> findLyricReady(final int limit) {
        return mapSongs(this.onlineTrackMapper.selectList(
            Wrappers.<OnlineTrackEntity>query()
                .isNotNull("relative_lyric_path")
                .ne("relative_lyric_path", "")
                .orderByAsc("title")
                .orderByAsc("file_name")
                .last("LIMIT " + Math.max(1, limit))
        ));
    }

    public List<CatalogSong> search(final String keyword, final int limit) {
        final QueryWrapper<OnlineTrackEntity> queryWrapper = Wrappers.query();
        queryWrapper
            .and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("artist", keyword)
                .or()
                .like("album", keyword))
            .orderByAsc("title")
            .orderByAsc("file_name")
            .last("LIMIT " + Math.max(1, limit));
        return mapSongs(this.onlineTrackMapper.selectList(queryWrapper));
    }

    public Optional<CatalogSong> findBySongId(final String songId) {
        return Optional.ofNullable(this.onlineTrackMapper.selectById(songId))
            .map(this::toCatalogSong);
    }

    @Transactional
    public void replaceAll(final List<CatalogSong> songs) {
        this.onlineTrackMapper.delete(Wrappers.<OnlineTrackEntity>query().isNotNull("song_id"));
        final Instant updatedAt = Instant.now();
        for (final CatalogSong song : songs) {
            this.onlineTrackMapper.insert(toEntity(song, updatedAt));
        }
    }

    private List<CatalogSong> mapSongs(final List<OnlineTrackEntity> entities) {
        return entities.stream().map(this::toCatalogSong).toList();
    }

    private CatalogSong toCatalogSong(final OnlineTrackEntity entity) {
        final String relativeLyricPath = entity.getRelativeLyricPath();
        return new CatalogSong(
            entity.getSongId(),
            entity.getFileName(),
            entity.getFileStem(),
            entity.getRelativeAudioPath(),
            relativeLyricPath == null || relativeLyricPath.isBlank() ? null : relativeLyricPath,
            this.storageLayout.rootDirectory().resolve(entity.getRelativeAudioPath()).normalize(),
            entity.getTitle(),
            entity.getArtist(),
            entity.getAlbum(),
            Duration.ofMillis(Math.max(0L, entity.getDurationMillis())),
            entity.getArtworkAvailable() > 0,
            Instant.ofEpochMilli(Math.max(0L, entity.getLastModifiedEpochMillis()))
        );
    }

    private OnlineTrackEntity toEntity(final CatalogSong song, final Instant updatedAt) {
        final OnlineTrackEntity entity = new OnlineTrackEntity();
        entity.setSongId(song.id());
        entity.setFileName(song.fileName());
        entity.setFileStem(song.fileStem());
        entity.setRelativeAudioPath(song.relativeAudioPath());
        entity.setRelativeLyricPath(song.relativeLyricPath());
        entity.setTitle(song.title());
        entity.setArtist(song.artist());
        entity.setAlbum(song.album());
        entity.setDurationMillis(song.duration().toMillis());
        entity.setArtworkAvailable(song.artworkAvailable() ? 1 : 0);
        entity.setLastModifiedEpochMillis(song.lastModifiedAt().toEpochMilli());
        entity.setUpdatedAtUtc(updatedAt.toString());
        return entity;
    }
}
