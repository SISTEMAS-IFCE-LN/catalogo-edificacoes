import {lazy} from 'react'

// Lazy-loaded pages for code-splitting
export const HomePage = lazy(() => import('@/routes/home/page').then(m => ({default: m.HomePage})))
export const LoginPage = lazy(() => import('@/routes/login/page').then(m => ({default: m.LoginPage})))
export const CallbackPage = lazy(() => import('@/routes/callback/page').then(m => ({default: m.CallbackPage})))
export const UnauthorizedPage = lazy(() => import('@/routes/unauthorized/page').then(m => ({default: m.UnauthorizedPage})))
export const ProtectedLayout = lazy(() => import('@/routes/_layout/protected-layout').then(m => ({default: m.ProtectedLayout})))
export const PublicadosPage = lazy(() => import('@/routes/ambientes/publicados/page').then(m => ({default: m.PublicadosPage})))
export const PublicadoDetalhePage = lazy(() => import('@/routes/ambientes/publicados/[id]/page').then(m => ({default: m.PublicadoDetalhePage})))
export const EsquadriasPage = lazy(() => import('@/routes/ambientes/publicados/esquadrias/page').then(m => ({default: m.EsquadriasPage})))
export const NaoPublicadosPage = lazy(() => import('@/routes/ambientes/nao-publicados/page').then(m => ({default: m.NaoPublicadosPage})))
export const NovoAmbientePage = lazy(() => import('@/routes/ambientes/nao-publicados/novo/page').then(m => ({default: m.NovoAmbientePage})))
export const NaoPublicadoDetalhePage = lazy(() => import('@/routes/ambientes/nao-publicados/[id]/page').then(m => ({default: m.NaoPublicadoDetalhePage})))
export const ValidacaoPage = lazy(() => import('@/routes/ambientes/validacao/page').then(m => ({default: m.ValidacaoPage})))
export const ValidacaoDetalhePage = lazy(() => import('@/routes/ambientes/validacao/[id]/page').then(m => ({default: m.ValidacaoDetalhePage})))
export const UsuariosPage = lazy(() => import('@/routes/usuarios/page').then(m => ({default: m.UsuariosPage})))
