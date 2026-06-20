import { apiRequest } from './http';
import type { AnalysisReport } from './types';

export const analysisApi = {
  report(gameId: string) {
    return apiRequest<AnalysisReport>('analysis', `/api/analysis/${gameId}`);
  },
};
