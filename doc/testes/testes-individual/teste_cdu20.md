# Documento de Casos de Teste do CDU 20 - Atualizar Perfil

## Introdução

Neste documento reúne-se casos de testes elaborados para o fluxo principal do caso de uso. Os testes buscam assegurar a coerência das funcionalidades, assim, atendendo os requisitos estabelecidos.
Cada caso de teste inclui informações sobre o cenário a ser avaliado, os dados de entrada necessários e resultados esperados.

## Especificação do CDU

- **Ator principal**: Cliente.

- **Atores secundários**: N/A.
- **Resumo**: O cliente acessa a área de configurações para alterar informações do seu perfil como foto e email.
- **Pré-condição**: O cliente deve estar devidamente autenticado.
- **Pós-Condição**: Suas informações são atualizadas e  alteradas no sistema.

## Fluxo principal

### Objetivo

Atualizar informações básicas do perfil do cliente autenticado

### Variáveis de Entrada

- Variável 1: Nome
- Variável 2: Username
- Variável 3: Email

### Condições de Negócio

- A(s) variável(is) que será(ão) alterada(s) não pode(rão) ser enviada(s) vazia(s)

- O nome deve ter pelo menos três caracteres e menos que 100 caracteres
- O Email deve estar no formato válido para emails (#@#.#) e menos de 254 caracteres
- O username deve ter menos de 100 caracteres

### Classes de Equivalência

| Variável | Classes Válidas | Classes Inválidas |
| -------- | --------------- | ----------------- |
| **Nome** | 1. Preenchido; 2. Mais que três caracteres e menos de 100 caracteres. | 3. Vazio; 4. 3 > Nome ou Nome > 100. |
| **Username** | 5. Preenchido; 6. Menos de 100 caracteres. | 7. Vazio; 8. Username > 100. |
| **Email** | 9. Preenchido; 10. Formato correto. | 11. Vazio; 12. Não estar no formato; 13. Email > 254. |

### Testes funcionais

#### Usuário de Referência

- Nome: Clarice Lispector

- Username: @Clarice
- Email: clarice@gmail.com

##### Os detaques em negrito significam as alterações, portanto, campos não destacados não estãos sendo alterados

---

| ID | Nome | Username | Email | Resultado Esperado | Situação | Classe(s) Coberta(s) |
| -- | ---- | -------- | ----- | ------------------ | -------- | -------------------- |
| **CT01** | **Cla** | @Clarice | clarice@gmail.com | Nome alterado com sucesso! | Pendente | 1, 2 |
| **CT02** | Clarice Lispector | **@ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | clarice@gmail.com | Username alterado com sucesso! | Pendente | 5, 6 |
| **CT03** | Clarice Lispector | @Clarice | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm** | Email alterado com sucesso! | Pendente | 9, 10 |
| **CT04** | **Cla** | **@ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | clarice@gmail.com | Nome e Username alterados com sucesso! | Pendente | 1, 2, 5, 6 |
| **CT05** | **Cla** | @Clarice | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm** | Nome e Email alterados com sucesso! | Pendente | 1, 2, 9, 10 |
| **CT06** | Clarice Lispector | **@ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm** | Username e Email alterados com sucesso! | Pendente | 5, 6, 9, 10 |
| **CT07** | **ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | **@CClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm** | Nome, Username e Email alterados com sucesso! | Pendente | 1, 2, 5, 6, 9, 10 |
| **CT08** | *vazio* | @Clarice | clarice@gmail.com | Erro: Nome não pode ser vazio | Pendente | 3 |
| **CT09** | **Cl** | @Clarice | clarice@gmail.com | Erro: Nome deve conter pelo menos três caracteres e menos que 100 caracteres | Pendente | 4 |
| **CT10** | **ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIssoX** | @Clarice | clarice@gmail.com | Erro: Nome deve conter pelo menos três caracteres e menos que 100 caracteres | Pendente | 4 |
| **CT11** | Clarice Lispector | *vazio* | clarice@gmail.com | Erro: Username vazio | Pendente | 7 |
| **CT12** | Clarice Lispector | **@ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIssoX** | clarice@gmail.com | Erro: Username deve ter menos de 100 caracteres | Pendente | 8 |
| **CT13** | Clarice Lispector | @Clarice | *vazio* | Erro: Email não pode ser vazio | Pendente | 11 |
| **CT14** | Clarice Lispector | @Clarice | claricegmailcom | Erro: Email fora de formato | Pendente | 12 |
| **CT15** | Clarice Lispector | @Clarice | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccoommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm**| Erro: O Email deve ter menos de 254 caracteres| Pendente | 12 |
