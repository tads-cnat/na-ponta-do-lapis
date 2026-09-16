# Documento de Casos de Teste do CDU 22 - Convidar Membro à Família

## Introdução

Neste documento reúnem-se os casos de testes elaborados para o fluxo principal e fluxos de exceção do caso de uso. Os testes buscam assegurar a coerência das funcionalidades, atendendo aos requisitos estabelecidos. Cada caso de teste inclui informações sobre o cenário a ser avaliado, os dados de entrada necessários e os resultados esperados.

## Especificação do CDU

- **Ator principal:** Usuário administrador da família.

- **Atores secundários:** Usuário convidado.
- **Resumo:** Na página “Grupo Familiar” o usuário aperta o botão “convidar” para convidar um novo membro à sua família; caso ainda não tenha membros, a família será criada.
- **Pré-condição:** Usuário deve estar cadastrado, autenticado no sistema e ser administrador da família.
- **Pós-condição:** Um convite é enviado ao usuário convidado; caso necessário, uma família é criada e o usuário criador recebe o papel admin família; o membro só é vinculado à família após aceitar o convite.

## Fluxo Principal

### Objetivo

Preencher um formulário para enviar um convite de entrada a um grupo familiar para um usuário do sistema

### Variáveis de Entrada

- Variável 1: Username
- Variável 2: Email

### Condições de Negócio

- Pelo menos um dos campos (Username ou Email) deve ser preenchido, o sistema deve validar se ambos estão vazios.
- O Username ou Email informado deve pertencer a um usuário válido e já cadastrado no sistema.
- O usuário convidado não pode fazer parte da família atual do administrador.

### Classes de Equivalência

| Variável | Classes Válidas | Classes Inválidas |
| -------- | --------------- | ----------------- |
| **Username** | 1. Preenchido; 2. menos de 100 caracteres; 3. Usuário existe; 4. Usuário não é membro da família. 5. Vazio (apenas se o Email estiver preenchido). | 6. Vazio (se o Email também estiver vazio); 7. Mais de 100 caracteres; 8. Usuário inexistente; 9. Usuário já é membro da família atual. |
| **Email** | 10. Preenchido; 11. Formato correto (#@#.#); 12. Menos de 254 caracteres 13. Usuário existe; 14. Usuário não é membro da família; 15. Vazio (apenas se o Username estiver preenchido). | 16. Vazio (se o Username também estiver vazio); 17. Não estar no formato; 18. Mais de 254 caracteres 19. Usuário inexistente. 20. Usuário já é membro da família atual. |

### Testes Funcionais

| ID | Username | Email | Resultado Esperado | Situação | Classe(s) Coberta(s) |
| -- | -------- | ----- | ------------------ | -------- | -------------------- |
| **CT01** | @ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso | *vazio* | Convite enviado com sucesso. Família criada | Pendente | 1, 2, 3, 4, 15 |
| **CT02** | *vazio* | clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm | Convite enviado com sucesso. Família criada | Pendente | 5, 10, 11, 12, 13, 14 |
| **CT03** | *vazio* | *vazio* | Erro: "Preencha o Username ou o Email para convidar". Convite não enviado. | Pendente | 6, 16 |
| **CT04** | @ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIssoXD | *vazio* | Erro: "Username de tamanho inválido". Convite não enviado. | Pendente | 7 |
| **CT05** | @123 | *vazio* | Usuário inválido. Convite não enviado. | Pendente | 8 |
| **CT06** | @C | *vazio* | Erro: "Este usuário já pertence à sua família." | Pendente | 9 |
| **CT07** | *vazio* | testeemail | Erro de validação de formato de e-mail. Convite não enviado. | Pendente | 17 |
| **CT08** | *vazio* | clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccooommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm | Erro: "Email de tamanho inválido". Convite não enviado. | Pendente | 18 |
| **CT9** | *vazio* | JogosEGames@gmail.com | Usuário inválido. Convite não enviado. | Pendente | 19 |
| **CT10** | *vazio* | clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm | Erro: "Este usuário já pertence à sua família." | Pendente | 20 |