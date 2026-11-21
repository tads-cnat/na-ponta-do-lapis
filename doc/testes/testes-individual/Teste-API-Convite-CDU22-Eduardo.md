# Scripts do Postman - CDU 22: Convidar Membro à Família

Endpoints testados: `POST {{baseUrl}}/convites` (`ConviteController.enviarConvite` → `ConviteService.enviarConvite`), com apoio de `GET /convites/me`, `PATCH /convites/{id}?status=` e `POST /familias`.

Para cada requisição: crie-a no Postman com o método, a URL e o body indicados, escolha a autenticação indicada na aba **Authorization**, e cole o código nas abas indicadas (**Scripts → Pre-request** e **Post-response**, ou **Tests** nas versões antigas).

Variáveis de environment usadas: `baseUrl`, `emailA`, `passwordA`, `emailB`, `passwordB`, `token`, `tokenB`, `usernameA`, `usernameB`, `emailBReal`, `familiaId`, `conviteId`, `usernameInexistente`, `emailInexistente`, `nomeFamilia`.

Valores iniciais: `baseUrl` = `http://localhost:8080/api`; `emailA`/`passwordA`/`emailB`/`passwordB` = credenciais de **dois usuários descartáveis, ambos sem família**, em banco de teste; os demais começam vazios.

---

## Achados da análise do código (leia antes de executar)

Cruzei o documento de CT com `ConviteController`, `ConviteService`, `Convite` e `Usuario`. Não vi `ConviteDTO`, `ConviteResponseDTO` nem `ApiExceptionHandler`; usei só o que o service acessa (`destinatarioUsername()`, `familiaId()`). O documento de Criar/Excluir Família indica que `RuntimeException` cai em handler genérico com **500**; assumo o mesmo aqui. Confiança entre parênteses.

| # | Achado | Impacto nos testes | Confiança |
| --- | -------- | -------------------- | ----------- |
| B1 | **Convite por email não existe.** O DTO só expõe `destinatarioUsername`; o service nunca lê email. | CT02, CT07, CT08, CT09 não têm como passar: email-only gera "Username do destinatário é obrigatório". | Alta (~95%) |
| B2 | **Destinatário inexistente causa NPE.** `findByUsername` devolve `null` (retorno `Usuario`, não `Optional`) e a linha seguinte faz `destinatario.equals(solicitante)`. | CT05 retorna **500** em vez de 404. | Alta |
| B3 | **Todas as validações são `RuntimeException` genéricas** → 500 (sem handler dedicado). Isso afeta CT04, CT06, não-admin, família divergente e auto-convite. | Os testes exigem 400/403/404/409 e **devem falhar** até existir exceção de negócio mapeada. | Média-alta (~80%) |
| B4 | **"Família criada" não acontece em `enviarConvite`.** O service exige `papel == ADMIN_FAMILIA` e `familia != null`; criar a família é responsabilidade de `POST /familias` (o front orquestra). | CT01–CT03 precisam de preparo: A cria a família antes. Sem isso, A (papel USUARIO) recebe "Apenas admins podem enviar convite". | Alta |
| B5 | **`@PreAuthorize` permite `USUARIO`**, mas o service rejeita não-admin com `RuntimeException`. A regra de papel vive na camada errada e o status vira 500 em vez de 403. | Extra E3. | Alta |
| B6 | **CT06/CT09 checam "já é membro de QUALQUER família"**, não "da sua família" (`destinatario.getFamilia() != null`). Mensagem real: "Usuário já está em uma família", diferente da spec ("Este usuário já pertence à sua família."). | Os testes validam status e a palavra "família" na mensagem, não o texto exato. Convidar alguém de outra família cai na mesma regra e a spec não cobre. | Alta |
| B7 | **Não há checagem de convite pendente duplicado.** Convidar o mesmo usuário N vezes cria N convites PENDENTE. | Extra E6 deve falhar (201 em vez de 409). | Alta |
| B8 | **CT03 passa por acidente:** o campo `email` é ignorado e só o username é usado. | Não prova a precedência Username→Email da spec. | Alta |
| B9 | **Spec CT04 vs. código:** mensagem esperada ("Preencha o Username ou o Email para convidar") não existe; o código diz "Username do destinatário é obrigatório". | Teste valida 400 e menção a username/email, não o texto exato. | Alta |
| B10 | **Autenticação do endpoint:** `buscarUsuarioAutenticado(principal, username)` faz `findByUsername(principal.getName())`. Funciona se `principal.getName()` for o username (comportamento padrão de `UserDetails`), mas depende do `JwtAuthFilter`, que não vi. | Se o principal for o email, tudo em `/convites` retorna 500 (verifique logo no CT01). | Média |

