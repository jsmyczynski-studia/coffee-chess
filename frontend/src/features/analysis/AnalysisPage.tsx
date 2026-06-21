import { useState, useEffect } from 'react';
import { Chessboard } from 'react-chessboard';
import { analysisApi } from '../../lib/api/analysisApi';
import { gameApi } from '../../lib/api/gameApi';
import { useAuth } from '../../lib/auth/AuthContext';
import type { GameDto, MoveDto, PositionAnalysisResponse } from '../../lib/api/types';
import { Card } from '../../components/ui/Card';
import { Alert } from '../../components/ui/Alert';
import { Button } from '../../components/ui/Button';

const START_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1';

export function AnalysisPage() {
  const { token, user } = useAuth();
  
  const [games, setGames] = useState<GameDto[]>([]);
  const [selectedGameId, setSelectedGameId] = useState<string>('');
  
  const [moves, setMoves] = useState<MoveDto[]>([]);
  const [currentPly, setCurrentPly] = useState<number>(-1); // -1 = start pos
  
  const [analysis, setAnalysis] = useState<PositionAnalysisResponse | null>(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch user's games on mount
  useEffect(() => {
    if (!token) return;
    gameApi.listGames(token)
      .then(data => {
        // Sort by most recently started
        setGames(data.sort((a, b) => new Date(b.startedAt || 0).getTime() - new Date(a.startedAt || 0).getTime()));
      })
      .catch(err => setError(err.message || 'Nie udało się pobrać listy gier'));
  }, [token]);

  // Fetch moves when a game is selected
  useEffect(() => {
    if (!selectedGameId || !token) {
      setMoves([]);
      setCurrentPly(-1);
      return;
    }
    
    gameApi.getMoves(selectedGameId, token)
      .then(data => {
        setMoves(data.sort((a, b) => a.moveNumber - b.moveNumber));
        setCurrentPly(-1); // start from beginning
      })
      .catch(err => setError(err.message || 'Nie udało się pobrać ruchów'));
  }, [selectedGameId, token]);

  // Current fen
  const currentFen = currentPly === -1 ? START_FEN : (moves[currentPly]?.fenAfter || START_FEN);

  // Fetch analysis when FEN changes
  useEffect(() => {
    if (!currentFen) return;
    setIsAnalyzing(true);
    
    // Simple debounce so we don't spam the server while stepping fast
    const timeoutId = setTimeout(() => {
      analysisApi.analyzePosition(currentFen, 5)
        .then(res => setAnalysis(res))
        .catch(err => console.error('Błąd analizy:', err))
        .finally(() => setIsAnalyzing(false));
    }, 300);

    return () => clearTimeout(timeoutId);
  }, [currentFen]);

  // Navigation handlers
  const handleFirst = () => setCurrentPly(-1);
  const handlePrev = () => setCurrentPly(p => Math.max(-1, p - 1));
  const handleNext = () => setCurrentPly(p => Math.min(moves.length - 1, p + 1));
  const handleLast = () => setCurrentPly(moves.length - 1);

  // Keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'ArrowLeft') handlePrev();
      if (e.key === 'ArrowRight') handleNext();
      if (e.key === 'ArrowUp') handleFirst();
      if (e.key === 'ArrowDown') handleLast();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [moves.length]);

  // Rendering evaluation bar
  const renderEvalBar = () => {
    if (!analysis || analysis.candidates.length === 0) return null;
    const top = analysis.candidates[0];
    
    let whiteAdvantage = 50; // 50% = equal
    let scoreText = '';
    
    if (top.mate !== null) {
      // Mate evaluation
      if (top.mate > 0) {
        whiteAdvantage = 100;
        scoreText = `M${top.mate}`;
      } else {
        whiteAdvantage = 0;
        scoreText = `M${Math.abs(top.mate)}`;
      }
    } else if (top.eval !== null) {
      // Normal evaluation
      // Convert eval (e.g. +1.5) to a percentage. 
      // Roughly mapping [-5, +5] to [5%, 95%] for visual representation
      const clampedEval = Math.max(-5, Math.min(5, top.eval));
      whiteAdvantage = 50 + (clampedEval * 10);
      scoreText = top.eval > 0 ? `+${top.eval.toFixed(2)}` : top.eval.toFixed(2);
    }

    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '1rem' }}>
        <div style={{ width: '45px', fontWeight: 'bold', fontSize: '1.2rem', textAlign: 'right', color: 'var(--text)' }}>
          {scoreText}
        </div>
        <div style={{ flex: 1, height: '1.5rem', background: '#333', borderRadius: '4px', overflow: 'hidden', position: 'relative' }}>
          <div style={{
            position: 'absolute',
            left: 0,
            top: 0,
            bottom: 0,
            width: `${whiteAdvantage}%`,
            background: '#e2e2e2',
            transition: 'width 0.3s ease-out'
          }} />
        </div>
      </div>
    );
  };

  return (
    <div className="page-grid">
      <Card title="Wybierz partię do analizy" subtitle="GET /api/games — game-service">
        {error && <div style={{ marginBottom: '1rem' }}><Alert tone="error">{error}</Alert></div>}
        <select 
          className="field-input" 
          value={selectedGameId} 
          onChange={(e) => setSelectedGameId(e.target.value)}
        >
          <option value="">-- Wybierz swoją grę --</option>
          {games.map(g => {
            const isWhite = g.whitePlayerId === user?.id;
            const vs = g.vsBot ? `Bot (${g.botDifficulty})` : (isWhite ? 'Czarnym graczem' : 'Białym graczem');
            return (
              <option key={g.id} value={g.id}>
                {new Date(g.startedAt || '').toLocaleString()} - {isWhite ? 'Białe' : 'Czarne'} vs {vs} ({g.status})
              </option>
            );
          })}
        </select>
      </Card>

      {selectedGameId && (
        <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap', marginTop: '1rem' }}>
          {/* Left column: Board & Eval */}
          <div style={{ flex: '1 1 400px', maxWidth: '500px' }}>
            <Card title="Analiza pozycji" subtitle="GET /api/analysis/position — analysis-service">
              <div style={{ width: '100%' }}>
                <Chessboard options={{ position: currentFen, animationDurationInMs: 150 }} />
              </div>
              
              {/* Controls */}
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem' }}>
                <Button variant="secondary" onClick={handleFirst}>|&lt;</Button>
                <Button variant="secondary" onClick={handlePrev}>&lt;</Button>
                <Button variant="secondary" onClick={handleNext}>&gt;</Button>
                <Button variant="secondary" onClick={handleLast}>&gt;|</Button>
              </div>
              
              {renderEvalBar()}
            </Card>
          </div>

          {/* Right column: Move list & Engine lines */}
          <div style={{ flex: '1 1 300px' }}>
            <Card title="Zapis partii" className="mb-4" subtitle="GET /api/games/{id}/moves">
              <div style={{ maxHeight: '200px', overflowY: 'auto', display: 'flex', flexWrap: 'wrap', gap: '0.4rem', lineHeight: '2' }}>
                {moves.length === 0 ? <p className="muted small">Brak ruchów (partia rozpoczęta lub zepsuta)</p> : null}
                {moves.map((m, idx) => {
                  const isWhite = m.color === 'WHITE';
                  return (
                    <span key={idx} style={{ display: 'inline-flex', gap: '0.2rem' }}>
                      {isWhite && <strong style={{ color: 'var(--text-muted)', marginLeft: '0.5rem' }}>{m.moveNumber}.</strong>}
                      <span 
                        onClick={() => setCurrentPly(idx)}
                        style={{
                          cursor: 'pointer',
                          padding: '0.1rem 0.4rem',
                          borderRadius: '4px',
                          background: currentPly === idx ? 'var(--accent)' : 'var(--bg-input)',
                          color: currentPly === idx ? '#fff' : 'inherit',
                        }}
                      >
                        {m.san}
                      </span>
                    </span>
                  );
                })}
              </div>
            </Card>

            <Card title="Linie silnika (Top 5)">
              {isAnalyzing && <p className="muted small">Analizowanie przez Stockfisha...</p>}
              {!analysis && !isAnalyzing && <p className="muted small">Oczekiwanie na silnik...</p>}
              {analysis && (
                <table style={{ width: '100%', textAlign: 'left', fontSize: '0.9rem', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-soft)' }}>
                      <th style={{ padding: '0.5rem 0' }}>Ruch</th>
                      <th>Ocena</th>
                      <th>Win %</th>
                    </tr>
                  </thead>
                  <tbody>
                    {analysis.candidates.map((c, i) => (
                      <tr key={i} style={{ borderBottom: '1px solid var(--border-soft)' }}>
                        <td style={{ padding: '0.5rem 0', fontFamily: 'monospace' }}><strong>{c.san}</strong></td>
                        <td style={{ color: c.mate !== null ? (c.mate > 0 ? '#4caf50' : '#f44336') : 'inherit' }}>
                          {c.mate !== null ? `M${c.mate}` : (c.eval !== null ? (c.eval > 0 ? `+${c.eval.toFixed(2)}` : c.eval.toFixed(2)) : '-')}
                        </td>
                        <td>{c.winChance !== null ? `${(c.winChance * 100).toFixed(1)}%` : '-'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
