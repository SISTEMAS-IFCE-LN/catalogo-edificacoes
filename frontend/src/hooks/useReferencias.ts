import {useQuery} from '@tanstack/react-query'
import {
    fetchBlocos,
    fetchCampus,
    fetchUnidades,
} from '@/lib/api/api-referencia'

export function useCampus() {
    return useQuery({
        queryKey: ['referencias', 'campus'],
        queryFn: ({signal}) => fetchCampus(signal),
    })
}

export function useUnidades(campusId: number | null) {
    return useQuery({
        queryKey: ['referencias', 'unidades', campusId],
        queryFn: ({signal}) => fetchUnidades(campusId!, signal),
        enabled: campusId !== null,
    })
}

export function useBlocos(unidadeId: number | null) {
    return useQuery({
        queryKey: ['referencias', 'blocos', unidadeId],
        queryFn: ({signal}) => fetchBlocos(unidadeId!, signal),
        enabled: unidadeId !== null,
    })
}