**Suposição de contrato:** `UsuarioResponseDTO` expõe `username`, `email`, `papel` e algo equivalente a `familia`/`familiaId`; `ConviteResponseDTO` expõe `id` e `status`. Ajuste se os nomes reais diferirem.

**Mapa CT → requisição:**

| CT | Requisição | Classes | Resultado esperado (spec) | Resultado provável hoje |
| ---- | ----------- | --------- | --------------------------- | ------------------------- |
| CT01 | 2.1 | 1, 2, 3 | 201, convite enviado | Passa (com preparo da seção 1) |
| CT02 | 2.3 | 4, 8, 9, 10, 11 | 201, convite por email | **Falha** (B1) |
| CT03 | 2.5 | 1, 2, 3 | 201, username tem precedência | Passa (B8) |
| CT04 | 3.1 | 5, 12 | 400, preencha username ou email | **Falha** (B3/B9) |
| CT05 | 3.2 | 6 | 404, usuário inexistente | **Falha** (B2: 500) |
| CT07 | 3.3 | 13 | 400, email inválido | **Falha** (B1) |
| CT08 | 3.4 | 14 | 404, usuário inexistente por email | **Falha** (B1) |
| CT06 | 5.1 | 7 | 409, já pertence à família | **Falha** (B3: 500) |
| CT09 | 5.2 | 15 | 409, já pertence à família (email) | **Falha** (B1) |

**Ordem de execução:** o fluxo é sequencial e deixa estado. Execute na ordem das seções. CT06 e CT09 vêm por último porque exigem B já dentro da família, e **não há endpoint conhecido para desvincular membro**. Após a coleção, resete o banco de teste (ou use usuários descartáveis).

---

## 0. Setup (login e perfis)

### 0.1 Login - Usuário A (será admin da família)

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

### 0.2 Login - Usuário B (convidado, sem família)

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
pm.test("Resposta traz o campo token (login válido)", function () {
  var j = pm.response.json();
  pm.expect(j.token, "token ausente: confira email/senha no Environment").to.be.a("string").and.not.empty;
  pm.environment.set("tokenB", j.token);
});
```

### 0.3 Capturar perfil de A

- **Método e URL:** `GET {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Perfil de A capturado", function () {
  var u = pm.response.json();
  pm.expect(u.username).to.be.a("string").and.not.empty;
  pm.environment.set("usernameA", u.username);
});
```

### 0.4 Capturar perfil de B (e garantir que não tem família)

- **Método e URL:** `GET {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Perfil de B capturado", function () {
  var u = pm.response.json();
  pm.expect(u.username).to.be.a("string").and.not.empty;
  pm.environment.set("usernameB", u.username);
  pm.environment.set("emailBReal", u.email);
});
pm.test("Pré-condição: B não pertence a nenhuma família", function () {
  var u = pm.response.json();
  pm.expect(u.familiaId || u.familia || null, "B já tem família: resete o banco de teste").to.eql(null);
});
```

---

## 1. Preparo: A cria a família e vira ADMIN_FAMILIA

> O enunciado do CDU diz "caso ainda não tenha membros, a família será criada". No código, isso é `POST /familias` (achado B4).

### 1.1 A cria a família

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
pm.environment.set("nomeFamilia", "Família CDU22 " + Date.now());
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 201 Created", function () { pm.response.to.have.status(201); });
pm.test("Família criada com id numérico", function () {
  var j = pm.response.json();
  pm.expect(j.id).to.be.a("number");
  pm.environment.set("familiaId", j.id);
});
pm.test("Criador entrou como único membro", function () {
  pm.expect(pm.response.json().membros).to.be.an("array").with.lengthOf(1);
});
```

