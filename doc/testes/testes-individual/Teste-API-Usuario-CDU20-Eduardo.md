# Scripts do Postman - CDU 20: Atualizar Perfil

Endpoint testado: `PATCH {{baseUrl}}/usuario/me` (`UsuarioController.atualizarUsuarioLogado` → `UsuarioService.atualizarUsuarioLogado`).

Para cada requisição: crie-a no Postman com o método, a URL e o body indicados, escolha a autenticação indicada na aba **Authorization**, e cole o código nas abas indicadas (**Scripts → Pre-request** e **Post-response**, ou **Tests** nas versões antigas).

Variáveis de environment usadas: `baseUrl`, `emailA`, `passwordA`, `token`, `nomeOriginal`, `usernameOriginal`, `emailOriginal`, `nomeTeste`, `usernameTeste`, `emailTeste`.

Valores iniciais: `baseUrl` = `http://localhost:8080/api`; `emailA`/`passwordA` = credenciais de um usuário **descartável** de banco de teste; os demais começam vazios.

---

**Mapa CT → requisição:**

| CT | Requisição | Classes | Resultado esperado (spec) | Resultado provável hoje |
| -- | ---------- | ------- | ------------------------- | ------------------------- |
| CT01 | 3.1 | 1, 2 | 200, nome alterado | Passa |
| CT02 | 3.2 | 5, 6 | 200, username alterado | **Falha** (A1) |
| CT03 | 3.3 | 9, 10, 11 | 200, email alterado | Passa (com a massa gerada) |
| CT04 | 3.4 | 1, 2, 5, 6 | 200, nome e username | **Falha** (A1) |
| CT05 | 3.5 | 1, 2, 9, 10, 11 | 200, nome e email | Passa |
| CT06 | 3.6 | 5, 6, 9, 10, 11 | 200, username e email | **Falha** (A1) |
| CT07 | 3.7 | 1, 2, 5, 6, 9, 10, 11 | 200, os três | **Falha** (A1) |
| CT08 | 2.1 | 3 | 400, nome vazio | **Falha** (A2) |
| CT09 | 2.2 | 4 | 400, nome < 3 | Provável 500 (A5) |
| CT10 | 2.3 | 4 | 400, nome ≥ 100 | **Falha** (A4: 200) |
| CT11 | 2.5 | 7 | 400, username vazio | **Falha** (A1/A2) |
| CT12 | 2.6 | 8 | 400, username ≥ 100 | **Falha** (A1: 200) |
| CT13 | 2.7 | 12 | 400, email vazio | **Falha** (A2) |
| CT14 | 2.8 | 13 | 400, email fora de formato | Provável 500 (A5) |
| CT15 | 2.9 | 14 | 400, email > 254 | Provável 500 (A3) |

---

## 0. Setup (login e captura do perfil original)

### 0.1 Login - Usuário A

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

### 0.2 Capturar perfil original (GET /usuario/me)

- **Método e URL:** `GET {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Perfil traz nome, username e email e foram guardados como originais", function () {
  var u = pm.response.json();
  pm.expect(u).to.have.property("nome");
  pm.expect(u).to.have.property("username");
  pm.expect(u).to.have.property("email");
  pm.environment.set("nomeOriginal", u.nome);
  pm.environment.set("usernameOriginal", u.username);
  pm.environment.set("emailOriginal", u.email);
});
```

---

## 1. Autenticação (pré-condição: cliente autenticado)

### 1.1 Atualizar perfil SEM token

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** No Auth
- **Body (raw, JSON):**

```json
{
  "nome": "Nome Sem Token"
}
```

**Post-response / Tests:**

```javascript
pm.test("Sem token é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

### 1.2 Atualizar perfil com token inválido

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** No Auth
- **Header extra:** `Authorization: Bearer token_invalido_123`
- **Body (raw, JSON):**

```json
{
  "nome": "Nome Token Invalido"
}
```

**Post-response / Tests:**

```javascript
pm.test("Token inválido é bloqueado (401 ou 403)", function () {
  pm.expect(pm.response.code).to.be.oneOf([401, 403]);
});
```

---

## 2. Fluxos de exceção (classes inválidas) - rodar ANTES dos de sucesso

> **Snippet A (usar em todas as requisições desta seção).** Cole no fim do Post-response, chamando `verificarInalterado();`. Ele confirma por GET que nada foi persistido.

> ```javascript
> function verificarInalterado() {
>   pm.sendRequest({
>     url: pm.environment.get("baseUrl") + "/usuario/me",
>     method: "GET",
>     header: { Authorization: "Bearer " + pm.environment.get("token") }
>   }, function (err, res) {
>     pm.test("Perfil persistido não foi alterado", function () {
>       pm.expect(err).to.be.null;
>       var u = res.json();
>       pm.expect(u.nome).to.eql(pm.environment.get("nomeOriginal"));
>       pm.expect(u.username).to.eql(pm.environment.get("usernameOriginal"));
>       pm.expect(u.email).to.eql(pm.environment.get("emailOriginal"));
>     });
>   });
> }
> ```

### 2.1 CT08 - Nome vazio (classe 3)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": ""
}
```

