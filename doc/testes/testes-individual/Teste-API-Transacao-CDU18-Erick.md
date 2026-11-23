# Scripts do Postman — CDU 018 — Manter Transação

Cenários baseados no [plano de testes funcional](./cdu18-Erick.md). Para cada requisição, configure no Postman o método e a URL indicados, selecione a autenticação em **Authorization** e cole o body, quando houver, em **Body → raw → JSON**. Copie os scripts para **Scripts → Pre-request** e **Post-response**, conforme indicado. Em versões antigas, use **Tests** para o script de resposta.

Na aba **Variables** da coleção, configure `baseUrl` (padrão `http://localhost:8080/api`) e os dados abaixo. Execute as pastas em ordem no Collection Runner, em um banco de teste. Crie as variáveis listadas na própria coleção antes de executar as requisições.

Use um usuário descartável que tenha uma conta própria. Preencha `emailA`, `passwordA`, `contaId` e `categoriaId`. Confirme que `contaInexistenteId` aponta para um ID livre no banco de teste. A edição muda a transação de `PENDENTE` para `REALIZADA` e a exclusão usa o ID capturado na criação.

Variáveis da coleção: `baseUrl`, `emailA`, `passwordA`, `contaId`, `categoriaId`, `contaInexistenteId`, `tokenA`, `transacaoId`, `dataHora`.

## Achados da análise do código

| Caso | Expectativa do roteiro | Comportamento indicado pelo código |
| --- | --- | --- |
| Criação simples | 201 com os dados persistidos | `POST /transacoes` retorna uma lista com uma transação. |
| Descrição, zero e negativos | 400 com erro no campo | `@Valid` e `ApiExceptionHandler` produzem um objeto com erros por campo. |
| Data de 30 de fevereiro e estado `XYZ` | 400 | A desserialização pode ser atendida pelo handler genérico (500). O parser de datas também pode normalizar a data conforme a configuração. |
| Conta inexistente | 400 conforme o plano | `ContaIdNaoExisteException` cai no handler genérico (500). |
| Categoria ausente | 400 conforme o DTO atual | O plano a descreve como opcional. O DTO exige `idCategoria` e deixa `marcadorId` opcional. |

O contrato usa `tipo` (`DESPESA`/`RECEITA`) e `estado` (`PENDENTE`/`REALIZADA`). A API confirma as operações com status e dados. Toasts, confirmação visual e atualização do gráfico pertencem aos testes de interface. Em caso de falha inesperada com criação de registros, use o reset do banco descartável.

## 0. Preparo

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

### 0.2 Conferir conta do usuário

- **Método e URL** `GET {{baseUrl}}/contas/me`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json().some(conta => conta.id === Number(pm.variables.get("contaId")))).to.eql(true);
  });
}
```

### 0.3 Conferir categoria

- **Método e URL** `GET {{baseUrl}}/categorias/{{categoriaId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json().id).to.eql(Number(pm.variables.get("categoriaId")));
  });
}
```

## 1. Criação, edição e exclusão

### 1.1 Criar despesa válida

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": 150.5,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("transacaoId");
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 201", function () {
  pm.response.to.have.status(201);
});

if (pm.response.code === 201) {
  pm.test("Contrato da resposta", function () {
    const lista = pm.response.json();
    pm.expect(lista).to.be.an("array").with.lengthOf(1);
    const [registro] = lista;
    pm.expect(registro.id).to.be.a("number");
    pm.collectionVariables.set("transacaoId", registro.id);
    pm.expect(registro.descricao).to.eql("Compra de Supermercado");
    pm.expect(registro.valor).to.eql(150.5);
    pm.expect(registro.tipo).to.eql("DESPESA");
    pm.expect(registro.estado).to.eql("PENDENTE");
    pm.expect(registro.conta.id).to.eql(Number(pm.variables.get("contaId")));
    pm.expect(registro.categoria.id).to.eql(Number(pm.variables.get("categoriaId")));
  });
}
```

### 1.2 Consultar a transação criada

- **Método e URL** `GET {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const registro = pm.response.json();
    pm.expect(registro.id).to.eql(Number(pm.variables.get("transacaoId")));
    pm.expect(registro.descricao).to.eql("Compra de Supermercado");
    pm.expect(registro.valor).to.eql(150.5);
    pm.expect(registro.tipo).to.eql("DESPESA");
    pm.expect(registro.estado).to.eql("PENDENTE");
    pm.expect(registro.conta.id).to.eql(Number(pm.variables.get("contaId")));
    pm.expect(registro.categoria.id).to.eql(Number(pm.variables.get("categoriaId")));
  });
}
```

