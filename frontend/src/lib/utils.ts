export function classNames(
    ...xs: (string | false | null | undefined)[]
): string {
    return xs.filter(Boolean).join(' ')
}