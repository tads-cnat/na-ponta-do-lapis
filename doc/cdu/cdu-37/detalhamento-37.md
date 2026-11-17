# CDU 37. Visualizar Conquistas e Gamificação Financeira

- **Ator principal**: Cliente.
- **Atores secundários**: N/A.
- **Resumo**: O sistema conta com um mecanismo de gamificação financeira que desbloqueia **medalhas e conquistas** conforme o usuário evolui no controle de suas finanças (ex.: primeira transação registrada, primeira meta criada, primeiro aporte, meta concluída, etc.). O usuário visualiza seu quadro de conquistas desbloqueadas e bloqueadas na tela de **Perfil** (`GET /api/v1/conquistas/me`) e recebe animações de celebração ao atingir novos marcos.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: O painel de medalhas/conquistas e o progresso de gamificação do usuário são exibidos.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente acessa a página de **Perfil** (`/app/perfil`) ou conclui uma ação financeira relevante (ex.: concluir meta). | |
| | 2 - O sistema consulta `GET /api/v1/conquistas/me`, avalia os critérios atingidos pelo usuário e retorna a lista de conquistas (desbloqueadas com data de obtenção e pendentes). |
| | 3 - O componente de conquistas exibe as medalhas ilustradas (`MedalhaConquistaComponent`) e o progresso geral do usuário. |

