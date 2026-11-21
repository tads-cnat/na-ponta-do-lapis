# Scripts do Postman - Criar, Editar e Excluir Marcador

Para cada requisição: crie-a no Postman com o método, a URL e o body indicados, escolha a autenticação indicada na aba **Authorization**, e cole o código nas abas indicadas (**Scripts → Pre-request** e **Post-response**, ou **Tests** nas versões antigas).

Variáveis de environment usadas: `baseUrl`, `emailA`, `passwordA`, `emailB`, `passwordB`, `token`, `tokenB`, `marcadorId`, `marcadorAlheioId`, `marcadorSqlId`, `nomeMarcador`.

Valores iniciais: `baseUrl` = `http://localhost:8080/api/v1`; `emailA` = `lucas@gmail.com`, `emailB` = `bruno@gmail.com`, `passwordA` = `passwordB` = `123456` (usuários do DatabaseSeeder, ambos com papel USUARIO); os demais começam vazios.

> O `baseUrl` termina em `/api/v1` porque o `WebConfig` do backend adiciona o prefixo `/v1` a todos os controllers. O recurso é `/api/v1/marcadores`.


## 0. Setup (login)


### Login - Usuário A (será dono do marcador)

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

### Login - Usuário B (outro usuário comum)

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

## 1. Criar Marcador (POST /marcadores)


