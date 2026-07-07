# CDU 28. Alterar Senha

- **Ator principal**: Cliente.

- **Atores secundários**: N/A.
- **Resumo**: O Cliente pode alterar a senha nas configurações, basta informar a nova senha desejada.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: A senha do Cliente é atualizada e é enviado uma notificação no e-mail sobre a alteração.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente acessa as configurações da conta e seleciona Alterar senha. | |
| |2 - O sistema exibe o formulário para alteração de senha. |
|3 - O Cliente informa a nova senha e confirma a operação. | |
| |4 - O sistema valida os dados e persiste no banco. |
| |5 - O sistema envia um e-mail notificando a alteração da senha. |