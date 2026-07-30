import {render, screen} from '@testing-library/react'
import {MemoryRouter} from 'react-router'
import {describe, it, expect} from 'vitest'
import {LoginPage} from './page'

describe('LoginPage', () => {
    it('exibe botão Entrar com Google', () => {
        render(
            <MemoryRouter>
                <LoginPage/>
            </MemoryRouter>,
        )
        expect(
            screen.getByRole('button', {name: /entrar com google/i}),
        ).toBeInTheDocument()
    })
})