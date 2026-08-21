import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  fetchValidacao,
  fetchDetalheValidacao,
  fetchEsquadriasValidacao,
  publicarAmbiente,
  privarAmbiente,
} from './api-validacao'
import { api } from '@/lib/api/api'
import { TipoFiltro } from '@/types/ambientes/enums'

vi.mock('@/lib/api/api', () => ({
  api: { get: vi.fn(), patch: vi.fn() },
}))

const paginadoVazio = {
  ambientes: [],
  areaTotal: 0,
  dadosPaginacao: {
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    pageSize: 20,
    hasNext: false,
    hasPrevious: false,
  },
}

const esquadriasVazio = {
  ambientes: [],
  totalTipoMaterial: [],
  dadosPaginacao: {
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    pageSize: 100,
    hasNext: false,
    hasPrevious: false,
  },
}

describe('fetchValidacao', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api/ambientes/validacao (base)', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchValidacao({ page: 1, size: 20, tipoFiltro: TipoFiltro.NENHUM })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/validacao', expect.objectContaining({
      params: expect.objectContaining({ page: 1, size: 20 }),
    }))
  })

  it('chama /nome quando tipoFiltro é NOME', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchValidacao({ page: 0, size: 20, nome: 'sala', tipoFiltro: TipoFiltro.NOME })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/validacao/nome', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, nome: 'sala' }),
    }))
  })

  it('chama /tipo quando tipoFiltro é TIPO', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchValidacao({ page: 0, size: 20, tipo: 'SALA_AULA', tipoFiltro: TipoFiltro.TIPO })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/validacao/tipo', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, tipo: 'SALA_AULA' }),
    }))
  })

  it('chama /localizacao quando tipoFiltro é LOCALIZACAO', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchValidacao({ page: 0, size: 20, bloco: 'Bloco 1', unidade: 'Sede', andar: 2, tipoFiltro: TipoFiltro.LOCALIZACAO })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/validacao/localizacao', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, bloco: 'Bloco 1', unidade: 'Sede', andar: 2 }),
    }))
  })
})

describe('fetchDetalheValidacao', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api/ambientes/validacao/{id}', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        id: 1,
        nome: 'Sala 1',
        localizacao: { id: 1, bloco: 'Bloco 1', unidade: 'Sede', andar: 1 },
        tipo: 'Sala de Aula',
        capacidade: 30,
        geometrias: [],
        areaAmbiente: 50,
        pesDireitos: [],
        esquadriasDetalhes: { esquadrias: [], esquadriasTipoMaterial: [] },
        informacaoAdicional: '',
        status: 'AGUARDANDO_VALIDACAO',
      },
    })
    await fetchDetalheValidacao(1)
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/validacao/1', expect.any(Object))
  })

  it('valida resposta com Zod', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        id: 1,
        nome: 'Sala 1',
        localizacao: { id: 1, bloco: 'Bloco 1', unidade: 'Sede', andar: 1 },
        tipo: 'Sala de Aula',
        capacidade: 30,
        geometrias: [],
        areaAmbiente: 50,
        pesDireitos: [],
        esquadriasDetalhes: { esquadrias: [], esquadriasTipoMaterial: [] },
        informacaoAdicional: '',
        status: 'AGUARDANDO_VALIDACAO',
      },
    })
    const result = await fetchDetalheValidacao(1)
    expect(result.status).toBe('Aguardando Validação')
  })
})

describe('fetchEsquadriasValidacao', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api/ambientes/validacao/esquadrias com ids', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: esquadriasVazio })
    await fetchEsquadriasValidacao({ ids: [1, 2, 3], page: 0, size: 100 })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/validacao/esquadrias', expect.objectContaining({
      params: expect.objectContaining({ ids: '1,2,3', page: 0, size: 100 }),
    }))
  })
})

describe('publicarAmbiente / privarAmbiente', () => {
  beforeEach(() => {
    vi.mocked(api.patch).mockClear()
  })

  it('publicarAmbiente chama PATCH /publicar', async () => {
    vi.mocked(api.patch).mockResolvedValueOnce({ data: {} })
    await publicarAmbiente(1)
    expect(api.patch).toHaveBeenCalledWith('/api/ambientes/validacao/1/publicar')
  })

  it('privarAmbiente chama PATCH /privar', async () => {
    vi.mocked(api.patch).mockResolvedValueOnce({ data: {} })
    await privarAmbiente(1)
    expect(api.patch).toHaveBeenCalledWith('/api/ambientes/validacao/1/privar')
  })
})
