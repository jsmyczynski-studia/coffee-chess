import { apiRequest } from './http';
import type { AnalysisReport, PositionAnalysisResponse } from './types';

export const analysisApi = {
  report(gameId: string) {
    return apiRequest<AnalysisReport>('analysis', `/api/analysis/${gameId}`);
  },

  analyzePosition(fen: string, variants: number = 5) {
    return apiRequest<PositionAnalysisResponse>('analysis', `/api/analysis/position?fen=${encodeURIComponent(fen)}&variants=${variants}`);
  },
};