### 1.1 Criar marcador com sucesso

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "{{nomeMarcador}}",
  "cor": "#FF0000"
}
```

**Pre-request:**
```javascript
pm.environment.set("nomeMarcador", "Marcador Postman " + Date.now());
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
pm.test("Resposta contém campos obrigatórios (id, nome, cor)", function () {
  var j = pm.response.json();
  pm.expect(j).to.have.property("id");
  pm.expect(j).to.have.property("nome");
  pm.expect(j).to.have.property("cor");
});
// Prática 5 - Dados específicos (regra de negócio)
pm.test("id é número, nome e cor são os enviados", function () {
  var j = pm.response.json();
  pm.expect(j.id).to.be.a("number");
  pm.expect(j.nome).to.eql(pm.environment.get("nomeMarcador"));
  pm.expect(j.cor).to.eql("#FF0000");
  pm.environment.set("marcadorId", j.id);
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 1.2 Listar os marcadores do usuário logado (GET /marcadores/me)

- **Método e URL:** `GET {{baseUrl}}/marcadores/me`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// Prática 9 - Lista
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Resposta é uma lista (Array)", function () { pm.expect(pm.response.json()).to.be.an("array"); });
pm.test("O marcador recém-criado aparece na lista com os dados corretos", function () {
  var id = Number(pm.environment.get("marcadorId"));
  var m = pm.response.json().find(function (x) { return x.id === id; });
  pm.expect(m, "marcador criado no passo 1.1 não está na lista").to.exist;
  pm.expect(m.nome).to.eql(pm.environment.get("nomeMarcador"));
  pm.expect(m.cor).to.eql("#FF0000");
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 1.3 Outro usuário NÃO enxerga o marcador (isolamento)

- **Método e URL:** `GET {{baseUrl}}/marcadores/me`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Lista do usuário B não contém o marcador do usuário A", function () {
  var id = Number(pm.environment.get("marcadorId"));
  pm.expect(pm.response.json().some(function (m) { return m.id === id; })).to.be.false;
});
```

### 1.4 Criar marcador SEM token (autenticação)

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** No Auth
- **Body (raw, JSON):**
```json
{
  "nome": "Marcador Sem Token",
  "cor": "#FF0000"
}
```

**Post-response / Tests:**
```javascript
// Prática 8 - Autenticação
pm.test("Sem token é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 1.5 Criar marcador com token inválido

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** No Auth
- **Header extra:** `Authorization: Bearer token_invalido_123`
- **Body (raw, JSON):**
```json
{
  "nome": "Marcador Token Inválido",
  "cor": "#FF0000"
}
```

**Post-response / Tests:**
```javascript
pm.test("Token inválido é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 1.6 Criar marcador com nome vazio (cenário de erro)

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "",
  "cor": "#FF0000"
}
```

**Post-response / Tests:**
```javascript
// Prática 7 - Erro. @NotBlank no nome; a API deve responder 400 com a mensagem do campo.
pm.test("Nome vazio NÃO deve retornar 201", function () {
  pm.expect(pm.response.code).to.not.eql(201);
});
pm.test("Nome vazio retorna 400 Bad Request", function () {
  pm.response.to.have.status(400);
});
pm.test("Mensagem de validação do campo nome", function () {
  pm.expect(pm.response.json().nome).to.eql("O nome é obrigatório.");
});
```

### 1.7 Criar marcador com nome acima do limite (71 caracteres)

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "cor": "#FF0000"
}
```

**Post-response / Tests:**
```javascript
// O nome tem 71 caracteres; o limite é 70.
pm.test("Nome com 71 caracteres retorna 400", function () {
  pm.response.to.have.status(400);
});
pm.test("Mensagem de limite de tamanho do nome", function () {
  pm.expect(pm.response.json().nome).to.eql("O nome deve ter no máximo 70 caracteres.");
});
```

### 1.8 Criar marcador com cor inválida (texto)

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "Marcador Cor Inválida",
  "cor": "azul"
}
```

**Post-response / Tests:**
```javascript
pm.test("Cor fora do formato HEX retorna 400", function () {
  pm.response.to.have.status(400);
});
pm.test("Mensagem de validação do campo cor", function () {
  pm.expect(pm.response.json().cor).to.eql("A cor deve ser um HEX válido ex: #FF0000.");
});
```

### 1.9 Criar marcador com cor sem o caractere #

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "Marcador Cor Sem Hash",
  "cor": "FF0000"
}
```

**Post-response / Tests:**
```javascript
pm.test("Cor sem # retorna 400", function () {
  pm.response.to.have.status(400);
});
pm.test("Mensagem de validação do campo cor", function () {
  pm.expect(pm.response.json().cor).to.eql("A cor deve ser um HEX válido ex: #FF0000.");
});
```

### 1.10 Criar marcador com body vazio ({})

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{}
```

**Post-response / Tests:**
```javascript
pm.test("Body sem campos retorna 400", function () {
  pm.response.to.have.status(400);
});
pm.test("Resposta aponta nome e cor como obrigatórios", function () {
  var j = pm.response.json();
  pm.expect(j).to.have.property("nome");
  pm.expect(j).to.have.property("cor");
});
```

### 1.11 Criar marcador sem body

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json

```

**Post-response / Tests:**
```javascript
// ATENÇÃO: body ausente lança HttpMessageNotReadableException → hoje cai no handler genérico (500).
pm.test("Body ausente retorna 400 (Bad Request)", function () {
  pm.response.to.have.status(400);
});
```

## 2. Editar Marcador (PUT /marcadores/{id})


### 2.1 Editar SEM token (autenticação)

- **Método e URL:** `PUT {{baseUrl}}/marcadores/{{marcadorId}}`
- **Authorization:** No Auth
- **Body (raw, JSON):**
```json
{
  "nome": "Tentativa Sem Token",
  "cor": "#0000FF"
}
```

**Post-response / Tests:**
```javascript
pm.test("Sem token é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 2.2 Editar marcador com sucesso

- **Método e URL:** `PUT {{baseUrl}}/marcadores/{{marcadorId}}`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "{{nomeMarcador}}",
  "cor": "#00FF00"
}
```

**Pre-request:**
```javascript
pm.environment.set("nomeMarcador", "Marcador Editado " + Date.now());
```

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Content-Type é application/json", function () {
  pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});
pm.test("Resposta contém campos obrigatórios (id, nome, cor)", function () {
  var j = pm.response.json();
  pm.expect(j).to.have.property("id");
  pm.expect(j).to.have.property("nome");
  pm.expect(j).to.have.property("cor");
});
pm.test("Mesmo id, com nome e cor atualizados", function () {
  var j = pm.response.json();
  pm.expect(j.id).to.eql(Number(pm.environment.get("marcadorId")));
  pm.expect(j.nome).to.eql(pm.environment.get("nomeMarcador"));
  pm.expect(j.cor).to.eql("#00FF00");
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 2.3 Confirmar que a edição foi persistida (GET /marcadores/me)

- **Método e URL:** `GET {{baseUrl}}/marcadores/me`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Listagem traz o nome e a cor novos", function () {
  var id = Number(pm.environment.get("marcadorId"));
  var m = pm.response.json().find(function (x) { return x.id === id; });
  pm.expect(m).to.exist;
  pm.expect(m.nome).to.eql(pm.environment.get("nomeMarcador"));
  pm.expect(m.cor).to.eql("#00FF00");
});
```

### 2.4 Editar com cor inválida (cenário de erro)

- **Método e URL:** `PUT {{baseUrl}}/marcadores/{{marcadorId}}`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "Não deve gravar",
  "cor": "azul"
}
```

**Post-response / Tests:**
```javascript
pm.test("Cor inválida retorna 400", function () {
  pm.response.to.have.status(400);
});
pm.test("Mensagem de validação do campo cor", function () {
  pm.expect(pm.response.json().cor).to.eql("A cor deve ser um HEX válido ex: #FF0000.");
});
```

### 2.5 Editar com nome vazio (cenário de erro)

- **Método e URL:** `PUT {{baseUrl}}/marcadores/{{marcadorId}}`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "",
  "cor": "#00FF00"
}
```

