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
  requesterNickname: string;
  addresseeId: string;
  addresseeNickname: string;
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

export interface MoveDto {
  moveNumber: number;
  color: string;
  san: string;
  uci: string;
  fenAfter: string;
}

export interface PositionCandidateDto {
  san: string;
  uci: string;
  eval: number | null;
  mate: number | null;
  winChance: number | null;
  continuationFirst: string | null;
}

export interface PositionAnalysisResponse {
  fen: string;
  variants: number;
  candidates: PositionCandidateDto[];
}

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

export type GameStatus =
  | 'WAITING_FOR_OPPONENT'
  | 'IN_PROGRESS'
  | 'WHITE_WINS'
  | 'BLACK_WINS'
  | 'DRAW'
  | 'ABORTED';
export type EndReason =
  | 'CHECKMATE'
  | 'STALEMATE'
  | 'RESIGNATION'
  | 'INSUFFICIENT_MATERIAL'
  | 'FIFTY_MOVE_RULE'
  | 'THREEFOLD_REPETITION'
  | 'TIME_OUT'
  | 'AGREEMENT';
export type Color = 'WHITE' | 'BLACK';
export type BotDifficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface GameDto {
  id: string;
  whitePlayerId: string | null;
  blackPlayerId: string | null;
  status: GameStatus;
  endReason: EndReason | null;
  currentFen: string;
  moveListUci: string | null;
  timeControl: string | null;
  whiteTimeMs: number;
  blackTimeMs: number;
  turn: Color | null;
  drawOfferedBy: Color | null;
  vsBot: boolean;
  botColor: Color | null;
  botDifficulty: BotDifficulty | null;
  startedAt: string | null;
  endedAt: string | null;
}

export interface GameUpdateDto {
  fen: string;
  whiteTimeMs: number;
  blackTimeMs: number;
  status: GameStatus;
  lastMove: string | null;
}

export interface MoveRequest {
  move: string;
}

export interface CreateGameRequest {
  whitePlayerId?: string | null;
  blackPlayerId?: string | null;
  timeControl?: string | null;
  startingFen?: string | null;
  vsBot: boolean;
  playerColor?: Color | null;
  botDifficulty?: BotDifficulty | null;
}

export type ChatMessageType = 'USER' | 'SYSTEM' | 'BOT_LLM';

export interface ChatMessageDto {
  id: string;
  gameId: string;
  authorId: string | null;
  type: ChatMessageType;
  text: string;
  createdAt: string;
}

export interface SendChatRequest {
  text: string;
}
