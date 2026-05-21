package pl.coffeechess.analysis.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "move_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_analysis_id", nullable = false)
    private GameAnalysis gameAnalysis;

    @Column(name = "move_number", nullable = false)
    private int moveNumber;

    @Column(name = "move_san", nullable = false)
    private String moveSan;

    @Column(name = "fen_after", columnDefinition = "TEXT")
    private String fenAfter;

    @Column(name = "best_move")
    private String bestMove;

    @Column(name = "evaluation")
    private Double evaluation;

    @Enumerated(EnumType.STRING)
    @Column(name = "move_quality")
    private MoveQuality moveQuality;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    public enum MoveQuality {
        BRILLIANT, BEST, GOOD, INACCURACY, MISTAKE, BLUNDER
    }
}