import {type ComponentProps, useState} from 'react'
import {render, screen} from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import {describe, it, expect, vi} from 'vitest'
import {CampoReferencia} from './CampoReferencia'
import {
    REFERENCIA_VAZIA,
    type ReferenciaInput,
    type ReferenciaRes,
} from '@/types/ambientes/referencia'

const OPCOES: ReferenciaRes[] = [
    {id: 1, nome: 'LIMOEIRO DO NORTE'},
    {id: 2, nome: 'CIDADE ALTA'},
]

type PropsCampo = Omit<ComponentProps<typeof CampoReferencia>, 'value' | 'onChange'>

// Harness controlado: o campo é apresentacional, então o teste mantém o
// estado de `value` como o formulário real faria.
function renderizarCampo(
    overrides: Partial<PropsCampo> = {},
    valorInicial: ReferenciaInput = REFERENCIA_VAZIA,
) {
    const onChange = vi.fn()

    function Harness() {
        const [valor, setValor] = useState<ReferenciaInput>(valorInicial)
        return (
            <CampoReferencia
                label="Campus"
                opcoes={OPCOES}
                {...overrides}
                value={valor}
                onChange={(novo) => {
                    onChange(novo)
                    setValor(novo)
                }}
            />
        )
    }

    render(<Harness/>)
    return {onChange}
}

describe('CampoReferencia', () => {
    it('renderiza as opções e mostra o nome da selecionada', async () => {
        const user = userEvent.setup()
        renderizarCampo({}, {id: 1, nome: null})

        expect(screen.getByText('LIMOEIRO DO NORTE')).toBeInTheDocument()

        await user.click(screen.getByLabelText('Campus'))

        expect(await screen.findByRole('option', {name: 'CIDADE ALTA'})).toBeInTheDocument()
    })

    it('selecionar item existente envia {id, nome: null}', async () => {
        const user = userEvent.setup()
        const {onChange} = renderizarCampo()

        await user.click(screen.getByLabelText('Campus'))
        await user.click(await screen.findByRole('option', {name: 'CIDADE ALTA'}))

        expect(onChange).toHaveBeenCalledWith({id: 2, nome: null})
    })

    it('"+ Cadastrar novo" troca para input e Voltar limpa', async () => {
        const user = userEvent.setup()
        const {onChange} = renderizarCampo()

        await user.click(screen.getByLabelText('Campus'))
        await user.click(await screen.findByRole('option', {name: '+ Cadastrar novo'}))

        expect(onChange).toHaveBeenCalledWith({id: null, nome: ''})

        const input = screen.getByLabelText('Campus (novo)')
        await user.type(input, 'Ginásio')

        expect(input).toHaveValue('Ginásio')
        expect(onChange).toHaveBeenLastCalledWith({id: null, nome: 'Ginásio'})

        await user.click(screen.getByRole('button', {name: 'Voltar'}))

        expect(onChange).toHaveBeenLastCalledWith(REFERENCIA_VAZIA)
        expect(screen.getByLabelText('Campus')).toBeInTheDocument()
    })

    it('não mostra "+ Cadastrar novo" quando permitirCadastro é false', async () => {
        const user = userEvent.setup()
        renderizarCampo({permitirCadastro: false})

        await user.click(screen.getByLabelText('Campus'))

        expect(await screen.findByRole('option', {name: 'LIMOEIRO DO NORTE'})).toBeInTheDocument()
        expect(screen.queryByRole('option', {name: '+ Cadastrar novo'})).not.toBeInTheDocument()
    })

    it('somenteCadastro já renderiza o input e esconde o Voltar', () => {
        renderizarCampo({somenteCadastro: true})

        expect(screen.getByLabelText('Campus (novo)')).toBeInTheDocument()
        expect(screen.queryByLabelText('Campus')).not.toBeInTheDocument()
        expect(screen.queryByRole('button', {name: 'Voltar'})).not.toBeInTheDocument()
    })

    it('desabilita o select durante o carregamento', () => {
        renderizarCampo({carregando: true})

        expect(screen.getByLabelText('Campus')).toBeDisabled()
    })

    it('exibe o erro via ErroCampo', () => {
        renderizarCampo({erro: 'Selecione um campus.'})

        expect(screen.getByRole('alert')).toHaveTextContent('Selecione um campus.')
    })

    it('mostra "+ Cadastrar novo" quando a lista está vazia', async () => {
        const user = userEvent.setup()
        renderizarCampo({opcoes: []})

        await user.click(screen.getByLabelText('Campus'))

        expect(await screen.findByRole('option', {name: '+ Cadastrar novo'})).toBeInTheDocument()
    })
})
