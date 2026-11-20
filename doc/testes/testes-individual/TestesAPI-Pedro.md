# Scripts do Postman - Criar e Excluir Família

Para cada requisição: crie-a no Postman com o método, a URL e o body indicados, escolha a autenticação indicada na aba **Authorization**, e cole o código nas abas indicadas (**Scripts → Pre-request** e **Post-response**, ou **Tests** nas versões antigas).

Variáveis de environment usadas: `baseUrl`, `emailA`, `passwordA`, `emailB`, `passwordB`, `token`, `tokenB`, `familiaId`, `familiaAlheiaId`, `familiaSqlId`, `nomeFamilia`.

Valores iniciais: `baseUrl` = `http://localhost:8080/api`; os demais, exceto emails e senhas, começam vazios.


## 0. Setup (login)


### Login - Usuário A (será dono da família)

- **Método e URL:** `POST {{baseUrl}}/auth/login`
- **Authorization:** No Auth
- **Body (raw, JSON):**
```json
{
  "email": "{{emailA}}",
  "password": "{{passwordA}}"
}
```

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Resposta é um JSON válido", function () { pm.response.to.have.jsonBody(); });
pm.test("Resposta traz o campo token (login válido)", function () {
  var j = pm.response.json();
  pm.expect(j.token, "token ausente: confira email/senha no Environment").to.be.a("string").and.not.empty;
  pm.environment.set("token", j.token);
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### Login - Usuário B (comum, sem família)

- **Método e URL:** `POST {{baseUrl}}/auth/login`
- **Authorization:** No Auth
- **Body (raw, JSON):**
```json
{
  "email": "{{emailB}}",
  "password": "{{passwordB}}"
}
```

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Resposta é um JSON válido", function () { pm.response.to.have.jsonBody(); });
pm.test("Resposta traz o campo token (login válido)", function () {
  var j = pm.response.json();
  pm.expect(j.token, "token ausente: confira email/senha no Environment").to.be.a("string").and.not.empty;
  pm.environment.set("tokenB", j.token);
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

## 1. Criar Família (POST /familias)


### 1.1 Criar família com sucesso

- **Método e URL:** `POST {{baseUrl}}/familias`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "{{nomeFamilia}}",
  "foto": null
}
```

**Pre-request:**
```javascript
pm.environment.set("nomeFamilia", "Família Postman " + Date.now());
```

**Post-response / Tests:**
```javascript
// Prática 1 - Status code
pm.test("Status code é 201 Created", function () { pm.response.to.have.status(201); });
// Prática 3 - Content-Type
pm.test("Content-Type é application/json", function () {
  pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});
// Prática 4 - JSON válido
pm.test("Resposta é um JSON válido", function () { pm.response.to.have.jsonBody(); });
// Prática 2 - Campos obrigatórios
pm.test("Resposta contém campos obrigatórios (id, nome, foto, membros)", function () {
  var j = pm.response.json();
  pm.expect(j).to.have.property("id");
  pm.expect(j).to.have.property("nome");
  pm.expect(j).to.have.property("foto");
  pm.expect(j).to.have.property("membros");
});
// Prática 5 - Dados específicos (regra de negócio)
pm.test("id é número e nome é o enviado", function () {
  var j = pm.response.json();
  pm.expect(j.id).to.be.a("number");
  pm.expect(j.nome).to.eql(pm.environment.get("nomeFamilia"));
  pm.environment.set("familiaId", j.id);
});
pm.test("Criador entrou como único membro da família", function () {
  var j = pm.response.json();
  pm.expect(j.membros).to.be.an("array").with.lengthOf(1);
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 1.2 Consultar a família criada (GET /familias/{id})

- **Método e URL:** `GET {{baseUrl}}/familias/{{familiaId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("ID da família é o criado no passo 1.1", function () {
  pm.expect(pm.response.json().id).to.eql(Number(pm.environment.get("familiaId")));
});
pm.test("Nome persistido corresponde ao enviado", function () {
  pm.expect(pm.response.json().nome).to.eql(pm.environment.get("nomeFamilia"));
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 1.3 Listar famílias (paginação / array)

- **Método e URL:** `GET {{baseUrl}}/familias`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// Prática 9 - Lista
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Resposta é uma lista (Array)", function () { pm.expect(pm.response.json()).to.be.an("array"); });
pm.test("A família recém-criada aparece na lista", function () {
  var id = Number(pm.environment.get("familiaId"));
  pm.expect(pm.response.json().some(function (f) { return f.id === id; })).to.be.true;
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 1.4 Criar família SEM token (autenticação)

- **Método e URL:** `POST {{baseUrl}}/familias`
- **Authorization:** No Auth
- **Body (raw, JSON):**
```json
{
  "nome": "Família Sem Token",
  "foto": null
}
```

**Post-response / Tests:**
```javascript
// Prática 8 - Autenticação
pm.test("Sem token é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 1.5 Criar família com token inválido

- **Método e URL:** `POST {{baseUrl}}/familias`
- **Authorization:** No Auth
- **Header extra:** `Authorization: Bearer token_invalido_123`
- **Body (raw, JSON):**
```json
{
  "nome": "Família Token Inválido",
  "foto": null
}
```

**Post-response / Tests:**
```javascript
pm.test("Token inválido é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 1.6 Criar família com nome vazio/nulo (cenário de erro)

- **Método e URL:** `POST {{baseUrl}}/familias`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": null,
  "foto": null
}
```

**Post-response / Tests:**
```javascript
// Prática 7 - Erro. A coluna nome é NOT NULL; a API não deve quebrar com 500.
pm.test("Nome nulo NÃO deve retornar 201", function () {
  pm.expect(pm.response.code).to.not.eql(201);
});
pm.test("Nome nulo retorna erro de cliente (400 ou 409), não 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 409, 422]);
});
```

### 1.7 Criar família sem body

- **Método e URL:** `POST {{baseUrl}}/familias`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json

```

**Post-response / Tests:**
```javascript
pm.test("Body ausente retorna 400 (Bad Request)", function () {
  pm.response.to.have.status(400);
});
```

## 2. Excluir Família (DELETE /familias/{id})


### 2.1 Excluir SEM token (autenticação)

- **Método e URL:** `DELETE {{baseUrl}}/familias/{{familiaId}}`
- **Authorization:** No Auth

**Post-response / Tests:**
```javascript
pm.test("Sem token é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 2.2 Excluir com usuário comum (papel USUARIO, sem permissão)

- **Método e URL:** `DELETE {{baseUrl}}/familias/{{familiaId}}`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**
```javascript
// O endpoint exige ADMIN_FAMILIA ou ADMIN_SITE.
// ATENÇÃO: se vier 500 em vez de 403, é bug do ApiExceptionHandler (ver README).
pm.test("Usuário sem papel de admin recebe 403 Forbidden", function () {
  pm.response.to.have.status(403);
});
pm.test("Família continua existindo (nada foi apagado)", function () {
  pm.expect(pm.response.code).to.not.eql(204);
});
```

### 2.3 Excluir família inexistente (404)

- **Método e URL:** `DELETE {{baseUrl}}/familias/999999999`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// Prática 7 - Cenário 404
pm.test("Status code é 404 para família inexistente", function () {
  pm.response.to.have.status(404);
});
```

### 2.4 Excluir com ID inválido / SQL Injection no path

- **Método e URL:** `DELETE {{baseUrl}}/familias/1%20OR%201=1`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// Prática 10 - Segurança
pm.test("API não quebra com Injeção SQL no path (não retorna 500)", function () {
  pm.expect(pm.response.code).to.not.eql(500);
  pm.expect(pm.response.code).to.be.oneOf([400, 404]);
});
```

### 2.5 Preparo: usuário B cria uma família que NÃO é do usuário A

- **Método e URL:** `POST {{baseUrl}}/familias`
- **Authorization:** Bearer Token = {{tokenB}}
- **Body (raw, JSON):**
```json
{
  "nome": "Família Alheia Postman",
  "foto": null
}
```

**Post-response / Tests:**
```javascript
pm.test("Família alheia criada (201)", function () { pm.response.to.have.status(201); });
pm.environment.set("familiaAlheiaId", pm.response.json().id);
```

### 2.6 Admin de OUTRA família tenta excluir família alheia (autorização)

- **Método e URL:** `DELETE {{baseUrl}}/familias/{{familiaAlheiaId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// ACHADO: FamiliaService.excluirFamilia não confere se o solicitante pertence à família.
// Esperado (correto): 403. Se vier 204, é uma falha de autorização (IDOR).
pm.test("Admin de outra família NÃO pode excluir esta família (403)", function () {
  pm.response.to.have.status(403);
});
```

### 2.7 Limpeza: B exclui a própria família

- **Método e URL:** `DELETE {{baseUrl}}/familias/{{familiaAlheiaId}}`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**
```javascript
pm.test("Limpeza concluída (204) ou já removida (404)", function () {
  pm.expect(pm.response.code).to.be.oneOf([204, 404]);
});
```

### 2.8 Excluir família com sucesso (204)

- **Método e URL:** `DELETE {{baseUrl}}/familias/{{familiaId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
pm.test("Status code é 204 No Content", function () { pm.response.to.have.status(204); });
pm.test("Resposta não possui corpo", function () { pm.expect(pm.response.text()).to.be.empty; });
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 2.9 Confirmar que a família sumiu (GET → 404)

- **Método e URL:** `GET {{baseUrl}}/familias/{{familiaId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// ATENÇÃO: buscarFamiliaPorID lança RuntimeException → hoje cai no handler genérico (500).
pm.test("Família excluída não é mais encontrada (404)", function () {
  pm.response.to.have.status(404);
});
```

### 2.10 Família excluída não aparece em GET /familias

- **Método e URL:** `GET {{baseUrl}}/familias`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Lista não contém mais a família excluída", function () {
  var id = Number(pm.environment.get("familiaId"));
  pm.expect(pm.response.json().some(function (f) { return f.id === id; })).to.be.false;
});
```

### 2.11 Excluir de novo (idempotência → 404)

- **Método e URL:** `DELETE {{baseUrl}}/familias/{{familiaId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
pm.test("Segunda exclusão da mesma família retorna 404", function () {
  pm.response.to.have.status(404);
});
```

## 3. Segurança (SQL Injection no nome)


### 3.1 Criar família com SQL Injection no nome

- **Método e URL:** `POST {{baseUrl}}/familias`
- **Authorization:** Bearer Token = {{tokenB}}
- **Body (raw, JSON):**
```json
{
  "nome": "'; DROP TABLE familia; --",
  "foto": null
}
```

**Post-response / Tests:**
```javascript
// Prática 10 - Segurança
pm.test("API não quebra com Injeção SQL (não retorna 500)", function () {
  pm.expect(pm.response.code).to.not.eql(500);
  pm.expect(pm.response.code).to.be.oneOf([201, 400]);
});
pm.test("Se criou, o nome foi gravado como texto literal", function () {
  if (pm.response.code === 201) {
    pm.expect(pm.response.json().nome).to.eql("'; DROP TABLE familia; --");
    pm.environment.set("familiaSqlId", pm.response.json().id);
  }
});
```

### 3.2 Limpeza da família criada no teste de SQL Injection

- **Método e URL:** `DELETE {{baseUrl}}/familias/{{familiaSqlId}}`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**
```javascript
pm.test("Limpeza concluída (204) ou nada a limpar (404)", function () {
  pm.expect(pm.response.code).to.be.oneOf([204, 404]);
});
```
