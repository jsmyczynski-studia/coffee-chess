import { useEffect, useState, useRef, useCallback, type CSSProperties, type FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import { Chessboard } from 'react-chessboard';
import { Client } from '@stomp/stompjs';
import { Card } from '../../components/ui/Card';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../lib/auth/AuthContext';
import { gameApi } from '../../lib/api/gameApi';
import type { GameDto, ChatMessageDto } from '../../lib/api/types';

const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

export function PlayPage() {
  const { id } = useParams<{ id: string }>();
  const { token, user } = useAuth();

  const [game, setGame] = useState<GameDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [whiteTime, setWhiteTime] = useState<number>(0);
  const [blackTime, setBlackTime] = useState<number>(0);

  const [chatMessages, setChatMessages] = useState<ChatMessageDto[]>([]);
  const [chatInput, setChatInput] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);

  // Click-to-move: first click selects a square, second click submits the move.
  const [selectedSquare, setSelectedSquare] = useState<string | null>(null);
  const [legalTargets, setLegalTargets] = useState<string[]>([]);
  const selectionRequestRef = useRef(0);

  // The board position is always whatever the backend last told us. The backend is the
  // single source of truth for all chess logic and validation.
  const fen = game?.currentFen || START_FEN;

  // --- Data loading (HTTP is authoritative; websocket is only a live-update bonus) ---

  const lastTurnRef = useRef<string | null>(null);

  const loadGame = useCallback(async () => {
    if (!id || !token) return;
    const data = await gameApi.getGame(id, token);
    setGame(data);
    // Only resync the clock from the server when the turn changed (a move happened) or the
    // game isn't actively running. Between moves the server clock is already counting down
    // live, but resyncing on every 1.5s poll fought the local 1s tick and made the displayed
    // time visibly jump backwards. Letting the local tick run between turn changes is smooth.
    const turnChanged = data.turn !== lastTurnRef.current;
    if (turnChanged || data.status !== 'IN_PROGRESS') {
      setWhiteTime(data.whiteTimeMs);
      setBlackTime(data.blackTimeMs);
      lastTurnRef.current = data.turn;
    }
  }, [id, token]);

  const loadChat = useCallback(async () => {
    if (!id || !token) return;
    const msgs = await gameApi.getChat(id, token);
    setChatMessages(msgs);
  }, [id, token]);

  // Initial load.
  useEffect(() => {
    if (!id || !token) return;
    loadGame().catch((e) => setError(e?.message || 'Nie udało się pobrać gry'));
    loadChat().catch(() => undefined);
  }, [id, token, loadGame, loadChat]);

  // Polling fallback: keep game + chat fresh over plain HTTP so moves (including the bot's),
  // opponent moves and chat all appear even if the websocket never connects.
  useEffect(() => {
    if (!id || !token) return;
    if (game && game.status !== 'IN_PROGRESS' && game.status !== 'WAITING_FOR_OPPONENT') return;

    const interval = setInterval(() => {
      loadGame().catch(() => undefined);
      loadChat().catch(() => undefined);
    }, 1500);

    return () => clearInterval(interval);
  }, [id, token, game?.status, loadGame, loadChat]);

  // Optional websocket for instant updates. If it fails, polling already covers us.
  useEffect(() => {
    if (!id || !token) return;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    const client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/games/${id}`, () => {
          loadGame().catch(() => undefined);
        });
        client.subscribe(`/topic/games/${id}/chat`, () => {
          loadChat().catch(() => undefined);
        });
      },
    });

    client.activate();
    return () => {
      client.deactivate().catch(() => undefined);
    };
  }, [id, token, loadGame, loadChat]);

  // Local clock tick between server updates.
  useEffect(() => {
    if (game?.status !== 'IN_PROGRESS') return;
    const tick = setInterval(() => {
      if (game.turn === 'WHITE') setWhiteTime((t) => Math.max(0, t - 1000));
      else setBlackTime((t) => Math.max(0, t - 1000));
    }, 1000);
    return () => clearInterval(tick);
  }, [game?.status, game?.turn]);

  // Auto-scroll chat.
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages]);

  // --- Move handling ---

  const playerColor = user?.id === game?.whitePlayerId ? 'WHITE'
    : user?.id === game?.blackPlayerId ? 'BLACK' : null;
  const isParticipant = playerColor !== null;
  const isMyTurn = !!game && game.status === 'IN_PROGRESS' && game.turn === playerColor;

  useEffect(() => {
    selectionRequestRef.current += 1;
    setSelectedSquare(null);
    setLegalTargets([]);
  }, [game?.currentFen, game?.status, game?.turn]);

  const clearSelection = () => {
    selectionRequestRef.current += 1;
    setSelectedSquare(null);
    setLegalTargets([]);
  };

  const selectPiece = (square: string) => {
    if (!id || !token) return;
    const requestId = selectionRequestRef.current + 1;
    selectionRequestRef.current = requestId;
    setSelectedSquare(square);
    setLegalTargets([]);

    gameApi.getLegalMoves(id, square, token)
      .then((targets) => {
        if (selectionRequestRef.current === requestId) {
          setLegalTargets(targets);
        }
      })
      .catch(() => {
        if (selectionRequestRef.current === requestId) {
          setLegalTargets([]);
        }
      });
  };

  const submitMove = useCallback(async (from: string, to: string) => {
    if (!id || !token || !game) return;

    // Auto-queen on promotion; the backend decides legality.
    let uci = from + to;
    const movingPawnToLastRank =
      (playerColor === 'WHITE' && to.endsWith('8')) ||
      (playerColor === 'BLACK' && to.endsWith('1'));
    if (movingPawnToLastRank) uci += 'q';

    setError(null);
    try {
      const update = await gameApi.submitMove(id, uci, token);
      // Apply the authoritative result immediately, then refetch full state (covers the
      // bot's reply, draw flags, end reason, etc.). loadGame() handles the clock sync.
      setGame((prev) => prev ? { ...prev, currentFen: update.fen, status: update.status,
        turn: update.fen.split(' ')[1] === 'w' ? 'WHITE' : 'BLACK',
        whiteTimeMs: update.whiteTimeMs, blackTimeMs: update.blackTimeMs } : prev);
      await loadGame();
      await loadChat();
    } catch (e: any) {
      // Illegal/rejected move: just resync to the real position.
      setError(null);
      await loadGame().catch(() => undefined);
    }
  }, [id, token, game, playerColor, loadGame, loadChat]);

  const onSquareClick = ({ square, piece }: { square: string; piece: { pieceType: string } | null }) => {
    if (!isMyTurn) return;

    if (!selectedSquare) {
      // Select only your own piece.
      if (!piece) return;
      const isWhitePiece = piece.pieceType.startsWith('w');
      if ((playerColor === 'WHITE') !== isWhitePiece) return;
      selectPiece(square);
      return;
    }

    if (square === selectedSquare) {
      clearSelection();
      return;
    }

    // Clicking another of your own pieces re-selects it.
    if (piece) {
      const isWhitePiece = piece.pieceType.startsWith('w');
      if ((playerColor === 'WHITE') === isWhitePiece) {
        selectPiece(square);
        return;
      }
    }

    const from = selectedSquare;
    const isLegalTarget = legalTargets.includes(square);
    clearSelection();
    if (isLegalTarget) {
      void submitMove(from, square);
    }
  };

  // Drag still works as a convenience, routed through the same submit path.
  const onPieceDrop = ({ sourceSquare, targetSquare }: { sourceSquare: string; targetSquare: string | null }) => {
    if (!isMyTurn || !targetSquare) return false;
    clearSelection();
    void submitMove(sourceSquare, targetSquare);
    return false;
  };

  const squareStyles: Record<string, CSSProperties> = {};
  if (selectedSquare) {
    squareStyles[selectedSquare] = { background: 'rgba(255, 213, 79, 0.55)' };
  }
  for (const target of legalTargets) {
    squareStyles[target] = {
      background: 'radial-gradient(circle, rgba(30, 150, 80, 0.8) 0 18%, transparent 20%)',
    };
  }

  // --- Game actions ---

  const runAction = async (action: () => Promise<unknown>) => {
    if (!id || !token) return;
    setError(null);
    try {
      await action();
      await loadGame();
    } catch (e: any) {
      setError(e?.message || 'Nie udało się wykonać akcji');
    }
  };

  const handleResign = () => runAction(() => gameApi.resign(id!, token!));
  const handleOfferDraw = () => runAction(() => gameApi.offerDraw(id!, token!));
  const handleAcceptDraw = () => runAction(() => gameApi.acceptDraw(id!, token!));
  const handleDeclineDraw = () => runAction(() => gameApi.declineDraw(id!, token!));

  const handleSendChat = (e: FormEvent) => {
    e.preventDefault();
    if (!id || !token || !chatInput.trim()) return;
    const text = chatInput.trim();
    setChatInput('');
    gameApi.sendChat(id, text, token)
      .then(() => loadChat())
      .catch((err) => setError(err?.message || 'Nie udało się wysłać wiadomości'));
  };

  const formatTime = (ms: number) => {
    const total = Math.floor(ms / 1000);
    const m = Math.floor(total / 60);
    const s = total % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  if (error && !game) {
    return <Alert tone="error" title="Błąd">{error}</Alert>;
  }

  if (!game) {
    return (
      <div className="page-grid">
        <Card title="Plansza" className="span-2">Ładowanie gry…</Card>
      </div>
    );
  }

  const drawOfferedByOpponent = game.drawOfferedBy && game.drawOfferedBy !== playerColor;
  const drawOfferedByMe = game.drawOfferedBy && game.drawOfferedBy === playerColor;
  const gameOver = game.status !== 'IN_PROGRESS' && game.status !== 'WAITING_FOR_OPPONENT';

  return (
    <div className="page-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))' }}>
      <Card title="Plansza">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <div className={`clock-box ${game.turn === 'BLACK' ? 'active' : ''}`} style={{ padding: '0.75rem' }}>
              <span className="clock-label">Czarne</span>
              <span className="clock-time" style={{ fontSize: '1.4rem' }}>{formatTime(blackTime)}</span>
            </div>
            <div className={`clock-box ${game.turn === 'WHITE' ? 'active' : ''}`} style={{ padding: '0.75rem' }}>
              <span className="clock-label">Białe</span>
              <span className="clock-time" style={{ fontSize: '1.4rem' }}>{formatTime(whiteTime)}</span>
            </div>
          </div>

          <div style={{ width: '100%', maxWidth: '480px', margin: '0 auto' }}>
            <Chessboard
              options={{
                position: fen,
                onSquareClick: onSquareClick,
                onPieceDrop: onPieceDrop,
                squareStyles: squareStyles,
                animationDurationInMs: 200,
                boardOrientation: playerColor === 'BLACK' ? 'black' : 'white',
              }}
            />
          </div>

          {isMyTurn && (
            <p className="muted small" style={{ textAlign: 'center', margin: 0 }}>
              Twój ruch — kliknij figurę, a potem pole docelowe.
            </p>
          )}

          {gameOver && (
            <Alert tone="info" title="Gra zakończona">
              {game.endReason ? translateEndReason(game.endReason) : 'Partia zakończona.'}
            </Alert>
          )}

          {isParticipant && game.status === 'IN_PROGRESS' && (
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', justifyContent: 'center' }}>
              <Button variant="danger" onClick={handleResign}>Poddaj się</Button>
              {!game.drawOfferedBy && (
                <Button variant="secondary" onClick={handleOfferDraw}>Zaproponuj remis</Button>
              )}
              {drawOfferedByMe && (
                <span className="muted" style={{ alignSelf: 'center', fontSize: '0.9rem' }}>Remis zaproponowany…</span>
              )}
              {drawOfferedByOpponent && (
                <>
                  <Button variant="primary" onClick={handleAcceptDraw}>Akceptuj remis</Button>
                  <Button variant="secondary" onClick={handleDeclineDraw}>Odrzuć remis</Button>
                </>
              )}
            </div>
          )}
        </div>
      </Card>

      <Card title="Czat">
        <div style={{ display: 'flex', flexDirection: 'column', height: '100%', minHeight: '400px' }}>
          <div style={{ flex: 1, overflowY: 'auto', background: 'var(--bg-elevated)', borderRadius: 'var(--radius-sm)', padding: '1rem', marginBottom: '1rem', border: '1px solid var(--border-soft)' }}>
            {chatMessages.length === 0 ? (
              <p className="muted small text-center">Brak wiadomości. Przywitaj się!</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {chatMessages.map((msg, idx) => {
                  const isMine = msg.authorId === user?.id;
                  const isSystem = msg.type === 'SYSTEM';
                  const isBot = msg.type === 'BOT_LLM';
                  return (
                    <div
                      key={msg.id || idx}
                      style={{
                        alignSelf: isSystem ? 'center' : isMine ? 'flex-end' : 'flex-start',
                        background: isSystem ? 'transparent' : isBot ? 'var(--accent-soft)' : isMine ? '#304156' : 'var(--bg-input)',
                        color: isSystem ? 'var(--text-muted)' : isBot ? 'var(--accent)' : 'var(--text)',
                        padding: isSystem ? '0' : '0.5rem 0.75rem',
                        borderRadius: '8px',
                        maxWidth: '85%',
                        fontSize: isSystem ? '0.8rem' : '0.9rem',
                        border: isBot ? '1px solid var(--accent)' : 'none',
                      }}
                    >
                      {!isSystem && !isMine && (
                        <div style={{ fontSize: '0.7rem', opacity: 0.7, marginBottom: '0.2rem' }}>
                          {isBot ? 'Bot' : 'Przeciwnik'}
                        </div>
                      )}
                      {msg.text}
                    </div>
                  );
                })}
                <div ref={chatEndRef} />
              </div>
            )}
          </div>

          <form onSubmit={handleSendChat} style={{ display: 'flex', gap: '0.5rem' }}>
            <input
              type="text"
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              placeholder="Napisz wiadomość…"
              className="field-input"
              style={{ flex: 1 }}
              disabled={!isParticipant}
            />
            <Button type="submit" disabled={!isParticipant || !chatInput.trim()}>Wyślij</Button>
          </form>
        </div>
      </Card>
    </div>
  );
}

function translateEndReason(reason: string): string {
  switch (reason) {
    case 'CHECKMATE': return 'Mat.';
    case 'RESIGNATION': return 'Poddanie.';
    case 'STALEMATE': return 'Pat.';
    case 'AGREEMENT': return 'Remis za zgodą stron.';
    case 'TIME_OUT': return 'Koniec czasu.';
    case 'INSUFFICIENT_MATERIAL': return 'Niewystarczający materiał.';
    case 'THREEFOLD_REPETITION': return 'Trzykrotne powtórzenie pozycji.';
    case 'FIFTY_MOVE_RULE': return 'Reguła 50 posunięć.';
    default: return 'Partia zakończona.';
  }
}
