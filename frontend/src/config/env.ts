function required(name: string, value: string | undefined): string {
  if (!value?.trim()) {
    throw new Error(`Missing environment variable: ${name}`);
  }
  return value.trim();
}

export const env = {
  keycloak: {
    url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8080',
    realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'coffee-chess',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'coffee-chess-frontend',
  },
  api: {
    user: import.meta.env.VITE_API_USER_URL ?? '',
    game: import.meta.env.VITE_API_GAME_URL ?? '',
    analysis: import.meta.env.VITE_API_ANALYSIS_URL ?? '',
  },
  app: {
    name: 'Coffee Chess',
    isDev: import.meta.env.DEV,
  },
} as const;

export function validateEnv(): void {
  required('VITE_KEYCLOAK_REALM', env.keycloak.realm);
  required('VITE_KEYCLOAK_CLIENT_ID', env.keycloak.clientId);
}
