package sh.aki.japanesechat.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Decides whether a chat message is romaji and turns it into hiragana.
 *
 * <p>The message is split into words. Words with capital letters, URLs and known names (players)
 * are kept as they are. Other words that are not romaji are kept too, as long as they are few
 * (at most one for every three converted words); with more of them the message is treated as English.
 */
public final class Japanizer {

    private static final Pattern URL = Pattern.compile("(?i)^(https?://|www\\.).+|.+\\.(com|net|org|jp|io|gg)(/.*)?$");

    /** A run of converted words, or a word kept as written. */
    public record Part(String text, boolean japanese) {
    }

    /** The converted message as parts. Converted words next to each other form one part. */
    public record Result(List<Part> parts) {

        public String kana() {
            return join(Function.identity());
        }

        /** Applies {@code convert} to the Japanese parts only, such as kana to kanji. */
        public String join(Function<String, String> convert) {
            StringBuilder out = new StringBuilder();
            for (Part p : parts) {
                if (!out.isEmpty()) {
                    out.append(' ');
                }
                out.append(p.japanese() ? convert.apply(p.text()) : p.text());
            }
            return out.toString();
        }
    }

    private Japanizer() {
    }

    /** @param keep words to leave unchanged, compared in lower case (for example player names) */
    public static Optional<Result> convert(String message, Predicate<String> keep) {
        if (message == null || message.isBlank() || !message.chars().allMatch(c -> c >= 0x20 && c < 0x7F)) {
            return Optional.empty();
        }
        List<Part> parts = new ArrayList<>();
        int converted = 0;
        int unknown = 0;
        for (String word : message.strip().split("\\s+")) {
            String lower = word.toLowerCase(Locale.ROOT);
            boolean keepWord = !word.equals(lower) || URL.matcher(word).matches() || keep.test(lower)
                    || word.chars().noneMatch(Character::isLetter);
            String kana = keepWord ? null : word.equals("wa") ? "は" : Romaji.toKana(word);
            if (kana == null) {
                if (!keepWord) {
                    unknown++;
                }
                parts.add(new Part(word, false));
                continue;
            }
            converted++;
            int last = parts.size() - 1;
            if (last >= 0 && parts.get(last).japanese()) {
                // Japanese has no spaces between words.
                parts.set(last, new Part(parts.get(last).text() + kana, true));
            } else {
                parts.add(new Part(kana, true));
            }
        }
        if (converted == 0 || unknown * 3 > converted) {
            return Optional.empty();
        }
        return Optional.of(new Result(List.copyOf(parts)));
    }

    public static Optional<String> toKana(String message, Predicate<String> keep) {
        return convert(message, keep).map(Result::kana);
    }
}
