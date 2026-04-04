<p align="center">
	<strong>JavaFX 音乐播放器（Wizard Music Box）</strong>
</p>
<p align="center">
<a target="_blank" href="https://github.com/747897928/Javafx-Music-Player">
    <img src="https://img.shields.io/badge/license-GPL%20v3-blue.svg" ></img>
</a>
<a target="_blank" href="https://github.com/747897928/Javafx-Music-Player">
        <img src="https://img.shields.io/badge/Java-17%2B-green.svg" ></img>
        <img src="https://img.shields.io/badge/JavaFX-17.0.1-0A6BFF.svg" ></img>
        <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F.svg" ></img>
        <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.15-2C7BE5.svg" ></img>
        <img src="https://img.shields.io/badge/SQLite-3.51.1.0-003B57.svg" ></img>
        <img src="https://img.shields.io/badge/Jaudiotagger-2.0.3-orange.svg" ></img>
        <img src="https://img.shields.io/badge/Maven-3.9%2B-C71A36.svg" ></img></a>
</p>

<p align="center">
  <a href="./README_EN.md">English README</a>
</p>

## 介绍

这是一个以 Java 为主的跨平台音乐播放器项目，由 `JavaFX` 桌面端和 `Spring Boot 4` 后端组成。桌面端负责播放控制、迷你模式、桌面歌词、系统托盘、拖拽导入等交互；服务端负责在线曲库导入、媒体文件访问、搜索与元数据管理。

本地音频元数据解析基于 `Jaudiotagger`，可读取歌曲名、专辑、歌手、时长、嵌入封面等信息。后端数据层使用 `MyBatis-Plus`，默认数据库为 `SQLite`，同时保留 `MySQL` 和 `PostgreSQL` 运行时驱动，便于按需切换。

#### 软件架构

- 多模块 Maven 项目
- `player-fx` 为 `JavaFX 17.0.1` 桌面客户端
- `player-server` 为 `Spring Boot 4.0.5` 后端服务
- 后端默认使用 `SQLite`，数据访问基于 `MyBatis-Plus 3.5.15`
- 音频标签解析使用 `Jaudiotagger 2.0.3`
- 当前开发与运行环境建议使用 `JDK 17+`

#### 注意事项

  桌面端基于 JavaFX，并包含较多动画与图片处理逻辑，内存占用不会像原生 GUI 那样低。当前代码已经尽量复用对象、监听器与资源，实际运行时仍建议优先使用 `JDK 17+`，不要再以旧版 `JDK 8` 作为主要运行目标。

#### 使用说明

- 本地优先的跨平台桌面音乐播放器
- JavaFX 桌面端负责 UI、播放控制、桌面歌词、迷你模式、托盘等交互
- Spring Boot 4 后端负责在线模块的数据入口、文件访问、在线导入与元数据管理
- 默认数据库使用 SQLite，不要求额外安装 MySQL / PostgreSQL
- 客户端只调用自建后端

## 当前功能

- 本地音乐导入、扫描、播放
- 本地 `.lrc` 歌词加载
- 本地音频标签与内嵌封面读取
- 在线歌单、歌单详情、搜索、歌词、播放地址由自建后端承接
- 后端在线曲库导入、刷新、音频流访问
- 在线歌曲可下载到 `./LocalMusic`，并写入标题、歌手、专辑、封面标签
- 歌单封面可拖拽到系统保存，或右键复制封面链接
- 支持拖拽音频和 `.lrc` 文件批量导入本地音乐目录
- 迷你模式、桌面歌词、系统托盘

## 使用提示

1. 本地音乐默认从 `./LocalMusic/Music` 扫描，歌词默认从 `./LocalMusic/Lrc` 扫描。
2. 在线歌曲可直接播放，也可以通过“下载当前音乐”保存到本地音乐目录。
3. 歌单封面支持拖拽导出；表格和播放相关操作以右键菜单为主。
4. 本地导入支持音频文件和 `.lrc` 歌词文件一起处理。

## 模块结构

- `player-common`
  - 通用响应、路径工具、轻量 Jackson 工具
