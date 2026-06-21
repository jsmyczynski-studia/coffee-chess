import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [react()],
    server: {
      port: 5173,
      strictPort: true,
      proxy: {
        '/api/users': { target: env.VITE_DEV_USER_URL ?? 'http://localhost:8081', changeOrigin: true },
        '/api/ranking': { target: env.VITE_DEV_USER_URL ?? 'http://localhost:8081', changeOrigin: true },
        '/api/friends': { target: env.VITE_DEV_USER_URL ?? 'http://localhost:8081', changeOrigin: true },
        '/api/analysis': {
          target: env.VITE_DEV_ANALYSIS_URL ?? 'http://localhost:8083',
          changeOrigin: true,
        },
        '/api/games': { target: env.VITE_DEV_GAME_URL ?? 'http://localhost:8082', changeOrigin: true },
        '/ws': {
          target: env.VITE_DEV_GAME_URL ?? 'http://localhost:8082',
          ws: true,
          changeOrigin: true
        },
      },
    },
    preview: {
      port: 4173,
      strictPort: true,
    },
    build: {
      sourcemap: mode !== 'production',
      target: 'es2022',
      chunkSizeWarningLimit: 600,
    },
  };
});
