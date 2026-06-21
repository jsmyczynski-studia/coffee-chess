import { useEffect, useState, useRef, type FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import { Chessboard } from 'react-chessboard';
import { Client } from '@stomp/stompjs';
import { Card } from '../../components/ui/Card';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { useAuth } from '../../lib/auth/AuthContext';
import { gameApi } from '../../lib/api/gameApi';
import type { GameDto, GameUpdateDto, ChatMessageDto } from '../../lib/api/types';

export function PlayPage() {
  const { id } = useParams<{ id: string }>();
  const { token, user } = useAuth();
  
  const [game, setGame] = useState<GameDto | null>(null);
  const [fen, setFen] = useState<string>('start');
  const [error, setError] = useState<string | null>(null);
  const [localWhiteTime, setLocalWhiteTime] = useState<number>(0);
  const [localBlackTime, setLocalBlackTime] = useState<number>(0);
  
  const [chatMessages, setChatMessages] = useState<ChatMessageDto[]>([]);
  const [chatInput, setChatInput] = useState('');
  const chatEndRef = useRef<HTMLDivElement>(null);

  const applyGameUpdate = (update: GameUpdateDto) => {
    const nextTurn = update.fen.split(' ')[1] === 'w' ? 'WHITE' : 'BLACK';

    setFen(update.fen);
    setLocalWhiteTime(update.whiteTimeMs);
    setLocalBlackTime(update.blackTimeMs);

    setGame(prev => {
      if (!prev) return prev;
      return {
        ...prev,
        currentFen: update.fen,
        status: update.status,
        whiteTimeMs: update.whiteTimeMs,
        blackTimeMs: update.blackTimeMs,
        turn: nextTurn,
        moveListUci: update.lastMove
          ? (prev.moveListUci ? `${prev.moveListUci} ${update.lastMove}` : update.lastMove)
          : prev.moveListUci,
      };
    });
  };

  const refreshGame = async () => {
    if (!id || !token) return;
    const fullGame = await gameApi.getGame(id, token);
    setGame(fullGame);
    if (fullGame.currentFen) setFen(fullGame.currentFen);
    setLocalWhiteTime(fullGame.whiteTimeMs);
    setLocalBlackTime(fullGame.blackTimeMs);
  };

  // Initial fetch and WebSocket setup
  useEffect(() => {
    if (!id) return;

    // 1. Fetch initial game state and chat history
    gameApi.getGame(id, token ?? undefined)
      .then((data) => {
        setGame(data);
        if (data.currentFen) setFen(data.currentFen);
        setLocalWhiteTime(data.whiteTimeMs);
        setLocalBlackTime(data.blackTimeMs);
      })
      .catch((err) => {
        setError(err.message || 'Nie udało się pobrać gry');
      });

    if (token) {
      gameApi.getChat(id, token)
        .then(setChatMessages)
        .catch(console.error);
    }

    // 2. Setup STOMP WebSocket
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;

    const stompClient = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        // Chat Channel
        stompClient.subscribe(`/topic/games/${id}/chat`, (message) => {
          const chatMsg: ChatMessageDto = JSON.parse(message.body);
          setChatMessages(prev => [...prev, chatMsg]);
        });

        // Game Update Channel
        stompClient.subscribe(`/topic/games/${id}`, (message) => {
          const update: GameUpdateDto = JSON.parse(message.body);
          applyGameUpdate(update);

          // Re-fetch full game state in background to update drawOfferedBy and endReason smoothly without backend changes
          refreshGame().catch(console.error);
        });
      },
      onStompError: (frame) => {
        console.error('STOMP Broker reported error:', frame.headers['message']);
      }
    });

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [id, token]);

  // Local clock tick
  useEffect(() => {
    if (game?.status !== 'IN_PROGRESS') return;

    const tick = setInterval(() => {
      if (game.turn === 'WHITE') {
        setLocalWhiteTime(prev => Math.max(0, prev - 1000));
      } else {
        setLocalBlackTime(prev => Math.max(0, prev - 1000));
      }
    }, 1000);

    return () => clearInterval(tick);
  }, [game?.status, game?.turn]);

  // Auto-scroll chat to bottom
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages]);

  const onPieceDrop = ({ sourceSquare, targetSquare, piece }: { sourceSquare: string, targetSquare: string | null, piece: { pieceType: string } }) => {
    if (!id || !token || !game || !targetSquare) return false;

    let moveUci = sourceSquare + targetSquare;
    if (piece.pieceType === 'wP' && targetSquare.endsWith('8')) moveUci += 'q';
    if (piece.pieceType === 'bP' && targetSquare.endsWith('1')) moveUci += 'q';

    gameApi.submitMove(id, moveUci, token)
      .catch((err) => {
        console.error('Invalid move or error', err);
        const lastFen = game.currentFen || 'start';
        setFen('8/8/8/8/8/8/8/8 w - - 0 1');
        setTimeout(() => setFen(lastFen), 50);
      });
      
    return true;
  };

  const runGameAction = async (action: () => Promise<GameUpdateDto>) => {
    if (!id || !token) return;
    setError(null);
    try {
      const update = await action();
      applyGameUpdate(update);
      await refreshGame();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Nie udało się wykonać akcji');
    }
  };

  const handleResign = () => {
    void runGameAction(() => gameApi.resign(id!, token!));
  };

  const handleOfferDraw = () => {
    void runGameAction(() => gameApi.offerDraw(id!, token!));
  };

  const handleAcceptDraw = () => {
    void runGameAction(() => gameApi.acceptDraw(id!, token!));
  };

  const handleDeclineDraw = () => {
    void runGameAction(() => gameApi.declineDraw(id!, token!));
  };

  const handleSendChat = (e: FormEvent) => {
    e.preventDefault();
    if (!id || !token || !chatInput.trim()) return;
    gameApi.sendChat(id, chatInput.trim(), token)
      .then(() => setChatInput(''))
      .catch((err) => setError(err instanceof Error ? err.message : 'Nie udało się wysłać wiadomości'));
  };

  const formatTime = (ms: number) => {
    const totalSeconds = Math.floor(ms / 1000);
    const m = Math.floor(totalSeconds / 60);
    const s = totalSeconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  if (error) {
    return <Alert tone="error" title="Błąd">{error}</Alert>;
  }

  if (!game) {
    return (
      <div className="page-grid">
        <Card title="Plansza" className="span-2">Ładowanie gry...</Card>
      </div>
    );
  }

  const isPlayerWhite = user?.id === game.whitePlayerId;
  const isPlayerBlack = user?.id === game.blackPlayerId;
  const isParticipant = isPlayerWhite || isPlayerBlack;
  const playerColor = isPlayerWhite ? 'WHITE' : isPlayerBlack ? 'BLACK' : null;
  
  const drawOfferedByOpponent = game.drawOfferedBy && game.drawOfferedBy !== playerColor;
  const drawOfferedByMe = game.drawOfferedBy && game.drawOfferedBy === playerColor;

  return (
    <div className="page-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))' }}>
      <Card
        title="Plansza"
        subtitle={`Gra: ${game.id.split('-')[0]}... - Status: ${game.status}`}
      >
        <div className="board-panel" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          
          <div className="play-sidebar-preview" style={{ display: 'flex', gap: '1rem' }}>
            <div className={`clock-box ${game.turn === 'BLACK' ? 'active' : ''}`} style={{ padding: '0.75rem' }}>
              <span className="clock-label">Czarne</span>
              <span className="clock-time" style={{ fontSize: '1.4rem' }}>{formatTime(localBlackTime)}</span>
            </div>
            <div className={`clock-box ${game.turn === 'WHITE' ? 'active' : ''}`} style={{ padding: '0.75rem' }}>
              <span className="clock-label">Białe</span>
              <span className="clock-time" style={{ fontSize: '1.4rem' }}>{formatTime(localWhiteTime)}</span>
            </div>
          </div>

          <div style={{ width: '100%', maxWidth: '480px', margin: '0 auto' }}>
            <Chessboard 
              options={{
                position: fen, 
                onPieceDrop: onPieceDrop, 
                animationDurationInMs: 200,
                boardOrientation: isPlayerBlack ? 'black' : 'white'
              }}
            />
          </div>

          {game.status !== 'IN_PROGRESS' && game.status !== 'WAITING_FOR_OPPONENT' && (
            <Alert tone="info" title="Gra zakończona">
              Powód: {game.endReason || 'Brak danych'}
            </Alert>
          )}

          {isParticipant && game.status === 'IN_PROGRESS' && (
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', justifyContent: 'center' }}>
              <Button variant="danger" onClick={handleResign}>Poddaj się</Button>
              
              {!game.drawOfferedBy && (
                <Button variant="secondary" onClick={handleOfferDraw}>Zaproponuj remis</Button>
              )}
              
              {drawOfferedByMe && (
                <span className="muted" style={{ alignSelf: 'center', fontSize: '0.9rem' }}>Remis zaproponowany...</span>
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

      <Card title="Czat i Informacje">
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
                        border: isBot ? '1px solid var(--accent)' : 'none'
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
              placeholder="Napisz wiadomość..." 
              className="field-input"
              style={{ flex: 1 }}
              disabled={!isParticipant || game.status !== 'IN_PROGRESS'}
            />
            <Button type="submit" disabled={!isParticipant || !chatInput.trim() || game.status !== 'IN_PROGRESS'}>Wyślij</Button>
          </form>

        </div>
      </Card>
    </div>
  );
}
