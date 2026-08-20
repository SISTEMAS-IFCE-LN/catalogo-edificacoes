import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter } from 'react-router'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { UsuariosPage } from './page'
import type { UsuariosPaginados } from '@/lib/api/api-usuarios'
import { Role, type User } from '@/types/usuarios/user'

vi.mock('@/lib/api/api-usuarios', () => ({
    fetchUsuarios: vi.fn(),
    fetchUsuarioPorNome: vi.fn(),
    fetchUsuarioPorEmail: vi.fn(),
    atualizarPerfis: vi.fn(),
    desativarUsuario: vi.fn(),
    ativarUsuario: vi.fn(),
}))

vi.mock('sonner', () => ({
    toast: { error: vi.fn(), success: vi.fn() },
}))

import { fetchUsuarios, fetchUsuarioPorEmail } from '@/lib/api/api-usuarios'

const usuarioA: User = {
    id: 1,
    email: 'a@ifce.edu.br',
    nome: 'Usuário A',
    ativo: true,
    criadoEm: '2025-01-01T00:00:00.000Z',
    perfis: [Role.COLABORADOR],
}

const usuarioB: User = {
    id: 2,
    email: 'b@ifce.edu.br',
    nome: 'Usuário B',
    ativo: false,
    criadoEm: '2025-01-02T00:00:00.000Z',
    perfis: [Role.COLABORADOR, Role.ADMINISTRADOR],
}

const dadosPaginados: UsuariosPaginados = {
    usuarios: [usuarioA, usuarioB],
    dadosPaginacao: {
        totalElements: 2,
        totalPages: 1,
        currentPage: 0,
        pageSize: 20,
        hasNext: false,
        hasPrevious: false,
    },
}

function createQueryClient() {
    return new QueryClient({
        defaultOptions: {
            queries: {retry: false, gcTime: 0, refetchOnWindowFocus: false},
        },
    })
}

function renderPage(initialEntries: string[] = ['/usuarios']) {
    return render(
        <QueryClientProvider client={createQueryClient()}>
            <MemoryRouter initialEntries={initialEntries}>
                <UsuariosPage/>
            </MemoryRouter>
        </QueryClientProvider>,
    )
}

describe('UsuariosPage', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('lista usuários sem filtro e exibe paginação', async () => {
        vi.mocked(fetchUsuarios).mockResolvedValueOnce(dadosPaginados)
        renderPage()

        await waitFor(() => {
            expect(fetchUsuarios).toHaveBeenCalledWith(0, 20, expect.anything())
        })
        await waitFor(() => {
            expect(screen.getAllByText('Usuário A')).toHaveLength(2)
        })
        expect(screen.getByText(/Página 1 de 1/)).toBeInTheDocument()
    })

    it('busca por email e renderiza o usuário único sem chamar a listagem', async () => {
        vi.mocked(fetchUsuarioPorEmail).mockResolvedValueOnce(usuarioA)
        renderPage(['/usuarios?email=a%40ifce.edu.br'])

        await waitFor(() => {
            expect(fetchUsuarioPorEmail).toHaveBeenCalledWith('a@ifce.edu.br', expect.anything())
        })
        await waitFor(() => {
            expect(screen.getAllByText('Usuário A')).toHaveLength(2)
        })
        expect(fetchUsuarios).not.toHaveBeenCalled()
    })

    it('exibe mensagem quando não há usuários', async () => {
        vi.mocked(fetchUsuarios).mockResolvedValueOnce({...dadosPaginados, usuarios: []})
        renderPage()

        await waitFor(() => {
            expect(screen.getByText('Nenhum usuário encontrado.')).toBeInTheDocument()
        })
    })

    it('busca por email inexistente exibe "não encontrado" (404)', async () => {
        vi.mocked(fetchUsuarioPorEmail).mockRejectedValueOnce({
            isAxiosError: true,
            response: {status: 404, data: {mensagem: 'Usuário não encontrado'}},
        })
        renderPage(['/usuarios?email=nao%40existe.com'])

        await waitFor(() => {
            expect(screen.getByText('Nenhum usuário encontrado com este email.')).toBeInTheDocument()
        })
        // Não deve mascarar como erro genérico.
        expect(screen.queryByText('Erro ao buscar usuário.')).not.toBeInTheDocument()
    })

    it('busca por email com erro de servidor exibe mensagem de erro (não 404)', async () => {
        vi.mocked(fetchUsuarioPorEmail).mockRejectedValueOnce({
            isAxiosError: true,
            response: {status: 500, data: {mensagem: 'Erro interno'}},
        })
        renderPage(['/usuarios?email=erro%40ifce.edu.br'])

        await waitFor(() => {
            expect(screen.getByText('Erro ao buscar usuário.')).toBeInTheDocument()
        })
        expect(screen.queryByText('Nenhum usuário encontrado com este email.')).not.toBeInTheDocument()
    })
})
