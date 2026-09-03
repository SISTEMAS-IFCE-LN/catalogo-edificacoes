export function ErroCampo({mensagem}: { mensagem: string }) {
    return (
        <p role="alert" className="text-sm text-destructive">
            {mensagem}
        </p>
    )
}
