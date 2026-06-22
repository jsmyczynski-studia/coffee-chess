import { useState, type FormEvent } from 'react';
import { ApiError } from '../../lib/api/http';
import { userApi } from '../../lib/api/userApi';
import { gameApi } from '../../lib/api/gameApi';
import type { GameDto, UserProfile } from '../../lib/api/types';
import { useAuth } from '../../lib/auth/AuthContext';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { StatCard } from '../../components/ui/StatCard';

export function ProfilePage() {
  const { authenticated, user, token, login } = useAuth();
  const [nickname, setNickname] = useState('');
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [history, setHistory] = useState<GameDto[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSearch(event: FormEvent) {
    event.preventDefault();
    if (!nickname.trim()) return;

    setLoading(true);
    setError(null);
    setProfile(null);
    setHistory([]);

    try {
      const data = await userApi.profile(nickname.trim());
      setProfile(data);

      // Pull the game list straight from game-service (same source the analysis page uses),
      // so a player's games show up regardless of the completion-event pipeline.
      if (authenticated && token && user?.username === data.nickname) {
        try {
          const games = await gameApi.listGames(token);
          const finished = games
            .filter((g) => g.status !== 'IN_PROGRESS' && g.status !== 'WAITING_FOR_OPPONENT')
            .sort((a, b) => new Date(b.endedAt || b.startedAt || 0).getTime() - new Date(a.endedAt || a.startedAt || 0).getTime());
          setHistory(finished);
        } catch {
          setHistory([]);
        }
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać profilu');
    } finally {
      setLoading(false);
    }
  }

  function useMyNickname() {
    if (user?.username) setNickname(user.username);
  }

  return (
    <div className="page-grid">
      <Card title="Profil gracza" subtitle="Wyszukaj gracza po nazwie.">
        {!authenticated && (
          <Alert tone="info">
            Profil jest publiczny.{' '}
            <button type="button" className="link-btn" onClick={() => login()}>
              Zaloguj się
            </button>
            , aby zobaczyć własną historię partii.
          </Alert>
        )}

        <form className="inline-form" onSubmit={handleSearch}>
          <Input
            label="Nickname"
            placeholder="np. player1"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
          />
          {authenticated && (
            <Button type="button" variant="ghost" onClick={useMyNickname}>
              Mój nick
            </Button>
          )}
          <Button type="submit" disabled={loading}>
            {loading ? 'Szukam…' : 'Pokaż profil'}
          </Button>
        </form>

        {error && <Alert tone="error">{error}</Alert>}
      </Card>

      {profile && (
        <>
          <div className="stats-grid">
            <StatCard label="ELO" value={profile.eloRating} />
            <StatCard label="Rozegrane" value={profile.gamesPlayed} />
            <StatCard label="Wygrane" value={profile.gamesWon} />
            <StatCard label="Przegrane" value={profile.gamesLost} />
            <StatCard label="Remisy" value={profile.gamesDrawn} />
          </div>
          <p className="muted small" style={{ marginTop: '-0.5rem' }}>
            Statystyki z gier przeciwko innym graczom.
          </p>

          <Card title={profile.nickname}>
            <dl className="detail-list">
              <dt>Dołączył</dt>
              <dd>{new Date(profile.createdAt).toLocaleString('pl-PL')}</dd>
            </dl>
          </Card>

          {authenticated && user?.username === profile.nickname && (
            <Card title="Historia partii">
              {history.length === 0 ? (
                <p className="muted">Brak rozegranych partii.</p>
              ) : (
                <ul className="history-list">
                  {history.map((g) => {
                    const isWhite = g.whitePlayerId === user?.id;
                    let result = 'Remis';
                    if (g.endReason === 'CHECKMATE' || g.endReason === 'RESIGNATION' || g.endReason === 'TIME_OUT') {
                      // Winner is the side NOT to move at the end (the mated/flagged/resigned side is to move).
                      const sideToMove = g.currentFen?.split(' ')[1] === 'w' ? 'WHITE' : 'BLACK';
                      const iLost = (isWhite && sideToMove === 'WHITE') || (!isWhite && sideToMove === 'BLACK');
                      result = iLost ? 'Przegrana' : 'Wygrana';
                    }
                    return (
                      <li key={g.id}>
                        <span className="history-outcome">{result}</span>
                        <span>{g.vsBot ? 'Gra z komputerem' : 'vs gracz'} ({isWhite ? 'białe' : 'czarne'})</span>
                        <span className="muted">{new Date(g.endedAt || g.startedAt || '').toLocaleString('pl-PL')}</span>
                      </li>
                    );
                  })}
                </ul>
              )}
            </Card>
          )}
        </>
      )}
    </div>
  );
}
