import {describe, it, expect} from 'vitest'
import {nomeTecnicoDeRotulo} from './request'
import {TipoAmbiente, TipoGeometria} from '@/types/ambientes/enums'

describe('nomeTecnicoDeRotulo', () => {
    it('devolve a chave técnica para um rótulo válido', () => {
        expect(nomeTecnicoDeRotulo(TipoAmbiente, 'Sala de Aula')).toBe('SALA_AULA')
        expect(nomeTecnicoDeRotulo(TipoGeometria, 'Retangular')).toBe('RETANGULAR')
        expect(nomeTecnicoDeRotulo(TipoGeometria, 'Triangular')).toBe('TRIANGULAR')
    })

    it('lança Error com o rótulo desconhecido na mensagem', () => {
        expect(() => nomeTecnicoDeRotulo(TipoAmbiente, 'Sala Inexistente')).toThrow(Error)
        expect(() => nomeTecnicoDeRotulo(TipoAmbiente, 'Sala Inexistente')).toThrow(
            'Rótulo desconhecido: Sala Inexistente',
        )
    })
})
