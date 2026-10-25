import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Vite dev server on http://localhost:5173
// The API address comes from VITE_API_URL (see .env.example).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
});
