import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { fileURLToPath, URL } from 'node:url'

const resolve = (chemin: string) => fileURLToPath(new URL(chemin, import.meta.url))

// Configuration multi-pages : le build produit deux applications independantes
// (client.html et agence.html) que le backend Java sert telles quelles.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      // En developpement, les appels a /api/* sont relayes vers le backend Java (port 8081)
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    rollupOptions: {
      input: {
        client: resolve('./client.html'),
        agence: resolve('./agence.html'),
      },
    },
  },
})
