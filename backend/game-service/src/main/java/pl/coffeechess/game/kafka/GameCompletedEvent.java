package pl.coffeechess.game.kafka;

/**
 * Canonical Kafka payload for the {@code game-completed} topic.
 * All ids are strings on the wire; services parse to UUID locally as needed.
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
