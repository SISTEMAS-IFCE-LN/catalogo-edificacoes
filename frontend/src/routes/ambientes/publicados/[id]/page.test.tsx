import { render, screen, waitFor, fireEvent, within } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Routes, Route } from 'react-router'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PublicadoDetalhePage } from './page'
import type { AmbienteDetalhe } from '@/types/ambientes/ambiente'
import type { User } from '@/types/usuarios/user'
import { Role } from '@/types/usuarios/user'
import {
  TipoAmbiente,
  Bloco,
  Unidade,
  TipoGeometria,
  StatusAmbiente,
} from '@/types/ambientes/enums'

vi.mock('@/lib/api/api-publicados', () => ({
  fetchDetalhePublicados: vi.fn(),
}))

vi.mock('@/lib/api/api-validacao', () => ({
  privarAmbiente: vi.fn(),
}))

vi.mock('sonner', () => ({
  toast: { success: vi.fn(), error: vi.fn() },
}))

vi.mock('@/hooks/useAuth', () => ({
  useAuth: vi.fn(),
}))

import { fetchDetalhePublicados } from '@/lib/api/api-publicados'
import { privarAmbiente } from '@/lib/api/api-validacao'
import { useAuth } from '@/hooks/useAuth'

const userValidador: User = {
  id: 1,
  email: 'validador@ifce.edu.br',
  nome: 'Validador Teste',
  ativo: true,
  criadoEm: '2025-01-01T00:00:00.000Z',
  perfis: [Role.VALIDADOR],
}

const userColaborador: User = {
  id: 2,
  email: 'colaborador@ifce.edu.br',
  nome: 'Colaborador Teste',
  ativo: true,
  criadoEm: '2025-01-01T00:00:00.000Z',
  perfis: [Role.COLABORADOR],
}

function makeAmbiente(status: StatusAmbiente): AmbienteDetalhe {
  return {
    id: 1,
    nome: 'Sala 101',
    tipo: TipoAmbiente.SALA_AULA,
    localizacao: {
      id: 1,
      bloco: Bloco.BLOCO_1,
      unidade: Unidade.SEDE,
      andar: 1,
    },
    capacidade: 30,
    geometrias: [
      {
        id: 1,
        tipo: TipoGeometria.RETANGULAR,
        base: 5,
        altura: 10,
        repeticao: 1,
        area: 50,
      },
    ],
    areaAmbiente: 50,
    pesDireitos: [3.5, 2.8],
    esquadriasDetalhes: {
      esquadrias: [],
      esquadriasTipoMaterial: [],
    },
    informacaoAdicional: 'Sala com ar-condicionado',
    status,
  }
}

function createQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0, staleTime: 0, refetchOnWindowFocus: false },
    },
  })
}

