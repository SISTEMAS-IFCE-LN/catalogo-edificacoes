import { render, screen, within } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { DetalheAmbiente } from './DetalheAmbiente'
import type { AmbienteDetalhe } from '@/types/ambientes/response'
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
    expect(screen.getByText(/1º Andar/)).toBeInTheDocument()
  })

  it('renderiza tipo, capacidade e área', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText(/Tipo: Sala de Aula/)).toBeInTheDocument()
    expect(screen.getByText(/Capacidade: 30/)).toBeInTheDocument()
    // A área do ambiente é exibida na tabela de Dimensões (linha "Área Total do Ambiente")
    expect(screen.getAllByText('50.00')).toHaveLength(2)
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

  it('renderiza o andar térreo', () => {
    const ambienteTerreo = {
      ...mockAmbiente,
      localizacao: { ...mockAmbiente.localizacao, andar: 0 },
    }
    render(<DetalheAmbiente ambiente={ambienteTerreo} />)
    expect(screen.getByText(/Térreo/)).toBeInTheDocument()
  })

  it('renderiza geometrias em tabela com dimensões ordenadas e total', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    const tabela = screen.getByRole('table', { name: 'Dimensões' })
    expect(within(tabela).getByText('10.00')).toBeInTheDocument()
    expect(within(tabela).getByText('5.00')).toBeInTheDocument()
    expect(within(tabela).getByText('Retangular')).toBeInTheDocument()
    expect(within(tabela).getByText('Área Total do Ambiente')).toBeInTheDocument()
    expect(within(tabela).getAllByText('50.00')).toHaveLength(2)
  })

  it('renderiza pés-direitos', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    expect(screen.getByText('3.5m, 2.8m')).toBeInTheDocument()
  })

  it('renderiza esquadrias em tabela com dimensões ordenadas e total', () => {
    render(<DetalheAmbiente ambiente={mockAmbiente} />)
    const tabela = screen.getByRole('table', { name: 'Esquadrias' })
    expect(within(tabela).getByText('1.50')).toBeInTheDocument()
    expect(within(tabela).getByText('1.20')).toBeInTheDocument()
    expect(within(tabela).getByText('0.90')).toBeInTheDocument()
    expect(within(tabela).getByText('Janela')).toBeInTheDocument()
    expect(within(tabela).getByText('Alumínio')).toBeInTheDocument()
    expect(within(tabela).getByText('Com veneziana')).toBeInTheDocument()
    expect(within(tabela).getByText('Área Total das Esquadrias')).toBeInTheDocument()
    expect(within(tabela).getAllByText('3.60')).toHaveLength(2)
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
