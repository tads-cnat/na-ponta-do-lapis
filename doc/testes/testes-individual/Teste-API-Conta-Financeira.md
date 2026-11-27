# Scripts do Postman - Teste de API: Contas Financeiras

**Categoria:** Testes Automatizados | **Módulo:** Backend (Spring Boot) | **Tarefa:** Teste de API Conta

Endpoints (`ContaFinanceiraController`, prefixo `/contas`): `POST /contas`, `GET /contas`, `GET /contas/{id}`, `GET /contas/me`, `PUT /contas/{id}`, `PATCH /contas/{id}`, `DELETE /contas/{id}`.

Crie cada requisição no Postman com o método, URL, body e autenticação indicados (aba **Authorization**) e cole os scripts em **Pre-request** e **Post-response** (ou **Tests**). Execute **na ordem**: os passos dependem de variáveis gravadas pelos anteriores.

**Variáveis de environment:** `baseUrl` (`http://localhost:8080/api`), `emailA`, `passwordA`, `emailB`, `passwordB` (dois usuários descartáveis, papel `USUARIO`), `token`, `tokenB`, `userIdA`, `contaId`, `contaCartaoId`, `contaAlheiaId`, `nomeConta`, `nomeLongo` (as demais começam vazias).

**Regras:** `Moeda` = BRL/USD/EUR; `nome` ≤ 100; `cor` ≤ 7 caracteres; nome único por usuário; em cartão, `limiteDisponivel = limiteTotal − |saldo|`.

> Os testes verificam o comportamento **correto**. Os que baterem nos bugs listados em **Achados** (fim do arquivo) devem falhar; são hipóteses da leitura do código, a confirmar executando.

---

## 0. Setup

### 0.1 Login A e 0.2 Login B

- `POST {{baseUrl}}/auth/login` | No Auth
- Body A: `{ "email": "{{emailA}}", "password": "{{passwordA}}" }`
- Body B: `{ "email": "{{emailB}}", "password": "{{passwordB}}" }`

**Tests (A; em B troque `"token"` por `"tokenB"`):**

```javascript
pm.test("Status 200", function () { pm.response.to.have.status(200); });
pm.test("Traz token", function () {
  var j = pm.response.json();
  pm.expect(j.token).to.be.a("string").and.not.empty;
  pm.environment.set("token", j.token);
});
```

---

## 1. Criar conta (POST /contas)

### 1.1 Criar conta corrente (sucesso)

- Bearer `{{token}}`
- Body:

```json
{ "nome": "{{nomeConta}}", "saldo": 1500.50, "cor": "#FF5733", "moeda": "BRL" }
```

**Pre-request:**

```javascript
pm.environment.set("nomeConta", "Conta Postman " + Date.now());
pm.environment.set("nomeLongo", "A".repeat(101));
```

**Tests:**

```javascript
pm.test("Status 201", function () { pm.response.to.have.status(201); });
pm.test("Dados corretos e tipo padrão CONTA_CORRENTE", function () {
  var j = pm.response.json();
  pm.expect(j.nome).to.eql(pm.environment.get("nomeConta"));
  pm.expect(j.saldo).to.eql(1500.5);
  pm.expect(j.cor).to.eql("#FF5733");
  pm.expect(j.moeda).to.eql("BRL");
  pm.expect(j.tipoConta).to.eql("CONTA_CORRENTE");
  pm.environment.set("contaId", j.id);
  pm.environment.set("userIdA", j.usuario.id);
});
```

### 1.2 Criar cartão de crédito

- Bearer `{{token}}`
- Body:

```json
{ "nome": "Cartao Postman", "saldo": -250.00, "cor": "#8A05BE", "moeda": "BRL",
  "tipoConta": "CARTAO_CREDITO", "limiteTotal": 1000.00 }
```

**Tests:**

```javascript
pm.test("Status 201", function () { pm.response.to.have.status(201); });
pm.test("limiteDisponivel = 1000 - 250 = 750", function () {
  var j = pm.response.json();
  pm.expect(j.tipoConta).to.eql("CARTAO_CREDITO");
  pm.expect(j.limiteDisponivel).to.eql(750);
  pm.environment.set("contaCartaoId", j.id);
});
```

### 1.3 Sem token / token inválido

- `POST {{baseUrl}}/contas` | No Auth (na 2ª variação, header `Authorization: Bearer token_invalido_123`)
- Body: `{ "nome": "X", "saldo": 1, "cor": "#000000", "moeda": "BRL" }`

**Tests:**

