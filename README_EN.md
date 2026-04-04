# Wizard Music Box

<p align="center">
  <strong>JavaFX Music Player (Wizard Music Box)</strong>
</p>
<p align="center">
  <a target="_blank" href="https://github.com/747897928/Javafx-Music-Player">
    <img src="https://img.shields.io/badge/license-GPL%20v3-blue.svg"></img>
  </a>
  <a target="_blank" href="https://github.com/747897928/Javafx-Music-Player">
    <img src="https://img.shields.io/badge/Java-17%2B-green.svg"></img>
    <img src="https://img.shields.io/badge/JavaFX-17.0.1-0A6BFF.svg"></img>
    <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F.svg"></img>
    <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.15-2C7BE5.svg"></img>
    <img src="https://img.shields.io/badge/SQLite-3.51.1.0-003B57.svg"></img>
    <img src="https://img.shields.io/badge/Jaudiotagger-2.0.3-orange.svg"></img>
    <img src="https://img.shields.io/badge/Maven-3.9%2B-C71A36.svg"></img>
  </a>
</p>

<p align="center">
  <a href="./README.md">中文说明</a>
</p>

## 1. Overview

Wizard Music Box is a desktop music player project that ships both a JavaFX client and a Spring Boot backend in the same repository.

The project currently covers two major areas:

1. Local playback  
   The JavaFX client scans local folders, reads audio metadata, loads lyrics, plays tracks, and provides desktop oriented features such as mini mode, desktop lyrics, and system tray integration.

2. Online library management  
   The backend maintains an online music catalog backed by local files. It handles import, scan, metadata synchronization, search, lyric loading, and media streaming. The desktop client consumes this data over HTTP.

This README is written for both end users and developers. It covers startup, configuration, packaging, module responsibilities, runtime directories, and backend entry points.

## 2. UI Preview

### 2.1 Discover music

The discover music area is backed by the server and can be refreshed from the client UI.

![Discover music](README.assets/image-20260402195302708.png)

### 2.2 Playlist and local library

The table can display discover results, local music, and search results. Local music comes from `LocalMusic/Music`, and local lyrics come from `LocalMusic/Lrc`.

![Playlist and local library](README.assets/image-20260402195507538.png)

### 2.3 Desktop lyrics and mini mode

Lyrics can stay synchronized during playback and can also be shown in a dedicated desktop window.

![Desktop lyrics and mini mode](README.assets/image-20260402195616248.png)

### 2.4 Cover drag out and file import

Playlist covers can be dragged out and saved locally. Audio and lyric files can be dragged into the import area for batch copy.

![Cover drag out](README.assets/image-20260402195739321.png)

![File import 1](README.assets/image-20260402195817202.png)

![File import 2](README.assets/image-20260402195855296.png)

![File import 3](README.assets/image-20260402195916234.png)

### 2.5 More screenshots

![More UI 1](README.assets/image-20260402200030312.png)

![More UI 2](README.assets/image-20260402195940649.png)

## 3. Tech Stack

1. Java 17  
   Defined in the root `pom.xml` as the shared runtime and compilation baseline.

2. JavaFX 17.0.1  
   Used by the `player-fx` desktop module.

3. Spring Boot 4.0.5  
   Used by the `player-server` backend module.

4. MyBatis Plus 3.5.15  
   Used for backend persistence and catalog synchronization.

5. SQLite 3.51.1.0  
   The default database for local and packaged runtime use.

6. Jaudiotagger 2.0.3  
   Used for reading audio tags, artwork, and related metadata.

7. Maven 3.9+  
   Used for the multi module build.

## 4. Main Features

### 4.1 Local music

1. Scan local music folders
2. Read audio metadata from local files
3. Load `.lrc` lyric files
4. Read embedded cover artwork
5. Import audio and lyric files through drag and drop
6. Download online tracks into the local library

### 4.2 Desktop client features

1. Full player shell
2. Mini player mode
3. Desktop lyrics window
4. System tray support
5. Cover drag out support
6. Context menu driven actions

### 4.3 Backend online library features

1. Import online library files
2. Scan audio, lyric, and cover assets under `runtime/online`
3. Synchronize catalog data into SQLite
4. Provide playlist, song, search, lyric, and playback endpoints
5. Serve audio streams and artwork content
6. Provide compatibility endpoints for legacy desktop calls

## 5. Module Layout

### 5.1 `player-common`

Shared infrastructure utilities, including:

1. `ApiResponse`
2. `WorkspacePathResolver`
3. `JacksonUtils`

### 5.2 `player-model`

Shared lightweight models used by both client and server, including:

1. `SongSummary`
2. `PlaylistSummary`
3. `LyricLine`

### 5.3 `player-server`

Backend module responsible for online library management and media access.

1. `controller` for HTTP entry points
2. `service` for catalog query, refresh, and import
3. `repository` and `mapper` for persistence
4. `support/storage` for runtime directory layout and file storage
5. `support/metadata` for audio metadata reading
6. `config` for storage and MyBatis Plus configuration

### 5.4 `player-fx`

Desktop client module responsible for UI, playback, and backend communication.

1. `playback` for media playback control
2. `local` for local scanning and metadata handling
3. `remote` for backend URL resolution and HTTP access
4. `ui` for the JavaFX views and desktop widgets

## 6. Requirements

### 6.1 Runtime requirements

1. JDK 17 or later
2. Maven 3.9 or later
3. A desktop environment capable of running JavaFX

### 6.2 Packaging requirements

If you want to build the desktop distribution image, make sure the current JDK includes `jpackage`.

### 6.3 Default port

The backend listens on:

```text
18080
```

## 7. Quick Start

### 7.1 Start the backend

```bash
mvn -pl player-server spring-boot:run
```

Health check entry:

