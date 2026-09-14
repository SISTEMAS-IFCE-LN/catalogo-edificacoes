import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, expect, vi } from 'vitest'
import { ErrorLista } from './ErrorLista'

describe('ErrorLista', () => {
    it('exibe o alerta com a mensagem de erro', () => {
        render(<ErrorLista onTentarNovamente={vi.fn()} />)
        expect(screen.getByRole('alert')).toBeInTheDocument()
        expect(screen.getByText('Erro ao carregar ambientes.')).toBeInTheDocument()
    })

    it('chama onTentarNovamente ao clicar no botão', async () => {
        const onTentarNovamente = vi.fn()
        render(<ErrorLista onTentarNovamente={onTentarNovamente} />)
        const user = userEvent.setup()
        await user.click(screen.getByRole('button', { name: 'Tentar novamente' }))
        expect(onTentarNovamente).toHaveBeenCalledTimes(1)
    })
})