```javascript
pm.test("Bloqueado (401 ou 403)", function () { pm.expect(pm.response.code).to.be.oneOf([401, 403]); });
```

### 1.4 Validações (uma requisição por body, mesmo teste)

- `POST {{baseUrl}}/contas` | Bearer `{{token}}`

| Caso | Body | Esperado |
| ---- | ---- | -------- |
| Nome vazio | `{ "nome": "", "saldo": 10, "cor": "#000000", "moeda": "BRL" }` | 400 |
| Saldo ausente | `{ "nome": "Y", "cor": "#000000", "moeda": "BRL" }` | 400 |
| Cor vazia | `{ "nome": "Y", "saldo": 10, "cor": "", "moeda": "BRL" }` | 400 |
| Moeda inválida | `{ "nome": "Y", "saldo": 10, "cor": "#000000", "moeda": "XYZ" }` | 400 (A4) |
| Sem body | *(vazio)* | 400 (A4) |
| Nome com 101 caracteres | `{ "nome": "{{nomeLongo}}", "saldo": 10, "cor": "#000000", "moeda": "BRL" }` | 400 (A7) |
| Cor com 8 caracteres | `{ "nome": "Y", "saldo": 10, "cor": "#FF57331", "moeda": "BRL" }` | 400 (A7) |
| Nome duplicado (mesmo usuário) | `{ "nome": "{{nomeConta}}", "saldo": 10, "cor": "#000000", "moeda": "BRL" }` | 400 (A5) |

**Tests:**

```javascript
pm.test("Entrada inválida retorna 400 (não 201 nem 500)", function () {
  pm.response.to.have.status(400);
});
```

### 1.5 Usuário B cria conta com o MESMO nome (permitido)

- Bearer `{{tokenB}}`
- Body: `{ "nome": "{{nomeConta}}", "saldo": 77.70, "cor": "#0000FF", "moeda": "USD" }`

**Tests:**

```javascript
pm.test("Status 201 (unicidade é por usuário)", function () { pm.response.to.have.status(201); });
pm.test("Guarda conta alheia", function () {
  var j = pm.response.json();
  pm.environment.set("contaAlheiaId", j.id);
  pm.expect(j.usuario.id).to.not.eql(Number(pm.environment.get("userIdA")));
});
```

---

## 2. Listar (GET /contas e /contas/me)

### 2.1 GET /contas

- Bearer `{{token}}`

**Tests:**

```javascript
pm.test("Status 200 e array", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json()).to.be.an("array");
});
pm.test("Contém a conta criada", function () {
  var id = Number(pm.environment.get("contaId"));
  pm.expect(pm.response.json().some(function (c) { return c.id === id; })).to.be.true;
});
pm.test("NÃO expõe conta de outro usuário (A2)", function () {
  var id = Number(pm.environment.get("contaAlheiaId"));
  pm.expect(pm.response.json().some(function (c) { return c.id === id; })).to.be.false;
});
```

### 2.2 GET /contas/me

- Bearer `{{token}}`

**Tests:**

```javascript
pm.test("Status 200", function () { pm.response.to.have.status(200); });
pm.test("Só contas do usuário A (inclui conta e cartão)", function () {
  var lista = pm.response.json(), uid = Number(pm.environment.get("userIdA"));
  lista.forEach(function (c) { pm.expect(c.usuario.id).to.eql(uid); });
  var ids = lista.map(function (c) { return c.id; });
  pm.expect(ids).to.include(Number(pm.environment.get("contaId")));
  pm.expect(ids).to.include(Number(pm.environment.get("contaCartaoId")));
});
```

### 2.3 Listagens sem token

- `GET {{baseUrl}}/contas` e `GET {{baseUrl}}/contas/me` | No Auth

**Tests:** mesmo teste da 1.3 (401 ou 403).

---

## 3. Buscar por ID (GET /contas/{id})

### 3.1 Buscar a própria conta

- `GET {{baseUrl}}/contas/{{contaId}}` | Bearer `{{token}}`

**Tests:**

```javascript
pm.test("Status 200 e dados da conta", function () {
  pm.response.to.have.status(200);
  var j = pm.response.json();
  pm.expect(j.id).to.eql(Number(pm.environment.get("contaId")));
  pm.expect(j.nome).to.eql(pm.environment.get("nomeConta"));
});
```

### 3.2 Casos de erro (mesma requisição, URLs diferentes)

- `GET` | Bearer `{{token}}`