### 1.2 Re-login de A (renova o token com o papel atualizado)

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
pm.test("Token renovado", function () {
  var j = pm.response.json();
  pm.expect(j.token).to.be.a("string").and.not.empty;
  pm.environment.set("token", j.token);
});
```

### 1.3 Confirmar papel ADMIN_FAMILIA e vínculo

- **Método e URL:** `GET {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("A virou ADMIN_FAMILIA (pré-condição do CDU 22)", function () {
  pm.expect(pm.response.json().papel).to.eql("ADMIN_FAMILIA");
});
pm.test("A pertence à família criada", function () {
  var u = pm.response.json();
  var fid = u.familiaId || (u.familia && u.familia.id);
  pm.expect(Number(fid)).to.eql(Number(pm.environment.get("familiaId")));
});
```

---

## 2. Fluxo principal (classes válidas)

> Após cada sucesso há uma requisição de **limpeza** em que B recusa o convite, mantendo B sem família e sem pendências para o próximo teste (achado B7 permitiria acumular pendentes, mas isso mascararia o teste).
>
> **Snippet C (pré-request das limpezas e do aceite).** Busca o convite pendente mais recente de B:
>
> ```javascript
> pm.sendRequest({
>   url: pm.environment.get("baseUrl") + "/convites/me",
>   method: "GET",
>   header: { Authorization: "Bearer " + pm.environment.get("tokenB") }
> }, function (err, res) {
>   var lista = (res && res.code === 200) ? res.json() : [];
>   var ultimo = lista[lista.length - 1];
>   pm.environment.set("conviteId", ultimo ? ultimo.id : "");
> });
> ```

### 2.1 CT01 - Convidar apenas por Username (classes 1, 2, 3)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameB}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 201 Created", function () { pm.response.to.have.status(201); });
pm.test("Content-Type é application/json", function () {
  pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});
pm.test("Convite criado com status PENDENTE", function () {
  var j = pm.response.json();
  pm.expect(j).to.have.property("id");
  pm.expect(j.status).to.eql("PENDENTE");
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
pm.sendRequest({
  url: pm.environment.get("baseUrl") + "/usuario/me",
  method: "GET",
  header: { Authorization: "Bearer " + pm.environment.get("tokenB") }
}, function (err, res) {
  pm.test("B continua sem família antes de aceitar", function () {
    var u = res.json();
    pm.expect(u.familiaId || u.familia || null).to.eql(null);
  });
});
pm.sendRequest({
  url: pm.environment.get("baseUrl") + "/convites/me",
  method: "GET",
  header: { Authorization: "Bearer " + pm.environment.get("tokenB") }
}, function (err, res) {
  pm.test("Convite aparece em GET /convites/me de B", function () {
    pm.expect(res.json()).to.be.an("array").that.is.not.empty;
  });
});
```

### 2.2 CT01-limpeza: B recusa o convite

- **Método e URL:** `PATCH {{baseUrl}}/convites/{{conviteId}}?status=RECUSADO`
- **Authorization:** Bearer Token = {{tokenB}}

**Pre-request:** *(Snippet C)*

```javascript
pm.sendRequest({
  url: pm.environment.get("baseUrl") + "/convites/me",
  method: "GET",
  header: { Authorization: "Bearer " + pm.environment.get("tokenB") }
}, function (err, res) {
  var lista = (res && res.code === 200) ? res.json() : [];
  var ultimo = lista[lista.length - 1];
  pm.environment.set("conviteId", ultimo ? ultimo.id : "");
});
```

**Post-response / Tests:**

```javascript
pm.test("Convite recusado (200) ou nada a limpar (404/405)", function () {
  if (pm.environment.get("conviteId")) {
    pm.response.to.have.status(200);
    pm.expect(pm.response.json().status).to.eql("RECUSADO");
  } else {
    pm.expect(pm.response.code).to.be.oneOf([404, 405]);
  }
});
```

### 2.3 CT02 - Convidar apenas por Email (classes 4, 8, 9, 10, 11)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "{{emailBReal}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 201 Created", function () { pm.response.to.have.status(201); });
pm.test("Convite criado com status PENDENTE", function () {
  pm.expect(pm.response.json().status).to.eql("PENDENTE");
});
```

### 2.4 CT02-limpeza: B recusa o convite

- **Método e URL:** `PATCH {{baseUrl}}/convites/{{conviteId}}?status=RECUSADO`
- **Authorization:** Bearer Token = {{tokenB}}

**Pre-request:** *(Snippet C, mesmo código da 2.2)*

**Post-response / Tests:**

```javascript
pm.test("Convite recusado (200) ou nada a limpar (404/405)", function () {
  if (pm.environment.get("conviteId")) {
    pm.response.to.have.status(200);
  } else {
    pm.expect(pm.response.code).to.be.oneOf([404, 405]);
  }
});
```

### 2.5 CT03 - Username e Email preenchidos (precedência do Username)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameB}}",
  "email": "{{emailBReal}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 201 Created", function () { pm.response.to.have.status(201); });
pm.test("Convite criado para o usuário convidado por Username (PENDENTE)", function () {
  pm.expect(pm.response.json().status).to.eql("PENDENTE");
});
pm.sendRequest({
  url: pm.environment.get("baseUrl") + "/convites/me",
  method: "GET",
  header: { Authorization: "Bearer " + pm.environment.get("tokenB") }
}, function (err, res) {
  pm.test("Exatamente 1 convite pendente para B (não duplica por email)", function () {
    pm.expect(res.json()).to.be.an("array").with.lengthOf(1);
  });
});
```

### 2.6 CT03-limpeza: B recusa o convite

- **Método e URL:** `PATCH {{baseUrl}}/convites/{{conviteId}}?status=RECUSADO`
- **Authorization:** Bearer Token = {{tokenB}}

**Pre-request:** *(Snippet C, mesmo código da 2.2)*

**Post-response / Tests:**

```javascript
pm.test("Convite recusado (200)", function () { pm.response.to.have.status(200); });
```

---

## 3. Fluxos de exceção (classes inválidas, sem depender de B na família)

### 3.1 CT04 - Username e Email vazios (classes 5, 12)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "",
  "email": ""
}
```

**Post-response / Tests:**

```javascript
pm.test("Rejeita com erro de cliente (400 ou 422), não 201 nem 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
pm.test("Mensagem orienta a preencher Username ou Email", function () {
  pm.expect(pm.response.text()).to.match(/username|e-?mail/i);
});
```

### 3.2 CT05 - Username inexistente (classe 6)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameInexistente}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("usernameInexistente", "inexistente_" + Date.now());
```

**Post-response / Tests:**

```javascript
pm.test("Usuário inexistente retorna 404 (não 500)", function () {
  pm.expect(pm.response.code).to.eql(404);
});
pm.test("Mensagem indica usuário inexistente/não encontrado", function () {
  pm.expect(pm.response.text()).to.match(/inexistente|n[aã]o (existe|encontrado)/i);
});
```

### 3.3 CT07 - Email fora de formato (classe 13)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "testeemail"
}
```

**Post-response / Tests:**

```javascript
pm.test("Email inválido retorna 400/422, não 201 nem 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
pm.test("Mensagem cita formato de e-mail", function () {
  pm.expect(pm.response.text()).to.match(/e-?mail|formato/i);
});
```

### 3.4 CT08 - Email de usuário inexistente (classe 14)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "{{emailInexistente}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("emailInexistente", "inexistente" + Date.now() + "@exemplo.com");
```

**Post-response / Tests:**

```javascript
pm.test("Usuário inexistente por email retorna 404 (não 201 nem 500)", function () {
  pm.expect(pm.response.code).to.eql(404);
});
```

---

## 4. Extras: autenticação, autorização e regras do service (fora da tabela de CT)

### 4.1 E1 - Convidar SEM token

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** No Auth
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameB}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Sem token é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 4.2 E2 - Convidar com token inválido

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** No Auth
- **Header extra:** `Authorization: Bearer token_invalido_123`
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameB}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Token inválido é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 4.3 E3 - Usuário comum (não admin da família) tenta convidar

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{tokenB}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameA}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Não-admin recebe 403 Forbidden", function () {
  pm.response.to.have.status(403);
});
```

### 4.4 E4 - Convite com `familiaId` diferente da família do solicitante

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameB}}",
  "familiaId": 999999999
}
```

