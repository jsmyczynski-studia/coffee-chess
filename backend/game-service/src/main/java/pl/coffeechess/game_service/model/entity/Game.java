package pl.coffeechess.game_service.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "white_player_id")
    private UUID whitePlayerId;

    @Column(name = "black_player_id")
    private UUID blackPlayerId;

    @Column(nullable = false)
    @Builder.Default
    private String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Column(columnDefinition = "TEXT")
    private String pgn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameStatus status = GameStatus.WAITING;

    @Column(name = "white_time_left_ms")
    private long whiteTimeLeftMs;

    @Column(name = "black_time_left_ms")
    private long blackTimeLeftMs;

    @Column(name = "last_move_timestamp")
    private Instant lastMoveTimestamp;

    public enum GameStatus {
        WAITING,
        IN_PROGRESS,
        WHITE_WON,
        BLACK_WON,
        DRAW
    }
}