import {useNavigate, useParams} from 'react-router'
import {useQuery, useQueryClient} from '@tanstack/react-query'
import {useState} from 'react'
import {
    atualizarGeometrias,
    atualizarPesDireitos,
    atualizarEsquadrias,
    deletarAmbientes,
    enviarParaValidacao,
    fetchAmbienteNaoPublicado,
    incluirEsquadrias,
    incluirGeometrias,
    incluirPesDireitos,
} from '@/lib/api/api-naopublicados'
import {esquadriasDeDetalhe, geometriasDeDetalhe} from '@/lib/ambientes/mappers'
import {DetalheAmbiente} from '@/components/ambientes/DetalheAmbiente'
import {ModalConfirmacao} from '@/components/ambientes/ModalConfirmacao'
import {ModalDuplicar} from '@/components/ambientes/ModalDuplicar'
import {ModalEditarDadosBasicos} from '@/components/ambientes/ModalEditarDadosBasicos'
import {ModalGeometrias} from '@/components/ambientes/ModalGeometrias'
import {ModalPesDireitos} from '@/components/ambientes/ModalPesDireitos'
import {ModalEsquadrias} from '@/components/ambientes/ModalEsquadrias'
import {ModalInfoAdicional} from '@/components/ambientes/ModalInfoAdicional'
import {ModalAlterarTipo} from '@/components/ambientes/ModalAlterarTipo'
import {AcoesAmbiente} from '@/components/ambientes/AcoesAmbiente'
import type {AcaoAmbiente} from '@/components/ambientes/AcoesAmbiente'
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

    // Padrão dos modais de edição (UC07–UC14): sucesso → invalida o detalhe.
    function invalidarDetalhe() {
        void queryCliente.invalidateQueries({ queryKey: ['ambientes', 'nao-publicados', 'detalhe', id] })
    }

    // UC17-FE: duplicação exige nome/localização editáveis (ModalDuplicar) —
    // em erro (RN-1.7, ex.: duplicar 2×) o usuário corrige no próprio modal.
    function aoDuplicar(novoId: number) {
        void queryCliente.invalidateQueries({ queryKey: ['ambientes', 'nao-publicados'] })
        navigate(`/ambientes/nao-publicados/${novoId}`)
    }

    // UC16-FE: POST /{id} cria um NOVO ambiente e remove o antigo — invalidar a
    // LISTA e navegar para o registro novo (casos-uso-frontend.md UC16, passo 3).
    function aoAlterarTipo(novoId: number) {
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

    // Ações não-críticas (UC07–UC14, UC16, UC17) como opções do AcoesAmbiente;
    // a permissão de cada uma é checada pelo componente via canDo(actionKey).
    const acoes: AcaoAmbiente[] = [
        {value: 'Editar Dados Básicos', actionKey: 'ambiente:editar', onRun: () => setModal('editar-dados-basicos')},
        {value: 'Incluir Geometrias', actionKey: 'ambiente:editar', onRun: () => setModal('incluir-geometrias')},
        {value: 'Editar Geometrias', actionKey: 'ambiente:editar', onRun: () => setModal('editar-geometrias')},
        {value: 'Incluir Pés-direitos', actionKey: 'ambiente:editar', onRun: () => setModal('incluir-pes-direitos')},
        {value: 'Editar Pés-direitos', actionKey: 'ambiente:editar', onRun: () => setModal('editar-pes-direitos')},
        {value: 'Incluir Esquadrias', actionKey: 'ambiente:editar', onRun: () => setModal('incluir-esquadrias')},
        {value: 'Editar Esquadrias', actionKey: 'ambiente:editar', onRun: () => setModal('editar-esquadrias')},
        {value: 'Info Adicional', actionKey: 'ambiente:editar', onRun: () => setModal('info-adicional')},
        {value: 'Alterar Tipo', actionKey: 'ambiente:alterar-tipo', onRun: () => setModal('alterar-tipo')},
        {value: 'Duplicar', actionKey: 'ambiente:duplicar', onRun: () => setModal('duplicar')},
    ]

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
            {/* Barra de ações no topo (arquitetura §15.11): select de ações
                não-críticas à esquerda + críticas (UC15/UC18) como botões diretos. */}
            <AcoesAmbiente
                acoes={acoes}
                criticalActions={
                    <>
                        <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} onClick={() => setModal('enviar-validacao')}>
                            Enviar p/ Validação
                        </PermissionButton>
                        <PermissionButton requiredRoles={[Role.GESTOR_SISTEMA]} variant="destructive" onClick={() => setModal('deletar')}>
                            Deletar
                        </PermissionButton>
                    </>
                }
            />
            <DetalheAmbiente ambiente={ambiente}/>

            {/* Confirmações UC15/UC18 (UC17 Duplicar e UC16 Alterar Tipo têm modais
                próprios — o usuário define dados antes de confirmar) */}
            <ModalConfirmacao
                open={modal === 'deletar' || modal === 'enviar-validacao'}
                title={modal === 'deletar' ? 'Deletar ambiente?' : 'Enviar para validação?'}
                description={modal === 'deletar' ? 'Esta ação é permanente.' : 'O ambiente será enviado para validação.'}
                onConfirm={confirmar}
                onOpenChange={(o) => !o && setModal(null)}
                variant={modal === 'deletar' ? 'destructive' : 'default'}
                confirmLabel={modal === 'deletar' ? 'Deletar' : 'Enviar'}
            />

            {/* Modais UC07–UC17: montagem condicional — os defaultValues são relidos
                a cada abertura, sem efeito de reset (react-hooks/set-state-in-effect) */}
            {modal === 'duplicar' && (
                <ModalDuplicar
                    open
                    ambiente={ambiente}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSalvou={aoDuplicar}
                />
            )}
            {modal === 'editar-dados-basicos' && (
                <ModalEditarDadosBasicos
                    open
                    ambiente={ambiente}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSalvou={invalidarDetalhe}
                />
            )}
            {modal === 'incluir-geometrias' && (
                <ModalGeometrias
                    open
                    modo="incluir"
                    titulo="Incluir Geometrias"
                    inicial={[]}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSubmit={(geometrias) => incluirGeometrias(ambiente.id, geometrias)}
                    onSalvou={invalidarDetalhe}
                />
            )}
            {modal === 'editar-geometrias' && (
                <ModalGeometrias
                    open
                    modo="editar"
                    titulo="Editar Geometrias"
                    inicial={geometriasDeDetalhe(ambiente)}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSubmit={(geometrias) => atualizarGeometrias(ambiente.id, geometrias)}
                    onSalvou={invalidarDetalhe}
                />
            )}
            {modal === 'incluir-pes-direitos' && (
                <ModalPesDireitos
                    open
                    modo="incluir"
                    titulo="Incluir Pés-direitos"
                    inicial={[]}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSubmit={(alturas) => incluirPesDireitos(ambiente.id, alturas)}
                    onSalvou={invalidarDetalhe}
                />
            )}
            {modal === 'editar-pes-direitos' && (
                <ModalPesDireitos
                    open
                    modo="editar"
                    titulo="Editar Pés-direitos"
                    inicial={ambiente.pesDireitos}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSubmit={(alturas) => atualizarPesDireitos(ambiente.id, alturas)}
                    onSalvou={invalidarDetalhe}
                />
            )}
            {modal === 'incluir-esquadrias' && (
                <ModalEsquadrias
                    open
                    modo="incluir"
                    titulo="Incluir Esquadrias"
                    inicial={[]}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSubmit={(esquadrias) => incluirEsquadrias(ambiente.id, esquadrias)}
                    onSalvou={invalidarDetalhe}
                />
            )}
            {modal === 'editar-esquadrias' && (
                <ModalEsquadrias
                    open
                    modo="editar"
                    titulo="Editar Esquadrias"
                    inicial={esquadriasDeDetalhe(ambiente)}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSubmit={(esquadrias) => atualizarEsquadrias(ambiente.id, esquadrias)}
                    onSalvou={invalidarDetalhe}
                />
            )}
            {modal === 'info-adicional' && (
                <ModalInfoAdicional
                    open
                    ambiente={ambiente}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSalvou={invalidarDetalhe}
                />
            )}
            {modal === 'alterar-tipo' && (
                <ModalAlterarTipo
                    open
                    ambiente={ambiente}
                    onOpenChange={(o) => !o && setModal(null)}
                    onSalvou={aoAlterarTipo}
                />
            )}
        </div>
    )
}
