/// <reference types="vitest" />
import {defineConfig} from 'vitest/config'
import react from '@vitejs/plugin-react'
import {fileURLToPath} from "node:url"
import path from "node:path"

// Aponta para o diretório raiz do projeto
const __dirname = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig({
    plugins: [react()],
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
            '/login': 'http://localhost:8080',
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
