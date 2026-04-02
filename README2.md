# Wizard Music Box

## 介绍

这是当前重构后的 README 草案，对应现在仓库里的真实实现，而不是旧版直连第三方平台的历史方案。

项目当前定位是：

- 本地优先的跨平台桌面音乐播放器
- JavaFX 桌面端负责 UI、播放控制、桌面歌词、迷你模式、托盘等交互
- Spring Boot 4 后端负责在线模块的数据入口、文件访问、在线导入与元数据管理
- 默认数据库使用 SQLite，不要求额外安装 MySQL / PostgreSQL
- 客户端只调用自建后端，不再直连第三方音乐平台

## 当前功能

- 本地音乐导入、扫描、播放
- 本地 `.lrc` 歌词加载
- 本地音频标签与内嵌封面读取
- 在线歌单、歌单详情、搜索、歌词、播放地址由自建后端承接
- 后端在线曲库导入、刷新、音频流访问
- 在线歌曲可下载到 `./LocalMusic`，并写入标题、歌手、专辑、封面标签
- 迷你模式、桌面歌词、系统托盘

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

## 在线模块说明

当前在线模块的默认实现不是第三方抓取，而是后端管理的本地在线曲库：

- 后端扫描 `./runtime/online/...`
- 元数据同步到 SQLite
- JavaFX 通过后端接口获取歌单、搜索结果、歌词和播放地址
- 迁移期为了减少 UI 改动，后端保留了少量“网易云风格 JSON 子集”的 compat 接口

这层 compat 只兼容响应结构，不代表项目继续依赖网易云或任何未授权第三方平台。

## 合规边界

- 不再新增 `jsoup` 抓取、第三方直连解析、未授权下载逻辑
- 不复制第三方 Cookie、鉴权、签名流程
- 仓库默认实现只处理用户本地和后端管理的内容

## 当前状态

第二阶段核心目标已经完成：

- `player-fx` 在线模块已切到 `player-server`
- 第三方直连逻辑已移除
- 默认运行形态已落到 `Spring Boot 4 + SQLite`

## 文档

- 长期维护约束：`docs/maintenance-requirements.md`
- 第二阶段归档：`docs/archive/requirements-phase2.md`
- 第一阶段历史归档：`docs/archive/maintenance-requirements-phase1.md`
