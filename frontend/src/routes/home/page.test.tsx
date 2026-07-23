import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { HomePage } from './page'

describe('HomePage', () => {
    it('renderiza o título', () => {
        render(<HomePage/>)
        expect(screen.getByText('Catálogo de Edificações')).toBeInTheDocument()
    })
})