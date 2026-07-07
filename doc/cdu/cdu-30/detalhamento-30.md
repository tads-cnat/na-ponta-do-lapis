# CDU 30. Excluir Família

- **Ator principal**: Cliente.
- **Atores secundários**: N/A.
- **Resumo**: O Cliente pode excluir uma família da qual seja responsável. Após a confirmação, a família e seus vínculos são removidos do sistema.
- **Pré-condição**: O Cliente deve estar autenticado e deve ser o **fundador do grupo**.
- **Pós-Condição**: A família é removida do sistema e deixa de estar disponível para seus membros.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente acessa as configurações da família. | |
| 2 - O Cliente seleciona a opção **"Excluir Família"**. | |
| | 3 - O sistema exibe uma mensagem solicitando a confirmação da exclusão. |
| 4 - O Cliente confirma a operação. | |
| | 5 - O sistema remove a família do banco de dados. |
| | 6 - O sistema exibe uma mensagem de sucesso. |