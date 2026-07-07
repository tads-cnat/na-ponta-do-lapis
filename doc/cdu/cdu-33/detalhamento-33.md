# CDU 33. Auto exclusão da Conta

- **Ator principal**: Cliente.
- **Atores secundários**: N/A.
- **Resumo**: O Cliente pode solicitar a autoexclusão da própria conta nas configurações. Após a confirmação, a conta é desativada e o acesso ao sistema é bloqueado.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: A conta do Cliente é desativada, o acesso ao sistema é encerrado e uma notificação é enviada para o e-mail cadastrado.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente acessa as configurações da conta e seleciona **Auto exclusão da conta**. | |
| | 2 - O sistema exibe uma mensagem informando as consequências da auto exclusão e solicita confirmação. |
| 3 - O Cliente confirma a solicitação de auto exclusão. | |
| | 4 - O sistema desativa a conta do Cliente. |
| | 5 - O sistema envia um e-mail notificando a auto exclusão da conta. |
| | 6 - O sistema redireciona o usuário para a landing page. |
| | 7 - O sistema informa que a operação foi realizada com sucesso. |