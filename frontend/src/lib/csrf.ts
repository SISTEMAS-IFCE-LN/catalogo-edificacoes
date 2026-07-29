import axios from 'axios'

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL ?? ''

export function getCsrfToken(): string | null {
    const match = document.cookie
        .split('; ')
        .find((cookie) => cookie.startsWith('XSRF-TOKEN='))
    return match ? decodeURIComponent(match.split('=')[1]) : null
}

// Garante que o token cookie XSRF-TOKEN esteja presente, requisitando-o ao backend
export async function ensureCsrfToken(): Promise<void> {
    if (!getCsrfToken()) {
        await axios.get(`${BACKEND_URL}/auth/csrf-token`, { withCredentials: true })
    }
}