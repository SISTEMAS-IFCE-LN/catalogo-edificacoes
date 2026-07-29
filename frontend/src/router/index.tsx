import {createBrowserRouter} from 'react-router'
import {PublicOnly} from '@/components/auth/PublicOnly'
import {RequireAuth} from '@/components/auth/RequireAuth'
import {RequireRole} from '@/components/auth/RequireRole'
import {Role} from '@/types/user'
import {HomePage} from '@/routes/home/page'
import {LoginPage} from '@/routes/login/page'
import {CallbackPage} from '@/routes/callback/page'
import {UnauthorizedPage} from '@/routes/unauthorized/page'

export const router = createBrowserRouter([
    {
        element: <PublicOnly/>,
        children: [{
            path: '/login', element: <LoginPage/>
        }]
    },
    {path: '/callback', element: <CallbackPage/>},
    {path: '/unauthorized', element: <UnauthorizedPage/>},
    {
        element: <RequireAuth/>,
        children: [
            {
                path: '/usuarios',
                element: <RequireRole roles={[Role.ADMINISTRADOR]}/>,
                children: [{index: true, element: <HomePage/>}],
            },
        ],
    },
    {path: '/', element: <HomePage/>},
])