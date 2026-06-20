export interface UserProfile {
  id: string;
  nickname: string;
  eloRating: number;
  gamesPlayed: number;
  gamesWon: number;
  gamesLost: number;
  gamesDrawn: number;
  avatarUrl: string | null;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Friendship {
  id: string;
  requesterId: string;
  addresseeId: string;
  status: 'PENDING' | 'ACCEPTED' | 'BLOCKED';
  createdAt: string;
  updatedAt: string | null;
}

export interface GameHistoryEntry {
  id: string;
  gameId: string;
  whitePlayerId: string;
  blackPlayerId: string;
  outcome: 'WHITE_WINS' | 'BLACK_WINS' | 'DRAW';
  whiteEloChange: number;
  blackEloChange: number;
  pgn: string | null;
  timeControl: string | null;
  playedAt: string;
}

export type AnalysisStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';

export interface MoveAnalysis {
  moveNumber: number;
  moveSan: string;
  bestMove: string | null;
  evaluation: number | null;
  moveQuality: string | null;
  comment: string | null;
}

export interface AnalysisReport {
  gameId: string;
  status: AnalysisStatus;
  blunders: number;
  mistakes: number;
  inaccuracies: number;
  whiteAccuracy: number | null;
  blackAccuracy: number | null;
  summary: string | null;
  moves: MoveAnalysis[];
  completedAt: string | null;
}

export interface ApiProblem {
  status: number;
  title: string;
  detail?: string;
  message: string;
}