**Post-response / Tests:**
```javascript
pm.test("Nome vazio retorna 400", function () {
  pm.response.to.have.status(400);
});
pm.test("Mensagem de validação do campo nome", function () {
  pm.expect(pm.response.json().nome).to.eql("O nome é obrigatório.");
});
```

### 2.6 Editar marcador inexistente (404)

- **Método e URL:** `PUT {{baseUrl}}/marcadores/999999999`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "Não existe",
  "cor": "#FF0000"
}
```

**Post-response / Tests:**
```javascript
// Prática 7 - Cenário 404
// ATENÇÃO: o service lança ResponseStatusException(404), mas o handler genérico do
// ApiExceptionHandler hoje converte em 500. Se vier 500, é bug do handler (ver README).
pm.test("Status code é 404 para marcador inexistente", function () {
  pm.response.to.have.status(404);
});
```

### 2.7 Editar com ID inválido / SQL Injection no path

- **Método e URL:** `PUT {{baseUrl}}/marcadores/1%20OR%201=1`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "Injeção no path",
  "cor": "#FF0000"
}
```

**Post-response / Tests:**
```javascript
// Prática 10 - Segurança
// ATENÇÃO: o id não é numérico (erro de conversão do path); sem handler específico cai no 500.
pm.test("API não quebra com Injeção SQL no path (não retorna 500)", function () {
  pm.expect(pm.response.code).to.not.eql(500);
  pm.expect(pm.response.code).to.be.oneOf([400, 404]);
});
```

### 2.8 Preparo: usuário B cria um marcador que NÃO é do usuário A

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{tokenB}}
- **Body (raw, JSON):**
```json
{
  "nome": "Marcador Alheio Postman",
  "cor": "#123456"
}
```

**Post-response / Tests:**
```javascript
pm.test("Marcador alheio criado (201)", function () { pm.response.to.have.status(201); });
pm.environment.set("marcadorAlheioId", pm.response.json().id);
```

### 2.9 Usuário A tenta editar marcador do usuário B (autorização)

- **Método e URL:** `PUT {{baseUrl}}/marcadores/{{marcadorAlheioId}}`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**
```json
{
  "nome": "Marcador Invadido",
  "cor": "#FFFFFF"
}
```

**Post-response / Tests:**
```javascript
// O service confere o dono do marcador e lança ResponseStatusException(403).
// ATENÇÃO: se vier 500 em vez de 403, é bug do ApiExceptionHandler (ver README).
pm.test("Usuário A NÃO pode editar marcador do usuário B (403)", function () {
  pm.response.to.have.status(403);
});
```

### 2.10 Confirmar que o marcador do usuário B não foi alterado

