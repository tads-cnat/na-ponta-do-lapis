# CDU 18. Manter Transação(Persistente)

- **Ator principal**: Usuário

- **Atores secundários**: ...
- **Resumo**: O Sistema exibe os campos para criar, editar e excluir uma transação, como por exemplo campo, nome, valor, tipo e categoria. O Usuário preenche todos os campos obrigatórios e confirma o cadastro.
- **Pré-condição**: O usuário deve estar autenticado no sistema para conseguir registrar uma transação.
- **Pós-Condição**: O sistema persiste os dados e salva no banco.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - Usuário clica no botão "Transações" Ou no botão de acesso rápido | |  
| | 2 - Sistema exibe um formuário com os campos(descricao, valor, categoria, tipo, foi paga/recebida, data, marcador, conta) necessários para preencher para concluir o cadastro de transação|
| 3 - O usuário preenche os campos de input e clica em "Salvar". | |
| |  4 - O sistema verifica se as informações estão de acordo com as validações |
| |  5 - Persiste a transação e uma mensagem de confirmação é enviada com a seguinte mensagem: "Transação salva com sucesso"). |

## Fluxo Alternativo I - Informações incoerentes

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
| |
| | 4.1 - O sistema Identifica que alguns dos campos não foram preenchidos de maneira correta e mostra uma mensagem de erro, por exemplo: valor da transação deve ser positivo  |
|| Retorna ao passo 3 do fluxo principal|

## Fluxo Alternativo II - Editar transação

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.2 - Usuário seleciona uma transação já existente e aperta no icone de editar | |
| | 2.2 - Usuário altera os dados da transação e clica em salvar |

## Fluxo Alternativo III - Excluir transação

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.3 - Usuário seleciona uma transação já existente e clica no icone de lixeira para excluir transação | |
| | 2.3. O Sistema exibe um pop-up de confirmação com a mensagem: “Deseja realmente excluir esta transação?”. |
| 3.3. O Usuário confirma a exclusão. | |
| | 4.3. O Sistema remove a transação. |
|| 5.3. O Sistema atualiza automaticamente o gráfico e a tabela de transações, refletindo a exclusão. |

## Fluxo Alternativo IV - Adicionar transação por PDF

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
|-| -|
|-| -|
|-| -|


## Diagrama de Interação (Sequência ou Comunicação)

![Diagrama de Atividade](diagrama_atividade.png)

## Diagrama de Classes de Projeto

![Diagrama de Projeto](diagrama_projeto.png)