function renderPage(initialEntries: string[] = ['/ambientes/publicados/1']) {
  return render(
    <QueryClientProvider client={createQueryClient()}>
      <MemoryRouter initialEntries={initialEntries}>
        <Routes>
          <Route path="/ambientes/publicados/:id" element={<PublicadoDetalhePage />} />
          <Route path="/ambientes/publicados" element={<div>lista</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

function mockAuth(user: User) {
  vi.mocked(useAuth).mockReturnValue({
    user,
    isAuthenticated: true,
    isLoading: false,
    login: vi.fn(),
    logout: vi.fn(),
    refreshUser: vi.fn(),
  })
}

describe('PublicadoDetalhePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockAuth(userValidador)
  })

  it('exibe loading enquanto carrega', async () => {
    vi.mocked(fetchDetalhePublicados).mockReturnValueOnce(new Promise(() => {}))
    renderPage()
    expect(await screen.findByText('Carregando…')).toBeInTheDocument()
  })

  it('busca o ambiente com o id da URL', async () => {
    vi.mocked(fetchDetalhePublicados).mockResolvedValueOnce(makeAmbiente(StatusAmbiente.PUBLICADO))
    renderPage(['/ambientes/publicados/42'])
    await waitFor(() => {
      expect(fetchDetalhePublicados).toHaveBeenCalledWith(42, expect.anything())
    })
  })

  it('renderiza o detalhe do ambiente após carregar', async () => {
    vi.mocked(fetchDetalhePublicados).mockResolvedValueOnce(makeAmbiente(StatusAmbiente.PUBLICADO))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Sala 101')).toBeInTheDocument()
    })
    expect(screen.getByText('Voltar')).toBeInTheDocument()
  })

  it('exibe "Ambiente não encontrado" quando a busca falha', async () => {
    vi.mocked(fetchDetalhePublicados).mockRejectedValueOnce(new Error('rede'))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Ambiente não encontrado.')).toBeInTheDocument()
    })
    expect(screen.getByText('Voltar à lista')).toBeInTheDocument()
  })

  it('não chama a API e exibe "Ambiente não encontrado" quando o id da URL é inválido', async () => {
    renderPage(['/ambientes/publicados/abc'])
    await waitFor(() => {
      expect(screen.getByText('Ambiente não encontrado.')).toBeInTheDocument()
    })
    expect(screen.getByText('Voltar à lista')).toBeInTheDocument()
    expect(fetchDetalhePublicados).not.toHaveBeenCalled()
  })

  it('navega de volta para a lista ao clicar em Voltar', async () => {
    vi.mocked(fetchDetalhePublicados).mockResolvedValueOnce(makeAmbiente(StatusAmbiente.PUBLICADO))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Voltar')).toBeInTheDocument()
    })
    fireEvent.click(screen.getByText('Voltar'))
    expect(await screen.findByText('lista')).toBeInTheDocument()
  })

  describe('ação Privar (UC03-FE)', () => {
    it('não renderiza o botão Privar para usuário sem perfil VALIDADOR', async () => {
      mockAuth(userColaborador)
      vi.mocked(fetchDetalhePublicados).mockResolvedValueOnce(makeAmbiente(StatusAmbiente.PUBLICADO))
      renderPage()
      await waitFor(() => {
        expect(screen.getByText('Sala 101')).toBeInTheDocument()
      })
      expect(screen.queryByRole('button', { name: 'Privar' })).not.toBeInTheDocument()
    })

    it('renderiza Privar habilitado para validador quando PUBLICADO', async () => {
      vi.mocked(fetchDetalhePublicados).mockResolvedValueOnce(makeAmbiente(StatusAmbiente.PUBLICADO))
      renderPage()
      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Privar' })).toBeEnabled()
      })
    })

    it('desabilita Privar quando o status não é PUBLICADO', async () => {
      vi.mocked(fetchDetalhePublicados).mockResolvedValueOnce(makeAmbiente(StatusAmbiente.AGUARDANDO_VALIDACAO))
      renderPage()
      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Privar' })).toBeDisabled()
      })
    })

    it('priva o ambiente ao confirmar o modal', async () => {
      vi.mocked(fetchDetalhePublicados).mockResolvedValue(makeAmbiente(StatusAmbiente.PUBLICADO))
      vi.mocked(privarAmbiente).mockResolvedValue(undefined)
      renderPage()
      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Privar' })).toBeEnabled()
      })

      fireEvent.click(screen.getByRole('button', { name: 'Privar' }))
      expect(screen.getByText('Privar ambiente?')).toBeInTheDocument()
      fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Privar' }))

      await waitFor(() => {
        expect(privarAmbiente).toHaveBeenCalledWith(1)
      })
      const { toast } = await import('sonner')
      expect(toast.success).toHaveBeenCalledWith('Ambiente privado.')
      expect(await screen.findByText('lista')).toBeInTheDocument()
    })

    it('não chama a API ao cancelar o modal', async () => {
      vi.mocked(fetchDetalhePublicados).mockResolvedValueOnce(makeAmbiente(StatusAmbiente.PUBLICADO))
      renderPage()
      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Privar' })).toBeEnabled()
      })

      fireEvent.click(screen.getByRole('button', { name: 'Privar' }))
      expect(screen.getByText('Privar ambiente?')).toBeInTheDocument()
      fireEvent.click(screen.getByText('Cancelar'))

      await waitFor(() => {
        expect(screen.queryByText('Privar ambiente?')).not.toBeInTheDocument()
      })
      expect(privarAmbiente).not.toHaveBeenCalled()
    })

    it('mantém a página e exibe toast de erro quando a ação falha', async () => {
      vi.mocked(fetchDetalhePublicados).mockResolvedValue(makeAmbiente(StatusAmbiente.PUBLICADO))
      vi.mocked(privarAmbiente).mockRejectedValueOnce(new Error('rede'))
      renderPage()
      await waitFor(() => {
        expect(screen.getByRole('button', { name: 'Privar' })).toBeEnabled()
      })

      fireEvent.click(screen.getByRole('button', { name: 'Privar' }))
      fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Privar' }))

      await waitFor(() => {
        expect(privarAmbiente).toHaveBeenCalledWith(1)
      })
      const { toast } = await import('sonner')
      expect(toast.error).toHaveBeenCalledWith('Erro ao executar ação. Tente novamente.')
      expect(screen.queryByText('lista')).not.toBeInTheDocument()
      expect(screen.getByText('Privar ambiente?')).toBeInTheDocument()
    })
  })
})
