import {render, screen} from '@testing-library/react'
import {MemoryRouter} from 'react-router'
import {describe, it, expect} from 'vitest'
import {LoginPage} from './page'

function renderLogin(entrada = '/login') {
    return render(
        <MemoryRouter initialEntries={[entrada]}>
            <LoginPage/>
        </MemoryRouter>,
    )
}

describe('LoginPage', () => {
    it('exibe botão Entrar com Google', () => {
        renderLogin()
        expect(
            screen.getByRole('button', {name: /entrar com google/i}),
        ).toBeInTheDocument()
    })

    it.each([
        ['inativo', 'Usuário inativo. Contate o administrador.'],
        ['dominio', 'Acesso negado. Domínio não autorizado.'],
        ['falha', 'Falha ao autenticar. Tente novamente.'],
        ['outro', 'Falha ao autenticar. Tente novamente.'],
    ])('exibe a mensagem quando erro=%s', (param, mensagem) => {
        renderLogin(`/login?erro=${param}`)
        expect(screen.getByRole('alert')).toHaveTextContent(mensagem)
    })

    it('não exibe mensagem sem o parâmetro erro', () => {
        renderLogin()
        expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    })
})
