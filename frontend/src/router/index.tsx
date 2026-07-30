import {createBrowserRouter} from 'react-router'
import {PublicOnly} from '@/components/auth/PublicOnly'
import {RequireAuth} from '@/components/auth/RequireAuth'
import {RequireRole} from '@/components/auth/RequireRole'
import {Role} from '@/types/user'
import {HomePage} from '@/routes/home/page'
import {LoginPage} from '@/routes/login/page'
import {CallbackPage} from '@/routes/callback/page'
import {UnauthorizedPage} from '@/routes/unauthorized/page'
import {ProtectedLayout} from '@/routes/_layout/protected-layout'
import {ROUTES} from '@/constants/routes'

export const router = createBrowserRouter([
    {
        element: <PublicOnly/>,
        children: [{path: ROUTES.LOGIN, element: <LoginPage/>}],
    },
    {path: '/callback', element: <CallbackPage/>},
    {path: '/unauthorized', element: <UnauthorizedPage/>},
    // Publica (UC21-FE)
    {path: '/ambientes/publicados', element: <HomePage/>},
    // Autenticadas
    {
        element: <RequireAuth/>,
        children: [
            {
                element: <ProtectedLayout/>, children: [
                    // Colaborador (UC19-FE, UC20-FE)
                    {
                        path: '/ambientes/publicados/:id',
                        element: <RequireRole roles={[Role.COLABORADOR]}/>,
                        children: [{index: true, element: <HomePage/>}],
                    },
                    {
                        path: '/ambientes/publicados/esquadrias',
                        element: <RequireRole roles={[Role.COLABORADOR]}/>,
                        children: [{index: true, element: <HomePage/>}],
                    },
                    // Validador (UC01-UC03-FE)
                    {
                        path: '/ambientes/validacao',
                        element: <RequireRole roles={[Role.VALIDADOR]}/>,
                        children: [{index: true, element: <HomePage/>}],
                    },
                    // Gestor (UC04-UC18-FE)
                    {
                        path: '/ambientes/nao-publicados',
                        element: <RequireRole roles={[Role.GESTOR_SISTEMA]}/>,
                        children: [{index: true, element: <HomePage/>}],
                    },
                    // Administrador (UC22-UC26-FE)
                    {
                        path: '/usuarios',
                        element: <RequireRole roles={[Role.ADMINISTRADOR]}/>,
                        children: [{index: true, element: <HomePage/>}],
                    },
                ]
            },
        ],
    },
    {path: '/', element: <HomePage/>},
    {path: '*', element: <UnauthorizedPage/>},
])