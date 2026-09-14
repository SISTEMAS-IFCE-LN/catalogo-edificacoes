import axios from 'axios'
import {API_ROUTES, BACKEND_URL} from "@/constants/routes";

// Ver docs/seguranca.md §4.2 e docs/arquitetura-frontend.md §3.5.
let maskedCsrfToken: string | null = null

export function getCsrfToken(): string | null {
    return maskedCsrfToken
}

// Garante que haja um token mascarado em memória. O raw no cookie persiste
// entre F5 (cookie de sessão), mas o mascarado em memória se perde no reload
// — por isso ensureCsrfToken() é chamado no boot do AuthProvider e antes de
// cada POST /auth/*.
export async function ensureCsrfToken(): Promise<void> {
    if (maskedCsrfToken) return
    const { data } = await axios.get(`${BACKEND_URL}${API_ROUTES.AUTH}/csrf-token`, { withCredentials: true })
    maskedCsrfToken = data.token
}

// Limpa o token em memória. Usar após logout para forçar nova aquisição.
export function clearCsrfToken(): void {
    maskedCsrfToken = null
}