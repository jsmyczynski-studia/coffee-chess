package pl.coffeechess.analysis.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_id", unique = true, nullable = false)
    private UUID gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AnalysisStatus status = AnalysisStatus.PENDING;

    @Column(name = "blunders")
    private int blunders;

    @Column(name = "mistakes")
    private int mistakes;

    @Column(name = "inaccuracies")
    private int inaccuracies;

    @Column(name = "white_accuracy")
    private Double whiteAccuracy;

    @Column(name = "black_accuracy")
    private Double blackAccuracy;

    @OneToMany(mappedBy = "gameAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MoveAnalysis> moveAnalyses = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum AnalysisStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}