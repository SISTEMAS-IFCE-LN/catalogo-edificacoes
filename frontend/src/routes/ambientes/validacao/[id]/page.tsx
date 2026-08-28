import {useNavigate, useParams} from 'react-router'
import {useQuery, useQueryClient} from '@tanstack/react-query'
import {DetalheAmbiente} from '@/components/ambientes/DetalheAmbiente'
import {Button} from '@/components/ui/button'
import {useState} from 'react'
import {fetchDetalheValidacao, privarAmbiente, publicarAmbiente} from '@/lib/api/api-validacao'
import {toast} from 'sonner'
import {ROUTES} from '@/constants/routes'
import {StatusAmbiente} from '@/types/ambientes/enums'
import {ModalConfirmacao} from '@/components/ambientes/ModalConfirmacao'
import {PermissionButton} from '@/components/auth/PermissionButton'
import {Role} from '@/types/usuarios/user'

export function ValidacaoDetalhePage() {
    const {id} = useParams<{ id: string }>()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const [modalOpen, setModalOpen] = useState<null | 'publicar' | 'privar'>(null)

    const {data: ambiente, isLoading, error} = useQuery({
        queryKey: ['ambientes', 'validacao', 'detalhe', id],
        queryFn: ({signal}) => fetchDetalheValidacao(Number(id), signal),
        enabled: !!id,
    })

    async function confirmar() {
        if (!ambiente) return
        if (modalOpen === 'publicar') await publicarAmbiente(ambiente.id)
        else if (modalOpen === 'privar') await privarAmbiente(ambiente.id)
        toast.success(modalOpen === 'publicar' ? 'Ambiente publicado.' : 'Ambiente privado.')
        void queryClient.invalidateQueries({queryKey: ['ambientes', 'validacao']})
        navigate(ROUTES.VALIDACAO)
    }

    if (isLoading) return <p>Carregando…</p>
    if (error || !ambiente) {
        return (
            <div className="space-y-4">
                <p>Ambiente não encontrado.</p>
                <Button variant="outline" onClick={() => navigate(ROUTES.VALIDACAO)}>
                    Voltar à lista
                </Button>
            </div>
        )
    }

    // Desabilitação por status (UC03-FE). `ambiente.status` já está normalizado
    // para os rótulos do enum `StatusAmbiente` (ex.: "Aguardando Validação").
    const podePublicar = ambiente.status === StatusAmbiente.AGUARDANDO_VALIDACAO
    const podePrivar = ambiente.status === StatusAmbiente.PUBLICADO ||
        ambiente.status === StatusAmbiente.AGUARDANDO_VALIDACAO

    return (
        <div className="space-y-4">
            <Button variant="outline" onClick={() => navigate(ROUTES.VALIDACAO)}>
                Voltar
            </Button>
            <DetalheAmbiente ambiente={ambiente}/>
            <div className="flex gap-2">
                <PermissionButton
                    requiredRoles={[Role.VALIDADOR]}
                    disabled={!podePublicar}
                    onClick={() => setModalOpen('publicar')}
                >
                    Publicar
                </PermissionButton>
                <PermissionButton
                    requiredRoles={[Role.VALIDADOR]}
                    variant="destructive"
                    disabled={!podePrivar}
                    onClick={() => setModalOpen('privar')}
                >
                    Privar
                </PermissionButton>
            </div>
            <ModalConfirmacao
                open={modalOpen !== null}
                title={modalOpen === 'publicar' ? 'Publicar ambiente?' : 'Privar ambiente?'}
                description={modalOpen === 'publicar'
                    ? 'O ambiente ficará visível publicamente.'
                    : 'O ambiente ficará disponível para edição.'}
                onConfirm={confirmar}
                onOpenChange={(o) => !o && setModalOpen(null)}
                variant={modalOpen === 'publicar' ? 'default' : 'destructive'}
                confirmLabel={modalOpen === 'publicar' ? 'Publicar' : 'Privar'}
            />
        </div>
    )
}