- **Método e URL:** `GET {{baseUrl}}/marcadores/me`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Marcador do usuário B continua com os dados originais", function () {
  var id = Number(pm.environment.get("marcadorAlheioId"));
  var m = pm.response.json().find(function (x) { return x.id === id; });
  pm.expect(m).to.exist;
  pm.expect(m.nome).to.eql("Marcador Alheio Postman");
  pm.expect(m.cor).to.eql("#123456");
});
```

## 3. Excluir Marcador (DELETE /marcadores/{id})


### 3.1 Excluir SEM token (autenticação)

- **Método e URL:** `DELETE {{baseUrl}}/marcadores/{{marcadorId}}`
- **Authorization:** No Auth

**Post-response / Tests:**
```javascript
pm.test("Sem token é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 3.2 Usuário A tenta excluir marcador do usuário B (autorização)

- **Método e URL:** `DELETE {{baseUrl}}/marcadores/{{marcadorAlheioId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// ATENÇÃO: se vier 500 em vez de 403, é bug do ApiExceptionHandler (ver README).
pm.test("Usuário A NÃO pode excluir marcador do usuário B (403)", function () {
  pm.response.to.have.status(403);
});
pm.test("Nada foi apagado (não retornou 204)", function () {
  pm.expect(pm.response.code).to.not.eql(204);
});
```

### 3.3 Excluir marcador inexistente (404)

- **Método e URL:** `DELETE {{baseUrl}}/marcadores/999999999`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// Prática 7 - Cenário 404
pm.test("Status code é 404 para marcador inexistente", function () {
  pm.response.to.have.status(404);
});
```

### 3.4 Excluir com ID inválido / SQL Injection no path

- **Método e URL:** `DELETE {{baseUrl}}/marcadores/1%20OR%201=1`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
// Prática 10 - Segurança
pm.test("API não quebra com Injeção SQL no path (não retorna 500)", function () {
  pm.expect(pm.response.code).to.not.eql(500);
  pm.expect(pm.response.code).to.be.oneOf([400, 404]);
});
```

### 3.5 Limpeza: B exclui o próprio marcador

- **Método e URL:** `DELETE {{baseUrl}}/marcadores/{{marcadorAlheioId}}`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**
```javascript
pm.test("Limpeza concluída (204) ou já removido (404)", function () {
  pm.expect(pm.response.code).to.be.oneOf([204, 404]);
});
```

### 3.6 Excluir marcador com sucesso (204)

- **Método e URL:** `DELETE {{baseUrl}}/marcadores/{{marcadorId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
pm.test("Status code é 204 No Content", function () { pm.response.to.have.status(204); });
pm.test("Resposta não possui corpo", function () { pm.expect(pm.response.text()).to.be.empty; });
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

### 3.7 Marcador excluído não aparece em GET /marcadores/me

- **Método e URL:** `GET {{baseUrl}}/marcadores/me`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Lista não contém mais o marcador excluído", function () {
  var id = Number(pm.environment.get("marcadorId"));
  pm.expect(pm.response.json().some(function (m) { return m.id === id; })).to.be.false;
});
```

### 3.8 Excluir de novo (idempotência → 404)

- **Método e URL:** `DELETE {{baseUrl}}/marcadores/{{marcadorId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**
```javascript
pm.test("Segunda exclusão do mesmo marcador retorna 404", function () {
  pm.response.to.have.status(404);
});
```

## 4. Segurança (SQL Injection no nome)


### 4.1 Criar marcador com SQL Injection no nome

- **Método e URL:** `POST {{baseUrl}}/marcadores`
- **Authorization:** Bearer Token = {{tokenB}}
- **Body (raw, JSON):**
```json
{
  "nome": "'; DROP TABLE marcador; --",
  "cor": "#FF0000"
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
    pm.expect(pm.response.json().nome).to.eql("'; DROP TABLE marcador; --");
    pm.environment.set("marcadorSqlId", pm.response.json().id);
  }
});
```

### 4.2 Limpeza do marcador criado no teste de SQL Injection

- **Método e URL:** `DELETE {{baseUrl}}/marcadores/{{marcadorSqlId}}`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**
```javascript
pm.test("Limpeza concluída (204) ou nada a limpar (404)", function () {
  pm.expect(pm.response.code).to.be.oneOf([204, 404]);
});
```