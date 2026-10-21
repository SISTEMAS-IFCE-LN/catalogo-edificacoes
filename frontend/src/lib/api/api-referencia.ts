import {api} from '@/lib/api/api'
import {API_ROUTES} from '@/constants/routes'
import {
    ReferenciaListSchema,
    type ReferenciaRes,
} from '@/types/ambientes/referencia'

export async function fetchCampus(signal?: AbortSignal): Promise<ReferenciaRes[]> {
    const {data} = await api.get(API_ROUTES.CAMPUS, {signal})
    return ReferenciaListSchema.parse(data)
}

export async function fetchUnidades(
    campusId: number,
    signal?: AbortSignal
): Promise<ReferenciaRes[]> {
    const {data} = await api.get(API_ROUTES.UNIDADES, {
        params: {campusId},
        signal,
    })
    return ReferenciaListSchema.parse(data)
}

export async function fetchBlocos(
    unidadeId: number,
    signal?: AbortSignal
): Promise<ReferenciaRes[]> {
    const {data} = await api.get(API_ROUTES.BLOCOS, {
        params: {unidadeId},
        signal,
    })
    return ReferenciaListSchema.parse(data)
}
