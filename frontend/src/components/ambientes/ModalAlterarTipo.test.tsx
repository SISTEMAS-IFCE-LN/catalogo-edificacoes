import {fireEvent, render, screen, waitFor} from '@testing-library/react'
import {describe, it, expect, vi, beforeEach} from 'vitest'
import {ModalAlterarTipo} from './ModalAlterarTipo'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {Bloco, MaterialEsquadria, StatusAmbiente, TipoAmbiente, TipoEsquadria, TipoGeometria, Unidade} from '@/types/ambientes/enums'

vi.mock('sonner', () => ({
    toast: {success: vi.fn(), error: vi.fn()},
}))

const mockAlterarTipo = vi.hoisted(() => vi.fn())
vi.mock('@/lib/api/api-naopublicados', () => ({
    alterarTipo: (...args: unknown[]) => mockAlterarTipo(...args),
}))

const AMBIENTE: AmbienteDetalhe = {
    id: 7,
    nome: 'Sala 101',
    tipo: TipoAmbiente.SALA_AULA,
    localizacao: {
        id: 3,
        bloco: Bloco.BLOCO_1,
        unidade: Unidade.SEDE,
        andar: 2,
    },
    capacidade: 30,
    geometrias: [
        {id: 10, tipo: TipoGeometria.RETANGULAR, base: 4, altura: 3, repeticao: 2, area: 24},
    ],
    areaAmbiente: 24,
    pesDireitos: [3],
    esquadriasDetalhes: {
        esquadrias: [
            {
                id: 11,
                tipo: TipoEsquadria.PORTA,
                geometria: {id: 12, base: 0.9, altura: 2.1, repeticao: 1, area: 1.89},
                alturaPeitoril: 0,
                area: 1.89,
                material: MaterialEsquadria.ALUMINIO,
                informacaoAdicional: '',
            },
        ],
        esquadriasTipoMaterial: [],
    },
    informacaoAdicional: '',
    status: StatusAmbiente.NAO_PUBLICADO,
}

// Payload completo (AmbienteReq) esperado no POST /{id} — nomes técnicos. O
// tipo segue o valor pré-preenchido (SALA_AULA): a conversão rótulo→técnico é
// responsabilidade dos mappers (coberta em mappers.test.ts).
const PAYLOAD_ESPERADO = {
    nome: 'Sala 101',
    tipo: 'SALA_AULA',
    capacidade: 30,
    localizacao: {bloco: 'BLOCO_1', unidade: 'SEDE', andar: 2},
    geometrias: [{tipo: 'RETANGULAR', base: 4, altura: 3, repeticao: 2}],
    pesDireitos: [3],
    esquadrias: [
        {
            tipo: 'PORTA',
            material: 'ALUMINIO',
            geometria: {base: 0.9, altura: 2.1, repeticao: 1},
            alturaPeitoril: 0,
            informacaoAdicional: '',
        },
    ],
    informacaoAdicional: '',
}

type PropsDoComponente = Parameters<typeof ModalAlterarTipo>[0]

function renderModal(props: Partial<PropsDoComponente> = {}) {
    const onSalvou = vi.fn<(novoId: number) => void>()
    const onOpenChange = vi.fn()

    render(
        <ModalAlterarTipo
            open
            ambiente={AMBIENTE}
            onOpenChange={onOpenChange}
            onSalvou={onSalvou}
            {...props}
        />,
    )

    return {onSalvou, onOpenChange}
}

// O texto da etapa é dividido entre nós de texto e um <strong> (ver
// FormAmbiente.test.tsx) — compara o textContent do wrapper.
function buscarEtapa(n: number, nome: string) {
    const alvo = `Etapa ${n} de 5: ${nome}`
    return screen.queryByText((_, el) => el?.tagName === 'DIV' && el.textContent === alvo)
}

async function avancar(n: number, nome: string) {
    fireEvent.click(screen.getByRole('button', {name: 'Próximo'}))
    await waitFor(() => expect(buscarEtapa(n, nome)).not.toBeNull())
}

// Avança as 4 primeiras etapas (dados pré-preenchidos são válidos) e submete
async function submeterWizard() {
    await avancar(2, 'Geometrias')
    await avancar(3, 'Pés-direitos')
    await avancar(4, 'Esquadrias')
    await avancar(5, 'Informação Adicional')
    fireEvent.click(screen.getByRole('button', {name: 'Salvar'}))
}

describe('ModalAlterarTipo (UC16-FE)', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('alerta que a operação cria um novo ambiente e remove o antigo', () => {
        renderModal()
        expect(screen.getByText(/cria um novo ambiente e remove o antigo/)).toBeInTheDocument()
    })

    it('pré-preenche o wizard com os dados do ambiente convertidos para nomes técnicos', () => {
        renderModal()
        expect(screen.getByLabelText('Nome')).toHaveValue('Sala 101')
        expect(screen.getByLabelText('Capacidade')).toHaveValue(30)
        expect(screen.getByLabelText('Andar')).toHaveValue(2)
        // Selects mostram o valor técnico (sem items abertos)
        expect(screen.getByLabelText('Bloco')).toHaveTextContent('BLOCO_1')
        expect(screen.getByLabelText('Unidade')).toHaveTextContent('SEDE')
        expect(screen.getByLabelText('Tipo')).toHaveTextContent('SALA_AULA')
    })

    it('submete o AmbienteReq COMPLETO (não só o tipo), toasta e chama onSalvou com o novo id', async () => {
        mockAlterarTipo.mockResolvedValueOnce({...AMBIENTE, id: 8, tipo: TipoAmbiente.LABORATORIO})
        const {onSalvou, onOpenChange} = renderModal()

        await submeterWizard()

        await waitFor(() => {
            expect(mockAlterarTipo).toHaveBeenCalledWith(7, PAYLOAD_ESPERADO)
        })
        const {toast} = await import('sonner')
        expect(toast.success).toHaveBeenCalledWith('Tipo alterado.')
        expect(onSalvou).toHaveBeenCalledWith(8)
        expect(onOpenChange).toHaveBeenCalledWith(false)
    })

    it('erro do backend mantém o modal aberto (página não navega)', async () => {
        mockAlterarTipo.mockRejectedValueOnce(
            Object.assign(new Error('Request failed'), {
                isAxiosError: true,
                response: {status: 400, data: {mensagem: 'Tipo inválido.'}},
            }),
        )
        const {onSalvou, onOpenChange} = renderModal()

        await submeterWizard()

        const {toast} = await import('sonner')
        await waitFor(() => {
            expect(toast.error).toHaveBeenCalledWith('Tipo inválido.')
        })
        expect(onSalvou).not.toHaveBeenCalled()
        expect(onOpenChange).not.toHaveBeenCalled()
        expect(screen.getByText('Alterar Tipo')).toBeInTheDocument()
    })
})
