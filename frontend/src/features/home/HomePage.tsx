import { Link } from 'react-router-dom';
import { useAuth } from '../../lib/auth/AuthContext';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { StatCard } from '../../components/ui/StatCard';

export function HomePage() {
  const { authenticated, login } = useAuth();

  return (
    <div className="page-grid">
      <Card
        title="Coffee Chess"
        subtitle="Frontend produkcyjny spięty z mikroserwisami Spring Boot."
        className="span-2"
      >
        <p className="lead">
          Ranking, profile, znajomi i analiza partii są podłączone do REST API. Moduł gry czeka na
          GameController w game-service.
        </p>
        <div className="hero-actions">
          {authenticated ? (
            <>
              <Link to="/ranking" className="btn btn-primary">
                Ranking ELO
              </Link>
              <Link to="/friends" className="btn btn-secondary">
                Znajomi
              </Link>
            </>
          ) : (
            <Button onClick={() => login()}>Zaloguj się</Button>
          )}
        </div>
      </Card>

      <div className="stats-grid">
        <StatCard label="User Service" value="8081" hint="profil, ranking, znajomi" />
        <StatCard label="Game Service" value="8082" hint="silnik gry (API w toku)" />
        <StatCard label="Analysis" value="8083" hint="raport partii" />
        <StatCard label="Keycloak" value="8080" hint="OAuth2 / JWT" />
      </div>

      <Card title="Mapowanie epików" className="span-2">
        <ul className="check-list">
          <li className="done">Epic 2 — ranking, profil, znajomi (UI + API)</li>
          <li className="done">Epic 3 — pobieranie raportu analizy</li>
          <li className="done">Epic 4 — logowanie Keycloak + JWT do API</li>
          <li className="pending">Epic 1 — plansza po dodaniu REST/WebSocket w game-service</li>
        </ul>
        <Alert tone="info" title="Produkcja">
          W Dockerze frontend serwowany jest przez nginx z proxy <code>/api/*</code> do mikroserwisów —
          jeden origin, bez problemów CORS.
        </Alert>
      </Card>
    </div>
  );
}
