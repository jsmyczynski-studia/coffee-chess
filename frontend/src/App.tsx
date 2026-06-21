import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/layout/AppShell';
import { AnalysisPage } from './features/analysis/AnalysisPage';
import { FriendsPage } from './features/friends/FriendsPage';
import { HomePage } from './features/home/HomePage';
import { GameLobbyPage } from './features/play/GameLobbyPage';
import { PlayPage } from './features/play/PlayPage';
import { ProfilePage } from './features/profile/ProfilePage';
import { RankingPage } from './features/ranking/RankingPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppShell />}>
          <Route index element={<HomePage />} />
          <Route path="ranking" element={<RankingPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route path="friends" element={<FriendsPage />} />
          <Route path="analysis" element={<AnalysisPage />} />
          <Route path="play" element={<GameLobbyPage />} />
          <Route path="play/:id" element={<PlayPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
