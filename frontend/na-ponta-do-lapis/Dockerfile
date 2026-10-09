# Etapa de build e preparação
FROM node:22-alpine AS builder

WORKDIR /app

# Copia os arquivos de depedencias
COPY package*.json ./

# Instala as depedencias
RUN npm ci

# Copia o restante dos arquivos
COPY . .

# Builda o projeto
RUN npm run build

# Etapa de Configuração de ambiente
FROM nginx:alpine

# Copia o arquivo de configuração do Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Copia o aplicação apos a compição da outra imagem 
COPY --from=builder /app/dist/na-ponta-do-lapis/browser /usr/share/nginx/html

# Documenta que a porta 80 está em uso
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80 || exit 1

# Inicia o Nginx
CMD ["nginx", "-g", "daemon off;"]