**Post-response / Tests:**

```javascript
pm.test("Nome vazio é rejeitado com erro de cliente (400 ou 422), não 200 nem 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
pm.test("Mensagem de erro menciona o campo nome", function () {
  pm.expect(pm.response.text()).to.match(/nome/i);
});
verificarInalterado();
```

### 2.2 CT09 - Nome com menos de 3 caracteres (classe 4)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "Cl"
}
```

**Post-response / Tests:**

```javascript
pm.test("Nome com 2 caracteres é rejeitado (400 ou 422), não 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
pm.test("Mensagem de erro menciona o campo nome", function () {
  pm.expect(pm.response.text()).to.match(/nome/i);
});
verificarInalterado();
```

### 2.3 CT10 - Nome com 100 caracteres (limite superior, classe 4)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "{{nomeTeste}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("nomeTeste", ("Clarice" + "e".repeat(200)).slice(0, 100));
```

**Post-response / Tests:**

```javascript
pm.test("Massa de teste tem 100 caracteres", function () {
  pm.expect(pm.environment.get("nomeTeste").length).to.eql(100);
});
pm.test("Nome com 100 caracteres é rejeitado (400 ou 422)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
verificarInalterado();
```

### 2.4 Extra - Nome com 101 caracteres (estoura a coluna)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "{{nomeTeste}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("nomeTeste", ("Clarice" + "e".repeat(200)).slice(0, 101));
```

**Post-response / Tests:**

```javascript
pm.test("Nome com 101 caracteres é rejeitado com 400/422 (não 500)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
verificarInalterado();
```

### 2.5 CT11 - Username vazio (classe 7)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "username": ""
}
```

**Post-response / Tests:**

```javascript
pm.test("Username vazio é rejeitado (400 ou 422)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
pm.test("Mensagem de erro menciona username", function () {
  pm.expect(pm.response.text()).to.match(/username/i);
});
verificarInalterado();
```

### 2.6 CT12 - Username com 100 caracteres (limite, classe 8)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "username": "{{usernameTeste}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("usernameTeste", ("u" + Date.now() + "a".repeat(200)).slice(0, 100));
```

**Post-response / Tests:**

```javascript
pm.test("Massa de teste tem 100 caracteres", function () {
  pm.expect(pm.environment.get("usernameTeste").length).to.eql(100);
});
pm.test("Username com 100 caracteres é rejeitado (400 ou 422)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
verificarInalterado();
```

### 2.7 CT13 - Email vazio (classe 12)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": ""
}
```

**Post-response / Tests:**

```javascript
pm.test("Email vazio é rejeitado (400 ou 422)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
pm.test("Mensagem de erro menciona email", function () {
  pm.expect(pm.response.text()).to.match(/e-?mail/i);
});
verificarInalterado();
```

### 2.8 CT14 - Email fora de formato (classe 13)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "claricegmailcom"
}
```

**Post-response / Tests:**

```javascript
pm.test("Email sem @ é rejeitado (400 ou 422), não 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
pm.test("Mensagem de erro menciona email", function () {
  pm.expect(pm.response.text()).to.match(/e-?mail/i);
});
verificarInalterado();
```

### 2.9 CT15 - Email com mais de 254 caracteres (classe 14)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "{{emailTeste}}"
}
```

**Pre-request:**

```javascript
var local = ("e" + Date.now() + "a".repeat(64)).slice(0, 64);
pm.environment.set("emailTeste", local + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(62));
```

**Post-response / Tests:**

```javascript
pm.test("Massa de teste tem 255 caracteres", function () {
  pm.expect(pm.environment.get("emailTeste").length).to.eql(255);
});
pm.test("Email com 255 caracteres é rejeitado (400 ou 422), não 500", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
verificarInalterado();
```

### 2.10 Extra - Email com exatamente 254 caracteres (decisão de spec pendente)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "{{emailTeste}}"
}
```

**Pre-request:**

```javascript
var local = ("e" + Date.now() + "a".repeat(64)).slice(0, 64);
pm.environment.set("emailTeste", local + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61));
```

**Post-response / Tests:**

```javascript
pm.test("Massa de teste tem 254 caracteres", function () {
  pm.expect(pm.environment.get("emailTeste").length).to.eql(254);
});
pm.test("Email com 254 caracteres é rejeitado conforme a spec (400 ou 422)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
verificarInalterado();
```

### 2.11 Extra - Email sem TLD (`clarice@gmail`), formato `#@#.#` da spec

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "clarice@gmail"
}
```

**Post-response / Tests:**

```javascript
pm.test("Email sem ponto no domínio é rejeitado (400 ou 422)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
verificarInalterado();
```

### 2.12 Extra - Nome só com espaços

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "     "
}
```

**Post-response / Tests:**

```javascript
pm.test("Nome só com espaços é rejeitado (400 ou 422)", function () {
  pm.expect(pm.response.code).to.be.oneOf([400, 422]);
});
verificarInalterado();
```

---

## 3. Fluxo principal (classes válidas)

> Em toda requisição que envia `email`, a API devolve `{ usuario, token }` (achado A7). O script atualiza `token`, senão as requisições seguintes falham com 401/500.
>
> **Snippet B (persistência).** Cole no Post-response e chame `verificarPersistido({campos})`:
>
> ```javascript
> function verificarPersistido(esperado) {
>   pm.sendRequest({
>     url: pm.environment.get("baseUrl") + "/usuario/me",
>     method: "GET",
>     header: { Authorization: "Bearer " + pm.environment.get("token") }
>   }, function (err, res) {
>     pm.test("GET /usuario/me confirma os valores persistidos", function () {
>       pm.expect(err).to.be.null;
>       var u = res.json();
>       Object.keys(esperado).forEach(function (k) { pm.expect(u[k], "campo " + k).to.eql(esperado[k]); });
>     });
>   });
> }
> ```

### 3.1 CT01 - Alterar apenas o nome ("Cla", 3 caracteres - classes 1, 2)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "Cla"
}
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Content-Type é application/json", function () {
  pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});
