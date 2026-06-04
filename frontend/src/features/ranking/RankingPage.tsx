import { userApi } from '../../lib/api/userApi';
import { useAsync } from '../../hooks/useAsync';
import { Alert } from '../../components/ui/Alert';
import { Card } from '../../components/ui/Card';
import { EmptyState } from '../../components/ui/EmptyState';

export function RankingPage() {
  const { data, error, loading } = useAsync(() => userApi.ranking(0, 50), []);

  return (
    <Card title="Ranking ELO" subtitle="GET /api/ranking — endpoint publiczny (bez JWT).">
      {loading && <p className="muted">Ładowanie rankingu…</p>}
      {error && <Alert tone="error">{error}</Alert>}

      {!loading && !error && data && data.content.length === 0 && (
        <EmptyState
          title="Brak graczy"
          description="Uruchom user-service i dodaj użytkowników w bazie PostgreSQL."
        />
      )}

      {!loading && !error && data && data.content.length > 0 && (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Gracz</th>
                <th>ELO</th>
                <th>Mecze</th>
                <th>W</th>
                <th>L</th>
                <th>D</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((player, index) => (
                <tr key={player.id}>
                  <td>{data.number * data.size + index + 1}</td>
                  <td>
                    <strong>{player.nickname}</strong>
                  </td>
                  <td>
                    <span className="elo-badge">{player.eloRating}</span>
                  </td>
                  <td>{player.gamesPlayed}</td>
                  <td className="win">{player.gamesWon}</td>
                  <td className="loss">{player.gamesLost}</td>
                  <td>{player.gamesDrawn}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
}
