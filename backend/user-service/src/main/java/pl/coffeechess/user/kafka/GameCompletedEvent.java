package pl.coffeechess.user.kafka;

/**
 * Canonical Kafka payload for the {@code game-completed} topic.
 * Must stay in sync with {@code pl.coffeechess.game.kafka.GameCompletedEvent}.
 */
public record GameCompletedEvent(
        String gameId,
        String whitePlayerId,
        String blackPlayerId,
        String outcome,
        String moveListUci,
        String timeControl,
        String endReason
) {}