**Post-response / Tests:**

```javascript
pm.test("Família divergente é bloqueada (403 ou 400), não 201 nem 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 403]);
});
```

### 4.5 E5 - Auto-convite

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameA}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Auto-convite é rejeitado com 400/409/422, não 201 nem 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 409, 422]);
});
```

---

## 5. Usuário já é membro da família (CT06 e CT09) - preparo e execução

### 5.1 Preparo: A convida B

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameB}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Convite criado (201)", function () { pm.response.to.have.status(201); });
```

### 5.2 E6 - Convite duplicado pendente para o mesmo usuário

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameB}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Convite pendente duplicado é rejeitado (409 ou 400), não 201", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 409]);
});
```

### 5.3 Preparo: B aceita o convite

- **Método e URL:** `PATCH {{baseUrl}}/convites/{{conviteId}}?status=ACEITO`
- **Authorization:** Bearer Token = {{tokenB}}

**Pre-request:** *(Snippet C, mesmo código da 2.2)*

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Convite ACEITO", function () {
  pm.expect(pm.response.json().status).to.eql("ACEITO");
});
```

### 5.4 Confirmar que B agora pertence à família de A

- **Método e URL:** `GET {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{tokenB}}

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("B é membro da família de A após aceitar", function () {
  var u = pm.response.json();
  var fid = u.familiaId || (u.familia && u.familia.id);
  pm.expect(Number(fid)).to.eql(Number(pm.environment.get("familiaId")));
});
```

