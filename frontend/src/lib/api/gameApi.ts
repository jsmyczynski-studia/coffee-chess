import { apiRequest } from './http';
import type { CreateGameRequest, GameDto, GameUpdateDto, MoveRequest, ChatMessageDto, SendChatRequest, MoveDto } from './types';

export const gameApi = {
  getGame(id: string, token?: string) {
    return apiRequest<GameDto>('game', `/api/games/${id}`, {}, token);
  },

  getMoves(id: string, token?: string) {
    return apiRequest<MoveDto[]>('game', `/api/games/${id}/moves`, {}, token);
  },

  createGame(request: CreateGameRequest, token: string) {
    return apiRequest<GameDto>(
      'game',
      '/api/games',
      { method: 'POST', body: JSON.stringify(request) },
      token,
    );
  },

  submitMove(id: string, moveUci: string, token: string) {
    const body: MoveRequest = { move: moveUci };
    return apiRequest<GameUpdateDto>(
      'game',
      `/api/games/${id}/moves`,
      { method: 'POST', body: JSON.stringify(body) },
      token,
    );
  },

  listGames(token: string) {
    return apiRequest<GameDto[]>('game', '/api/games', {}, token);
  },

  getChat(id: string, token: string) {
    return apiRequest<ChatMessageDto[]>('game', `/api/games/${id}/chat`, {}, token);
  },

  sendChat(id: string, text: string, token: string) {
    const body: SendChatRequest = { text };
    return apiRequest<ChatMessageDto>(
      'game',
      `/api/games/${id}/chat`,
      { method: 'POST', body: JSON.stringify(body) },
      token,
    );
  },

  resign(id: string, token: string) {
    return apiRequest<GameUpdateDto>(
      'game',
      `/api/games/${id}/resign`,
      { method: 'POST' },
      token,
    );
  },

  offerDraw(id: string, token: string) {
    return apiRequest<GameUpdateDto>(
      'game',
      `/api/games/${id}/draw/offer`,
      { method: 'POST' },
      token,
    );
  },

  acceptDraw(id: string, token: string) {
    return apiRequest<GameUpdateDto>(
      'game',
      `/api/games/${id}/draw/accept`,
      { method: 'POST' },
      token,
    );
  },

  declineDraw(id: string, token: string) {
    return apiRequest<GameUpdateDto>(
      'game',
      `/api/games/${id}/draw/decline`,
      { method: 'POST' },
      token,
    );
  },
};
