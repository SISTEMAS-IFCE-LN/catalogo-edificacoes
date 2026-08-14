import {renderHook, act} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {toast} from 'sonner'
import {useAsyncAction} from './useAsyncAction'

vi.mock('sonner', () => ({
    toast: {error: vi.fn()},
}))

describe('useAsyncAction', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('fecha no sucesso e não exibe erro', async () => {
        const onClose = vi.fn()
        const {result} = renderHook(() => useAsyncAction({onClose}))

        await act(async () => {
            await result.current.executar(async () => {})
        })

        expect(onClose).toHaveBeenCalledTimes(1)
        expect(toast.error).not.toHaveBeenCalled()
    })

    it('marca executando durante a ação e limpa ao final', async () => {
        let resolver!: () => void
        const {result} = renderHook(() => useAsyncAction({onClose: vi.fn()}))

        let promise!: Promise<void>
        act(() => {
            promise = result.current.executar(
                () => new Promise<void>((resolve) => {
                    resolver = resolve
                }),
            )
        })
        expect(result.current.executando).toBe(true)

        await act(async () => {
            resolver()
            await promise
        })
        expect(result.current.executando).toBe(false)
    })

    it('usa mensagem do backend (ErroRes.mensagem) quando disponível', async () => {
        const {result} = renderHook(() => useAsyncAction({onClose: vi.fn()}))

        await act(async () => {
            await result.current.executar(async () => {
                throw {
                    isAxiosError: true,
                    response: {
                        status: 409,
                        data: {
                            dataHora: '2026-08-14 10:00:00',
                            status: 409,
                            mensagem: 'Ação negada: Não é possível remover ou desativar o último Administrador do sistema.',
                        },
                    },
                }
            })
        })

        expect(toast.error).toHaveBeenCalledWith(
            'Ação negada: Não é possível remover ou desativar o último Administrador do sistema.',
        )
    })

    it('não fecha quando a ação falha', async () => {
        const onClose = vi.fn()
        const {result} = renderHook(() => useAsyncAction({onClose}))

        await act(async () => {
            await result.current.executar(async () => {
                throw {isAxiosError: true, response: {status: 409, data: {mensagem: 'erro'}}}
            })
        })

        expect(onClose).not.toHaveBeenCalled()
    })

    it('usa mensagemPadrao quando o erro não tem corpo (network)', async () => {
        const {result} = renderHook(() => useAsyncAction({
            onClose: vi.fn(),
            mensagemPadrao: 'Erro ao atualizar perfis.',
        }))

        await act(async () => {
            await result.current.executar(async () => {
                throw {isAxiosError: true}
            })
        })

        expect(toast.error).toHaveBeenCalledWith('Erro ao atualizar perfis.')
    })

    it('usa mensagemPadrao default quando não informada', async () => {
        const {result} = renderHook(() => useAsyncAction({onClose: vi.fn()}))

        await act(async () => {
            await result.current.executar(async () => {
                throw new Error('boom')
            })
        })

        expect(toast.error).toHaveBeenCalledWith('Erro ao executar ação. Tente novamente.')
    })
})
