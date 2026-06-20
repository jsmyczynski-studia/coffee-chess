package pl.coffeechess.game.model.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.coffeechess.game.model.enums.BotDifficulty;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.EndReason;
import pl.coffeechess.game.model.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "vs_bot", nullable = false)
    @Builder.Default
    private boolean vsBot = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "bot_color", length = 8)
    private Color botColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "bot_difficulty", length = 16)
    private BotDifficulty botDifficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason", length = 32)
    private EndReason endReason;

    @Column(name = "current_fen", nullable = false, columnDefinition = "TEXT")
    private String currentFen;

    @Column(name = "move_list_uci", columnDefinition = "TEXT")
    private String moveListUci;

    @Column(name = "time_control", length = 32)
    private String timeControl;

    @Column(name = "white_time_ms", nullable = false)
    private long whiteTimeMs;

    @Column(name = "black_time_ms", nullable = false)
    private long blackTimeMs;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "draw_offered_by", length = 8)
    private Color drawOfferedBy;

    @Column(name = "halfmove_clock", nullable = false)
    @Builder.Default
    private long halfmoveClock = 0L;

    @Column(name = "position_history", columnDefinition = "TEXT")
    @Builder.Default
    private String positionHistory = "";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Move> moves = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void appendPositionHistory(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (positionHistory == null || positionHistory.isEmpty()) {
            positionHistory = key;
        } else {
            positionHistory = positionHistory + ";" + key;
        }
    }
}