| URL | Esperado |
| --- | -------- |
| `{{baseUrl}}/contas/999999999` | 404 (A3) |
| `{{baseUrl}}/contas/abc` | 400 (A4) |
| `{{baseUrl}}/contas/1%20OR%201=1` (SQL injection) | 400 ou 404, nunca 500/200 |
| `{{baseUrl}}/contas/{{contaAlheiaId}}` (conta de B) | 403 ou 404 (A1) |
| `{{baseUrl}}/contas/{{contaId}}` sem token | 401 ou 403 |

**Tests:**

```javascript
pm.test("Erro tratado (400, 401, 403 ou 404), nunca 500/200", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 401, 403, 404]);
});
```

---

## 4. Atualização parcial (PATCH /contas/{id})

> Base: `contaId`. Casos de erro antes dos de sucesso.

### 4.1 Erros

- `PATCH {{baseUrl}}/contas/{{contaId}}` | Bearer `{{token}}`

| Caso | Body | Esperado |
| ---- | ---- | -------- |
| Sem campos | `{}` | 400 |
| Nome vazio | `{ "nome": "   " }` | 400 |
| Nome 101 caracteres | `{ "nome": "{{nomeLongo}}" }` | 400 |
| Cor vazia | `{ "cor": "" }` | 400 |
| Cor 8 caracteres | `{ "cor": "#1122334" }` | 400 |
| Nome duplicado (usa a do cartão) | `{ "nome": "Cartao Postman" }` | 400 (A5) |

**Tests:**

```javascript
pm.test("Entrada inválida retorna 400", function () { pm.response.to.have.status(400); });
```

### 4.2 Conta inexistente, sem token e conta de outro usuário

| Requisição | Esperado |
| ---------- | -------- |
| `PATCH {{baseUrl}}/contas/999999999` (Bearer A) | 404 (A3) |
| `PATCH {{baseUrl}}/contas/{{contaId}}` (No Auth) | 401 ou 403 |
| `PATCH {{baseUrl}}/contas/{{contaAlheiaId}}` (Bearer A) | 403 ou 404 (A1) |

- Body: `{ "nome": "Teste" }`

**Tests:**

```javascript
pm.test("Bloqueado ou não encontrado (401, 403 ou 404)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403, 404]);
});
```

### 4.3 PATCH parcial com sucesso (preserva os demais campos)

- `PATCH {{baseUrl}}/contas/{{contaId}}` | Bearer `{{token}}`
- Body: `{ "saldo": 450.75 }`

**Tests:**

```javascript
pm.test("Status 200, saldo alterado e nome preservado", function () {
  pm.response.to.have.status(200);
  var j = pm.response.json();
  pm.expect(j.saldo).to.eql(450.75);
  pm.expect(j.nome).to.eql(pm.environment.get("nomeConta"));
  pm.expect(j.cor).to.eql("#FF5733");
});
```

### 4.4 PATCH só com moeda

- Mesma URL, Body: `{ "moeda": "EUR" }`

**Tests:**

```javascript
// A6: o validator ignora "moeda" ao checar se há algum campo
pm.test("Status 200 e moeda EUR", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json().moeda).to.eql("EUR");
});
```

---

## 5. Atualização completa (PUT /contas/{id})

### 5.1 Erros

- `PUT` | Bearer `{{token}}` | body base: `{ "nome": "Z", "saldo": 1, "cor": "#000000", "moeda": "BRL" }`

| Caso | Requisição | Esperado |
| ---- | ---------- | -------- |
| Sem token | `/contas/{{contaId}}` (No Auth) | 401 ou 403 |
| Inexistente | `/contas/999999999` | 404 (A3) |
| Nome vazio | `/contas/{{contaId}}` com `"nome": ""` | 400 |
| Saldo ausente | `/contas/{{contaId}}` sem `saldo` | 400 |
| Conta de outro usuário | `/contas/{{contaAlheiaId}}` | 403 ou 404 (A1) |

**Tests:**

```javascript
pm.test("Erro tratado (400, 401, 403 ou 404)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 401, 403, 404]);
});
```

### 5.2 Atualizar com sucesso

- `PUT {{baseUrl}}/contas/{{contaId}}` | Bearer `{{token}}`
- Body: `{ "nome": "{{nomeConta}} editada", "saldo": 2000.00, "cor": "#00AA55", "moeda": "USD" }`

**Tests:**

```javascript
pm.test("Status 200 e campos atualizados", function () {
  pm.response.to.have.status(200);
  var j = pm.response.json();
  pm.expect(j.nome).to.eql(pm.environment.get("nomeConta") + " editada");
  pm.expect(j.saldo).to.eql(2000);
  pm.expect(j.cor).to.eql("#00AA55");
  pm.expect(j.moeda).to.eql("USD");
});
```

