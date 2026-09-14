import {Navigate, Outlet, useLocation} from 'react-router'
import {useAuth} from '@/hooks/useAuth'
import {Loading} from '@/components/ui/Loading'

export function RequireAuth() {
    const {isAuthenticated, isLoading} = useAuth()
    const location = useLocation()

    if (isLoading) return <Loading />
    if (!isAuthenticated) {
        return <Navigate to="/login" replace state={{ from: location }} />
    }
    return <Outlet />
}