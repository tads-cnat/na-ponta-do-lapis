# Scripts do Postman — CDU 023 — Tornar Administrador da Família

Cenários baseados no [plano de testes funcional](./cdu23-Erick.md). Para cada requisição, configure no Postman o método e a URL indicados, selecione a autenticação em **Authorization** e cole o body, quando houver, em **Body → raw → JSON**. Copie os scripts para **Scripts → Pre-request** e **Post-response**, conforme indicado. Em versões antigas, use **Tests** para o script de resposta.

Na aba **Variables** da coleção, configure `baseUrl` (padrão `http://localhost:8080/api`) e os dados abaixo. Execute as pastas em ordem no Collection Runner, em um banco de teste. Crie as variáveis listadas na própria coleção antes de executar as requisições.

Prepare quatro usuários descartáveis com papel `USUARIO` e sem família. Preencha `emailA` a `emailD` e `passwordA` a `passwordD`. A cria a família e adiciona B e D. C permanece sem vínculo. O teste de acesso de B ocorre antes de sua promoção. Para repetir a coleção, use uma base de teste restaurada ou novos usuários.

Variáveis da coleção: `baseUrl`, `emailA`, `passwordA`, `tokenA`, `idA`, `usernameA`, `emailB`, `passwordB`, `tokenB`, `idB`, `usernameB`, `emailC`, `passwordC`, `tokenC`, `idC`, `usernameC`, `emailD`, `passwordD`, `tokenD`, `idD`, `usernameD`, `familiaId`, `nomeFamilia`.

## Achados da análise do código

| Caso | Expectativa do roteiro | Comportamento indicado pelo código |
| --- | --- | --- |
| CT01 — A promove B | 200 e B aparece como `ADMIN_FAMILIA` em uma consulta posterior | A resposta usa `UsuarioResponseDTO`, com `id`, `papel` e `idFamilia`. |
| CT02 — B já é administrador | 400 com mensagem de operação inviável | O serviço atribui o mesmo papel novamente e tende a retornar 200. |
| CT03 — B comum tenta promover D | 403 e D permanece `USUARIO` | A permissão declarada no controller restringe a operação. |
| CT04 — C sem vínculo | 403 ou 404 e C permanece `USUARIO` | `NaoEdaMesmaFamiliaException` cai no handler genérico (500). |

O `PATCH` usa IDs na rota, autenticação Bearer e nenhum body. O roteiro confirma o papel persistido via `GET /usuario/me`. O botão desabilitado e a mensagem visual pertencem aos testes de interface.

## 0. Login e perfis

### 0.1 Login A

- **Método e URL** `POST {{baseUrl}}/auth/login`
- **Authorization** No Auth
- **Body** raw, JSON

```json
{
  "email": "{{emailA}}",
  "password": "{{passwordA}}"
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("tokenA");
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const { token } = pm.response.json();
    pm.expect(token).to.be.a("string").and.not.empty;
    pm.collectionVariables.set("tokenA", token);
  });
}
```

### 0.2 Conferir perfil A

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.papel).to.eql("USUARIO");
    pm.expect(usuario.idFamilia).to.eql(null);
    pm.collectionVariables.set("idA", usuario.id);
    pm.collectionVariables.set("usernameA", encodeURIComponent(usuario.username));
  });
}
```

### 0.3 Login B

- **Método e URL** `POST {{baseUrl}}/auth/login`
- **Authorization** No Auth
- **Body** raw, JSON

```json
{
  "email": "{{emailB}}",
  "password": "{{passwordB}}"
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("tokenB");
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const { token } = pm.response.json();
    pm.expect(token).to.be.a("string").and.not.empty;
    pm.collectionVariables.set("tokenB", token);
  });
}
```

### 0.4 Conferir perfil B

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenB}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.papel).to.eql("USUARIO");
    pm.expect(usuario.idFamilia).to.eql(null);
    pm.collectionVariables.set("idB", usuario.id);
    pm.collectionVariables.set("usernameB", encodeURIComponent(usuario.username));
  });
}
```

### 0.5 Login C

- **Método e URL** `POST {{baseUrl}}/auth/login`
- **Authorization** No Auth
- **Body** raw, JSON

```json
{
  "email": "{{emailC}}",
  "password": "{{passwordC}}"
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("tokenC");
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const { token } = pm.response.json();
    pm.expect(token).to.be.a("string").and.not.empty;
    pm.collectionVariables.set("tokenC", token);
  });
}
```

### 0.6 Conferir perfil C

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenC}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.papel).to.eql("USUARIO");
    pm.expect(usuario.idFamilia).to.eql(null);
    pm.collectionVariables.set("idC", usuario.id);
    pm.collectionVariables.set("usernameC", encodeURIComponent(usuario.username));
  });
}
```

### 0.7 Login D

- **Método e URL** `POST {{baseUrl}}/auth/login`
- **Authorization** No Auth
- **Body** raw, JSON

```json
{
  "email": "{{emailD}}",
  "password": "{{passwordD}}"
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("tokenD");
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const { token } = pm.response.json();
    pm.expect(token).to.be.a("string").and.not.empty;
    pm.collectionVariables.set("tokenD", token);
  });
}
```

### 0.8 Conferir perfil D

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenD}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.papel).to.eql("USUARIO");
    pm.expect(usuario.idFamilia).to.eql(null);
    pm.collectionVariables.set("idD", usuario.id);
    pm.collectionVariables.set("usernameD", encodeURIComponent(usuario.username));
  });
}
```

## 1. Preparo da família

### 1.1 A cria a família

- **Método e URL** `POST {{baseUrl}}/familias`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "nome": "{{nomeFamilia}}",
  "foto": null
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("nomeFamilia", "Família CDU23 " + Date.now());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 201", function () {
  pm.response.to.have.status(201);
});

