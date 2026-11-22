package sh.aki.japanesechat.convert;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Kana to kanji with Google Input Tools. The service is unofficial, so every failure simply
 * means "no kanji" and the caller falls back to kana. Results are cached.
 */
public final class GoogleInputTools {

    private static final int CACHE_SIZE = 2000;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final Map<String, String> cache = Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > CACHE_SIZE;
        }
    });
    private volatile Duration timeout;

    public GoogleInputTools(Duration timeout) {
        this.timeout = timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /** Blocks for up to the timeout. Never call it on the main thread. */
    public Optional<String> convert(String kana) {
        String hit = cache.get(kana);
        if (hit != null) {
            return Optional.of(hit);
        }
        try {
            URI uri = URI.create("https://inputtools.google.com/request?itc=ja-t-i0-und&num=1&cp=0&cs=1"
                    + "&ie=utf-8&oe=utf-8&text=" + URLEncoder.encode(kana, StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(timeout).GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            Optional<String> result = parse(response.body());
            result.ifPresent(r -> cache.put(kana, r));
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** {@code ["SUCCESS",[["input",["candidate",...],...]],...]} */
    static Optional<String> parse(String json) {
        try {
            JsonArray root = JsonParser.parseString(json).getAsJsonArray();
            if (!"SUCCESS".equals(root.get(0).getAsString())) {
                return Optional.empty();
            }
            StringBuilder out = new StringBuilder();
            for (var segment : root.get(1).getAsJsonArray()) {
                JsonArray candidates = segment.getAsJsonArray().get(1).getAsJsonArray();
                if (candidates.isEmpty()) {
                    return Optional.empty();
                }
                out.append(candidates.get(0).getAsString());
            }
            return out.isEmpty() ? Optional.empty() : Optional.of(out.toString());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
