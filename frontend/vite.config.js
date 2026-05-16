import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws/collab': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
      '/ws/chat': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
      '/ws/notifications': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },
});