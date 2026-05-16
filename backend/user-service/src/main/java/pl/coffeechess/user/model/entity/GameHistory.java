package pl.coffeechess.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_id", nullable = false, unique = true)
    private UUID gameId;

    @Column(name = "white_player_id", nullable = false)
    private UUID whitePlayerId;

    @Column(name = "black_player_id", nullable = false)
    private UUID blackPlayerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameOutcome outcome;

    @Column(name = "white_elo_change")
    private int whiteEloChange;

    @Column(name = "black_elo_change")
    private int blackEloChange;

    @Column(columnDefinition = "TEXT")
    private String pgn;

    @Column(name = "time_control")
    private String timeControl;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    public enum GameOutcome {
        WHITE_WINS, BLACK_WINS, DRAW
    }
}