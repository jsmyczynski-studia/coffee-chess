import { apiRequest } from './http';
import type { Friendship, GameHistoryEntry, PageResponse, UserProfile } from './types';

export const userApi = {
  ranking(page = 0, size = 20) {
    return apiRequest<PageResponse<UserProfile>>('user', `/api/ranking?page=${page}&size=${size}`);
  },

  profile(nickname: string) {
    return apiRequest<UserProfile>('user', `/api/users/${encodeURIComponent(nickname)}/profile`);
  },

  gameHistory(userId: string, page = 0, size = 10, token: string) {
    return apiRequest<PageResponse<GameHistoryEntry>>(
      'user',
      `/api/users/${userId}/history?page=${page}&size=${size}`,
      {},
      token,
    );
  },

  friends(token: string) {
    return apiRequest<Friendship[]>('user', '/api/friends', {}, token);
  },

  pendingInvites(token: string) {
    return apiRequest<Friendship[]>('user', '/api/friends/invites/pending', {}, token);
  },

  sendFriendRequest(nickname: string, token: string) {
    return apiRequest<void>(
      'user',
      '/api/friends/request',
      { method: 'POST', body: JSON.stringify({ nickname }) },
      token,
    );
  },

  acceptFriendRequest(friendshipId: string, token: string) {
    return apiRequest<void>(
      'user',
      `/api/friends/request/${friendshipId}/accept`,
      { method: 'PUT' },
      token,
    );
  },
};
