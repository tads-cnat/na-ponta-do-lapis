# CDU 38. Gerenciar Notificações do Usuário

- **Ator principal**: Cliente.
- **Atores secundários**: Motor de Metas, Convites e Conquistas.
- **Resumo**: O Cliente recebe notificações internas no cabeçalho da aplicação (ícone de sino com contador de não lidas) sobre eventos relevantes: alertas de progresso de metas (`50%`, `80%`, `100%` ou meta estourada), convites de família e conquistas desbloqueadas. O usuário pode abrir o painel, clicar em uma notificação para ser redirecionado à tela correspondente, marcar uma ou todas como lidas e excluir notificações.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: O estado de leitura ou exclusão das notificações é atualizado no banco de dados.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente clica no ícone de sino no cabeçalho (`HeaderComponent`). | |
| | 2 - O sistema consulta `GET /api/v1/notificacoes` e `GET /api/v1/notificacoes/nao-lidas/count` e exibe o popover com a lista de notificações. |
| 3 - O Cliente clica sobre uma notificação (ou em **"Marcar todas como lidas"**). | |
| | 4 - O sistema atualiza o status para lida (`PATCH /api/v1/notificacoes/{id}/ler` ou `PATCH /api/v1/notificacoes/ler-todas`), decrementa o contador de não lidas e redireciona para o link associado, se houver. |

## Fluxo Alternativo I - Excluir Notificação

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.1 - O Cliente clica no ícone de exclusão ao lado de uma notificação. | |
| | 3.2 - O sistema remove a notificação (`DELETE /api/v1/notificacoes/{id}`) e atualiza a lista. |

