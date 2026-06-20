import { useLocation } from 'react-router-dom';
import { useAuth } from '../../lib/auth/AuthContext';
import { Button } from '../ui/Button';
import { Card } from '../ui/Card';

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { authenticated, login } = useAuth();
  const location = useLocation();

  if (!authenticated) {
    return (
      <Card title="Wymagane logowanie" subtitle="Ta sekcja korzysta z chronionych endpointów user-service.">
        <p className="muted">
          Zaloguj się przez Keycloak, aby uzyskać token JWT wysyłany do backendu.
        </p>
        <Button onClick={() => login()}>Zaloguj się</Button>
        <p className="muted small" style={{ marginTop: '1rem' }}>
          Próbowano wejść na: {location.pathname}
        </p>
      </Card>
    );
  }

  return children;
}