### 5.5 CT06 - Convidar por Username um usuário já membro (classe 7)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "destinatarioUsername": "{{usernameB}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Membro existente é rejeitado com 409 (ou 400/422), não 201 nem 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 409, 422]);
});
pm.test("Mensagem informa que o usuário já pertence à família", function () {
  pm.expect(pm.response.text()).to.match(/fam[ií]lia/i);
});
```

### 5.6 CT09 - Convidar por Email um usuário já membro (classe 15)

- **Método e URL:** `POST {{baseUrl}}/convites`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "{{emailBReal}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Membro existente (por email) é rejeitado com 409 (ou 400/422), não 201 nem 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 409, 422]);
});
pm.test("Mensagem informa que o usuário já pertence à família", function () {
  pm.expect(pm.response.text()).to.match(/fam[ií]lia/i);
});
```

---

## 6. Limpeza final (melhor esforço)

### 6.1 A tenta excluir a família

- **Método e URL:** `DELETE {{baseUrl}}/familias/{{familiaId}}`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**

```javascript
pm.test("Limpeza executada (204) ou bloqueada por membros (400/409)", function () {
  pm.expect(pm.response.code).to.be.oneOf([204, 400, 409]);
});
```

---

## Cobertura

- **Classes de equivalência:** 1–15 mapeadas nos CT01–CT09. As classes 4, 8–11 (Email válido), 13, 14 e 15 **não podem passar** enquanto não existir convite por email (B1).
