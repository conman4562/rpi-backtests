import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: parseInt(process.env.VITE_APP_PORT || '5173'),
    proxy: {
      '/api': {
        target: process.env.API_PROXY_TARGET || 'http://backend:8080',
        changeOrigin: true,
      },
    },
  },
});
