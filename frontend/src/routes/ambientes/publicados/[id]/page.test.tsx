import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { MemoryRouter, Routes, Route } from 'react-router'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { PublicadoDetalhePage } from './page'
import type { AmbienteDetalhe } from '@/types/ambientes/ambiente'
import {
  TipoAmbiente,
  Bloco,
  Unidade,
  TipoGeometria,
  StatusAmbiente,
} from '@/types/ambientes/enums'

vi.mock('@/lib/api/api-ambientes', () => ({
  fetchDetalheAmbiente: vi.fn(),
}))

import { fetchDetalheAmbiente } from '@/lib/api/api-ambientes'

const mockAmbiente: AmbienteDetalhe = {
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
  status: StatusAmbiente.PUBLICADO,
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

describe('PublicadoDetalhePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('exibe loading enquanto carrega', async () => {
    vi.mocked(fetchDetalheAmbiente).mockReturnValueOnce(new Promise(() => {}))
    renderPage()
    expect(await screen.findByText('Carregando…')).toBeInTheDocument()
  })

  it('busca o ambiente com o id da URL', async () => {
    vi.mocked(fetchDetalheAmbiente).mockResolvedValueOnce(mockAmbiente)
    renderPage(['/ambientes/publicados/42'])
    await waitFor(() => {
      expect(fetchDetalheAmbiente).toHaveBeenCalledWith(42, expect.anything())
    })
  })

  it('renderiza o detalhe do ambiente após carregar', async () => {
    vi.mocked(fetchDetalheAmbiente).mockResolvedValueOnce(mockAmbiente)
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Sala 101')).toBeInTheDocument()
    })
    expect(screen.getByText('Voltar')).toBeInTheDocument()
  })

  it('exibe "Ambiente não encontrado" quando a busca falha', async () => {
    vi.mocked(fetchDetalheAmbiente).mockRejectedValueOnce(new Error('rede'))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Ambiente não encontrado.')).toBeInTheDocument()
    })
    expect(screen.getByText('Voltar à lista')).toBeInTheDocument()
  })

  it('navega de volta para a lista ao clicar em Voltar', async () => {
    vi.mocked(fetchDetalheAmbiente).mockResolvedValueOnce(mockAmbiente)
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Voltar')).toBeInTheDocument()
    })
    fireEvent.click(screen.getByText('Voltar'))
    expect(await screen.findByText('lista')).toBeInTheDocument()
  })
})
