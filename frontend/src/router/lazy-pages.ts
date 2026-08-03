import {lazy} from 'react'

// Lazy-loaded pages for code-splitting
export const HomePage = lazy(() => import('@/routes/home/page').then(m => ({default: m.HomePage})))
export const LoginPage = lazy(() => import('@/routes/login/page').then(m => ({default: m.LoginPage})))
export const CallbackPage = lazy(() => import('@/routes/callback/page').then(m => ({default: m.CallbackPage})))
export const UnauthorizedPage = lazy(() => import('@/routes/unauthorized/page').then(m => ({default: m.UnauthorizedPage})))
export const ProtectedLayout = lazy(() => import('@/routes/_layout/protected-layout').then(m => ({default: m.ProtectedLayout})))
export const PublicadosPage = lazy(() => import('@/routes/ambientes/publicados/page').then(m => ({default: m.PublicadosPage})))