### 1.3 Atualizar a mesma transação

- **Método e URL** `PUT {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado revisada",
  "valor": 175.25,
  "tipo": "DESPESA",
  "estado": "REALIZADA",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const registro = pm.response.json();
    pm.expect(registro.id).to.eql(Number(pm.variables.get("transacaoId")));
    pm.expect(registro.descricao).to.eql("Compra de Supermercado revisada");
    pm.expect(registro.valor).to.eql(175.25);
    pm.expect(registro.tipo).to.eql("DESPESA");
    pm.expect(registro.estado).to.eql("REALIZADA");
    pm.expect(registro.conta.id).to.eql(Number(pm.variables.get("contaId")));
    pm.expect(registro.categoria.id).to.eql(Number(pm.variables.get("categoriaId")));
  });
}
```

### 1.4 Conferir alteração persistida, sem duplicação

- **Método e URL** `GET {{baseUrl}}/transacoes/me`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    const registros = pm.response.json().filter(item => item.id === Number(pm.variables.get("transacaoId")));
    pm.expect(registros).to.have.lengthOf(1);
    const [registro] = registros;
    pm.expect(registro.descricao).to.eql("Compra de Supermercado revisada");
    pm.expect(registro.valor).to.eql(175.25);
    pm.expect(registro.tipo).to.eql("DESPESA");
    pm.expect(registro.estado).to.eql("REALIZADA");
    pm.expect(registro.conta.id).to.eql(Number(pm.variables.get("contaId")));
    pm.expect(registro.categoria.id).to.eql(Number(pm.variables.get("categoriaId")));
  });
}
```

### 1.5 Excluir a transação criada

- **Método e URL** `DELETE {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 204", function () {
  pm.response.to.have.status(204);
});

if (pm.response.code === 204) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.text()).to.eql("");
  });
}
```

### 1.6 Confirmar exclusão por ID

- **Método e URL** `GET {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 404", function () {
  pm.response.to.have.status(404);
});
```

### 1.7 Confirmar ausência na listagem

- **Método e URL** `GET {{baseUrl}}/transacoes/me`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

if (pm.response.code === 200) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json().some(item => item.id === Number(pm.variables.get("transacaoId")))).to.eql(false);
  });
}
```

## 2. Partições inválidas

### 2.1 Descrição vazia

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "",
  "valor": 50,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});

if (pm.response.code === 400) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json()).to.have.property("descricao").that.is.a("string").and.not.empty;
  });
}
```

### 2.2 Valor zero

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": 0,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});

if (pm.response.code === 400) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json()).to.have.property("valor").that.is.a("string").and.not.empty;
  });
}
```

### 2.3 Valor negativo

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": -50,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});

if (pm.response.code === 400) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json()).to.have.property("valor").that.is.a("string").and.not.empty;
  });
}
```

### 2.4 Data inexistente

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": 150.5,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "2026-02-30T12:00:00.000Z",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});
```

### 2.5 Estado fora do domínio

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": 150.5,
  "tipo": "DESPESA",
  "estado": "XYZ",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});
```

### 2.6 Conta inexistente

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": 150.5,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaInexistenteId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});
```

### 2.7 Categoria ausente

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": 150.5,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});

if (pm.response.code === 400) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json()).to.have.property("idCategoria").that.is.a("string").and.not.empty;
  });
}
```

### 2.8 Descrição com 101 caracteres

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "valor": 150.5,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});

if (pm.response.code === 400) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json()).to.have.property("descricao").that.is.a("string").and.not.empty;
  });
}
```

### 2.9 Valor imediatamente abaixo de zero

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": -0.01,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 400", function () {
  pm.response.to.have.status(400);
});

if (pm.response.code === 400) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.json()).to.have.property("valor").that.is.a("string").and.not.empty;
  });
}
```

## 3. Limites válidos e limpeza

### 3.1 Menor valor e descrição de um caractere

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "A",
  "valor": 0.01,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("transacaoId");
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 201", function () {
  pm.response.to.have.status(201);
});

if (pm.response.code === 201) {
  pm.test("Contrato da resposta", function () {
    const lista = pm.response.json();
    pm.expect(lista).to.be.an("array").with.lengthOf(1);
    const [registro] = lista;
    pm.expect(registro.id).to.be.a("number");
    pm.collectionVariables.set("transacaoId", registro.id);
    pm.expect(registro.descricao).to.eql("A");
    pm.expect(registro.valor).to.eql(0.01);
    pm.expect(registro.tipo).to.eql("DESPESA");
    pm.expect(registro.estado).to.eql("PENDENTE");
    pm.expect(registro.conta.id).to.eql(Number(pm.variables.get("contaId")));
    pm.expect(registro.categoria.id).to.eql(Number(pm.variables.get("categoriaId")));
  });
}
```

