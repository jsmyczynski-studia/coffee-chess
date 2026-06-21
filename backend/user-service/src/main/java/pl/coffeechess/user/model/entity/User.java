package pl.coffeechess.user.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false, length = 32)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "elo_rating", nullable = false)
    @Builder.Default
    private int eloRating = 1200;

    @Column(name = "games_played", nullable = false)
    @Builder.Default
    private int gamesPlayed = 0;

    @Column(name = "games_won", nullable = false)
    @Builder.Default
    private int gamesWon = 0;

    @Column(name = "games_lost", nullable = false)
    @Builder.Default
    private int gamesLost = 0;

    @Column(name = "games_drawn", nullable = false)
    @Builder.Default
    private int gamesDrawn = 0;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}