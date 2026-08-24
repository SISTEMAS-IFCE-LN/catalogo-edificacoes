import {Suspense} from 'react'
import {createBrowserRouter} from 'react-router'
import {PublicOnly} from '@/components/auth/PublicOnly'
import {RequireAuth} from '@/components/auth/RequireAuth'
import {RequireRole} from '@/components/auth/RequireRole'
import {Role} from '@/types/usuarios/user'
import {Loading} from '@/components/ui/Loading'
import {ROUTES} from '@/constants/routes'
import {
    HomePage,
    LoginPage,
    CallbackPage,
    UnauthorizedPage,
    ProtectedLayout,
    PublicadosPage,
    PublicadoDetalhePage,
    EsquadriasPage,
    ValidacaoPage,
    UsuariosPage,
} from './lazy-pages'

export const router = createBrowserRouter([
    {
        element: <PublicOnly/>,
        children: [{path: ROUTES.LOGIN, element: <Suspense fallback={<Loading/>}><LoginPage/></Suspense>}],
    },
    {path: '/callback', element: <Suspense fallback={<Loading/>}><CallbackPage/></Suspense>},
    {path: '/unauthorized', element: <Suspense fallback={<Loading/>}><UnauthorizedPage/></Suspense>},
    // Layout comum (Header auth-aware + Footer) para público e autenticado
    {
        element: <Suspense fallback={<Loading/>}><ProtectedLayout/></Suspense>,
        children: [
            // Pública (UC21-FE) — sem RequireAuth, mas com Header
            {path: '/ambientes/publicados', element: <Suspense fallback={<Loading/>}><PublicadosPage/></Suspense>},
            // Autenticadas
            {
                element: <RequireAuth/>,
                children: [
                    // Colaborador (UC19-FE, UC20-FE)
                    {
                        path: '/ambientes/publicados/:id',
                        element: <RequireRole roles={[Role.COLABORADOR]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading/>}><PublicadoDetalhePage/></Suspense>}],
                    },
                    {
                        path: '/ambientes/publicados/esquadrias',
                        element: <RequireRole roles={[Role.COLABORADOR]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading/>}><EsquadriasPage contexto="publicados"/></Suspense>}],
                    },
                    // Validador (UC01-UC03-FE)
                    {
                        path: '/ambientes/validacao',
                        element: <RequireRole roles={[Role.VALIDADOR]}/>,
                        children: [
                            {index: true, element: <Suspense fallback={<Loading/>}><ValidacaoPage/></Suspense>},
                            {path: 'esquadrias', element: <Suspense fallback={<Loading/>}><EsquadriasPage contexto="validacao"/></Suspense>},
                        ],
                    },
                    // Gestor (UC04-UC18-FE)
                    {
                        path: '/ambientes/nao-publicados',
                        element: <RequireRole roles={[Role.GESTOR_SISTEMA]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading/>}><HomePage/></Suspense>}],
                    },
                    // Administrador (UC22-UC26-FE)
                    {
                        path: '/usuarios',
                        element: <RequireRole roles={[Role.ADMINISTRADOR]}/>,
                        children: [{index: true, element: <Suspense fallback={<Loading/>}><UsuariosPage/></Suspense>}],
                    },
                ],
            },
        ],
    },
    {path: '/', element: <Suspense fallback={<Loading/>}><HomePage/></Suspense>},
    {path: '*', element: <Suspense fallback={<Loading/>}><UnauthorizedPage/></Suspense>},
])