### 5.3 Cartão: recalcula limiteDisponivel

- `PUT {{baseUrl}}/contas/{{contaCartaoId}}` | Bearer `{{token}}`
- Body: `{ "nome": "Cartao Postman", "saldo": -300.00, "cor": "#8A05BE", "moeda": "BRL", "tipoConta": "CARTAO_CREDITO", "limiteTotal": 2000.00 }`

**Tests:**

```javascript
pm.test("limiteDisponivel = 2000 - 300 = 1700", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json().limiteDisponivel).to.eql(1700);
});
```

---

## 6. Excluir (DELETE /contas/{id})

### 6.1 Erros

- `DELETE` | Bearer `{{token}}`

| URL | Esperado |
| --- | -------- |
| `{{baseUrl}}/contas/{{contaId}}` (No Auth) | 401 ou 403 |
| `{{baseUrl}}/contas/999999999` | 404 (A3) |
| `{{baseUrl}}/contas/1%20OR%201=1` | 400 ou 404 (A4) |
| `{{baseUrl}}/contas/{{contaAlheiaId}}` (conta de B) | 403 ou 404 (A1) |

**Tests:**

```javascript
pm.test("Bloqueado ou não encontrado (400, 401, 403 ou 404)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 401, 403, 404]);
});
```

### 6.2 Excluir com sucesso

- `DELETE {{baseUrl}}/contas/{{contaId}}` | Bearer `{{token}}`

**Tests:**

```javascript
pm.test("Status 204 sem corpo", function () {
  pm.response.to.have.status(204);
  pm.expect(pm.response.text()).to.be.empty;
});
```

### 6.3 Conta sumiu e exclusão é idempotente

| Requisição | Esperado |
| ---------- | -------- |
| `GET {{baseUrl}}/contas/{{contaId}}` | 404 (A3) |
| `DELETE {{baseUrl}}/contas/{{contaId}}` | 404 (A3) |

**Tests:**

```javascript
pm.test("Conta excluída retorna 404", function () { pm.response.to.have.status(404); });
```

---

## 7. Segurança e limpeza

### 7.1 SQL Injection no nome

- `POST {{baseUrl}}/contas` | Bearer `{{token}}`
- Body: `{ "nome": "'; DROP TABLE conta_financeira; --", "saldo": 1, "cor": "#000000", "moeda": "BRL" }`

**Tests:**

```javascript
pm.test("Não quebra (201 ou 400) e grava como texto literal", function () {
  pm.expect(pm.response.code).to.be.oneOf([201, 400]);
  if (pm.response.code === 201) {
    pm.expect(pm.response.json().nome).to.eql("'; DROP TABLE conta_financeira; --");
    pm.environment.set("contaSqlId", pm.response.json().id);
  }
});
```

### 7.2 Limpeza

`DELETE` em `{{contaCartaoId}}` e `{{contaSqlId}}` (Bearer `{{token}}`) e em `{{contaAlheiaId}}` (Bearer `{{tokenB}}`).

**Tests:**

```javascript
pm.test("Limpeza concluída (204) ou já removida (404)", function () {
  pm.expect(pm.response.code).to.be.oneOf([204, 404]);
});
```

---

## Achados (hipóteses a confirmar na execução)

| ID | Achado | Onde |
| -- | ------ | ---- |
| A1 | **IDOR:** GET por id, PUT, PATCH e DELETE só checam se a conta existe, não se é do usuário logado. | `ContaFinanceiraService` |
| A2 | **Vazamento:** `GET /contas` usa `findAll()` e devolve contas de todos os usuários. | `ContaFinanceiraService.listarContas` |
| A3 | **404 vira 500:** `ContaIdNaoExisteException` é capturada pelo handler genérico `Exception.class`. | `ApiExceptionHandler` |
| A4 | **Requisição malformada vira 500:** body ausente, enum inválido e id não numérico não têm handler. | `ApiExceptionHandler` |
| A5 | **Duplicidade mal tratada:** o `catch` testa a classe de exceção errada; provável 409 com mensagem de "excluir". | `ContaFinanceiraService` |
| A6 | **PATCH só com `moeda` é rejeitado:** o validator não considera esse campo. | `ContaFinanceiraPatchValidator` |
| A7 | **Sem `@Size` no DTO:** nome > 100 e cor > 7 só falham na entidade/banco (500). | `ContaFinanceiraDTO` |

Fora de escopo: contas sincronizadas via Pluggy e exclusão de conta com transações vinculadas.
