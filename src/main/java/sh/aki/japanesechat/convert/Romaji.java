package sh.aki.japanesechat.convert;

import java.util.HashMap;
import java.util.Map;

/**
 * Romaji to hiragana, close to how a Japanese IME reads it. Returns null when letters are left
 * that do not form kana, which usually means the word is not romaji at all. The l- and c- spellings
 * of IMEs are left out on purpose: they would turn English words like "hello" into kana.
 */
public final class Romaji {

    private static final Map<String, String> KANA = new HashMap<>();
    private static final Map<Character, String> PUNCTUATION = Map.of(
            ',', "、", '.', "。", '?', "？", '!', "！", '-', "ー", '~', "〜", '[', "「", ']', "」");
    private static final String VOWELS = "aiueo";

    static {
        row("", "あ い う え お");
        row("k", "か き く け こ");
        row("s", "さ し す せ そ");
        row("t", "た ち つ て と");
        row("n", "な に ぬ ね の");
        row("h", "は ひ ふ へ ほ");
        row("m", "ま み む め も");
        row("y", "や い ゆ いぇ よ");
        row("r", "ら り る れ ろ");
        row("w", "わ うぃ う うぇ を");
        row("g", "が ぎ ぐ げ ご");
        row("z", "ざ じ ず ぜ ぞ");
        row("d", "だ ぢ づ で ど");
        row("b", "ば び ぶ べ ぼ");
        row("p", "ぱ ぴ ぷ ぺ ぽ");
        row("f", "ふぁ ふぃ ふ ふぇ ふぉ");
        row("v", "ゔぁ ゔぃ ゔ ゔぇ ゔぉ");
        row("j", "じゃ じ じゅ じぇ じょ");
        row("x", "ぁ ぃ ぅ ぇ ぉ");
        row("sh", "しゃ し しゅ しぇ しょ");
        row("ch", "ちゃ ち ちゅ ちぇ ちょ");
        row("ts", "つぁ つぃ つ つぇ つぉ");
        row("th", "てゃ てぃ てゅ てぇ てょ");
        row("dh", "でゃ でぃ でゅ でぇ でょ");
        row("cy", "ちゃ ちぃ ちゅ ちぇ ちょ");
        row("ty", "ちゃ ちぃ ちゅ ちぇ ちょ");
        row("sy", "しゃ しぃ しゅ しぇ しょ");
        row("zy", "じゃ じぃ じゅ じぇ じょ");
        row("jy", "じゃ じぃ じゅ じぇ じょ");
        row("dy", "ぢゃ ぢぃ ぢゅ ぢぇ ぢょ");
        row("kw", "くぁ くぃ く くぇ くぉ");
        row("gw", "ぐぁ ぐぃ ぐ ぐぇ ぐぉ");
        row("xy", "ゃ ぃ ゅ ぇ ょ");
        for (String c : new String[]{"k", "n", "h", "m", "r", "g", "b", "p", "v", "f"}) {
            String i = KANA.get(c + "i");
            String base = i.length() == 1 ? i : i.substring(0, 1);
            row(c + "y", base + "ゃ " + base + "ぃ " + base + "ゅ " + base + "ぇ " + base + "ょ");
        }
        KANA.put("shi", "し");
        KANA.put("chi", "ち");
        KANA.put("tsu", "つ");
        KANA.put("fu", "ふ");
        KANA.put("ji", "じ");
        KANA.put("si", "し");
        KANA.put("ti", "ち");
        KANA.put("tu", "つ");
        KANA.put("hu", "ふ");
        KANA.put("zi", "じ");
        KANA.put("di", "ぢ");
        KANA.put("du", "づ");
        KANA.put("wi", "うぃ");
        KANA.put("we", "うぇ");
        KANA.put("xtu", "っ");
        KANA.put("xtsu", "っ");
        KANA.put("xwa", "ゎ");
        KANA.put("xka", "ゕ");
        KANA.put("xke", "ゖ");
        KANA.put("n'", "ん");
        KANA.put("xn", "ん");
    }

    private static void row(String consonant, String kana) {
        String[] k = kana.split(" ");
        String[] v = {"a", "i", "u", "e", "o"};
        for (int i = 0; i < 5; i++) {
            KANA.putIfAbsent(consonant + v[i], k[i]);
        }
    }

    private Romaji() {
    }

    /** Hiragana for one lowercase word, or null if it is not romaji. */
    public static String toKana(String word) {
        StringBuilder out = new StringBuilder(word.length());
        int i = 0;
        while (i < word.length()) {
            char c = word.charAt(i);
            if (c < 'a' || c > 'z') {
                String p = PUNCTUATION.get(c);
                out.append(p != null ? p : String.valueOf(c));
                i++;
                continue;
            }
            if (c == 'n') {
                char next = at(word, i + 1);
                if (next == 'n') {
                    // "nn" is ん; "minna" keeps the second n for な.
                    char after = at(word, i + 2);
                    out.append('ん');
                    i += VOWELS.indexOf(after) >= 0 || after == 'y' ? 1 : 2;
                    continue;
                }
                if (VOWELS.indexOf(next) < 0 && next != 'y' && next != '\'') {
                    out.append('ん');
                    i++;
                    continue;
                }
            }
            if (isConsonant(c) && c != 'n' && (at(word, i + 1) == c || (c == 't' && at(word, i + 1) == 'c'))) {
                out.append('っ');
                i++;
                continue;
            }
            String match = null;
            int len = Math.min(4, word.length() - i);
            for (; len >= 1; len--) {
                match = KANA.get(word.substring(i, i + len));
                if (match != null) {
                    break;
                }
            }
            if (match == null) {
                return null;
            }
            out.append(match);
            i += len;
        }
        return out.toString();
    }

    private static boolean isConsonant(char c) {
        return c >= 'a' && c <= 'z' && VOWELS.indexOf(c) < 0;
    }

    private static char at(String s, int i) {
        return i < s.length() ? s.charAt(i) : '\0';
    }
}