```text
http://127.0.0.1:18080/api/system/summary
```

### 7.2 Start the desktop client

```bash
mvn -pl player-fx javafx:run
```

### 7.3 Recommended local workflow

1. Start `player-server`
2. Verify `/api/system/summary`
3. Start `player-fx`

## 8. Configuration

### 8.1 Backend configuration

Main backend configuration file:

```text
player-server/src/main/resources/application.yml
```

Important settings include:

1. Application name
2. SQLite datasource location
3. Server port
4. Log file path
5. Online library directories under `runtime/online`

### 8.2 Desktop client configuration

Desktop runtime configuration file:

```text
config/player-fx.ini
```

Example:

```ini
server.base-url=http://127.0.0.1:18080
```

Update this value when the desktop client needs to connect to another backend instance.

## 9. Runtime Directories and Data Files

### 9.1 Common runtime directories

1. Local music

```text
./LocalMusic/Music
```

2. Local lyrics

```text
./LocalMusic/Lrc
```

3. Backend online music

```text
./runtime/online/music
```

4. Backend online lyrics

```text
./runtime/online/lyrics
```

5. Backend online covers

```text
./runtime/online/covers
```

6. Backend cache

```text
./runtime/online/cache
```

7. SQLite database

```text
./runtime/musicbox.db
```

8. Logs

```text
./logs
```

### 9.2 Database schema

Initialization script:

```text
player-server/src/main/resources/schema.sql
```

The main table is `online_track`, which stores:

1. Song id
2. File name and stem
3. Relative audio and lyric paths
4. Title, artist, album
5. Duration
6. Artwork availability
7. Last modified time
8. Synchronization time

## 10. Packaging and Deployment

### 10.1 Package the backend

PowerShell:

```powershell
./scripts/package-server.ps1
```

Shell:

```bash
./scripts/package-server.sh
```

Default output:

```text
./dist/server/WizardMusicServer
```

Zip output:

```text
./dist/server/WizardMusicServer.zip
```

The packaged backend includes:

1. `wizard-music-server.jar`
2. `config/application.yml`
3. `bin/start-server.ps1`
4. `bin/start-server.sh`
5. `logs`
6. `runtime/online/*`

### 10.2 Package the desktop client

PowerShell:

```powershell
./scripts/package-client.ps1
```

Custom backend URL:

```powershell
./scripts/package-client.ps1 -ServerBaseUrl "http://your-server-host:18080"
```

Shell:

```bash
./scripts/package-client.sh
```

Default output:

```text
./dist/client/WizardMusicBox
```

Zip output:

```text
./dist/client/WizardMusicBox.zip
```

The client packaging script uses `jpackage` and writes `config/player-fx.ini` into the generated app image.

## 11. Backend Endpoint Overview

### 11.1 System endpoint

1. `GET /api/system/summary`  
   Returns application name, working directory, storage root, database file path, and startup time.

### 11.2 Online library endpoints

1. `GET /api/online/library/tracks`  
   Returns the current online catalog track list.

2. `POST /api/online/library/refresh`  
   Scans the online library directories and synchronizes the catalog.

3. `POST /api/online/library/import`  
   Imports audio and lyric files through multipart upload.

### 11.3 Media endpoints

1. `GET /api/files/audio/{songId}`  
   Streams audio content for desktop playback.

2. `GET /api/files/covers/song/{songId}`  
   Returns artwork bytes for a given song.

### 11.4 Compatibility endpoints

Base path:

```text
/api/compat/netease
```

Implemented entries:

1. `GET /personalized`
2. `GET /playlist/detail`
3. `GET /search/get/web`
4. `GET /song/lyric`
5. `GET /song/enhance/player/url`

These endpoints keep a smaller legacy shaped contract while the Spring Boot backend serves as the real data source.

## 12. Usage Notes

### 12.1 Local music

1. Put audio files into `LocalMusic/Music`
2. Put lyric files into `LocalMusic/Lrc`
3. Open the local music area in the client
4. Reload the library

### 12.2 Online library

1. The backend reads assets from `runtime/online`
2. Refresh updates the synchronized catalog
3. The client fetches playlists, songs, lyrics, and playback URLs from the backend

### 12.3 Download and drag operations

1. Online songs can be downloaded into the local library
2. Playlist covers can be dragged out of the app
3. Audio and `.lrc` files can be dragged into the import area

## 13. Common Issues

### 13.1 Backend is up but the client cannot connect

Check the following:

1. The backend process is running
2. `server.base-url` in `config/player-fx.ini` points to the expected host
3. Port `18080` is reachable
4. `/api/system/summary` returns a valid response

### 13.2 Desktop packaging fails

Check the following:

1. `jpackage` is available in the current JDK
2. `JAVA_HOME` points to the intended JDK
3. Maven build finished successfully

### 13.3 Local songs or lyrics do not appear

Check the following:

1. Files are stored in the expected directories
2. File extensions are correct
3. The files are readable by the current process

### 13.4 Online library content does not refresh

Check the following:

1. Files exist under `runtime/online`
2. Refresh has been triggered
3. Backend logs contain no import or scan errors

## 14. Development Notes

### 14.1 Recommended workflow

1. Run Maven commands from the repository root
2. Start the backend before the client
3. When changing backend APIs, verify the matching desktop caller
4. When changing remote desktop logic, inspect `player-fx/remote` first

### 14.2 Integration points to watch

1. Both the catalog endpoints and compatibility endpoints are used
2. Media streaming endpoints directly affect playback
3. Directory conventions affect scan, import, playback, and download behavior

### 14.3 Good next steps for the project

1. Add fuller API documentation
2. Expand automated test coverage
3. Improve playback state management boundaries
4. Document database switching and migration steps
5. Extend runtime configuration documentation
