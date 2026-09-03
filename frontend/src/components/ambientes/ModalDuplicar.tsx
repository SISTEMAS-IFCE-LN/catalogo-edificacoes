import {useState} from 'react'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import {Button} from '@/components/ui/button'
import {Input} from '@/components/ui/input'
import {Label} from '@/components/ui/label'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import {duplicarAmbiente} from '@/lib/api/api-naopublicados'
import {duplicacaoSchema, nomeTecnicoDeRotulo} from '@/types/ambientes/request'
import {Bloco, Unidade} from '@/types/ambientes/enums'
import {useAsyncAction} from '@/hooks/useAsyncAction'
import type {AmbienteDetalhe} from '@/types/ambientes/response'
import {toast} from 'sonner'
import type {z} from 'zod'

// Limite do backend (@Size(max=50) de AmbienteNomeLocalizacaoReq.kt)
const NOME_MAX = 50

interface Props {
    open: boolean
    ambiente: AmbienteDetalhe | null
    onOpenChange: (o: boolean) => void
    onSalvou: (novoId: number) => void
}

// Erros de validação por caminho do schema ('nome', 'localizacao.andar'...)
type ErrosPorCampo = Record<string, string>

function errosPorCampo(error: z.ZodError): ErrosPorCampo {
    const erros: ErrosPorCampo = {}
    for (const issue of error.issues) {
        const campo = issue.path.join('.')
        if (!erros[campo]) erros[campo] = issue.message
    }
    return erros
}

export function ModalDuplicar({open, ambiente, onOpenChange, onSalvou}: Props) {
    const [nome, setNome] = useState('')
    // Campos de localização em NOME TÉCNICO (request); a resposta devolve rótulos.
    const [bloco, setBloco] = useState('')
    const [unidade, setUnidade] = useState('')
    const [andar, setAndar] = useState('')
    const [erros, setErros] = useState<ErrosPorCampo>({})
    // Último ambiente usado no pré-preenchimento
    const [ambientePreenchido, setAmbientePreenchido] = useState<AmbienteDetalhe | null>(null)

    // Pré-preenchimento com os dados ORIGINAIS do ambiente — SEM sufixo "(cópia)":
    // o usuário define nome e localização antes de confirmar (UC17-FE).
    // Reação à troca de `ambiente` durante o render — padrão "You Might Not Need
    // an Effect" (setState em efeito é rejeitado pelo lint react-hooks v7).
    if (ambiente !== ambientePreenchido) {
        setAmbientePreenchido(ambiente)
        if (ambiente) {
            setNome(ambiente.nome)
            // Rótulo ('Bloco 1') → nome técnico ('BLOCO_1') — ver plano 11 §4.
            setBloco(nomeTecnicoDeRotulo(Bloco, ambiente.localizacao.bloco))
            setUnidade(nomeTecnicoDeRotulo(Unidade, ambiente.localizacao.unidade))
            setAndar(String(ambiente.localizacao.andar))
        }
    }

    // Padrão compartilhado: useAsyncAction trata loading + ErroRes.mensagem
    // (parte 09). Sem try/catch manual — em erro o modal permanece aberto.
    const {executando, executar} = useAsyncAction({
        onClose: () => onOpenChange(false),
        mensagemPadrao: 'Erro ao duplicar ambiente.',
    })

    function salvar() {
        if (!ambiente) return
        const parsed = duplicacaoSchema.safeParse({
            nome,
            localizacao: {
                bloco,
                unidade,
                andar: andar === '' ? Number.NaN : Number(andar),
            },
        })
        if (!parsed.success) {
            setErros(errosPorCampo(parsed.error))
            return
        }
        setErros({})
        void executar(async () => {
            const novo = await duplicarAmbiente(ambiente.id, parsed.data)
            toast.success('Ambiente duplicado.')
            onSalvou(novo.id)
        })
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Duplicar Ambiente</DialogTitle>
                    <DialogDescription>
                        Defina o nome e a localização do novo ambiente.
                    </DialogDescription>
                </DialogHeader>
                <div className="space-y-3">
                    <div className="space-y-1.5">
                        <Label htmlFor="duplicar-nome">Nome</Label>
                        <Input
                            id="duplicar-nome"
                            value={nome}
                            maxLength={NOME_MAX}
                            onChange={(e) => setNome(e.target.value)}
                        />
                        {erros['nome'] && <ErroCampo mensagem={erros['nome']}/>}
                    </div>
                    <div className="space-y-1.5">
                        <Label>Bloco</Label>
                        {/* value = chave técnica; texto = rótulo — ver plano 11 §4 */}
                        <Select
                            value={bloco}
                            onValueChange={(v) => {
                                if (v !== null) setBloco(v)
                            }}
                        >
                            <SelectTrigger aria-label="Bloco" className="w-full">
                                <SelectValue/>
                            </SelectTrigger>
                            <SelectContent>
                                {Object.entries(Bloco).map(([chave, rotulo]) => (
                                    <SelectItem key={chave} value={chave}>{rotulo}</SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                        {erros['localizacao.bloco'] && (
                            <ErroCampo mensagem={erros['localizacao.bloco']}/>
                        )}
                    </div>
                    <div className="space-y-1.5">
                        <Label>Unidade</Label>
                        <Select
                            value={unidade}
                            onValueChange={(v) => {
                                if (v !== null) setUnidade(v)
                            }}
                        >
                            <SelectTrigger aria-label="Unidade" className="w-full">
                                <SelectValue/>
                            </SelectTrigger>
                            <SelectContent>
                                {Object.entries(Unidade).map(([chave, rotulo]) => (
                                    <SelectItem key={chave} value={chave}>{rotulo}</SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                        {erros['localizacao.unidade'] && (
                            <ErroCampo mensagem={erros['localizacao.unidade']}/>
                        )}
                    </div>
                    <div className="space-y-1.5">
                        <Label htmlFor="duplicar-andar">Andar</Label>
                        <Input
                            id="duplicar-andar"
                            type="number"
                            inputMode="numeric"
                            min={0}
                            value={andar}
                            onChange={(e) => setAndar(e.target.value)}
                        />
                        {erros['localizacao.andar'] && (
                            <ErroCampo mensagem={erros['localizacao.andar']}/>
                        )}
                    </div>
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>
                        Cancelar
                    </Button>
                    <Button onClick={salvar} disabled={executando}>
                        {executando ? 'Salvando…' : 'Duplicar'}
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    )
}

function ErroCampo({mensagem}: { mensagem: string }) {
    return (
        <p role="alert" className="text-sm text-destructive">
            {mensagem}
        </p>
    )
}
