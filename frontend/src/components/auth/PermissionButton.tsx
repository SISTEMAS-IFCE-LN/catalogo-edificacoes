import React from 'react'
import {ButtonProps} from '@/components/ui/button'
import {Button} from '@/components/ui/button'
import {usePermission} from '@/hooks/usePermission'
import type {Role} from '@/types/usuarios/user'

interface Props extends ButtonProps {
    requiredRoles: Role[]
    children: React.ReactNode
}

export function PermissionButton({requiredRoles, children, ...rest}: Props) {
    const {hasRole} = usePermission()
    if (!hasRole(requiredRoles)) return null
    return <Button {...rest}>{children}</Button>
}