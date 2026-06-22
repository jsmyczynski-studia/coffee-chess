package pl.coffeechess.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.coffeechess.game.client.LlmClient;

// generuje krótkie zaczepne komentarze bota
@Service
@RequiredArgsConstructor
public class TrashTalkService {

    private static final String SYSTEM_PROMPT = """
        jestes pewnym siebie botem szachowym zaczepiajacym przeciwnika
        odpowiedz po polsku jednym bardzo krotkim komentarzem
        komentuj tylko ruch bota ktory zostal podany w kontekscie
        nie oceniaj ruchu przeciwnika jako dobrego lub slabego
        nie zaprzeczaj analizie silnika
        bez emoji
        bez interpunkcji
        maksymalnie dwanascie slow
        ton lekko zaczepny ale nie obrazliwy
        """; 

    private static final String MOVE_COMMENT_PROMPT = """
        jestes komentatorem partii szachowej grajacym jako bot
        odpowiedz po polsku jednym bardzo krotkim zdaniem
        komentujesz tylko ostatni ruch przeciwnika
        klasyfikacja ruchu zostala podana przez silnik szachowy i jest prawdziwa
        nie oceniaj innych ruchow i nie zaprzeczaj klasyfikacji
        gdy ruch jest dobry pochwal go lekko zaczepnie
        gdy ruch jest slaby zaczep przeciwnika rzeczowo
        bez emoji
        bez interpunkcji
        maksymalnie dwanascie slow
        """; 

    private static final String CHAT_REPLY_PROMPT = """
            jestes pewnym siebie botem szachowym rozmawiajacym z przeciwnikiem
            odpowiedz po polsku bezposrednio na jego wiadomosc
            odpowiedz ma byc lekko zaczepna ale nie obrazliwa
            maksymalnie dwanascie slow""";

    private final LlmClient llmClient;

    // tworzy komentarz na podstawie kontekstu ostatniego ruchu, zwraca null jeśli się nie uda
    public String generateRemark(String context) {
        String raw = llmClient.complete(SYSTEM_PROMPT, context);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return sanitize(raw);
    }

    public String generateMoveComment(String move, String quality) {
      String raw = llmClient.complete(
            MOVE_COMMENT_PROMPT,
            "ostatni ruch przeciwnika to " + move
                    + " klasyfikacja silnika to " + quality
      );

      return raw == null || raw.isBlank() ? null : sanitize(raw);
    } 

    public String generateChatReply(String playerMessage) {
        String raw = llmClient.complete(CHAT_REPLY_PROMPT, playerMessage);
        return raw == null || raw.isBlank() ? null : sanitize(raw);
    }

    // wymusza wymagany styl: małe litery, bez interpunkcji i emoji, krótko
    private String sanitize(String text) {
        String cleaned = text.toLowerCase()
                .replaceAll("[\\p{So}\\p{Cn}]", "")
                .replaceAll("[^\\p{L}\\p{N}\\s]", "")
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
