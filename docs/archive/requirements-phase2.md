# 第二阶段需求文档

本文件保留第二阶段的实施目标、迁移策略与完成结果；阶段收口后转入归档保存。

## 0. 当前状态

截至当前仓库状态，第二阶段的目标已经完成：

- `player-fx` 在线模块已切到自建 `player-server`
- `player-fx` 不再直连第三方音乐平台
- 后端已提供迁移期 compat 接口、在线文件流接口、在线导入与刷新接口
- 在线模块默认运行形态已落到 `Spring Boot 4 + SQLite + 相对路径目录`
- 本地音乐模块与后端在线模块的存储目录已分离
- `下载当前音乐` 已改成通过后端接口把在线音频、歌词和基础标签写入 `./LocalMusic`
- `player-fx` 本地音乐已统一走本地音频标签工具读取标题、歌手、专辑与内嵌封面

收口说明：

- 发布前仍建议对 JavaFX 真机界面做一轮完整点击回归
- 上述回归建议不再视为第二阶段未完成项

## 1. 阶段目标

第二阶段目标是：

- 由 `player-server` 正式接管当前桌面端原“在线模块”所需的数据入口
- 将桌面端对第三方在线流程的直接依赖替换为对自建 Spring Boot 4 后端的调用
- 在不大改 JavaFX 页面和交互的前提下，先完成“接口接管”
- 允许后端响应结构在迁移期兼容网易云 JSON 子集，以降低桌面端改造成本
- 默认运行形态保持轻量，数据库以 SQLite 为默认方案，不要求额外安装数据库

本阶段完成后，应满足：

- `player-fx` 不再直接请求 `music.163.com`
- `player-fx` 不再保留对 HTML 抓取和网易云 JSON 的直接解析逻辑
- 旧 `LegacyOnlineMusicService` 已下线
- 本地模块主链路保持现状，主要新增工作集中在“在线交互改为自建后端承载”

## 2. 阶段输入

当前已知基础：

- Phase 1 已经把桌面播放器主链路跑通
- `player-server` 已有 Spring Boot 4 骨架与基础配置
- 本地模块当前可视为“基本可用、非主要改造面”
- 桌面端当前真实依赖的 legacy 在线能力主要包括：
  - 推荐歌单
  - 歌单详情
  - 搜索歌曲
  - 歌词获取
  - 播放地址获取
  - 下载当前歌曲时附带歌词落盘

Phase 1 历史记录见：

- `docs/archive/maintenance-requirements-phase1.md`

## 3. 核心策略

### 3.1 接口接管策略

- JavaFX 不再直接调用第三方接口
- JavaFX 统一改调本地 Spring Boot 4 后端
- 后端负责把在线模块需要的歌单、搜索、歌词、播放地址等能力组织成桌面端可消费的接口
- 当前阶段不主动重做本地模块交互；本地模块只做与后端接管相关的必要收口
- 为减少桌面端改造量，迁移期允许后端在控制器响应层输出“网易云兼容 JSON 子集”

### 3.2 兼容边界

允许兼容的只有“响应结构”，不包括：

- 不复制网易云域名、Cookie、鉴权或签名流程
- 不伪装成网易云官方服务
- 不把网易云字段命名扩散到领域模型和基础设施层

这里的“仿网易云后端”在本项目内只表示：

- 保留客户端熟悉的接口路径风格和 JSON 字段结构
- 由自建 Spring Boot 4 服务统一承接这些接口

不表示：

- 项目默认实现第三方未授权抓取、代理或分发逻辑
- 把具体第三方数据源耦合进当前仓库的核心边界

必须遵循：

- 兼容结构只存在于 `player-server` 的 controller / response adapter 层
- `player-model`、`player-common` 继续使用项目自己的语义模型
- 文件系统、元数据同步与存储布局能力收敛在 `player-server` 内部的 `support / repository / mapper` 等包中
- 桌面端只消费当前真正用到的字段子集，禁止为了“看起来像”而复制整套历史字段

### 3.3 数据来源

第二阶段后端默认只服务以下数据：

- 本地扫描的音乐
- 本地导入的歌词
- 本地读取或缓存的封面
- 后端维护的歌单、播放队列快照、历史记录、收藏等数据

结论：

- JSON 可以兼容网易云
- 仓库默认实现仍以自建与本地数据为准

补充说明：

- 如果后续确实需要扩展在线 provider，也应作为独立适配层处理，而不是把第三方实现直接写死到桌面端或领域层
- 开源仓库默认交付内容应优先保证边界清晰、部署轻量、可单机运行

## 4. 当前兼容接口范围

基于历史 `LegacyOnlineMusicService` 的真实消费情况，第二阶段只需要兼容以下接口语义。

### 4.1 推荐歌单

建议接口：

- `GET /api/compat/netease/personalized?limit=18`

建议响应子集：

```json
{
  "code": 200,
  "result": [
    {
      "id": "pl-online-001",
      "name": "在线推荐歌单",
      "picUrl": "http://127.0.0.1:18080/api/files/covers/pl-online-001.jpg",
      "copywriter": "在线模块推荐"
    }
  ]
}
```

### 4.2 歌单详情

建议接口：

- `GET /api/compat/netease/playlist/detail?id={playlistId}`

建议响应子集：