pm.test("Nome alterado com sucesso", function () {
  var j = pm.response.json();
  var u = j.usuario || j;
  pm.expect(u.nome).to.eql("Cla");
});
pm.test("Sem email/senha no body, a API não emite novo token", function () {
  pm.expect(pm.response.json()).to.not.have.property("token");
});
pm.test("Tempo de resposta aceitável (< 2000ms)", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
verificarPersistido({ nome: "Cla" });
```

### 3.2 CT02 - Alterar apenas o username (99 caracteres - classes 5, 6)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "username": "{{usernameTeste}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("usernameTeste", ("u" + Date.now() + "a".repeat(200)).slice(0, 99));
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Massa de teste tem 99 caracteres", function () {
  pm.expect(pm.environment.get("usernameTeste").length).to.eql(99);
});
pm.test("Username alterado com sucesso", function () {
  var j = pm.response.json();
  var u = j.usuario || j;
  pm.expect(u.username).to.eql(pm.environment.get("usernameTeste"));
});
verificarPersistido({ username: pm.environment.get("usernameTeste") });
```

### 3.3 CT03 - Alterar apenas o email (253 caracteres - classes 9, 10, 11)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "email": "{{emailTeste}}"
}
```

**Pre-request:**

```javascript
var local = ("e" + Date.now() + "a".repeat(64)).slice(0, 64);
pm.environment.set("emailTeste", local + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(60));
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Massa de teste tem 253 caracteres", function () {
  pm.expect(pm.environment.get("emailTeste").length).to.eql(253);
});
pm.test("Email alterado com sucesso", function () {
  var u = pm.response.json().usuario;
  pm.expect(u.email).to.eql(pm.environment.get("emailTeste"));
});
pm.test("Resposta traz novo token (JWT usa o email como subject)", function () {
  var j = pm.response.json();
  pm.expect(j.token).to.be.a("string").and.not.empty;
  pm.environment.set("token", j.token);
});
verificarPersistido({ email: pm.environment.get("emailTeste") });
```

### 3.4 CT04 - Alterar nome e username (classes 1, 2, 5, 6)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "Cla",
  "username": "{{usernameTeste}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("usernameTeste", ("u" + Date.now() + "a".repeat(200)).slice(0, 99));
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Nome e Username alterados com sucesso", function () {
  var j = pm.response.json();
  var u = j.usuario || j;
  pm.expect(u.nome).to.eql("Cla");
  pm.expect(u.username, "ACHADO A1: username não é aplicado").to.eql(pm.environment.get("usernameTeste"));
});
verificarPersistido({ nome: "Cla", username: pm.environment.get("usernameTeste") });
```

