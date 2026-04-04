<p align="center">
  <strong>JavaFX Music Player (Wizard Music Box)</strong>
</p>

<p align="center">
  <a target="_blank" href="https://github.com/747897928/Javafx-Music-Player">
    <img src="https://img.shields.io/badge/license-GPL%20v3-blue.svg" />
  </a>
  <a target="_blank" href="https://github.com/747897928/Javafx-Music-Player">
    <img src="https://img.shields.io/badge/Java-17%2B-green.svg" />
    <img src="https://img.shields.io/badge/JavaFX-17.0.1-0A6BFF.svg" />
    <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F.svg" />
    <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.15-2C7BE5.svg" />
    <img src="https://img.shields.io/badge/SQLite-3.51.1.0-003B57.svg" />
    <img src="https://img.shields.io/badge/Jaudiotagger-2.0.3-orange.svg" />
    <img src="https://img.shields.io/badge/Maven-3.9%2B-C71A36.svg" />
  </a>
</p>

<p align="center">
  <a href="./README.md">中文说明</a>
</p>

## Overview

Wizard Music Box is a cross-platform music player built with a `JavaFX` desktop client and a `Spring Boot 4` backend. The desktop app handles playback, mini mode, desktop lyrics, system tray integration, drag-and-drop import, and the main player UI. The backend provides online library ingestion, media file serving, search, and metadata management.

Local audio metadata is parsed with `Jaudiotagger`, including title, album, artist, duration, and embedded artwork. The backend persistence layer uses `MyBatis-Plus`. `SQLite` is the default database, while `MySQL` and `PostgreSQL` drivers are available as optional runtime support.

## Architecture

- `player-common`
  - Shared response models, path helpers, and lightweight Jackson utilities
- `player-model`
  - Shared business models used by both desktop and server modules
- `player-server`
  - Spring Boot 4 backend
- `player-fx`
  - JavaFX desktop client

## Main Features

- Local music import, scan, and playback
- Local `.lrc` lyric loading
- Local audio tag and embedded artwork reading
- Online playlists, playlist details, search, lyrics, and playback URLs served by the backend
- Backend-managed online library import and refresh
- Download online tracks into `./LocalMusic` and write title, artist, album, and artwork tags
- Drag playlist covers out of the app to save them locally
- Batch import audio files and `.lrc` files through drag and drop
- Mini player mode, desktop lyrics, and system tray support

## Default Directories

- Local music
  - `./LocalMusic/Music`
- Local lyrics
  - `./LocalMusic/Lrc`
- Backend online music
  - `./runtime/online/music`
- Backend online lyrics
  - `./runtime/online/lyrics`
- Backend cover images
  - `./runtime/online/covers`
- SQLite database
  - `./runtime/musicbox.db`
- Backend log
  - `./logs/player-server.log`
- Desktop client backend config
  - `./config/player-fx.ini`

## Requirements

- JDK 17+
- Maven 3.9+

No external database is required in the default setup.

## Run

Start the backend first:

```bash
mvn -pl player-server spring-boot:run
```

Then start the desktop client:

```bash
mvn -pl player-fx javafx:run
```

If the desktop client should connect to another backend, edit:

- `config/player-fx.ini`

Example:

```ini
server.base-url=http://127.0.0.1:18080
```

## Packaging

The backend and desktop app are produced separately and should be packaged independently.

- Server packaging scripts
  - `scripts/package-server.ps1`
  - `scripts/package-server.sh`
- Client packaging scripts
  - `scripts/package-client.ps1`
  - `scripts/package-client.sh`

Example:

```powershell
./scripts/package-server.ps1
./scripts/package-client.ps1 -ServerBaseUrl "http://your-server-host:18080"
```

Default output directories:

- `./dist/server/WizardMusicServer`
- `./dist/client/WizardMusicBox`

## UI Preview

1. The Discover Music area is backed by the server. You can refresh its content with the action button on the right.

![Discover music](README.assets/image-20260402195302708.png)

2. The playlist table can show discovered playlists, local music, or search results. Local music is loaded from `LocalMusic/Music`, and lyrics are loaded from `LocalMusic/Lrc`.

![Playlist table](README.assets/image-20260402195507538.png)

3. Lyrics are synchronized and can also be shown in a desktop lyric window. Mini mode supports quick playback control and lyric toggle.

![Desktop lyrics](README.assets/image-20260402195616248.png)

4. Playlist covers can be dragged out to save locally, and audio or `.lrc` files can be dragged into the import area for batch copy.

![Cover drag out](README.assets/image-20260402195739321.png)

![Audio drag import](README.assets/image-20260402195817202.png)

![Audio drag import 2](README.assets/image-20260402195855296.png)

![Audio drag import 3](README.assets/image-20260402195916234.png)

5. More UI screenshots:

![More UI 1](README.assets/image-20260402200030312.png)

![More UI 2](README.assets/image-20260402195940649.png)

## Online Module Notes

The current online module is a backend-managed local online library:

- The backend scans `./runtime/online/...`
- Metadata is synchronized into SQLite by default
- The JavaFX client fetches playlists, search results, lyrics, and playback URLs from backend APIs
