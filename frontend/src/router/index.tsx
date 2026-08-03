/* eslint-disable react-refresh/only-export-components */
import {lazy, Suspense} from 'react'
import {createBrowserRouter} from 'react-router'
import {PublicOnly} from '@/components/auth/PublicOnly'
import {RequireAuth} from '@/components/auth/RequireAuth'
import {RequireRole} from '@/components/auth/RequireRole'
import {Role} from '@/types/user'
import {Loading} from '@/components/ui/Loading'
import {ROUTES} from '@/constants/routes'

// Lazy-loaded pages for code-splitting
const HomePage = lazy(() => import('@/routes/home/page').then(m => ({ default: m.HomePage })))
const LoginPage = lazy(() => import('@/routes/login/page').then(m => ({ default: m.LoginPage })))
const CallbackPage = lazy(() => import('@/routes/callback/page').then(m => ({ default: m.CallbackPage })))
const UnauthorizedPage = lazy(() => import('@/routes/unauthorized/page').then(m => ({ default: m.UnauthorizedPage })))
const ProtectedLayout = lazy(() => import('@/routes/_layout/protected-layout').then(m => ({ default: m.ProtectedLayout })))
const PublicadosPage = lazy(() => import('@/routes/ambientes/publicados/page').then(m => ({ default: m.PublicadosPage })))

export const router = createBrowserRouter([
    {
        element: <PublicOnly/>,
        children: [{path: ROUTES.LOGIN, element: <Suspense fallback={<Loading />}><LoginPage /></Suspense>}],
    },
    {path: '/callback', element: <Suspense fallback={<Loading />}><CallbackPage /></Suspense>},
    {path: '/unauthorized', element: <Suspense fallback={<Loading />}><UnauthorizedPage /></Suspense>},
    // Publica (UC21-FE)
    {path: '/ambientes/publicados', element: <Suspense fallback={<Loading />}><PublicadosPage /></Suspense>},
    // Autenticadas
    {
        element: <RequireAuth/>,
        children: [
            {
                element: <Suspense fallback={<Loading />}><ProtectedLayout /></Suspense>, children: [
                    // Colaborador (UC19-FE, UC20-FE)
                    {
                        path: '/ambientes/publicados/:id',
                        element: <RequireRole roles={[Role.COLABORADOR]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading />}><HomePage /></Suspense>}],
                    },
                    {
                        path: '/ambientes/publicados/esquadrias',
                        element: <RequireRole roles={[Role.COLABORADOR]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading />}><HomePage /></Suspense>}],
                    },
                    // Validador (UC01-UC03-FE)
                    {
                        path: '/ambientes/validacao',
                        element: <RequireRole roles={[Role.VALIDADOR]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading />}><HomePage /></Suspense>}],
                    },
                    // Gestor (UC04-UC18-FE)
                    {
                        path: '/ambientes/nao-publicados',
                        element: <RequireRole roles={[Role.GESTOR_SISTEMA]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading />}><HomePage /></Suspense>}],
                    },
                    // Administrador (UC22-UC26-FE)
                    {
                        path: '/usuarios',
                        element: <RequireRole roles={[Role.ADMINISTRADOR]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading />}><HomePage /></Suspense>}],
                    },
                ]
            },
        ],
    },
    {path: '/', element: <Suspense fallback={<Loading />}><HomePage /></Suspense>},
    {path: '*', element: <Suspense fallback={<Loading />}><UnauthorizedPage /></Suspense>},
])