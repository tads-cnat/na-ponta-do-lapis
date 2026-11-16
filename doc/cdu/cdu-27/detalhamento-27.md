# CDU 27. Aceitar Convite de Família

- **Ator principal**: Cliente.
- **Atores secundários**: Administrador da Família (remetente do convite).
- **Resumo**: Permite que um usuário convidado para um grupo familiar visualize seus convites pendentes e aceite ou recuse o ingresso na família. Ao aceitar o convite, o usuário passa a integrar a família e o status do convite é atualizado para `ACEITO`.
- **Pré-condição**: O Cliente deve estar autenticado, possuir um convite com status `PENDENTE` e ainda não pertencer a outra família.
- **Pós-Condição**: O usuário é vinculado à família convidante e o convite tem seu status atualizado no banco de dados.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente visualiza a notificação/lista de convites recebidos (`GET /api/v1/convites/me`). | |
| 2 - O Cliente seleciona a opção para **Aceitar** o convite da família. | |
| | 3 - O sistema processa `PATCH /api/v1/convites/{conviteId}` com o novo status. |
| | 4 - O sistema vincula o usuário à família correspondente, atualiza o status do convite para `ACEITO` e invalida o cache de autenticação do usuário. |
| | 5 - O sistema exibe confirmação e libera o acesso aos dados compartilhados na aba **Família** (`/app/familia`). |

## Fluxo Alternativo I - Recusar Convite

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 2.1 - O Cliente opta por **Recusar** o convite recebido. | |
| | 2.2 - O sistema atualiza o status do convite para `RECUSADO` sem vincular o usuário à família. |

