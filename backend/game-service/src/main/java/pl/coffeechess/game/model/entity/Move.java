package pl.coffeechess.game.model.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.coffeechess.game.model.enums.Color;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "moves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "move_number", nullable = false)
    private int moveNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Color color;

    @Column(nullable = false, length = 16)
    private String san;

    @Column(nullable = false, length = 8)
    private String uci;

    @Column(name = "fen_after", nullable = false, columnDefinition = "TEXT")
    private String fenAfter;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;
}
