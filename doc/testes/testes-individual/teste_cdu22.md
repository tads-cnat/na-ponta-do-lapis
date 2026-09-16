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
- O Email deve estar no formato válido para emails (#@#.#)
- O usuário convidado não pode fazer parte da família atual do administrador.
- Caso ambos os campos sejam preenchidos corretamente, o sistema primeiramente irá tentar convidar pelo Username antes do Email

### Classes de Equivalência

| Variável | Classes Válidas | Classes Inválidas |
| -------- | --------------- | ----------------- |
| **Username** | 1. Preenchido; 2. Usuário existe; 3. Usuário não é membro da família. 4. Vazio (apenas se o Email estiver preenchido). | 5. Vazio (se o Email também estiver vazio); 6. Usuário inexistente; 7. Usuário já é membro da família atual. |
| **Email** | 8. Preenchido; 9. Formato correto; 10. Usuário existe; 11. Usuário não é membro da família; | 12. Vazio (se o Username também estiver vazio); 13. Não estar no formato; 14. Usuário inexistente. 15. Usuário já é membro da família atual. |

### Testes Funcionais

| ID | Username | Email | Resultado Esperado | Situação | Classe(s) Coberta(s) |
| -- | -------- | ----- | ------------------ | -------- | -------------------- |
| **CT01** | @Clarice | *vazio* | Convite enviado com sucesso. Família criada | Pendente | 1, 2, 3 |
| **CT02** | *vazio* | clarice@gmail.com | Convite enviado com sucesso. Família criada | Pendente | 4, 8, 9, 10, 11 |
| **CT03** | @Clarice | clarice@gmail.com | Convite enviado com sucesso. Família criada | Pendente | 1, 2, 3 |
| **CT04** | *vazio* | *vazio* | Erro: "Preencha o Username ou o Email para convidar". Convite não enviado. | Pendente | 5, 12 |
| **CT05** | @123 | *vazio* | Usuário inexistente. Convite não enviado. | Pendente | 6 |
| **CT06** | @Clarice | *vazio* | Erro: "Este usuário já pertence à sua família." | Pendente | 7 |
| **CT07** | *vazio* | testeemail | Erro de validação de formato de e-mail. Convite não enviado. | Pendente | 13 |
| **CT08** | *vazio* | JogosEGames@gmail.com | Usuário inválido. Convite não enviado. | Pendente | 14 |
| **CT09** | *vazio* | clarice@gmail.com | Erro: "Este usuário já pertence à sua família." | Pendente | 15 |