### 3.5 CT05 - Alterar nome e email (classes 1, 2, 9, 10, 11)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "Cla",
  "email": "{{emailTeste}}"
}
```

**Pre-request:**

```javascript
var local = ("e" + Date.now() + "a".repeat(64)).slice(0, 64);
pm.environment.set("emailTeste", local + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(60));
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Nome e Email alterados com sucesso", function () {
  var u = pm.response.json().usuario;
  pm.expect(u.nome).to.eql("Cla");
  pm.expect(u.email).to.eql(pm.environment.get("emailTeste"));
});
pm.test("Novo token emitido e salvo", function () {
  var j = pm.response.json();
  pm.expect(j.token).to.be.a("string").and.not.empty;
  pm.environment.set("token", j.token);
});
verificarPersistido({ nome: "Cla", email: pm.environment.get("emailTeste") });
```

### 3.6 CT06 - Alterar username e email (classes 5, 6, 9, 10, 11)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "username": "{{usernameTeste}}",
  "email": "{{emailTeste}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("usernameTeste", ("u" + Date.now() + "a".repeat(200)).slice(0, 99));
var local = ("e" + Date.now() + "a".repeat(64)).slice(0, 64);
pm.environment.set("emailTeste", local + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(60));
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Username e Email alterados com sucesso", function () {
  var u = pm.response.json().usuario;
  pm.expect(u.email).to.eql(pm.environment.get("emailTeste"));
  pm.expect(u.username, "ACHADO A1: username não é aplicado").to.eql(pm.environment.get("usernameTeste"));
});
pm.test("Novo token emitido e salvo", function () {
  var j = pm.response.json();
  pm.expect(j.token).to.be.a("string").and.not.empty;
  pm.environment.set("token", j.token);
});
verificarPersistido({ username: pm.environment.get("usernameTeste"), email: pm.environment.get("emailTeste") });
```

### 3.7 CT07 - Alterar nome, username e email (classes 1, 2, 5, 6, 9, 10, 11)

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "{{nomeTeste}}",
  "username": "{{usernameTeste}}",
  "email": "{{emailTeste}}"
}
```

**Pre-request:**

```javascript
pm.environment.set("nomeTeste", ("Clarice" + "e".repeat(200)).slice(0, 99));
pm.environment.set("usernameTeste", ("u" + Date.now() + "a".repeat(200)).slice(0, 99));
var local = ("e" + Date.now() + "a".repeat(64)).slice(0, 64);
pm.environment.set("emailTeste", local + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(60));
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Nome, Username e Email alterados com sucesso", function () {
  var u = pm.response.json().usuario;
  pm.expect(u.nome).to.eql(pm.environment.get("nomeTeste"));
  pm.expect(u.email).to.eql(pm.environment.get("emailTeste"));
  pm.expect(u.username, "ACHADO A1: username não é aplicado").to.eql(pm.environment.get("usernameTeste"));
});
pm.test("Novo token emitido e salvo", function () {
  var j = pm.response.json();
  pm.expect(j.token).to.be.a("string").and.not.empty;
  pm.environment.set("token", j.token);
});
verificarPersistido({
  nome: pm.environment.get("nomeTeste"),
  username: pm.environment.get("usernameTeste"),
  email: pm.environment.get("emailTeste")
});
```

---

## 4. Restauração

### 4.1 Restaurar nome e email originais

- **Método e URL:** `PATCH {{baseUrl}}/usuario/me`
- **Authorization:** Bearer Token = {{token}}
- **Body (raw, JSON):**

```json
{
  "nome": "{{nomeOriginal}}",
  "email": "{{emailOriginal}}"
}
```

**Post-response / Tests:**

```javascript
pm.test("Status code é 200", function () { pm.response.to.have.status(200); });
pm.test("Perfil restaurado e token renovado", function () {
  var j = pm.response.json();
  var u = j.usuario || j;
  pm.expect(u.nome).to.eql(pm.environment.get("nomeOriginal"));
  pm.expect(u.email).to.eql(pm.environment.get("emailOriginal"));
  if (j.token) { pm.environment.set("token", j.token); }
});
```

---

## Cobertura

- **Classes de equivalência:** 1–14 cobertas pelos CT01–CT15. Extras: 101 chars no nome, 254 chars no email, email sem TLD, nome só com espaços e autenticação.
