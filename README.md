# minecraft-JapanizeChat-plugin

Minecraft Paper 用のチャット日本語化プラグインです。ローマ字入力をひらがなへ変換し、変換結果をチャットに表示します。

## 機能
- ローマ字入力をひらがなへ変換
- 変換結果を元のチャットに括弧付きで表示
- 個別に変換表示のオン/オフ切り替え
- 権限を持つプレイヤーによる設定リロード

## コマンド
- `/jp toggle` 変換表示のオン/オフを切り替え
- `/jp help` 使い方を表示
- `/jp reload` 設定を再読み込み（権限: `japanizechat.admin`）

## 権限
- `japanizechat.admin` リロード許可

## 設定
`config.yml` の `converted-text-color` で変換文字色を指定できます。
- `gray` のような色名
- `#RRGGBB` の 16 進カラー

## 対応環境
- Paper 1.20 系（想定）

## ビルド
```bash
gradle build
```

## 仕様メモ
- 日本語入力 API に Google Input Tools を利用しています。
- 英数字以外の入力は変換しません。
