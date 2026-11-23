# JapaneseChat

[English](README.md) | **日本語**

ダウンロード: [Modrinth](https://modrinth.com/plugin/japanesechat) · [GitHub Releases](https://github.com/AxAce67/JapaneseChat/releases)

ローマ字で打ったチャットに、日本語の読みを付ける Minecraft サーバー（Paper / Purpur）用のプラグインです。発言の後ろに変換結果が付きます。

```
<Steve> kyou ha ii tenki (今日はいい天気)
```

- IME と同じようにローマ字をかなに変換し（`konnichiha`、`minna`、`gakkou`、`kyou`、`ra-men` など）、さらに漢字に変換
- 英語は変換しません。大文字を含む単語・URL・プレイヤー名はそのまま残します（`kore OK desu` → `これ OK です`）。
  ローマ字の文に少しだけ混ざった英単語もそのまま残します（`kyou no event tanoshimi` → `今日の event 楽しみ`）。
  英単語がそれより多い発言は変換しません。
- 他のプラグインのチャットの見た目（書式）を崩しません。変換結果は発言の後ろに付け足します
- 自分の発言を変換するか、他の人の変換を表示するかを、プレイヤーごとに選べます
- メッセージは日本語と英語。プレイヤーのゲームの言語に合わせて自動で切り替え

## 動作環境

- Paper 1.18.2〜26.x（Purpur などのフォークも可）。Spigot と Folia には対応していません。
  （MiniMessage が Paper に入ったのが 1.18.2 のため、それより前には対応していません）
- Java 17 以降（Paper のバージョンが必要とするもの）

## 漢字変換とプライバシー

漢字への変換には、Google の非公式サービス「Google Input Tools」を使います。変換する発言ごとに、
そのかなが Google に送られます（プレイヤー名やほかの発言は送りません）。応答を待つのは最大
`kanji.timeout-ms`（2 秒）までで、結果は一時保存して使い回します。応答がないときは、かなを表示します。
`kanji.enabled: false` にすると Google には何も送らず、かなだけを表示します。

変換は Paper のチャット用スレッドで行うため、Google の応答を待ってもサーバーは止まりません。

## コマンド

`/jp` は `/japanesechat` の省略形です。

| コマンド | 権限 | |
|---|---|---|
| `/jp` | `japanesechat.use` | 自分の設定とヘルプ |
| `/jp on`、`/jp off` | `japanesechat.use` | 自分の発言を変換する・しない |
| `/jp view [on\|off]` | `japanesechat.use` | 他の人の発言の変換を表示する・しない |
| `/jp test <文章>` | `japanesechat.admin` | 変換を試す（コンソールからも可） |
| `/jp reload` | `japanesechat.admin` | 設定とメッセージを再読み込み |

`japanesechat.use` は全員（この権限があるプレイヤーの発言だけが変換されます）、`japanesechat.admin` は OP
に付きます。プレイヤーの選択はプレイヤーデータに保存されます。

## 設定

```yaml
language: auto            # auto（プレイヤーのゲームの言語）、en、ja

kanji:
  enabled: true           # false にすると、かなのみ（Google に何も送らない）
  timeout-ms: 2000

format: " <gray>(<converted>)"   # 発言の後ろに付ける書式（MiniMessage）

defaults:                 # /jp で選んでいないプレイヤーの設定
  convert: true
  view: true
```

メッセージを変えたいときは、`plugins/JapaneseChat/lang/messages_ja.properties` を作り、変えたいキーだけを
書いてください。キーの一覧は [messages_ja.properties](src/main/resources/lang/messages_ja.properties) にあります。

1.x（JapanizeChat）からの更新: 古い jar は削除してください。`plugins/JapanizeChat` フォルダーは `plugins/JapaneseChat`
に移され、権限は `japanesechat.*` に変わります。`config.yml` は最初の起動時に自動で変換されます（元のファイルは `config-1.x.yml` として残ります）。
「変換を表示しない」設定は、各プレイヤーが参加したときに引き継がれます。

## ビルド

```bash
./gradlew build                     # build/libs に jar を作成し、単体テストを実行
./gradlew runServer                 # テスト用サーバー
scripts/smoke-test.sh 1.18.2 26.3   # バージョンごとに実サーバーで確認
```

## ライセンス

[MIT](LICENSE)
