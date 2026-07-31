import './styles/globals.css'
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import {AuthProvider} from '@/components/auth/AuthProvider'
import {QueryClient, QueryClientProvider} from '@tanstack/react-query'

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 30_000,
            retry: 1,
            refetchOnWindowFocus: false,
        }
    }
})

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <App/>
            </AuthProvider>
        </QueryClientProvider>
    </React.StrictMode>,
)