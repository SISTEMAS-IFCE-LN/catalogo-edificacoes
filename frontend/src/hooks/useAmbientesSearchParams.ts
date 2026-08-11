import { useState, useRef, useEffect, useMemo } from 'react'
import { useSearchParams } from 'react-router'
import { type Filtros, UrlFiltrosSchema, FILTROS_VAZIOS } from '@/types/ambientes/filtros'
import { TipoFiltro } from '@/types/ambientes/enums'

export interface AmbientesSearchParams {
  page: number
  size: number
  filtros: Filtros
  filtrosLocal: Filtros
  setFiltrosLocal: (f: Filtros) => void
  handleFiltrosChange: (f: Filtros) => void
  handlePageChange: (page: number) => void
  handleSizeChange: (size: string | null) => void
  tipoFiltro: TipoFiltro
}

export function useAmbientesSearchParams(): AmbientesSearchParams {
  const [searchParams, setSearchParams] = useSearchParams()

  // Validar parâmetros da URL com Zod (evita NaN em page/size/andar)
  const urlFiltros = useMemo(() => {
    const result = UrlFiltrosSchema.safeParse({
      page: searchParams.get('page'),
      size: searchParams.get('size'),
      nome: searchParams.get('nome'),
      bloco: searchParams.get('bloco'),
      unidade: searchParams.get('unidade'),
      andar: searchParams.get('andar'),
      tipo: searchParams.get('tipo'),
    })
    return result.success ? result.data : { page: 0, size: 20, ...FILTROS_VAZIOS }
  }, [searchParams])

  // Memoizar filtros para evitar recriação em cada render
  const filtros = useMemo<Filtros>(() => ({
    nome: urlFiltros.nome,
    bloco: urlFiltros.bloco,
    unidade: urlFiltros.unidade,
    andar: urlFiltros.andar,
    tipo: urlFiltros.tipo,
  }), [urlFiltros])

  const page = urlFiltros.page
  const size = urlFiltros.size

  // Estado local apenas para o formulário de filtros (antes de aplicar)
  const [filtrosLocal, setFiltrosLocal] = useState<Filtros>(filtros)

  // Rastrear última URL para detectar mudanças externas (back/forward)
  const lastUrlRef = useRef(searchParams.toString())

  // Sincronizar filtrosLocal quando a URL mudar externamente
  useEffect(() => {
    const currentUrl = searchParams.toString()
    if (currentUrl !== lastUrlRef.current) {
      lastUrlRef.current = currentUrl
      setFiltrosLocal(filtros)
    }
  }, [filtros, searchParams])

  function updateSearchParams(updates: Record<string, string | number | null>) {
    setSearchParams((params) => {
      for (const [key, value] of Object.entries(updates)) {
        if (value === null || value === '' || value === undefined) {
          params.delete(key)
        } else {
          params.set(key, String(value))
        }
      }
      return params
    })
  }

  function handleFiltrosChange(novosFiltros: Filtros) {
    setFiltrosLocal(novosFiltros)
    updateSearchParams({
      nome: novosFiltros.nome || null,
      bloco: novosFiltros.bloco || null,
      unidade: novosFiltros.unidade || null,
      andar: novosFiltros.andar,
      tipo: novosFiltros.tipo || null,
      page: 0, // resetar página ao aplicar filtros
    })
  }

  function handlePageChange(newPage: number) {
    updateSearchParams({ page: newPage })
  }

  function handleSizeChange(novoSize: string | null) {
    if (novoSize) {
      updateSearchParams({ size: Number(novoSize), page: 0 })
    }
  }

  // Derivar tipoFiltro a partir dos filtros ativos
  const tipoFiltro = useMemo<TipoFiltro>(() => {
    if (filtros.nome) return TipoFiltro.NOME
    if (filtros.tipo) return TipoFiltro.TIPO
    if (filtros.bloco || filtros.unidade || filtros.andar !== null) return TipoFiltro.LOCALIZACAO
    return TipoFiltro.NENHUM
  }, [filtros])

  return {
    page,
    size,
    filtros,
    filtrosLocal,
    setFiltrosLocal,
    handleFiltrosChange,
    handlePageChange,
    handleSizeChange,
    tipoFiltro,
  }
}
