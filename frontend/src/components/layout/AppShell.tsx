import { NavLink, Outlet } from 'react-router-dom';
import { env } from '../../config/env';
import { useAuth } from '../../lib/auth/AuthContext';
import { Button } from '../ui/Button';

type NavItem = {
  to: string;
  label: string;
  end?: boolean;
  protected?: boolean;
};

const navItems: NavItem[] = [
  { to: '/', label: 'Start', end: true },
  { to: '/ranking', label: 'Ranking' },
  { to: '/profile', label: 'Profil' },
  { to: '/friends', label: 'Znajomi', protected: true },
  { to: '/analysis', label: 'Analiza' },
  { to: '/play', label: 'Gra' },
];

export function AppShell() {
  const { authenticated, user, login, logout } = useAuth();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon" aria-hidden>
            ♞
          </span>
          <div>
            <strong>{env.app.name}</strong>
            <span className="brand-tag">Platforma szachowa</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end ?? false}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              {item.label}
              {item.protected && <span className="nav-badge">JWT</span>}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          {authenticated && user ? (
            <>
              <div className="user-chip">
                <span className="user-avatar">{user.username.charAt(0).toUpperCase()}</span>
                <div>
                  <span className="user-name">{user.username}</span>
                  <span className="user-meta">Zalogowany</span>
                </div>
              </div>
              <Button variant="ghost" onClick={() => logout()}>
                Wyloguj
              </Button>
            </>
          ) : (
            <Button variant="primary" onClick={() => login()}>
              Zaloguj przez Keycloak
            </Button>
          )}
        </div>
      </aside>

      <div className="content-area">
        <header className="topbar">
          <div>
            <h2 className="topbar-title">Panel gracza</h2>
            <p className="topbar-subtitle">User · Game · Analysis services</p>
          </div>
          <div className="service-pills">
            <span className="pill">:8081 user</span>
            <span className="pill">:8082 game</span>
            <span className="pill">:8083 analysis</span>
            <span className="pill">:8761 eureka</span>
          </div>
        </header>
        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
