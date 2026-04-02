CREATE TABLE IF NOT EXISTS online_track (
    song_id TEXT PRIMARY KEY,
    file_name TEXT NOT NULL,
    file_stem TEXT NOT NULL,
    relative_audio_path TEXT NOT NULL,
    relative_lyric_path TEXT,
    title TEXT NOT NULL,
    artist TEXT NOT NULL,
    album TEXT NOT NULL,
    duration_millis INTEGER NOT NULL,
    artwork_available INTEGER NOT NULL,
    last_modified_epoch_millis INTEGER NOT NULL,
    updated_at_utc TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_online_track_title
    ON online_track(title);

CREATE INDEX IF NOT EXISTS idx_online_track_artist
    ON online_track(artist);

CREATE INDEX IF NOT EXISTS idx_online_track_last_modified
    ON online_track(last_modified_epoch_millis DESC);
