# Changelog

All notable changes to this project are documented here. Versions follow
[Semantic Versioning](https://semver.org/).

## Unreleased

## 2.0.0 - 2026-11-23

A rewrite, renamed from JapanizeChat. The old `plugins/JapanizeChat` folder is moved to
`plugins/JapaneseChat`, the 1.x config is converted automatically and kept as `config-1.x.yml`; players' choices
are moved into their player data when they join.

### Added

- `/jp on|off` for your own messages, `/jp view [on|off]` for other players' conversions.
- `/jp test <text>` to try a conversion, also from the console.
- `format` for the appended conversion (MiniMessage), `kanji.enabled` for kana only, `kanji.timeout-ms`.
- Words with capital letters, URLs and player names are kept as written, and so are a few English
  words in a romaji sentence. Only the Japanese parts are sent for kanji conversion.
- Messages per player language, overridable from the plugin folder.
- Support for Paper 1.18.2 through 26.x (Java 17+), tested on real servers in CI.

### Changed

- Converted words are joined without spaces (`kyou ha ii tenki` → `今日はいい天気`).
- Renamed from JapanizeChat to JapaneseChat (permissions `japanesechat.*`).
- `/jp toggle` is replaced by `/jp view`.

### Fixed

- The request to Google had no time limit; a message sent by a plugin on the main thread could stop
  the server. Requests now time out, results are cached, and synchronous chat gets kana only.
- English messages were converted (`hello` → `へllo`).
- Romaji mistakes: `konnichiha`, `minna` (lost ん), `cho` (ちょう), and missing spellings such as
  `nya`, `ryo`, `fa`, `thi`, `-` and punctuation.
- The chat format of other plugins was replaced by the default one.
- `default-toggle-state` was ignored.
- An unused library was bundled into the jar.

## 1.0

First release.
