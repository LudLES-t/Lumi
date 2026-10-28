import { defineConfig } from 'vite';

const pagesBasePath = process.env.VITE_BASE_PATH || '/';

export default defineConfig({
  base: pagesBasePath.endsWith('/') ? pagesBasePath : `${pagesBasePath}/`,
  server: {
    host: '0.0.0.0',
    port: 5173,
    strictPort: true,
    proxy: { '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true } }
  }
});
