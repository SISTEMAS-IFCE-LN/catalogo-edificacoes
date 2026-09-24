# =============================================================================
# Estágio 1 — build da SPA (Vite + React)
# =============================================================================
FROM node:26.8.2-alpine3.24 AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Recebe a origem do backend do build arg (interpolado de BACKEND_URL no .env
# raiz pelo docker-compose). Vazio = mesma origem: o Nginx do estágio 2 faz o
# proxy de /api, /auth, /oauth2 e /login/oauth2 para o serviço `api`.
ARG VITE_BACKEND_URL=''

RUN npm run build

# =============================================================================
# Estágio 2 — runtime (Nginx servindo dist/, usuário não-root)
# =============================================================================
FROM nginx:1.30.4-alpine3.24-slim AS release

COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/frontend.conf /etc/nginx/conf.d/default.conf
COPY nginx/security-headers.conf /etc/nginx/security-headers.conf
COPY --from=build /app/dist /var/www/catalogo-frontend/dist

# Usuário não-root: cache de arquivos temporários e conf.d precisam ser graváveis
RUN chown -R nginx:nginx /var/cache/nginx /etc/nginx/conf.d

USER nginx

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget -qO- http://localhost:8000/health-frontend || exit 1
