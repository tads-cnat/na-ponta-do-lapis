# Frontend — Na Ponta do Lápis (Angular)

Interface web Single Page Application (SPA) desenvolvida com **Angular 21**, **PrimeNG**, **Tailwind CSS** e **Chart.js**, focada em usabilidade, design moderno e controle financeiro intuitivo.

---

## 📋 Pré-requisitos

- **Node.js 22+** instalado (`node -v`).
- **npm 11+** (`npm -v`).
- *(Opcional)* Angular CLI global: `npm install -g @angular/cli@21`.

---

## 🚀 Como Executar

Acesse a pasta do projeto frontend:

```bash
cd frontend/na-ponta-do-lapis
```

### 1. Instalar as dependências

Necessário na primeira execução ou após alterações no `package.json`:

```bash
npm install
```
*(ou `npm ci` para uma instalação estrita baseada no `package-lock.json`)*

### 2. Iniciar o servidor de desenvolvimento

```bash
npm start
```

O servidor iniciará com escuta em `0.0.0.0` e abrirá a aplicação em seu navegador padrão:  
🌐 [http://localhost:4200](http://localhost:4200)

---

## ⚡ Desenvolvimento Paralelo (Front + Back)

Para rodar frontend e backend simultaneamente com recarregamento automático (hot-reload):
- Execute diretamente na **raiz do repositório**:
  ```bash
  npm run install:all   # Instala dependências do front e back (necessário na 1ª vez)
  npm run dev           # Inicia ambos os servidores
  ```
- Ou no **VS Code**, pressione `Ctrl+Shift+B` para executar a task **Start All (Dev)** com terminais dedicados.

---

## 🌐 Comunicação com a API

A URL base da API é gerenciada dinamicamente pelos arquivos de ambiente em `src/environments/`:

- **Desenvolvimento (`environment.development.ts`):**  
  Detecta se a execução ocorre em localhost (`http://localhost:8080/api/v1`) ou em ambiente GitHub Codespaces (`https://...-8080.app.github.dev/api/v1`).
- **Produção (`environment.ts`):**  
  Utiliza a rota relativa `/api`, sendo roteada pelo Nginx diretamente para o container do backend.

---

## 📜 Scripts do Frontend (`package.json`)

| Script | Descrição |
|---|---|
| `npm start` | Inicia o servidor Angular na porta `4200` acessível pela rede local |
| `npm run build` | Compila o projeto otimizado para produção na pasta `dist/` |
| `npm run watch` | Compila em modo desenvolvimento observando mudanças de arquivos |
| `npm test` | Executa a suíte de testes unitários com Vitest |

---

## 🧪 Testes Automatizados

Para executar os testes unitários da aplicação com Vitest:

```bash
npm test
```

---

## 🐳 Build da Imagem Docker

A aplicação utiliza um `Dockerfile` multi-stage: o build do Angular é feito em Node.js e os arquivos estáticos compilados são servidos via **Nginx** com proxy reverso embutido:

```bash
# Compilar a imagem
docker build -t npl2026/frontend:local .

# Executar o container localmente na porta 8081
docker run -p 8081:80 npl2026/frontend:local
```

Acesse em: [http://localhost:8081](http://localhost:8081).
