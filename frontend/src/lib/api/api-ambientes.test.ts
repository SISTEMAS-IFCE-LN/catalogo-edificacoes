import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchAmbientes, fetchDetalheAmbiente, fetchEsquadriasAmbientes } from './api-ambientes'
import { api } from '@/lib/api/api'
import { TipoFiltro } from '@/types/ambientes/enums'

vi.mock('@/lib/api/api', () => ({
  api: { get: vi.fn() },
}))

const ROTA = '/ambientes/teste'

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

const detalheValido = {
  id: 1,
  nome: 'Sala 1',
  localizacao: { id: 1, bloco: 'Bloco 1', unidade: 'Sede', andar: 1 },
  tipo: 'Sala de Aula',
  capacidade: 30,
  geometrias: [],
  areaAmbiente: 50,
  pesDireitos: [],
  esquadriasDetalhes: {
    esquadrias: [],
    esquadriasTipoMaterial: [],
  },
  informacaoAdicional: '',
  status: 'PUBLICADO',
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

describe('fetchAmbientes', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama a rota base com page/size', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchAmbientes({ page: 1, size: 20, tipoFiltro: TipoFiltro.NENHUM }, ROTA)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}`, expect.objectContaining({
      params: expect.objectContaining({ page: 1, size: 20 }),
    }))
  })

  it('chama /nome quando tipoFiltro é NOME', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchAmbientes({ page: 0, size: 20, nome: 'sala', tipoFiltro: TipoFiltro.NOME }, ROTA)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}/nome`, expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, nome: 'sala' }),
    }))
  })

  it('chama /tipo quando tipoFiltro é TIPO', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchAmbientes({ page: 0, size: 20, tipo: 'SALA_AULA', tipoFiltro: TipoFiltro.TIPO }, ROTA)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}/tipo`, expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, tipo: 'SALA_AULA' }),
    }))
  })

  it('chama /localizacao quando tipoFiltro é LOCALIZACAO', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchAmbientes({ page: 0, size: 20, bloco: 'Bloco 1', unidade: 'Sede', andar: 2, tipoFiltro: TipoFiltro.LOCALIZACAO }, ROTA)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}/localizacao`, expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, bloco: 'Bloco 1', unidade: 'Sede', andar: 2 }),
    }))
  })

  it('repassa signal ao axios', async () => {
    const controller = new AbortController()
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    await fetchAmbientes({ page: 0, size: 20, tipoFiltro: TipoFiltro.NENHUM }, ROTA, controller.signal)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}`, expect.objectContaining({
      signal: controller.signal,
    }))
  })

  it('valida resposta com Zod', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: paginadoVazio })
    const result = await fetchAmbientes({ page: 0, size: 20, tipoFiltro: TipoFiltro.NENHUM }, ROTA)
    expect(result).toHaveProperty('ambientes')
    expect(result).toHaveProperty('areaTotal')
    expect(result).toHaveProperty('dadosPaginacao')
  })

  it('lança erro Zod quando resposta é inválida', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: { ...paginadoVazio, ambientes: 'invalid' } })
    await expect(fetchAmbientes({ page: 0, size: 20, tipoFiltro: TipoFiltro.NENHUM }, ROTA)).rejects.toThrow()
  })
})

describe('fetchDetalheAmbiente', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api{rota}/{id}', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: detalheValido })
    await fetchDetalheAmbiente(1, ROTA)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}/1`, expect.any(Object))
  })

  it('repassa signal ao axios', async () => {
    const controller = new AbortController()
    vi.mocked(api.get).mockResolvedValueOnce({ data: detalheValido })
    await fetchDetalheAmbiente(1, ROTA, controller.signal)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}/1`, expect.objectContaining({
      signal: controller.signal,
    }))
  })

  it('valida resposta com Zod e normaliza enums', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: detalheValido })
    const result = await fetchDetalheAmbiente(1, ROTA)
    expect(result.status).toBe('Publicado')
    expect(result).not.toHaveProperty('area')
    expect(result).toHaveProperty('areaAmbiente', 50)
  })

  it('lança erro Zod quando resposta é inválida', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: { ...detalheValido, id: 'invalid' } })
    await expect(fetchDetalheAmbiente(1, ROTA)).rejects.toThrow()
  })
})

describe('fetchEsquadriasAmbientes', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api{rota}/esquadrias com ids serializados', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: esquadriasVazio })
    await fetchEsquadriasAmbientes({ ids: [1, 2, 3], page: 0, size: 100 }, ROTA)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}/esquadrias`, expect.objectContaining({
      params: expect.objectContaining({ ids: '1,2,3', page: 0, size: 100 }),
    }))
  })

  it('repassa signal ao axios', async () => {
    const controller = new AbortController()
    vi.mocked(api.get).mockResolvedValueOnce({ data: esquadriasVazio })
    await fetchEsquadriasAmbientes({ ids: [1], page: 0, size: 100 }, ROTA, controller.signal)
    expect(api.get).toHaveBeenCalledWith(`/api${ROTA}/esquadrias`, expect.objectContaining({
      params: expect.objectContaining({ ids: '1', page: 0, size: 100 }),
      signal: controller.signal,
    }))
  })

  it('valida resposta com Zod', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: esquadriasVazio })
    const result = await fetchEsquadriasAmbientes({ ids: [1], page: 0, size: 100 }, ROTA)
    expect(result).toHaveProperty('ambientes')
    expect(result).toHaveProperty('totalTipoMaterial')
    expect(result).toHaveProperty('dadosPaginacao')
  })

  it('lança erro Zod quando resposta é inválida', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: { ...esquadriasVazio, ambientes: 'invalid' } })
    await expect(fetchEsquadriasAmbientes({ ids: [1], page: 0, size: 100 }, ROTA)).rejects.toThrow()
  })
})
