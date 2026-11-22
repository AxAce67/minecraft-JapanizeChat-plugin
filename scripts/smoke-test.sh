#!/usr/bin/env bash
# Starts real Paper servers of the given versions with the built plugin, converts a 1.x config,
# runs the console commands including test conversions, and checks the log for errors.
#
#   ./gradlew build && scripts/smoke-test.sh 1.18.2 1.21.4 26.3
#
# Server jars are downloaded from PaperMC (checksum verified) into run/smoke/<version>/ and reused.
# Requires: java (17+), curl, python3. Set JAVA=/path/to/java to run the servers on a specific JDK.
set -uo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PLUGIN_JAR="$(ls "$ROOT"/build/libs/JapaneseChat-*.jar 2>/dev/null | head -1)"
UA="JapaneseChat-smoke-test"
JAVA="${JAVA:-java}"
PORT=25612

sha256() {
  if command -v sha256sum >/dev/null; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi
}
[ -n "$PLUGIN_JAR" ] || { echo "Build first: ./gradlew build"; exit 2; }
[ $# -gt 0 ] || { echo "usage: $0 <paper-version>..."; exit 2; }

wait_for() { # file pattern timeout
  local i
  for ((i = 0; i < $3; i++)); do
    grep -qE "$2" "$1" 2>/dev/null && return 0
    sleep 1
  done
  return 1
}

overall=0
for V in "$@"; do
  DIR="$ROOT/run/smoke/$V"
  rm -rf "$DIR/plugins/JapaneseChat"
  mkdir -p "$DIR/plugins/JapaneseChat"
  if [ ! -f "$DIR/paper.jar" ]; then
    echo "[$V] downloading Paper..."
    meta="$(curl -fsSL -A "$UA" "https://fill.papermc.io/v3/projects/paper/versions/$V/builds/latest")" || {
      echo "[$V] FAIL: unknown version"; overall=1; continue; }
    url="$(python3 -c 'import json,sys;print(json.load(sys.stdin)["downloads"]["server:default"]["url"])' <<<"$meta")"
    sha="$(python3 -c 'import json,sys;print(json.load(sys.stdin)["downloads"]["server:default"]["checksums"]["sha256"])' <<<"$meta")"
    curl -fsSL -A "$UA" -o "$DIR/paper.jar.part" "$url" || { echo "[$V] FAIL: download"; overall=1; continue; }
    actual="$(sha256 "$DIR/paper.jar.part")"
    [ "$actual" = "$sha" ] || { echo "[$V] FAIL: checksum mismatch"; rm -f "$DIR/paper.jar.part"; overall=1; continue; }
    mv "$DIR/paper.jar.part" "$DIR/paper.jar"
  fi
  rm -f "$DIR"/plugins/JapaneseChat-*.jar
  cp "$PLUGIN_JAR" "$DIR/plugins/"
  echo "eula=true" >"$DIR/eula.txt"
  printf 'online-mode=false\nview-distance=3\nsimulation-distance=3\nserver-port=%s\nmax-players=20\n' "$PORT" \
    >"$DIR/server.properties"
  # A 1.x install (plugins/JapanizeChat): the folder must be moved and the config converted.
  rm -rf "$DIR/plugins/JapanizeChat" "$DIR"/plugins/JapanizeChat-*.jar
  mkdir -p "$DIR/plugins/JapanizeChat"
  rmdir "$DIR/plugins/JapaneseChat"
  printf 'converted-text-color: "aqua"\ndefault-toggle-state: true\n' >"$DIR/plugins/JapanizeChat/config.yml"

  : >"$DIR/cmd.txt"
  rm -f "$DIR/server.log"
  (cd "$DIR" && tail -f "$DIR/cmd.txt" | "$JAVA" -Duser.language=en -Duser.country=US -Xms512M -Xmx1G -jar paper.jar nogui >server.log 2>&1) &
  send() { echo "$1" >>"$DIR/cmd.txt"; sleep 2; }

  result="PASS"
  if ! wait_for "$DIR/server.log" 'Done \(' 300; then
    result="FAIL: server did not start"
  else
    for c in "jp" "jp help" "jp on" "jp view" "jp nope" "jp test konnichiha" "jp test kyou ha ii tenki" \
      "jp test hello world" \
      "jp test sakki no boss tsuyosugidesyo" "jp reload"; do send "$c"; done
    sleep 3
  fi
  echo "stop" >>"$DIR/cmd.txt"
  wait_for "$DIR/server.log" 'Disabling JapaneseChat' 60
  sleep 4
  pkill -f "tail -f $DIR/cmd.txt" 2>/dev/null

  log="$DIR/server.plain.log"
  sed -E $'s/\x1b\\[[0-9;]*m//g' "$DIR/server.log" >"$log"
  cfg="$DIR/plugins/JapaneseChat/config.yml"
  if [ "$result" = "PASS" ]; then
    grep -q 'Enabling JapaneseChat' "$log" || result="FAIL: plugin not enabled"
    grep -q 'Moved plugins/JapanizeChat' "$log" || result="FAIL: old data folder not moved"
    grep -q 'Converted config.yml from 1.x' "$log" || result="FAIL: 1.x config not converted"
    grep -q '<aqua>(<converted>)' "$cfg" || result="FAIL: converted config not saved"
    grep -qE 'konnichiha → (こんにちは|今日は)' "$log" || result="FAIL: konnichiha not converted"
    grep -qE 'kyou ha ii tenki → .+ \([0-9]+ ms\)' "$log" || result="FAIL: sentence not converted"
    grep -q 'hello world → not converted' "$log" || result="FAIL: English was converted"
    grep -qE 'sakki no boss tsuyosugidesyo → .* boss ' "$log" || result="FAIL: mixed message"
    grep -q 'Only players can do that' "$log" || result="FAIL: console on/view"
    grep -q 'Unknown subcommand' "$log" || result="FAIL: unknown subcommand"
    grep -q 'Reloaded.' "$log" || result="FAIL: reload"
    errors="$(grep -cE 'Exception|SEVERE|\[ERROR\]|Error occurred|WARN\].*JapaneseChat' "$log")"
    [ "$errors" = "0" ] || result="FAIL: $errors error/warning lines in log"
  fi
  echo "[$V] $result"
  [ "$result" = "PASS" ] || overall=1
done
exit $overall
