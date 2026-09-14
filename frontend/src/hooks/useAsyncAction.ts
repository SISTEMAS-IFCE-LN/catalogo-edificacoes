import {useState} from 'react'
import {toast} from 'sonner'
import axios from 'axios'

interface ErroRes {
    dataHora?: string
    status?: number
    mensagem?: string
}

function extrairMensagemDoBackend(e: unknown): string | undefined {
    if (!axios.isAxiosError(e)) return undefined
    const data = e.response?.data as ErroRes | undefined
    return typeof data?.mensagem === 'string' ? data.mensagem : undefined
}

interface Opcoes {
    onClose: () => void
    mensagemPadrao?: string
}

export function useAsyncAction({
                                   onClose,
                                   mensagemPadrao = 'Erro ao executar ação. Tente novamente.',
                               }: Opcoes) {
    const [executando, setExecutando] = useState(false)

    async function executar(action: () => Promise<void>) {
        setExecutando(true)
        try {
            await action()
            onClose()
        } catch (e) {
            const mensagem = extrairMensagemDoBackend(e) ?? mensagemPadrao
            toast.error(mensagem)
        } finally {
            setExecutando(false)
        }
    }

    return {executando, executar}
}
