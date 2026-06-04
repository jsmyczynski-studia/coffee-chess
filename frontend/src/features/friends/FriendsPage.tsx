import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { ApiError } from '../../lib/api/http';
import { userApi } from '../../lib/api/userApi';
import type { Friendship } from '../../lib/api/types';
import { useAuth } from '../../lib/auth/AuthContext';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { ProtectedRoute } from '../../components/layout/ProtectedRoute';

function FriendsContent() {
  const { token } = useAuth();
  const [friends, setFriends] = useState<Friendship[]>([]);
  const [invites, setInvites] = useState<Friendship[]>([]);
  const [nickname, setNickname] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!token) return;
    setError(null);
    try {
      const [f, i] = await Promise.all([userApi.friends(token), userApi.pendingInvites(token)]);
      setFriends(f);
      setInvites(i);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Błąd ładowania znajomych');
    }
  }, [token]);

  useEffect(() => {
    void load();
  }, [load]);

  async function handleInvite(event: FormEvent) {
    event.preventDefault();
    if (!token || !nickname.trim()) return;
    setError(null);
    setSuccess(null);
    try {
      await userApi.sendFriendRequest(nickname.trim(), token);
      setNickname('');
      setSuccess('Wysłano zaproszenie.');
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się wysłać zaproszenia');
    }
  }

  async function handleAccept(id: string) {
    if (!token) return;
    setError(null);
    try {
      await userApi.acceptFriendRequest(id, token);
      setSuccess('Zaakceptowano zaproszenie.');
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Błąd akceptacji');
    }
  }

  return (
    <div className="page-grid">
      <Card title="Znajomi" subtitle="Wymaga JWT — Keycloak subject musi istnieć w tabeli users.">
        <form className="inline-form" onSubmit={handleInvite}>
          <Input
            label="Nickname znajomego"
            placeholder="min. 3 znaki"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            minLength={3}
            maxLength={32}
          />
          <Button type="submit">Wyślij zaproszenie</Button>
        </form>
        {success && <Alert tone="success">{success}</Alert>}
        {error && <Alert tone="error">{error}</Alert>}
        <Alert tone="warning" title="Backend">
          Endpointy używają UUID z JWT (Keycloak). Użytkownik musi być zsynchronizowany z user-service —
          inaczej zaproszenia zwrócą błąd.
        </Alert>
      </Card>

      <Card title={`Zaproszenia (${invites.length})`}>
        {invites.length === 0 ? (
          <p className="muted">Brak oczekujących zaproszeń.</p>
        ) : (
          <ul className="action-list">
            {invites.map((invite) => (
              <li key={invite.id}>
                <span>Od gracza {invite.requesterId.slice(0, 8)}…</span>
                <Button variant="secondary" onClick={() => handleAccept(invite.id)}>
                  Akceptuj
                </Button>
              </li>
            ))}
          </ul>
        )}
      </Card>

      <Card title={`Twoi znajomi (${friends.length})`}>
        {friends.length === 0 ? (
          <p className="muted">Brak zaakceptowanych znajomych.</p>
        ) : (
          <ul className="action-list">
            {friends.map((f) => (
              <li key={f.id}>
                <span>
                  {f.requesterId.slice(0, 8)}… ↔ {f.addresseeId.slice(0, 8)}…
                </span>
                <span className="status-pill">{f.status}</span>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}

export function FriendsPage() {
  return (
    <ProtectedRoute>
      <FriendsContent />
    </ProtectedRoute>
  );
}
