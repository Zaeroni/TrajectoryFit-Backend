import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Proxy /api to the Spring Boot backend so the browser talks to one origin (no CORS).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.API_TARGET ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
