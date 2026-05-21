package pl.coffeechess.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "elo_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EloHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "elo_before", nullable = false)
    private int eloBefore;

    @Column(name = "elo_change", nullable = false)
    private int eloChange;

    @Column(name = "elo_after", nullable = false)
    private int eloAfter;

    @Column(name = "opponent_elo", nullable = false)
    private int opponentElo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameResult result;

    @Column(name = "recorded_at", nullable = false)
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();

    public enum GameResult {
        WIN, LOSS, DRAW
    }
}