- `player-model`
  - 桌面端与服务端共享的轻量业务模型
- `player-server`
  - Spring Boot 4 后端
- `player-fx`
  - JavaFX 桌面客户端

## 默认目录

- 本地音乐
  - `./LocalMusic/Music`
- 本地歌词
  - `./LocalMusic/Lrc`
- 后端在线音乐
  - `./runtime/online/music`
- 后端在线歌词
  - `./runtime/online/lyrics`
- 后端在线封面
  - `./runtime/online/covers`
- SQLite
  - `./runtime/musicbox.db`
- 后端日志
  - `./logs/player-server.log`
- 桌面端后端地址配置
  - `./config/player-fx.ini`

## 环境要求

- JDK 17+
- Maven 3.9+

默认开发模式不要求额外安装数据库。

## 启动方式

先启动后端：

```bash
mvn -pl player-server spring-boot:run
```

再启动桌面端：

```bash
mvn -pl player-fx javafx:run
```

如果桌面端需要连接其他后端地址，可修改：

- `config/player-fx.ini`

示例：

```ini
server.base-url=http://127.0.0.1:18080
```

## 打包与部署

服务端和桌面端是两套独立产物，部署时应分开处理：

- 服务端部署到服务器或自管主机
- 桌面端打包后分发给最终用户

仓库已提供打包脚本：

- 服务端打包
  - `scripts/package-server.ps1`
  - `scripts/package-server.sh`
- 桌面端打包
  - `scripts/package-client.ps1`
  - `scripts/package-client.sh`

示例：

```powershell
./scripts/package-server.ps1
./scripts/package-client.ps1 -ServerBaseUrl "http://your-server-host:18080"
```

打包输出默认位于：

- `./dist/server/WizardMusicServer`
- `./dist/client/WizardMusicBox`

## 界面预览

#### 使用说明


1.  发现音乐的歌单是从后端获取的，可以点击发现音乐栏右边的按钮更新发现音乐栏的内容，这个操作会发送一条https请求。
 ![image-20260402195302708](README.assets/image-20260402195302708.png)

2. 歌单列表可以是发现音乐栏里的一个，也可以是本地音乐，还可以是搜索结果后的，如果要加载本地音乐，点击本地音乐栏右边的文件夹按钮，打开本地音乐文件夹，一般这个文件夹都是相对于程序所在路径，在LocalMusic/Muisc文件夹内放音乐文件重新点击本地音乐栏就能加载本地音乐，如果需要歌词，请在LocalMusic/Lrc文件夹下放.lrc文件，注意，下载当前音乐会把音乐文件下载到LocalMusic文件夹内，lrc和音乐文件会放到对应的文件夹。表格右键会弹出菜单，比如你喜欢当前播放的专辑封面，可以点击菜单栏复制到剪切板。

   ![image-20260402195507538](README.assets/image-20260402195507538.png)

3.  歌词是同步的，支持桌面歌词，让你在写代码听音乐的时候也能看到歌词。精简模式下可方便切换歌曲，播放暂停歌曲和调出or隐藏桌面歌词

![image-20260402195616248](README.assets/image-20260402195616248.png)

4. 新功能：
   拖拽：比如你喜欢歌单封面，你可以选择右键弹出菜单复制链接或者直接拖拽图片出来，鼠标松开即可将图片保存到本地。
   
   ![image-20260402195739321](README.assets/image-20260402195739321.png)

   拖拽音乐或者lrc文件将其批量复制到本地音乐文件夹，鼠标必须移动到在红框内才会生效。

![image-20260402195817202](README.assets/image-20260402195817202.png)

![image-20260402195855296](README.assets/image-20260402195855296.png)

![image-20260402195916234](README.assets/image-20260402195916234.png)

5. 其他

![image-20260402200030312](README.assets/image-20260402200030312.png)

![image-20260402195940649](README.assets/image-20260402195940649.png)

## 在线模块说明

当前在线模块是后端管理的本地在线曲库：

- 后端扫描 `./runtime/online/...`
- 元数据同步到 SQLite
- JavaFX 通过后端接口获取歌单、搜索结果、歌词和播放地址
