package sh.aki.japanesechat.convert;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoogleInputToolsTest {

    @Test
    void parsesCandidates() {
        assertEquals(Optional.of("今日は"), GoogleInputTools.parse(
                "[\"SUCCESS\",[[\"きょうは\",[\"今日は\",\"京は\"],[],{\"candidate_type\":[0,0]}]]]"));
        assertEquals(Optional.of("今日はいい天気"), GoogleInputTools.parse(
                "[\"SUCCESS\",[[\"きょうは\",[\"今日は\"]],[\"いいてんき\",[\"いい天気\"]]]]"));
    }

    @Test
    void failuresAreEmpty() {
        assertEquals(Optional.empty(), GoogleInputTools.parse("[\"FAILED_TO_PARSE_REQUEST_BODY\"]"));
        assertEquals(Optional.empty(), GoogleInputTools.parse("[\"SUCCESS\",[[\"x\",[]]]]"));
        assertEquals(Optional.empty(), GoogleInputTools.parse("<html>"));
        assertEquals(Optional.empty(), GoogleInputTools.parse("[]"));
    }
}
