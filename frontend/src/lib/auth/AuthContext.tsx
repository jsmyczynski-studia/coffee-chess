import Keycloak from 'keycloak-js';
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { env } from '../../config/env';

export interface AuthUser {
  id: string;
  username: string;
  email?: string;
  name?: string;
}

interface AuthContextValue {
  ready: boolean;
  authenticated: boolean;
  user: AuthUser | null;
  token: string | undefined;
  login: () => Promise<void>;
  register: () => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<boolean>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

const keycloak = new Keycloak({
  url: env.keycloak.url,
  realm: env.keycloak.realm,
  clientId: env.keycloak.clientId,
});

function mapUser(): AuthUser | null {
  if (!keycloak.authenticated || !keycloak.tokenParsed) {
    return null;
  }

  const parsed = keycloak.tokenParsed as Record<string, unknown>;
  return {
    id: String(parsed.sub ?? ''),
    username: String(parsed.preferred_username ?? parsed.sub ?? ''),
    email: parsed.email ? String(parsed.email) : undefined,
    name: parsed.name ? String(parsed.name) : undefined,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [user, setUser] = useState<AuthUser | null>(null);

  useEffect(() => {
    let refreshTimer: ReturnType<typeof setInterval> | undefined;

    keycloak
      .init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        checkLoginIframe: false,
        silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
      })
      .then((auth) => {
        setAuthenticated(auth);
        setUser(mapUser());
        setReady(true);

        if (auth) {
          refreshTimer = setInterval(() => {
            keycloak.updateToken(60).catch(() => {
              void keycloak.logout({ redirectUri: window.location.origin });
            });
          }, 30_000);
        }
      })
      .catch(() => {
        setReady(true);
        setAuthenticated(false);
        setUser(null);
      });

    return () => {
      if (refreshTimer) clearInterval(refreshTimer);
    };
  }, []);

  const login = useCallback(async () => {
    await keycloak.login({ redirectUri: window.location.href });
  }, []);

  const register = useCallback(async () => {
    await keycloak.register({ redirectUri: window.location.href });
  }, []);

  const logout = useCallback(async () => {
    await keycloak.logout({ redirectUri: window.location.origin });
  }, []);

  const refreshToken = useCallback(async () => {
    try {
      const refreshed = await keycloak.updateToken(30);
      setAuthenticated(keycloak.authenticated ?? false);
      setUser(mapUser());
      return refreshed;
    } catch {
      return false;
    }
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      ready,
      authenticated,
      user,
      token: keycloak.token,
      login,
      register,
      logout,
      refreshToken,
    }),
    [ready, authenticated, user, login, register, logout, refreshToken],
  );

  if (!ready) {
    return (
      <div className="boot-screen">
        <div className="boot-card">
          <div className="spinner" aria-hidden />
          <p>Łączenie z Coffee Chess…</p>
        </div>
      </div>
    );
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
}
