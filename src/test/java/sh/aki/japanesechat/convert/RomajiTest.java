package sh.aki.japanesechat.convert;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RomajiTest {

    @ParameterizedTest
    @CsvSource({
            "konnichiha, こんにちは",
            "konbanha, こんばんは",
            "konnbanha, こんばんは",
            "minna, みんな",
            "minnna, みんな",
            "kanji, かんじ",
            "nihonn, にほん",
            "nihon, にほん",
            "hon'ya, ほんや",
            "shinnyuu, しんにゅう",
            "gakkou, がっこう",
            "matcha, まっちゃ",
            "kitte, きって",
            "chotto, ちょっと",
            "kyou, きょう",
            "ryokou, りょこう",
            "nyanko, にゃんこ",
            "shashin, しゃしん",
            "jouzu, じょうず",
            "tsukue, つくえ",
            "fairu, ふぁいる",
            "thi-shatsu, てぃーしゃつ",
            "ra-men, らーめん",
            "arigatou!, ありがとう！",
            "doko?, どこ？",
            "xtu, っ",
            "kawaii, かわいい",
            "desu., です。",
    })
    void converts(String romaji, String kana) {
        assertEquals(kana, Romaji.toKana(romaji));
    }

    @ParameterizedTest
    @ValueSource(strings = {"hello", "lol", "gg", "www", "ok", "cool", "nice", "yes", "stop", "xyz"})
    void rejectsNonRomaji(String word) {
        assertNull(Romaji.toKana(word), word);
    }
}
