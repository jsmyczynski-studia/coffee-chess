package pl.coffeechess.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coffeechess.user.model.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByNickname(String nickname);
    Optional<User> findByKeycloakId(String keycloakId);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    Page<User> findAllByOrderByEloRatingDesc(Pageable pageable);
}