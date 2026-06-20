import { useState, type FormEvent } from 'react';
import { ApiError } from '../../lib/api/http';
import { userApi } from '../../lib/api/userApi';
import type { GameHistoryEntry, UserProfile } from '../../lib/api/types';
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
  const [history, setHistory] = useState<GameHistoryEntry[]>([]);
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

      if (authenticated && token && user?.username === data.nickname) {
        try {
          const hist = await userApi.gameHistory(data.id, 0, 10, token);
          setHistory(hist.content);
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
      <Card title="Profil gracza" subtitle="GET /api/users/{nickname}/profile">
        {!authenticated && (
          <Alert tone="info">
            Profil jest publiczny.{' '}
            <button type="button" className="link-btn" onClick={() => login()}>
              Zaloguj się
            </button>
            , aby zobaczyć własną historię partii (wymaga JWT + UUID w bazie).
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

          <Card title={profile.nickname} subtitle={`ID: ${profile.id}`}>
            <dl className="detail-list">
              <dt>Dołączył</dt>
              <dd>{new Date(profile.createdAt).toLocaleString('pl-PL')}</dd>
              <dt>Avatar</dt>
              <dd>{profile.avatarUrl ?? '—'}</dd>
            </dl>
          </Card>

          {authenticated && user?.username === profile.nickname && (
            <Card title="Historia partii" subtitle="GET /api/users/{id}/history (JWT)">
              {history.length === 0 ? (
                <p className="muted">Brak zapisanych partii lub brak uprawnień.</p>
              ) : (
                <ul className="history-list">
                  {history.map((entry) => (
                    <li key={entry.id}>
                      <span className="history-outcome">{entry.outcome}</span>
                      <span>{new Date(entry.playedAt).toLocaleString('pl-PL')}</span>
                      <span className="muted">game {entry.gameId.slice(0, 8)}…</span>
                    </li>
                  ))}
                </ul>
              )}
            </Card>
          )}
        </>
      )}
    </div>
  );
}
