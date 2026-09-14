import {useNavigate} from 'react-router'
import {FormAmbiente} from '@/components/ambientes/FormAmbiente'
import {criarAmbiente} from '@/lib/api/api-naopublicados'
import type {AmbienteInput} from '@/types/ambientes/request'
import {useAsyncAction} from '@/hooks/useAsyncAction'
import {toast} from 'sonner'

export function NovoAmbientePage() {
    const navigate = useNavigate()

    // Erros de criação (ex.: RN-1.7, nome duplicado) exibem ErroRes.mensagem
    // via useAsyncAction — padrão da parte 09 (sem toast genérico).
    const {executar} = useAsyncAction({
        onClose: () => undefined, // não há modal a fechar; a navegação ocorre no sucesso
        mensagemPadrao: 'Erro ao criar ambiente.',
    })

    async function onSubmit(values: AmbienteInput) {
        await executar(async () => {
            const novo = await criarAmbiente(values)
            toast.success('Ambiente criado.')
            navigate(`/ambientes/nao-publicados/${novo.id}`)
        })
    }

    return (
        <div className="max-w-2xl mx-auto space-y-4">
            <h1 className="text-2xl font-bold">Novo Ambiente</h1>
            <FormAmbiente onSubmit={onSubmit}/>
        </div>
    )
}
