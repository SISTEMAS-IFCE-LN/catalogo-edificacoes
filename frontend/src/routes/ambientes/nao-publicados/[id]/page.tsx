import {useNavigate, useParams} from 'react-router'
import {useQuery, useQueryClient} from '@tanstack/react-query'
import {useState} from 'react'
import {
    deletarAmbientes,
    enviarParaValidacao,
    fetchAmbienteNaoPublicado
} from '@/lib/api/api-naopublicados'
import {DetalheAmbiente} from '@/components/ambientes/DetalheAmbiente'
import {ModalConfirmacao} from '@/components/ambientes/ModalConfirmacao'
import {ModalDuplicar} from '@/components/ambientes/ModalDuplicar'
import {PermissionButton} from '@/components/auth/PermissionButton'
import {Role} from '@/types/usuarios/user'
import {Button} from '@/components/ui/button'
import {PAGES_ROUTES} from '@/constants/routes'
import {toast} from 'sonner'

type ModalEstado =
    | null
    | 'deletar'
    | 'duplicar'
    | 'enviar-validacao'
    | 'editar-dados-basicos'
    | 'incluir-geometrias'
    | 'editar-geometrias'
    | 'incluir-pes-direitos'
    | 'editar-pes-direitos'
    | 'incluir-esquadrias'
    | 'editar-esquadrias'
    | 'info-adicional'
    | 'alterar-tipo'

export function NaoPublicadoDetalhePage() {
    const {id} = useParams()
    const navigate = useNavigate()
    const queryCliente = useQueryClient()
    const [modal, setModal] = useState<ModalEstado>(null)

    const {data: ambiente, isLoading, error} = useQuery({
        queryKey: ['ambientes', 'nao-publicados', 'detalhe', id],
        queryFn: ({signal}) => {
            const idNumero = Number(id)
            if (!Number.isInteger(idNumero) || idNumero <= 0) {
                throw new Error(`ID inválido: ${idNumero}`)
            }
            return fetchAmbienteNaoPublicado(idNumero, signal)
        },
        enabled: !!id,
    })

    // UC17-FE: duplicação exige nome/localização editáveis (ModalDuplicar) —
    // em erro (RN-1.7, ex.: duplicar 2×) o usuário corrige no próprio modal.
    function aoDuplicar(novoId: number) {
        void queryCliente.invalidateQueries({ queryKey: ['ambientes', 'nao-publicados'] })
        navigate(`/ambientes/nao-publicados/${novoId}`)
    }

    async function confirmar() {
        if (!ambiente) return
        // Sem try/catch: o ModalConfirmacao (sobre useAsyncAction) já trata o erro
        // (toast com ErroRes.mensagem e modal permanece aberto).
        if (modal === 'deletar') {
            await deletarAmbientes([ambiente.id])
            navigate(PAGES_ROUTES.NAO_PUBLICADOS)
        } else if (modal === 'enviar-validacao') {
            await enviarParaValidacao([ambiente.id])
            navigate(PAGES_ROUTES.NAO_PUBLICADOS)
        }
        toast.success('Operação concluída.')
        void queryCliente.invalidateQueries({ queryKey: ['ambientes', 'nao-publicados'] })
    }

    if (isLoading) return <p>Carregando…</p>
    if (error || !ambiente) {
        return (
            <div className="space-y-4">
                <p>Ambiente não encontrado.</p>
                <Button variant="outline" onClick={() => navigate(PAGES_ROUTES.NAO_PUBLICADOS)}>
                    Voltar à lista
                </Button>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            <Button variant="outline" onClick={() => navigate(PAGES_ROUTES.NAO_PUBLICADOS)}>Voltar</Button>
            <DetalheAmbiente ambiente={ambiente}/>
            <div className="flex flex-wrap gap-2">
                <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} variant="outline" onClick={() => setModal('editar-dados-basicos')}>
                    Editar Dados Básicos
                </PermissionButton>
                <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} variant="outline" onClick={() => setModal('editar-geometrias')}>
                    Editar Geometrias
                </PermissionButton>
                <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} variant="outline" onClick={() => setModal('editar-pes-direitos')}>
                    Editar Pés-direitos
                </PermissionButton>
                <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} variant="outline" onClick={() => setModal('editar-esquadrias')}>
                    Editar Esquadrias
                </PermissionButton>
                <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} variant="outline" onClick={() => setModal('alterar-tipo')}>
                    Alterar Tipo
                </PermissionButton>
                <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} variant="outline" onClick={() => setModal('duplicar')}>
                    Duplicar
                </PermissionButton>
                <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} onClick={() => setModal('enviar-validacao')}>
                    Enviar p/ Validação
                </PermissionButton>
                <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} variant="destructive" onClick={() => setModal('deletar')}>
                    Deletar
                </PermissionButton>
            </div>

            {/* Confirmações UC15/UC18 (UC17 Duplicar usa ModalDuplicar — o usuário
                define nome e localização; RN-1.7 é corrigível no modal) */}
            <ModalConfirmacao
                open={modal === 'deletar' || modal === 'enviar-validacao'}
                title={modal === 'deletar' ? 'Deletar ambiente?' : 'Enviar para validação?'}
                description={modal === 'deletar' ? 'Esta ação é permanente.' : 'O ambiente será enviado para validação.'}
                onConfirm={confirmar}
                onOpenChange={(o) => !o && setModal(null)}
                variant={modal === 'deletar' ? 'destructive' : 'default'}
                confirmLabel={modal === 'deletar' ? 'Deletar' : 'Enviar'}
            />
            <ModalDuplicar
                open={modal === 'duplicar'}
                ambiente={ambiente ?? null}
                onOpenChange={(o) => !o && setModal(null)}
                onSalvou={aoDuplicar}
            />

            {/* Modais de edição UC07–UC14/UC16: acoplar aqui, um por estado de `modal` */}
            {/* <ModalEditarDadosBasicos open={modal === 'editar-dados-basicos'} ambiente={ambiente}
                 onOpenChange={(o) => !o && setModal(null)} onSalvou={() => queryCliente.invalidateQueries({ queryKey: ['ambientes', 'nao-publicados', 'detalhe', id] })} /> */}
            {/* ... ModalIncluirGeometrias, ModalEditarGeometrias, ModalIncluirPesDireitos, ModalEditarPesDireitos,
                 ModalIncluirEsquadrias, ModalEditarEsquadrias, ModalInfoAdicional, ModalAlterarTipo ... */}
        </div>
    )
}
