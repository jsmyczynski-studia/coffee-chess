import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Alert } from '../../components/ui/Alert';
import { useAuth } from '../../lib/auth/AuthContext';
import { gameApi } from '../../lib/api/gameApi';
import { userApi } from '../../lib/api/userApi';
import type { GameDto, BotDifficulty, Color } from '../../lib/api/types';
import { ApiError } from '../../lib/api/http';

export function GameLobbyPage() {
  const { token, authenticated, login, user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeGames, setActiveGames] = useState<GameDto[]>([]);

  // Setup Flow State
  const [mode, setMode] = useState<'BOT' | 'HUMAN'>('BOT');
  const [botDifficulty, setBotDifficulty] = useState<BotDifficulty>('EASY');
  const [playerColor, setPlayerColor] = useState<Color>('WHITE');
  const [timeControl, setTimeControl] = useState('10+0');
  
  // Friend select state
  const [friendsList, setFriendsList] = useState<{ id: string, nickname: string }[]>([]);
  const [selectedFriendId, setSelectedFriendId] = useState('');

  useEffect(() => {
    if (authenticated && token) {
      // Load active games
      gameApi.listGames(token)
        .then(games => {
          setActiveGames(games.filter(g => g.status === 'IN_PROGRESS' || g.status === 'WAITING_FOR_OPPONENT'));
        })
        .catch(err => console.error('Failed to load active games', err));

      // Load friends and usernames for dropdown
      if (user) {
        Promise.all([
          userApi.friends(token),
          userApi.ranking(0, 1000) // fetch up to 1000 users to map IDs to usernames
        ]).then(([friendships, rankingPage]) => {
          const acceptedFriends = friendships.filter(f => f.status === 'ACCEPTED');
          const friendIds = acceptedFriends.map(f => f.requesterId === user.id ? f.addresseeId : f.requesterId);
          
          const usersMap = new Map(rankingPage.content.map(u => [u.id, u.nickname]));
          const mappedFriends = friendIds.map(id => ({
            id,
            nickname: usersMap.get(id) || 'Nieznany gracz'
          }));
          
          setFriendsList(mappedFriends);
          if (mappedFriends.length > 0) {
            setSelectedFriendId(mappedFriends[0].id);
          }
        }).catch(err => console.error('Failed to load friends list for lobby', err));
      }
    }
  }, [authenticated, token, user]);

  const handleCreateBotGame = () => {
    if (!authenticated || !token) {
      login();
      return;
    }

    setLoading(true);
    setError(null);

    gameApi.createGame({
      vsBot: true,
      botDifficulty,
      playerColor,
      timeControl
    }, token)
      .then((game) => {
        navigate(`/play/${game.id}`);
      })
      .catch((err) => {
        setError(err.message || 'Nie udało się utworzyć gry z botem');
        setLoading(false);
      });
  };

  const handleCreateHumanGame = async () => {
    if (!authenticated || !token || !user) {
      login();
      return;
    }
    if (!selectedFriendId) {
      setError('Wybierz znajomego z listy.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const whiteId = playerColor === 'WHITE' ? user.id : selectedFriendId;
      const blackId = playerColor === 'BLACK' ? user.id : selectedFriendId;

      const game = await gameApi.createGame({
        vsBot: false,
        whitePlayerId: whiteId,
        blackPlayerId: blackId,
        timeControl
      }, token);

      navigate(`/play/${game.id}`);
    } catch (err: any) {
      setError(err instanceof ApiError ? err.message : err.message || 'Nie udało się utworzyć gry ze znajomym');
      setLoading(false);
    }
  };

  return (
    <div className="page-grid">
      <Card title="Nowa Gra" className="span-2">
        {error && <div style={{ marginBottom: '1rem' }}><Alert tone="error" title="Błąd">{error}</Alert></div>}
        
        <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
          <Button 
            variant={mode === 'BOT' ? 'primary' : 'secondary'} 
            onClick={() => setMode('BOT')}
          >
            Graj z Botem
          </Button>
          <Button 
            variant={mode === 'HUMAN' ? 'primary' : 'secondary'} 
            onClick={() => setMode('HUMAN')}
          >
            Zaproś znajomego
          </Button>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', maxWidth: '400px' }}>
          
          {/* Wspólne opcje: Kolor i Czas */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <label style={{ fontWeight: 'bold' }}>Twój kolor</label>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <Button 
                variant={playerColor === 'WHITE' ? 'primary' : 'ghost'} 
                onClick={() => setPlayerColor('WHITE')}
                style={{ flex: 1 }}
              >
                ♔ Białe
              </Button>
              <Button 
                variant={playerColor === 'BLACK' ? 'primary' : 'ghost'} 
                onClick={() => setPlayerColor('BLACK')}
                style={{ flex: 1 }}
              >
                ♚ Czarne
              </Button>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <label style={{ fontWeight: 'bold' }}>Czas gry</label>
            <select 
              value={timeControl} 
              onChange={(e) => setTimeControl(e.target.value)}
              className="field-input"
            >
              <option value="5+0">5 min (Blitz)</option>
              <option value="10+0">10 min (Rapid)</option>
              <option value="15+10">15 min + 10s (Rapid)</option>
              <option value="30+0">30 min (Classic)</option>
            </select>
          </div>

          {/* Opcje specyficzne dla Bota */}
          {mode === 'BOT' && (
            <>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <label style={{ fontWeight: 'bold' }}>Trudność bota</label>
                <select 
                  value={botDifficulty} 
                  onChange={(e) => setBotDifficulty(e.target.value as BotDifficulty)}
                  className="field-input"
                >
                  <option value="EASY">Łatwy</option>
                  <option value="MEDIUM">Średni</option>
                  <option value="HARD">Trudny</option>
                </select>
              </div>
              <Button onClick={handleCreateBotGame} disabled={loading} style={{ marginTop: '1rem' }}>
                {loading ? 'Tworzenie gry...' : 'Graj'}
              </Button>
            </>
          )}

          {/* Opcje specyficzne dla Znajomego */}
          {mode === 'HUMAN' && (
            <>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <label style={{ fontWeight: 'bold' }}>Znajomy</label>
                {friendsList.length === 0 ? (
                  <p className="muted" style={{ margin: 0, fontSize: '0.9rem' }}>Nie masz jeszcze żadnych dodanych znajomych.</p>
                ) : (
                  <select 
                    value={selectedFriendId} 
                    onChange={(e) => setSelectedFriendId(e.target.value)}
                    className="field-input"
                  >
                    <option value="" disabled>-- Wybierz znajomego --</option>
                    {friendsList.map(f => (
                      <option key={f.id} value={f.id}>{f.nickname}</option>
                    ))}
                  </select>
                )}
              </div>
              <Button 
                onClick={handleCreateHumanGame} 
                disabled={loading || friendsList.length === 0 || !selectedFriendId} 
                style={{ marginTop: '1rem' }}
              >
                {loading ? 'Wysyłanie zaproszenia...' : 'Zaproś i Graj'}
              </Button>
            </>
          )}

        </div>
      </Card>

      {authenticated && activeGames.length > 0 && (
        <Card title="Twoje trwające gry" className="span-2">
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {activeGames.map(game => (
              <li key={game.id} style={{ padding: '0.5rem 0', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>
                  <strong>Gra:</strong> {game.id.slice(0, 8)}... - <strong>Status:</strong> {game.status}
                  {game.vsBot && <span className="muted" style={{ marginLeft: '1rem' }}>Z botem ({game.botDifficulty})</span>}
                </span>
                <Button onClick={() => navigate(`/play/${game.id}`)} variant="ghost">
                  Wznów grę
                </Button>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
}
