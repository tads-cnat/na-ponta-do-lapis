# Backend — Na Ponta do Lápis (Spring Boot)

API RESTful construída com **Java 21** e **Spring Boot 3.5**, responsável pela lógica de negócio, autenticação JWT, integração com IA (Google Gemini), upload de comprovantes (Cloudinary) e persistência de dados.

---

## 📋 Pré-requisitos

- **Java JDK 21** instalado e configurado no `PATH` (`java -version`).
- **Maven 3.9+** *(opcional, o wrapper `./mvnw` incluso no projeto já supre a necessidade)*.
- **PostgreSQL 16+** *(opcional, é possível rodar usando banco em memória H2)*.

---

## ⚙️ Configuração de Ambiente

Na pasta `backend/na-ponta-do-lapis/`, copie o arquivo de variáveis de ambiente:

```bash
cp .env.example .env
```

Principais variáveis:
- `DB_URL`, `DB_USER`, `DB_PASSWORD`: Credenciais do banco de dados PostgreSQL.
- `JWT_SECRET`: Chave secreta de no mínimo 32 caracteres (recomendado 64+) para assinatura dos tokens JWT.
- `CORS_ALLOWED_ORIGINS`: Origens autorizadas para consumo da API (padrão local: `http://localhost:4200`).
- `CLOUDINARY_*`: Credenciais para upload de imagens de comprovantes e perfis.
- `GEMINI_API_KEY`: Chave da API do Google Gemini para categorização inteligente de gastos.

---

## 🚀 Como Executar

Acesse o diretório do backend:

```bash
cd backend/na-ponta-do-lapis
```

Antes do primeiro uso no Linux/macOS, garanta permissão de execução ao wrapper:
```bash
chmod +x mvnw
```

### Opção 1: Com Banco PostgreSQL (Padrão)

Certifique-se de que o PostgreSQL está rodando (localmente ou via Docker) na porta `5432` com o banco `npl_db`:

```bash
./mvnw spring-boot:run
```

### Opção 2: Com Banco em Memória H2 (Sem instalar Postgres)

Para testar ou desenvolver rapidamente sem depender de um banco PostgreSQL instalado:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

O console do H2 estará disponível em: [http://localhost:8080/api/h2-console](http://localhost:8080/api/h2-console)  
- **JDBC URL:** `jdbc:h2:mem:npl_db`  
- **User:** `sa`  
- **Password:** *(vazio)*

---

## 🧪 Testes Automatizados

Para rodar os testes unitários e de integração:

```bash
# Executa apenas a suíte de testes
./mvnw test

# Executa testes e valida todo o ciclo de build
./mvnw verify --no-transfer-progress
```

---

## 📦 Gerando o Pacote JAR

Para gerar o arquivo executável otimizado para produção:

```bash
./mvnw clean package -DskipTests --no-transfer-progress
```

O arquivo gerado estará em `target/na-ponta-do-lapis-0.0.1-SNAPSHOT.jar` e pode ser executado com:

```bash
java -jar target/na-ponta-do-lapis-0.0.1-SNAPSHOT.jar
```

---

## 🐳 Build da Imagem Docker

Para construir a imagem Docker do backend localmente:

```bash
docker build -t npl2026/backend:local .
```

E para rodar:
```bash
docker run -p 8080:8080 --env-file .env npl2026/backend:local
```

---

## 📌 Endpoints Principais

A API responde sob o context-path `/api`:

| Endpoint | Descrição |
|---|---|
| `GET /api` | Redirecionamento automático para a documentação ou Web App |
| `GET /api/docs` | Documentação interativa Swagger UI |
| `GET /api/api-docs` | Especificação OpenAPI 3 em formato JSON |
| `GET /api/actuator/health` | Health Check da aplicação (Liveness & Readiness) |
| `/api/v1/auth/**` | Rotas públicas de autenticação (Login, Registro, Refresh) |
| `/api/v1/**` | Rotas autenticadas da aplicação (Transações, Metas, Contas, etc.) |
