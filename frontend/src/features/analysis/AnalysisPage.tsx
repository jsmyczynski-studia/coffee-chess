import { useState, type FormEvent } from 'react';
import { analysisApi } from '../../lib/api/analysisApi';
import { ApiError } from '../../lib/api/http';
import type { AnalysisReport } from '../../lib/api/types';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { StatCard } from '../../components/ui/StatCard';

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export function AnalysisPage() {
  const [gameId, setGameId] = useState('');
  const [report, setReport] = useState<AnalysisReport | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const id = gameId.trim();
    if (!UUID_REGEX.test(id)) {
      setError('Podaj poprawny UUID partii.');
      return;
    }

    setLoading(true);
    setError(null);
    setReport(null);

    try {
      const data = await analysisApi.report(id);
      setReport(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się pobrać analizy');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-grid">
      <Card title="Analiza partii" subtitle="GET /api/analysis/{gameId} — analysis-service">
        <form className="inline-form" onSubmit={handleSubmit}>
          <Input
            label="UUID partii"
            placeholder="00000000-0000-0000-0000-000000000000"
            value={gameId}
            onChange={(e) => setGameId(e.target.value)}
          />
          <Button type="submit" disabled={loading}>
            {loading ? 'Pobieranie…' : 'Pobierz raport'}
          </Button>
        </form>
        {error && <Alert tone="error">{error}</Alert>}
        <Alert tone="info">
          Raport generowany jest po evencie Kafka <code>game-completed</code>. Obecnie backend ma
          placeholder analizy — UI jest gotowe na pełne dane.
        </Alert>
      </Card>

      {report && (
        <>
          <div className="stats-grid">
            <StatCard label="Status" value={report.status} />
            <StatCard label="Błędy" value={report.blunders} hint="blunders" />
            <StatCard label="Pomyłki" value={report.mistakes} />
            <StatCard label="Niedokładności" value={report.inaccuracies} />
          </div>
          <Card title="Szczegóły">
            <dl className="detail-list">
              <dt>Dokładność białych</dt>
              <dd>{report.whiteAccuracy != null ? `${report.whiteAccuracy}%` : '—'}</dd>
              <dt>Dokładność czarnych</dt>
              <dd>{report.blackAccuracy != null ? `${report.blackAccuracy}%` : '—'}</dd>
              <dt>Zakończono</dt>
              <dd>
                {report.completedAt
                  ? new Date(report.completedAt).toLocaleString('pl-PL')
                  : '—'}
              </dd>
              <dt>Ruchy w raporcie</dt>
              <dd>{report.moves.length}</dd>
            </dl>
          </Card>
        </>
      )}
    </div>
  );
}
