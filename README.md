<div align="center">

<img src=".github/icon.png" width="96" height="96" alt="">

# JapaneseChat

Adds the Japanese reading to Minecraft chat typed in romaji

[![build](https://img.shields.io/github/actions/workflow/status/AxAce67/JapaneseChat/ci.yml?branch=main&label=build)](https://github.com/AxAce67/JapaneseChat/actions/workflows/ci.yml) [![release](https://img.shields.io/github/v/release/AxAce67/JapaneseChat?include_prereleases&label=release)](https://github.com/AxAce67/JapaneseChat/releases) [![Modrinth](https://img.shields.io/modrinth/dt/japanesechat?logo=modrinth&label=Modrinth&color=00AF5C)](https://modrinth.com/plugin/japanesechat) [![license](https://img.shields.io/github/license/AxAce67/JapaneseChat)](LICENSE)<br>
![Minecraft](https://img.shields.io/badge/Minecraft-1.18.2%E2%80%9326.3-62B47A) ![server](https://img.shields.io/badge/server-Paper%20%7C%20Purpur-1E88E5) ![Java](https://img.shields.io/badge/Java-17%2B-E76F00)

**English** | [日本語](README.ja.md)

</div>

Download: [Modrinth](https://modrinth.com/plugin/japanesechat) · [GitHub Releases](https://github.com/AxAce67/JapaneseChat/releases)

A Minecraft server plugin (Paper / Purpur) for Japanese chat typed in romaji. The Japanese reading is added after the message:

```
<Steve> kyou ha ii tenki (今日はいい天気)
```

- IME-style romaji to kana (`konnichiha`, `minna`, `gakkou`, `kyou`, `ra-men`, ...), then kanji
- English is left alone. Words with capital letters, URLs and player names stay as they are
  (`kore OK desu` → `これ OK です`), and so do a few English words in a romaji sentence
  (`kyou no event tanoshimi` → `今日の event 楽しみ`). With more English words than that, the message is
  not converted.
- Keeps the chat format of other plugins; the conversion is added to the message
- Players choose whether their messages are converted and whether they see conversions
- English and Japanese messages, per player

## Requirements

- Paper 1.18.2 – 26.x, or a fork such as Purpur. Spigot and Folia are not supported.
  (1.18.2 is the first Paper with MiniMessage built in.)
- Java 17 or newer (whatever your Paper version needs)

## Kanji and privacy

Kanji come from Google Input Tools, an unofficial Google service. For each converted message the
kana are sent to Google; the player name and other messages are not. The plugin waits at most
`kanji.timeout-ms` (2 seconds) and caches results; when Google does not answer, the kana are shown.
Set `kanji.enabled: false` to send nothing and show kana only.

Chat is converted on Paper's chat thread, so waiting for Google never stops the server.

## Commands

`/jp` is short for `/japanesechat`.

| Command | Permission | |
|---|---|---|
| `/jp` | `japanesechat.use` | Your settings and help |
| `/jp on`, `/jp off` | `japanesechat.use` | Convert your own messages or not |
| `/jp view [on\|off]` | `japanesechat.use` | Show conversions of other players' messages or not |
| `/jp test <text>` | `japanesechat.admin` | Try a conversion (also from the console) |
| `/jp reload` | `japanesechat.admin` | Reload config and messages |

`japanesechat.use` is granted to everyone (only players with it have their messages converted),
`japanesechat.admin` to operators. Player choices are saved with the player data.

## Configuration

```yaml
language: auto            # auto (each player's game language), en, ja

kanji:
  enabled: true           # false: kana only, nothing is sent to Google
  timeout-ms: 2000

format: " <gray>(<converted>)"   # MiniMessage, added after the message

defaults:                 # for players who have not used /jp
  convert: true
  view: true
```

To change messages, create `plugins/JapaneseChat/lang/messages_en.properties` (or `messages_ja`) with
only the keys you want to change. The keys are in
[messages.properties](src/main/resources/lang/messages.properties).

Upgrading from 1.x (JapanizeChat): remove the old jar. The `plugins/JapanizeChat` folder is moved to
`plugins/JapaneseChat`, permissions are now `japanesechat.*`, `config.yml` is converted on first start (the old file is kept as `config-1.x.yml`),
and players' "hide conversions" choice is moved over when they join.

## Building

```bash
./gradlew build                     # jar in build/libs, runs the unit tests
./gradlew runServer                 # test server
scripts/smoke-test.sh 1.18.2 26.3   # real servers per version
```

## License

[MIT](LICENSE)