### 3.2 Excluir a transação criada

- **Método e URL** `DELETE {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 204", function () {
  pm.response.to.have.status(204);
});

if (pm.response.code === 204) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.text()).to.eql("");
  });
}
```

### 3.3 Confirmar exclusão por ID

- **Método e URL** `GET {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 404", function () {
  pm.response.to.have.status(404);
});
```

### 3.4 Valor típico

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "Compra de Supermercado",
  "valor": 150,
  "tipo": "RECEITA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("transacaoId");
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 201", function () {
  pm.response.to.have.status(201);
});

if (pm.response.code === 201) {
  pm.test("Contrato da resposta", function () {
    const lista = pm.response.json();
    pm.expect(lista).to.be.an("array").with.lengthOf(1);
    const [registro] = lista;
    pm.expect(registro.id).to.be.a("number");
    pm.collectionVariables.set("transacaoId", registro.id);
    pm.expect(registro.descricao).to.eql("Compra de Supermercado");
    pm.expect(registro.valor).to.eql(150);
    pm.expect(registro.tipo).to.eql("RECEITA");
    pm.expect(registro.estado).to.eql("PENDENTE");
    pm.expect(registro.conta.id).to.eql(Number(pm.variables.get("contaId")));
    pm.expect(registro.categoria.id).to.eql(Number(pm.variables.get("categoriaId")));
  });
}
```

### 3.5 Excluir a transação criada

- **Método e URL** `DELETE {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 204", function () {
  pm.response.to.have.status(204);
});

if (pm.response.code === 204) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.text()).to.eql("");
  });
}
```

### 3.6 Confirmar exclusão por ID

- **Método e URL** `GET {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 404", function () {
  pm.response.to.have.status(404);
});
```

### 3.7 Valor elevado e descrição de 100 caracteres

- **Método e URL** `POST {{baseUrl}}/transacoes`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** raw, JSON

```json
{
  "descricao": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  "valor": 999999.99,
  "tipo": "DESPESA",
  "estado": "PENDENTE",
  "idCategoria": {{categoriaId}},
  "idContaFinanceira": {{contaId}},
  "dataHora": "{{dataHora}}",
  "parcelado": false
}
```

**Pre-request**

```javascript
pm.collectionVariables.unset("transacaoId");
pm.collectionVariables.set("dataHora", new Date().toISOString());
```

**Post-response / Tests**

```javascript
pm.test("HTTP 201", function () {
  pm.response.to.have.status(201);
});

if (pm.response.code === 201) {
  pm.test("Contrato da resposta", function () {
    const lista = pm.response.json();
    pm.expect(lista).to.be.an("array").with.lengthOf(1);
    const [registro] = lista;
    pm.expect(registro.id).to.be.a("number");
    pm.collectionVariables.set("transacaoId", registro.id);
    pm.expect(registro.descricao).to.eql("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    pm.expect(registro.valor).to.eql(999999.99);
    pm.expect(registro.tipo).to.eql("DESPESA");
    pm.expect(registro.estado).to.eql("PENDENTE");
    pm.expect(registro.conta.id).to.eql(Number(pm.variables.get("contaId")));
    pm.expect(registro.categoria.id).to.eql(Number(pm.variables.get("categoriaId")));
  });
}
```

### 3.8 Excluir a transação criada

- **Método e URL** `DELETE {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 204", function () {
  pm.response.to.have.status(204);
});

if (pm.response.code === 204) {
  pm.test("Contrato da resposta", function () {
    pm.expect(pm.response.text()).to.eql("");
  });
}
```

### 3.9 Confirmar exclusão por ID

- **Método e URL** `GET {{baseUrl}}/transacoes/{{transacaoId}}`
- **Authorization** Bearer Token = `{{tokenA}}`
- **Body** none

**Post-response / Tests**

```javascript
pm.test("HTTP 404", function () {
  pm.response.to.have.status(404);
});
```
