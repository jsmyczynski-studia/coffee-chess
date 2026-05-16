package pl.coffeechess.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EloService {

    @Value("${elo.k-factor.beginner:32}")
    private int kBeginner;

    @Value("${elo.k-factor.advanced:24}")
    private int kAdvanced;

    @Value("${elo.k-factor.expert:16}")
    private int kExpert;

    /**
     * Oblicza zmianę ELO dla gracza.
     *
     * @param playerElo   aktualne ELO gracza
     * @param opponentElo aktualne ELO przeciwnika
     * @param score       1.0 = wygrana, 0.5 = remis, 0.0 = przegrana
     * @return zmiana punktów ELO (może być ujemna)
     */
    public int calculateEloChange(int playerElo, int opponentElo, double score) {
        double expected = expectedScore(playerElo, opponentElo);
        int k = kFactor(playerElo);
        return (int) Math.round(k * (score - expected));
    }

    private double expectedScore(int playerElo, int opponentElo) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponentElo - playerElo) / 400.0));
    }

    private int kFactor(int elo) {
        if (elo >= 2400) return kExpert;
        if (elo >= 2100) return kAdvanced;
        return kBeginner;
    }
}