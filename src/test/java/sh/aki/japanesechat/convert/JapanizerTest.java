package sh.aki.japanesechat.convert;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JapanizerTest {

    private static final Predicate<String> NONE = w -> false;

    private static Optional<String> kana(String s) {
        return Japanizer.toKana(s, NONE);
    }

    @Test
    void convertsSentences() {
        assertEquals(Optional.of("こんにちは"), kana("konnichiha"));
        assertEquals(Optional.of("きょうはいいてんき"), kana("kyou wa ii tenki"));
        assertEquals(Optional.of("あした3じにあおう"), kana("ashita 3ji ni aou"));
    }

    @Test
    void leavesEnglishAlone() {
        assertEquals(Optional.empty(), kana("hello world"));
        assertEquals(Optional.empty(), kana("ok desu"));
        assertEquals(Optional.empty(), kana("i like this game"));
        assertEquals(Optional.empty(), kana("gg"));
        assertEquals(Optional.empty(), kana("lol"));
    }

    @Test
    void keepsCapitalsUrlsAndNames() {
        assertEquals(Optional.of("OK です"), kana("OK desu"));
        assertEquals(Optional.of("https://example.com みて"), kana("https://example.com mite"));
        assertEquals(Optional.of("steve さん"), Japanizer.toKana("steve san", Set.of("steve")::contains));
        assertEquals(Optional.of("これ OK です"), kana("kore OK desu"));
    }

    @Test
    void keepsAFewUnknownWords() {
        assertEquals(Optional.of("さっきの boss つよすぎでしょ"), kana("sakki no boss tsuyosugidesyo"));
    }

    @Test
    void onlyJapanesePartsAreConvertedFurther() {
        Japanizer.Result r = Japanizer.convert("kore OK desu", NONE).orElseThrow();
        assertEquals("[これ] OK [です]", r.join(k -> "[" + k + "]"));
    }

    @Test
    void skipsMessagesWithNothingToConvert() {
        assertEquals(Optional.empty(), kana("123"));
        assertEquals(Optional.empty(), kana("OK"));
        assertEquals(Optional.empty(), kana("   "));
        assertEquals(Optional.empty(), kana("こんにちは"));
        assertEquals(Optional.empty(), kana("konnichiha 世界"));
    }
}
