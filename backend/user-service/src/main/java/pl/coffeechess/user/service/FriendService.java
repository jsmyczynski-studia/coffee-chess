package pl.coffeechess.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.coffeechess.user.exception.UserNotFoundException;
import pl.coffeechess.user.model.dto.FriendshipDto;
import pl.coffeechess.user.model.entity.Friendship;
import pl.coffeechess.user.model.entity.User;
import pl.coffeechess.user.repository.FriendshipRepository;
import pl.coffeechess.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserProvisioningService userProvisioningService;
    private final UserRepository userRepository;

    @Transactional
    public void sendFriendRequest(UUID requesterId, String addresseeNickname) {
        // Resolve locally, falling back to provisioning from Keycloak if the addressee has
        // logged into Keycloak but never called user-service yet. This is the core fix for
        // friend invites being "borked" after the local DB stopped tracking every user.
        User addressee = userProvisioningService.resolveByNickname(addresseeNickname)
                .orElseThrow(() -> new UserNotFoundException(addresseeNickname));

        if (addressee.getId().equals(requesterId)) {
            throw new IllegalStateException("Cannot send a friend request to yourself");
        }

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

    public List<FriendshipDto> getFriends(UUID userId) {
        return toDtos(friendshipRepository.findAllAcceptedFriendships(userId));
    }

    public List<FriendshipDto> getPendingInvites(UUID userId) {
        return toDtos(friendshipRepository.findPendingInvites(userId));
    }

    private List<FriendshipDto> toDtos(List<Friendship> friendships) {
        List<UUID> ids = friendships.stream()
                .flatMap(f -> Stream.of(f.getRequesterId(), f.getAddresseeId()))
                .distinct()
                .toList();

        Map<UUID, String> nicknames = userRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        return friendships.stream()
                .map(f -> new FriendshipDto(
                        f.getId(),
                        f.getRequesterId(),
                        nicknames.getOrDefault(f.getRequesterId(), "?"),
                        f.getAddresseeId(),
                        nicknames.getOrDefault(f.getAddresseeId(), "?"),
                        f.getStatus(),
                        f.getCreatedAt(),
                        f.getUpdatedAt()))
                .toList();
    }
}