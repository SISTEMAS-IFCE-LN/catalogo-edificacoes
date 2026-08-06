import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { DetalheAmbiente } from './DetalheAmbiente'
import type { AmbienteDetalhe } from '@/types/ambientes/ambiente'
import {
  TipoAmbiente,
  Bloco,
  Unidade,
  TipoGeometria,
  TipoEsquadria,
  MaterialEsquadria,
  StatusAmbiente,
} from '@/types/ambientes/enums'

const mockAmbiente: AmbienteDetalhe = {
  id: 1,
  nome: 'Sala 101',
  tipo: TipoAmbiente.SALA_AULA,
  localizacao: {
    id: 1,
    bloco: Bloco.BLOCO_1,
    unidade: Unidade.SEDE,
    andar: 1,
  },
  capacidade: 30,
  area: 50,
  geometrias: [
    {
      id: 1,
      tipo: TipoGeometria.RETANGULAR,
      base: 5,
      altura: 10,
      repeticao: 1,
      area: 50,
    },
  ],
  areaAmbiente: 50,
  pesDireitos: [3.5, 2.8],
  esquadriasDetalhes: {
    esquadrias: [
      {
        id: 1,
        tipo: TipoEsquadria.JANELA,
        geometria: {
          id: 1,
          base: 1.5,
          altura: 1.2,
          repeticao: 2,
          area: 3.6,
        },
        alturaPeitoril: 0.9,
        area: 3.6,
        material: MaterialEsquadria.ALUMINIO,
        informacaoAdicional: 'Com veneziana',
      },
    ],
    esquadriasTipoMaterial: [
      {
        tipo: TipoEsquadria.JANELA,
        material: MaterialEsquadria.ALUMINIO,
        area: 3.6,
      },
    ],
  },
  informacaoAdicional: 'Sala com ar-condicionado',
  status: StatusAmbiente.PUBLICADO,
}

describe('DetalheAmbiente', () => {
  it('renderiza nome do ambiente', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText('Sala 101')).toBeInTheDocument()
  })

  it('renderiza localização', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText(/Bloco 1/)).toBeInTheDocument()
    expect(screen.getByText(/Sede/)).toBeInTheDocument()
    expect(screen.getByText(/Andar 1/)).toBeInTheDocument()
  })

  it('renderiza tipo, capacidade e área', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText(/Tipo: Sala de Aula/)).toBeInTheDocument()
    expect(screen.getByText(/Capacidade: 30/)).toBeInTheDocument()
    expect(screen.getByText(/Área: 50.00 m²/)).toBeInTheDocument()
  })

  it('renderiza informação adicional quando presente', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText('Sala com ar-condicionado')).toBeInTheDocument()
  })

  it('não renderiza informação adicional quando vazia', () => {
    const ambienteSemInfo = { ...mockAmbiente, informacaoAdicional: '' }
    render(<DetalheAmbiente ambiente={ambienteSemInfo} />)
    expect(screen.queryByText('Sala com ar-condicionado')).not.toBeInTheDocument()
  })

  it('renderiza geometrias', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText(/Retangular/)).toBeInTheDocument()
    expect(screen.getByText(/5x10m/)).toBeInTheDocument()
    expect(screen.getByText(/área 50.00 m²/)).toBeInTheDocument()
  })

  it('renderiza pés-direitos', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText('3.5m, 2.8m')).toBeInTheDocument()
  })

  it('renderiza esquadrias', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText(/Janela 1.5x1.2m/)).toBeInTheDocument()
    expect(screen.getByText(/Alumínio/)).toBeInTheDocument()
    expect(screen.getByText(/peitoril: 0.9m/)).toBeInTheDocument()
    expect(screen.getByText(/Com veneziana/)).toBeInTheDocument()
  })

  it('não renderiza peitoril quando altura é 0', () => {
    const ambienteSemPeitoril: AmbienteDetalhe = {
      ...mockAmbiente,
      esquadriasDetalhes: {
        ...mockAmbiente.esquadriasDetalhes,
        esquadrias: [
          {
            ...mockAmbiente.esquadriasDetalhes.esquadrias[0],
            alturaPeitoril: 0,
          },
        ],
      },
    }
    render(<DetalheAmbiente ambiente={ambienteSemPeitoril} />)
    expect(screen.queryByText(/peitoril/)).not.toBeInTheDocument()
  })
})
