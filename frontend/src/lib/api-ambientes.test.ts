import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchPublicados } from './api-ambientes'
import { api } from '@/lib/api'

vi.mock('@/lib/api', () => ({
  api: { get: vi.fn() },
}))

describe('fetchPublicados', () => {
  beforeEach(() => {
    vi.mocked(api.get).mockClear()
  })

  it('chama /api/ambientes/publicados com params', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
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
      },
    })
    await fetchPublicados({ page: 1, size: 20, nome: 'sala' })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
      params: expect.objectContaining({ page: 1, size: 20, nome: 'sala' }),
    }))
  })

  it('omite params vazios', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
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
      },
    })
    await fetchPublicados({ page: 0, size: 20 })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20 }),
    }))
    const callParams = vi.mocked(api.get).mock.calls[0][1]?.params
    expect(callParams).not.toHaveProperty('nome')
    expect(callParams).not.toHaveProperty('bloco')
    expect(callParams).not.toHaveProperty('unidade')
    expect(callParams).not.toHaveProperty('andar')
    expect(callParams).not.toHaveProperty('tipo')
  })

  it('inclui andar quando fornecido', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
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
      },
    })
    await fetchPublicados({ page: 0, size: 20, andar: 2 })
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
      params: expect.objectContaining({ page: 0, size: 20, andar: 2 }),
    }))
  })

  it('repassa signal ao axios', async () => {
    const controller = new AbortController()
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
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
      },
    })
    await fetchPublicados({ page: 0, size: 20 }, controller.signal)
    expect(api.get).toHaveBeenCalledWith('/api/ambientes/publicados', expect.objectContaining({
      signal: controller.signal,
    }))
  })

  it('valida resposta com Zod', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
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
      },
    })
    const result = await fetchPublicados({ page: 0, size: 20 })
    expect(result).toHaveProperty('ambientes')
    expect(result).toHaveProperty('areaTotal')
    expect(result).toHaveProperty('dadosPaginacao')
  })

  it('lança erro Zod quando resposta é inválida', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({
      data: {
        ambientes: 'invalid', // deveria ser array
        areaTotal: 0,
        dadosPaginacao: {
          totalElements: 0,
          totalPages: 0,
          currentPage: 0,
          pageSize: 20,
          hasNext: false,
          hasPrevious: false,
        },
      },
    })
    await expect(fetchPublicados({ page: 0, size: 20 })).rejects.toThrow()
  })
})
