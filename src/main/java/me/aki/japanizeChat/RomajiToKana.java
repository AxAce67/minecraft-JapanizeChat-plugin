package me.aki.japanizeChat;

import java.util.TreeMap;

public class RomajiToKana {
    private static final TreeMap<String, String> map = new TreeMap<>((a, b) -> {
        int res = b.length() - a.length();
        return res != 0 ? res : a.compareTo(b);
    });

    static {
        String[][] table = {
                {"a","あ"},{"i","い"},{"u","う"},{"e","え"},{"o","お"},
                {"ka","か"},{"ki","き"},{"ku","く"},{"ke","け"},{"ko","こ"},
                {"sa","さ"},{"shi","し"},{"si","し"},{"su","す"},{"se","せ"},{"so","そ"},
                {"ta","た"},{"chi","ち"},{"ti","ち"},{"tsu","つ"},{"tu","つ"},{"te","て"},{"to","と"},
                {"na","な"},{"ni","に"},{"nu","ぬ"},{"ne","ね"},{"no","の"},
                {"ha","は"},{"hi","ひ"},{"fu","ふ"},{"he","へ"},{"ho","ほ"},
                {"ma","ま"},{"mi","み"},{"mu","む"},{"me","め"},{"mo","も"},
                {"ya","や"},{"yu","ゆ"},{"yo","よ"},
                {"ra","ら"},{"ri","り"},{"ru","る"},{"re","れ"},{"ro","ろ"},
                {"wa","わ"},{"wo","を"},{"nn","ん"},{"n","ん"},
                {"ga","が"},{"gi","ぎ"},{"gu","ぐ"},{"ge","げ"},{"go","ご"},
                {"za","ざ"},{"ji","じ"},{"zu","ず"},{"ze","ぜ"},{"zo","ぞ"},
                {"da","だ"},{"di","ぢ"},{"du","づ"},{"de","で"},{"do","ど"},
                {"ba","ば"},{"bi","び"},{"bu","ぶ"},{"be","べ"},{"bo","ぼ"},
                {"pa","ぱ"},{"pi","ぴ"},{"pu","ぷ"},{"pe","ぺ"},{"po","ぽ"},
                {"kya","きゃ"},{"kyu","きゅ"},{"kyo","きょ"},
                {"sha","しゃ"},{"shu","しゅ"},{"sho","しょ"},
                {"cha","ちゃ"},{"chu","ちゅ"},{"cho","ちょう"},
                {"ja","じゃ"},{"ju","じゅ"},{"jo","じょ"}
        };
        for (String[] pair : table) map.put(pair[0], pair[1]);
    }

    public static String convertToKana(String input) {
        if (input == null) return null;

        String text = input.toLowerCase()
                .replaceAll("nnn", "nn")
                .replaceAll("\\bwa\\b", "ha")
                .replace("nn", "n");

        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            boolean found = false;
            for (int len = Math.min(3, text.length() - i); len >= 1; len--) {
                String sub = text.substring(i, i + len);
                if (map.containsKey(sub)) {
                    sb.append(map.get(sub));
                    i += len;
                    found = true;
                    break;
                }
            }
            if (!found) {
                char c = text.charAt(i);
                if (i + 1 < text.length() && c == text.charAt(i + 1) && "bcdfghjklmpqrstvwxyz".indexOf(c) >= 0 && c != 'n') {
                    sb.append("っ");
                } else {
                    sb.append(c);
                }
                i++;
            }
        }
        return sb.toString();
    }
}