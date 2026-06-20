import { Card } from '../../components/ui/Card';
import { Alert } from '../../components/ui/Alert';

const FILES = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'];

export function PlayPage() {
  return (
    <div className="page-grid">
      <Card
        title="Plansza"
        subtitle="Game-service — silnik gotowy, REST API w przygotowaniu."
        className="span-2"
      >
        <Alert tone="warning" title="Integracja">
          Po dodaniu <code>GameController</code> podłączymy POST /api/games oraz ruchy UCI. WebSocket
          w pom.xml — sync w czasie rzeczywistym w kolejnej iteracji.
        </Alert>

        <div className="board-panel">
          <div className="board-coords files">
            {FILES.map((f) => (
              <span key={f}>{f}</span>
            ))}
          </div>
          <div className="board-row">
            <div className="board-coords ranks">
              {[8, 7, 6, 5, 4, 3, 2, 1].map((r) => (
                <span key={r}>{r}</span>
              ))}
            </div>
            <div className="chess-board" aria-label="Plansza szachowa — podgląd">
              {Array.from({ length: 64 }, (_, i) => {
                const row = Math.floor(i / 8);
                const col = i % 8;
                const light = (row + col) % 2 === 0;
                const rank = 8 - row;
                const file = FILES[col];
                return (
                  <div
                    key={i}
                    className={`square ${light ? 'light' : 'dark'}`}
                    data-square={`${file}${rank}`}
                  />
                );
              })}
            </div>
          </div>
        </div>

        <div className="play-sidebar-preview">
          <div className="clock-box">
            <span className="clock-label">Czarne</span>
            <span className="clock-time">—:—</span>
          </div>
          <div className="clock-box active">
            <span className="clock-label">Białe</span>
            <span className="clock-time">—:—</span>
          </div>
          <p className="muted small">Zegar podłączony do pól whiteTimeMs / blackTimeMs w game-service.</p>
        </div>
      </Card>
    </div>
  );
}
