import {useNavigate} from 'react-router'
import {useQueryClient} from '@tanstack/react-query'
import {FormAmbiente} from '@/components/ambientes/FormAmbiente'
import {criarAmbiente} from '@/lib/api/api-naopublicados'
import type {AmbienteInput} from '@/types/ambientes/request'
import {useAsyncAction} from '@/hooks/useAsyncAction'
import {toast} from 'sonner'

export function NovoAmbientePage() {
    const navigate = useNavigate()
    const queryCliente = useQueryClient()

    // Erros de criação (ex.: RN-1.7, nome duplicado) exibem ErroRes.mensagem
    // via useAsyncAction — padrão da parte 09 (sem toast genérico).
    const {executar} = useAsyncAction({
        onClose: () => undefined, // não há modal a fechar; a navegação ocorre no sucesso
        mensagemPadrao: 'Erro ao criar ambiente.',
    })

    async function onSubmit(values: AmbienteInput) {
        await executar(async () => {
            const novo = await criarAmbiente(values)
            // Find-or-create pode criar campus/unidade/bloco → invalida o prefixo
            // inteiro para os selects refletirem os novos registros (plano 11 §4.3).
            void queryCliente.invalidateQueries({ queryKey: ['referencias'] })
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
