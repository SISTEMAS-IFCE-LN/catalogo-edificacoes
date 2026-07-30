/// <reference types="vitest" />
import {defineConfig, type Plugin} from 'vitest/config'
import react from '@vitejs/plugin-react'
import {fileURLToPath} from "node:url"
import path from "node:path"
import tailwindcss from "@tailwindcss/vite"

// Aponta para o diretório raiz do projeto
const __dirname = path.dirname(fileURLToPath(import.meta.url))

// Plugin que injeta a CSP correta conforme o ambiente.
// Em dev, permite connect-src para o backend local (localhost:8080).
// Em produção, restringe a 'self' (o Nginx sobrescreve via header).
function cspPlugin(): Plugin {
    return {
        name: 'csp-inject',
        transformIndexHtml(html) {
            const isDev = process.env.NODE_ENV === 'development'
            const connectSrc = isDev
                ? "'self' http://localhost:8080"
                : "'self'"
            return html.replace(
                /connect-src __CSP_CONNECT_SRC__/,
                `connect-src ${connectSrc}`,
            )
        },
    }
}

export default defineConfig({
    plugins: [react(), tailwindcss(), cspPlugin()],
    resolve: {
        alias: {
            // atribui ao diretório 'src' o alias '@', permitindo importar arquivos de forma mais simples
            '@': path.resolve(__dirname, 'src'),
        },
    },
    server: {
        proxy: {
            '/api': 'http://localhost:8080',
            '/auth': 'http://localhost:8080',
            '/oauth2': 'http://localhost:8080',
            '/login/oauth2': 'http://localhost:8080',
        },
    },
    test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: './src/test/setup.ts',
        coverage: {
            reporter: ['text', 'html'],
        },
    },
})
