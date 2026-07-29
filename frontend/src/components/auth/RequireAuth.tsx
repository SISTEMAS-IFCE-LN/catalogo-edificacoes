import {Navigate, Outlet, useLocation} from 'react-router'
import {useAuth} from '@/hooks/useAuth'

function FullScreenLoader() {
    return (
        <div role="status" className="flex items-center justify-center min-h-screen">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"/>
        </div>
    )
}

export function RequireAuth() {
    const {isAuthenticated, isLoading} = useAuth()
    const location = useLocation()

    if (isLoading) return <FullScreenLoader />
    if (!isAuthenticated) {
        return <Navigate to="/login" replace state={{ from: location }} />
    }
    return <Outlet />
}