package pl.coffeechess.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.coffeechess.user.model.entity.Friendship;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("""
        SELECT f FROM Friendship f
        WHERE (f.requesterId = :userId OR f.addresseeId = :userId)
        AND f.status = 'ACCEPTED'
        """)
    List<Friendship> findAllAcceptedFriendships(UUID userId);

    @Query("""
        SELECT f FROM Friendship f
        WHERE f.addresseeId = :userId
        AND f.status = 'PENDING'
        """)
    List<Friendship> findPendingInvites(UUID userId);

    @Query("""
        SELECT f FROM Friendship f
        WHERE (f.requesterId = :userA AND f.addresseeId = :userB)
        OR (f.requesterId = :userB AND f.addresseeId = :userA)
        """)
    Optional<Friendship> findBetweenUsers(UUID userA, UUID userB);
}