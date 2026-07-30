import {render, screen} from '@testing-library/react'
import {MemoryRouter, Routes, Route} from 'react-router'
import {describe, it, expect, vi} from 'vitest'
import {CallbackPage} from './page'

const {mockLogin, mockNavigate} = vi.hoisted(() => ({
    mockLogin: vi.fn(),
    mockNavigate: vi.fn(),
}))

vi.mock('@/hooks/useAuth', () => ({
    useAuth: () => ({login: mockLogin}),
}))

vi.mock('react-router', async () => {
    const actual = await vi.importActual('react-router')
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    }
})

describe('CallbackPage', () => {
    it('exibe mensagem de autenticando', () => {
        render(
            <MemoryRouter initialEntries={['/callback']}>
                <Routes>
                    <Route path="/callback" element={<CallbackPage/>}/>
                    <Route path="/login" element={<div>Login</div>}/>
                </Routes>
            </MemoryRouter>,
        )
        expect(screen.getByText(/autenticando/i)).toBeInTheDocument()
    })
})