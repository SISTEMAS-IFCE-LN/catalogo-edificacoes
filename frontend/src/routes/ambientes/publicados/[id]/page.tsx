import {useState} from 'react'
import {useNavigate, useParams} from 'react-router'
import {useQuery, useQueryClient} from '@tanstack/react-query'
import {fetchDetalhePublicados} from '@/lib/api/api-publicados'
import {privarAmbiente} from '@/lib/api/api-validacao'
import {DetalheAmbiente} from '@/components/ambientes/DetalheAmbiente'
import {Button} from '@/components/ui/button'
import {ROUTES} from '@/constants/routes'
import {StatusAmbiente} from '@/types/ambientes/enums'
import {ModalConfirmacao} from '@/components/ambientes/ModalConfirmacao'
import {PermissionButton} from '@/components/auth/PermissionButton'
import {Role} from '@/types/usuarios/user'
import {toast} from 'sonner'

export function PublicadoDetalhePage() {
    const {id} = useParams<{ id: string }>()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const [modalOpen, setModalOpen] = useState(false)

    const {data: ambiente, isLoading, error} = useQuery({
        queryKey: ['ambientes', 'publicados', 'detalhe', id],
        queryFn: ({signal}) => fetchDetalhePublicados(Number(id), signal),
        enabled: !!id,
    })

    async function confirmar() {
        if (!ambiente) return
        // O ModalConfirmacao já trata o erro e mantém o modal aberto (parte 10 §4)
        await privarAmbiente(ambiente.id)
        toast.success('Ambiente privado.')
        void queryClient.invalidateQueries({queryKey: ['ambientes', 'publicados']})
        navigate(ROUTES.PUBLICADOS)
    }

    if (isLoading) return <p>Carregando…</p>
    if (error || !ambiente) {
        return (
            <div className="space-y-4">
                <p>Ambiente não encontrado.</p>
                <Button variant="outline" onClick={() => navigate(ROUTES.PUBLICADOS)}>
                    Voltar à lista
                </Button>
            </div>
        )
    }

    // Privar (UC03-FE): na lista pública só aparecem ambientes PUBLICADO.
    const podePrivar = ambiente.status === StatusAmbiente.PUBLICADO

    return (
        <div className="space-y-4">
            <Button variant="outline" onClick={() => navigate(ROUTES.PUBLICADOS)}>
                Voltar
            </Button>
            <DetalheAmbiente ambiente={ambiente}/>
            <PermissionButton
                requiredRoles={[Role.VALIDADOR]}
                variant="destructive"
                disabled={!podePrivar}
                onClick={() => setModalOpen(true)}
            >
                Privar
            </PermissionButton>
            <ModalConfirmacao
                open={modalOpen}
                title="Privar ambiente?"
                description="O ambiente ficará disponível para edição."
                onConfirm={confirmar}
                onOpenChange={(o) => !o && setModalOpen(false)}
                variant="destructive"
                confirmLabel="Privar"
            />
        </div>
    )
}
