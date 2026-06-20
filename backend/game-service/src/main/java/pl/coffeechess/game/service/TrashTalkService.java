package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.coffeechess.game.client.LlmClient;

// generuje krótkie zaczepne komentarze bota
@Service
@RequiredArgsConstructor
public class TrashTalkService {

    private static final String SYSTEM_PROMPT = """
            you are a cocky online chess player throwing trash talk at your opponent
            reply with one short remark only
            rules you must follow exactly:
            all lowercase
            no punctuation at all
            no emojis
            keep it under twelve words
            informal and a bit mean
            sound like a real online blitz player not a robot""";

    private final LlmClient llmClient;

    // tworzy komentarz na podstawie kontekstu ostatniego ruchu, zwraca null jeśli się nie uda
    public String generateRemark(String context) {
        String raw = llmClient.complete(SYSTEM_PROMPT, context);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return sanitize(raw);
    }

    // wymusza wymagany styl: małe litery, bez interpunkcji i emoji, krótko
    private String sanitize(String text) {
        String cleaned = text.toLowerCase()
                .replaceAll("[\\p{So}\\p{Cn}]", "")
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isBlank()) {
            return null;
        }
        String[] words = cleaned.split(" ");
        if (words.length > 12) {
            cleaned = String.join(" ", java.util.Arrays.copyOfRange(words, 0, 12));
        }
        return cleaned;
    }
}
