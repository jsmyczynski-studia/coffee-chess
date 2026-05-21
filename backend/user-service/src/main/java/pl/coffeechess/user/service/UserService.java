package pl.coffeechess.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.coffeechess.user.exception.UserNotFoundException;
import pl.coffeechess.user.model.dto.UserProfileDto;
import pl.coffeechess.user.model.entity.User;
import pl.coffeechess.user.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserProfileDto getProfileByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UserNotFoundException(nickname));
        return toDto(user);
    }

    public UserProfileDto getProfileById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return toDto(user);
    }

    public Page<UserProfileDto> getRanking(Pageable pageable) {
        return userRepository.findAllByOrderByEloRatingDesc(pageable)
                .map(this::toDto);
    }

    private UserProfileDto toDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getNickname(),
                user.getEloRating(),
                user.getGamesPlayed(),
                user.getGamesWon(),
                user.getGamesLost(),
                user.getGamesDrawn(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}