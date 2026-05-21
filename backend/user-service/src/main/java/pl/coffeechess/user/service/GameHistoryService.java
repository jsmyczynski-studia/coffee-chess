package pl.coffeechess.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.coffeechess.user.model.entity.GameHistory;
import pl.coffeechess.user.repository.GameHistoryRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final GameHistoryRepository gameHistoryRepository;

    public Page<GameHistory> getGameHistory(UUID userId, Pageable pageable) {
        return gameHistoryRepository.findAllByPlayerId(userId, pageable);
    }
}