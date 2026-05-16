package pl.coffeechess.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.user.exception.UserNotFoundException;
import pl.coffeechess.user.model.entity.Friendship;
import pl.coffeechess.user.model.entity.User;
import pl.coffeechess.user.repository.FriendshipRepository;
import pl.coffeechess.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendFriendRequest(UUID requesterId, String addresseeNickname) {
        User addressee = userRepository.findByNickname(addresseeNickname)
                .orElseThrow(() -> new UserNotFoundException(addresseeNickname));

        friendshipRepository.findBetweenUsers(requesterId, addressee.getId())
                .ifPresent(f -> { throw new IllegalStateException("Friend request already exists"); });

        friendshipRepository.save(Friendship.builder()
                .requesterId(requesterId)
                .addresseeId(addressee.getId())
                .build());
    }

    @Transactional
    public void acceptFriendRequest(UUID friendshipId, UUID addresseeId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

        if (!friendship.getAddresseeId().equals(addresseeId)) {
            throw new IllegalStateException("Not authorized to accept this request");
        }

        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    public List<Friendship> getFriends(UUID userId) {
        return friendshipRepository.findAllAcceptedFriendships(userId);
    }

    public List<Friendship> getPendingInvites(UUID userId) {
        return friendshipRepository.findPendingInvites(userId);
    }
}