```json
{
  "code": 200,
  "result": {
    "id": "pl-online-001",
    "name": "在线推荐歌单",
    "tags": ["在线", "收藏"],
    "description": "由后端维护的在线歌单详情。",
    "coverImgUrl": "http://127.0.0.1:18080/api/files/covers/pl-online-001.jpg",
    "tracks": [
      {
        "id": "song-online-001",
        "name": "Blue Horizon",
        "dt": 204000,
        "ar": [
          {
            "name": "Studio River"
          }
        ],
        "al": {
          "name": "Night Sketches",
          "picUrl": "http://127.0.0.1:18080/api/files/covers/song-online-001.jpg"
        }
      }
    ]
  }
}
```

### 4.3 搜索歌曲

建议接口：

- `GET /api/compat/netease/search/get/web?s={keyword}&type=1&offset=0&limit=20`

建议响应子集：

```json
{
  "code": 200,
  "result": {
    "songCount": 1,
    "songs": [
      {
        "id": "song-online-001",
        "name": "Blue Horizon",
        "duration": 204000,
        "artists": [
          {
            "name": "Studio River"
          }
        ],
        "album": {
          "name": "Night Sketches",
          "picUrl": "http://127.0.0.1:18080/api/files/covers/song-online-001.jpg"
        }
      }
    ]
  }
}
```

### 4.4 歌词

建议接口：

- `GET /api/compat/netease/song/lyric?id={songId}&lv=1&kv=1&tv=-1`

建议响应子集：

```json
{
  "code": 200,
  "lrc": {
    "lyric": "[00:00.000]Blue Horizon"
  }
}
```

### 4.5 播放地址

建议接口：

- `GET /api/compat/netease/song/enhance/player/url?ids=[\"song-local-001\"]&br=128000`

建议响应子集：

```json
{
  "code": 200,
  "data": [
    {
      "id": "song-local-001",
      "url": "http://127.0.0.1:18080/api/files/audio/song-local-001.mp3"
    }
  ]
}
```

## 5. 服务端实现要求

### 5.1 分层要求

- controller 层负责兼容响应拼装
- service 层负责用项目内部语义组织用例
- repository / mapper / support 层负责读取数据库、文件系统、歌词文件、封面文件
- 不在 controller 中直接写文件扫描和复杂组装逻辑

### 5.2 兼容字段要求

- 仅输出桌面端当前已使用的字段
- `playlist/detail` 必须支持 `result.name`、`result.tags`、`result.description`、`result.coverImgUrl`、`result.tracks`
- `search/get/web` 必须支持 `result.songs`
- `song/lyric` 必须支持 `lrc.lyric`
- `song/enhance/player/url` 必须支持 `data[].id`、`data[].url`
- 歌曲时长同时兼容 `dt` 与 `duration` 两种读法时，服务端至少保证其中一个准确，最好两者同时给出

### 5.3 文件访问要求

- 音频、歌词、封面应通过后端受控路径暴露
- 不把桌面端本地文件绝对路径直接暴露给 UI
- 下载、播放、封面加载统一经由后端接口或后端静态资源映射完成

### 5.4 存储要求

- 默认数据库为 SQLite
- 默认配置必须允许开发者在不安装 MySQL / PostgreSQL 的情况下直接运行
- 后端在线模块的文件默认使用独立相对路径目录，例如 `./runtime/online/music`、`./runtime/online/lyrics`
- MySQL / PostgreSQL 只作为后续扩展能力保留

当前实现约定：

- 在线歌曲文件本体走后端磁盘目录
- 在线歌词文件走后端磁盘目录
- 在线模块的可搜索元数据同步进入默认 SQLite
- `player-fx` 的本地音乐目录 `./LocalMusic/...` 不参与后端在线模块存储

## 6. 桌面端改造要求

- 新建明确的后端接口客户端，不继续复用 `LegacyOnlineMusicService` 里的第三方抓取实现
- 将歌单、搜索、歌词、播放地址获取切换到 `player-server`
- 保持现有 `SongSummary`、`PlaylistDetail` 等 UI 消费模型可用
- 若响应结构兼容成功，桌面端应把改动控制在“请求地址 + 解析入口”层面
- 本地模块已有播放、导入、歌词、迷你模式、托盘等能力不是本阶段重点，只做必要兼容修补

建议替换顺序：

1. 推荐歌单
2. 歌单详情
3. 搜索
4. 歌词
5. 播放地址
6. 下载动作对接后端

当前状态：前 1-6 项已完成

## 7. 清理目标

第二阶段收口前，至少完成以下清理：

- 去掉 `player-fx` 中直连 `music.163.com` 的代码
- 去掉 HTML 抓取逻辑
- 去掉桌面端对网易云接口字段的散落解析
- 将网易云兼容语义收敛到后端 compat controller

## 8. 验收标准

- `mvn -q compile` 通过
- `player-server` 可启动并返回兼容接口数据
- `player-fx` 可在不访问 `music.163.com` 的情况下完成：
  - 发现页加载
  - 歌单详情加载
  - 搜索
  - 歌词显示
  - 音频播放
- 关闭 legacy 直连逻辑后，主链路仍可用

当前验证结论：

- 编译与打包链路已验证通过
- 后端 compat 接口、在线导入、在线刷新与音频流接口已验证
- JavaFX 启动链路已验证
- GUI 手工回归仍建议在发布前执行

## 9. 后续演进

本阶段结束后，可以再进入下一步：

- 逐步废弃 compat 接口
- 把桌面端改为项目自定义 DTO
- 将推荐、歌单、历史、收藏等模型彻底规范为项目自己的 API 合同

但在第二阶段内，优先级仍然是：

- 先完成后端接管
- 再考虑 API 美化
