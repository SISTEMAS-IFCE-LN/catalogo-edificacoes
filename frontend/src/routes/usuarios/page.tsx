import {useState} from 'react'
import {useQuery, useQueryClient} from '@tanstack/react-query'
import axios from 'axios'
import {
    fetchUsuarios,
    fetchUsuarioPorNome,
    fetchUsuarioPorEmail,
    atualizarPerfis,
    desativarUsuario,
    ativarUsuario
} from '@/lib/api/api-usuarios'
import {TabelaUsuarios} from '@/components/usuarios/TabelaUsuarios'
import {PesquisaBarUsuarios} from '@/components/usuarios/PesquisaBarUsuarios'
import {ModalEditarPerfis} from '@/components/usuarios/ModalEditarPerfis'
import {ModalConfirmacaoStatusUsuario} from '@/components/usuarios/ModalConfirmacaoStatusUsuario'
import {Button} from '@/components/ui/button'
import {
    Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import {useUsuariosSearchParams} from '@/hooks/useUsuariosSearchParams'
import {TipoFiltroUsuarios} from '@/types/usuarios/filtros'
import type {Role, User, StatusAcao} from '@/types/usuarios/user'
import {toast} from 'sonner'

const TAMANHOS_PAGINA = [10, 20, 50, 100]

export function UsuariosPage() {
    const {
        filtros,
        filtrosLocal,
        page,
        size,
        handleFiltrosChange,
        handlePageChange,
        handleSizeChange,
        tipoFiltro,
    } = useUsuariosSearchParams()

    const [modalPerfisOpen, setModalPerfisOpen] = useState(false)
    const [modalStatusOpen, setModalStatusOpen] = useState(false)
    const [usuarioSelecionado, setUsuarioSelecionado] = useState<User | null>(null)
    const [acaoStatus, setAcaoStatus] = useState<StatusAcao>('desativar')

    const qc = useQueryClient()

    // Lista paginada (por nome ou sem filtro) — desabilitada na busca por email
    const {data, isLoading} = useQuery({
        queryKey: ['usuarios', filtros.nome, page, size],
        queryFn: ({signal}) => filtros.nome
            ? fetchUsuarioPorNome(filtros.nome, page, size, signal)
            : fetchUsuarios(page, size, signal),
        enabled: tipoFiltro !== TipoFiltroUsuarios.EMAIL,
        staleTime: 30_000,
    })

    // Busca direta por email (UC23-FE) — retorna um único usuário
    const {
        data: usuarioPorEmail,
        isLoading: carregandoEmail,
        isError,
        error,
    } = useQuery({
        queryKey: ['usuarios', 'email', filtros.email],
        queryFn: ({signal}) => fetchUsuarioPorEmail(filtros.email, signal),
        enabled: tipoFiltro === TipoFiltroUsuarios.EMAIL,
        staleTime: 30_000,
    })

    // 404 = usuário não encontrado; demais falhas não devem ser mascaradas como "não encontrado".
    const emailNaoEncontrado = isError && axios.isAxiosError(error) && error.response?.status === 404

    function abrirEditarPerfis(usuario: User) {
        setUsuarioSelecionado(usuario)
        setModalPerfisOpen(true)
    }

    function abrirStatus(usuario: User, acao: StatusAcao) {
        setUsuarioSelecionado(usuario)
        setAcaoStatus(acao)
        setModalStatusOpen(true)
    }

    async function salvarPerfis(usuarioId: number, perfis: Role[]) {
        await atualizarPerfis(usuarioId, perfis)
        toast.success('Perfis atualizados com sucesso!')
        void qc.invalidateQueries({queryKey: ['usuarios']}).catch(() => {})
    }

    async function alterarStatus() {
        if (!usuarioSelecionado) return
        if (acaoStatus === 'desativar') await desativarUsuario(usuarioSelecionado.id)
        else await ativarUsuario(usuarioSelecionado.id)
        toast.success('Status alterado com sucesso!')
        void qc.invalidateQueries({queryKey: ['usuarios']}).catch(() => {})
    }

    return (
        <div className="space-y-4">
            <h1 className="text-2xl font-bold">Usuários</h1>
            <PesquisaBarUsuarios
                initial={filtrosLocal}
                onChange={handleFiltrosChange}
            />
            {tipoFiltro === TipoFiltroUsuarios.EMAIL ? (
                carregandoEmail ? (
                    <p>Carregando…</p>
                ) : usuarioPorEmail ? (
                    <TabelaUsuarios
                        itens={[usuarioPorEmail]}
                        onEditarPerfis={abrirEditarPerfis}
                        onAlterarStatus={abrirStatus}
                    />
                ) : (
                    <p className="text-muted-foreground">
                        {emailNaoEncontrado
                            ? 'Nenhum usuário encontrado com este email.'
                            : isError
                                ? 'Erro ao buscar usuário.'
                                : 'Nenhum usuário encontrado.'}
                    </p>
                )
            ) : isLoading ? (
                <p>Carregando…</p>
            ) : data && data.usuarios.length > 0 ? (
                <>
                    <TabelaUsuarios
                        itens={data.usuarios}
                        onEditarPerfis={abrirEditarPerfis}
                        onAlterarStatus={abrirStatus}
                    />
                    <div className="flex flex-wrap gap-2 items-center">
                        <div className="flex items-center gap-2">
                            <span className="text-sm text-muted-foreground">Itens por página:</span>
                            <Select value={String(size)} onValueChange={handleSizeChange}>
                                <SelectTrigger className="w-[70px]" aria-label="Itens por página">
                                    <SelectValue/>
                                </SelectTrigger>
                                <SelectContent>
                                    {TAMANHOS_PAGINA.map((t) => (
                                        <SelectItem key={t} value={String(t)}>
                                            {t}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                        <Button variant="outline" size="sm" disabled={!data.dadosPaginacao.hasPrevious}
                                onClick={() => handlePageChange(page - 1)}>
                            Anterior
                        </Button>
                        <span>Página {data.dadosPaginacao.currentPage + 1} de {data.dadosPaginacao.totalPages}</span>
                        <Button
                            variant="outline"
                            size="sm"
                            disabled={!data.dadosPaginacao.hasNext}
                            onClick={() => handlePageChange(page + 1)}
                        >
                            Próximo
                        </Button>
                    </div>
                </>
            ) : (
                <p className="text-muted-foreground">Nenhum usuário encontrado.</p>
            )}

            <ModalEditarPerfis
                open={modalPerfisOpen}
                usuario={usuarioSelecionado}
                onOpenChange={setModalPerfisOpen}
                onSalvar={salvarPerfis}
            />
            <ModalConfirmacaoStatusUsuario
                open={modalStatusOpen}
                usuario={usuarioSelecionado}
                acao={acaoStatus}
                onConfirmar={alterarStatus}
                onOpenChange={setModalStatusOpen}
            />
        </div>
    )
}
