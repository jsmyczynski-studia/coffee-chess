package pl.coffeechess.analysis.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "analysis_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "game_analysis_id", nullable = false, unique = true)
    private UUID gameAnalysisId;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "white_summary", columnDefinition = "TEXT")
    private String whiteSummary;

    @Column(name = "black_summary", columnDefinition = "TEXT")
    private String blackSummary;

    @Column(name = "key_moment_move")
    private Integer keyMomentMove;

    @Column(name = "key_moment_comment", columnDefinition = "TEXT")
    private String keyMomentComment;
}