if (pm.response.code === 201) {
  pm.test("Contrato da resposta", function () {
    const familia = pm.response.json();
    pm.expect(familia.id).to.be.a("number");
    pm.collectionVariables.set("familiaId", familia.id);
  });
}
```

### 1.2 Login A

- **Método e URL** `POST {{baseUrl}}/auth/login`
- **Authorization** No Auth
- **Body** raw, JSON

```json
{
  "email": "{{emailA}}",
  "password": "{{passwordA}}"
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("tokenA");
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const { token } = pm.response.json();
    pm.expect(token).to.be.a("string").and.not.empty;
    pm.collectionVariables.set("tokenA", token);
  });
}
```

### 1.3 Conferir perfil A

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idA")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("ADMIN_FAMILIA");
  });
}
```

### 1.4 Adicionar B à família

- **Método e URL** `POST {{baseUrl}}/familias/{{familiaId}}/membros?username={{usernameB}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json().id).to.eql(Number(pm.variables.get("familiaId")));
  });
}
```

### 1.5 Adicionar D à família

- **Método e URL** `POST {{baseUrl}}/familias/{{familiaId}}/membros?username={{usernameD}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json().id).to.eql(Number(pm.variables.get("familiaId")));
  });
}
```

### 1.6 Login B

- **Método e URL** `POST {{baseUrl}}/auth/login`
- **Authorization** No Auth
- **Body** raw, JSON

```json
{
  "email": "{{emailB}}",
  "password": "{{passwordB}}"
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("tokenB");
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const { token } = pm.response.json();
    pm.expect(token).to.be.a("string").and.not.empty;
    pm.collectionVariables.set("tokenB", token);
  });
}
```

### 1.7 Login D

- **Método e URL** `POST {{baseUrl}}/auth/login`
- **Authorization** No Auth
- **Body** raw, JSON

```json
{
  "email": "{{emailD}}",
  "password": "{{passwordD}}"
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("tokenD");
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const { token } = pm.response.json();
    pm.expect(token).to.be.a("string").and.not.empty;
    pm.collectionVariables.set("tokenD", token);
  });
}
```

### 1.8 Conferir perfil B

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenB}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idB")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("USUARIO");
  });
}
```

### 1.9 Conferir perfil D

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenD}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idD")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("USUARIO");
  });
}
```

### 1.10 A lista os membros

- **Método e URL** `GET {{baseUrl}}/familias/{{familiaId}}/membros`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const membros = pm.response.json();
    pm.expect(membros.map(membro => membro.id)).to.have.members([Number(pm.variables.get("idA")), Number(pm.variables.get("idB")), Number(pm.variables.get("idD"))]);
  });
}
```

## 2. Restrições de acesso

### 2.1 CT03 — B comum tenta promover D

- **Método e URL** `PATCH {{baseUrl}}/familias/{{familiaId}}/membros/{{idD}}`
- **Authorization** Bearer Token = `{{tokenB}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 403", function () {
  pm.response.to.have.status(403);
});
```

### 2.2 Conferir perfil D

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenD}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idD")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("USUARIO");
  });
}
```

### 2.3 CT04 — A tenta promover C sem vínculo

- **Método e URL** `PATCH {{baseUrl}}/familias/{{familiaId}}/membros/{{idC}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 403 ou 404", function () {
  pm.expect(pm.response.code).to.be.oneOf([403,404]);
});
```

### 2.4 Conferir perfil C

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenC}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idC")));
    pm.expect(usuario.idFamilia).to.eql(null);
    pm.expect(usuario.papel).to.eql("USUARIO");
  });
}
```

### 2.5 Extra — Requisição sem token

- **Método e URL** `PATCH {{baseUrl}}/familias/{{familiaId}}/membros/{{idD}}`
- **Authorization** No Auth
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 401 ou 403", function () {
  pm.expect(pm.response.code).to.be.oneOf([401,403]);
});
```

### 2.6 Conferir perfil D

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenD}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idD")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("USUARIO");
  });
}
```

## 3. Promoção e repetição

### 3.1 CT01 — A promove B

- **Método e URL** `PATCH {{baseUrl}}/familias/{{familiaId}}/membros/{{idB}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idB")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("ADMIN_FAMILIA");
  });
}
```

### 3.2 Login B

- **Método e URL** `POST {{baseUrl}}/auth/login`
- **Authorization** No Auth
- **Body** raw, JSON

```json
{
  "email": "{{emailB}}",
  "password": "{{passwordB}}"
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("tokenB");
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const { token } = pm.response.json();
    pm.expect(token).to.be.a("string").and.not.empty;
    pm.collectionVariables.set("tokenB", token);
  });
}
```

### 3.3 Conferir perfil B

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenB}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idB")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("ADMIN_FAMILIA");
  });
}
```

### 3.4 Conferir perfil A

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idA")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("ADMIN_FAMILIA");
  });
}
```

### 3.5 CT02 — A tenta promover B novamente

- **Método e URL** `PATCH {{baseUrl}}/familias/{{familiaId}}/membros/{{idB}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});

if (pm.response.code === 400) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.text()).to.match(/j[áa].*administrador/i);
  });
}
```

### 3.6 Conferir perfil B

- **Método e URL** `GET {{baseUrl}}/usuario/me`
- **Authorization** Bearer Token = `{{tokenB}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const usuario = pm.response.json();
    pm.expect(usuario.id).to.be.a("number");
    pm.expect(usuario.id).to.eql(Number(pm.variables.get("idB")));
    pm.expect(usuario.idFamilia).to.eql(Number(pm.variables.get("familiaId")));
    pm.expect(usuario.papel).to.eql("ADMIN_FAMILIA");
  });
}
```
