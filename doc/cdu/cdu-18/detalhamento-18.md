# CDU 18. Manter Transação(Persistente)

- **Ator principal**: Usuário

- **Atores secundários**: ...	 

- **Resumo**: O Sistema exibe os campos para criar, editar e excluir uma transação, como por exemplo campo, nome, valor, tipo e categoria. O Usuário preenche todos os campos obrigatórios e confirma o cadastro.

- **Pré-condição**: O usuário deve estar autenticado no sistema para conseguir registrar uma transação.

- **Pós-Condição**: O sistema persiste os dados e salva no banco.


## Fluxo Principal
| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: | 
| 1 - Usuário clica no botão "Transação" | |  
| | 2 - Sistema exibe um formuário com os campos necessários para preencher para concluir o cadastro de transação(descrição, valor, tipo) | 
  |  3 - o usuário preenche os campos de input e clica em salvar.    |
  |                    |  4 - O sistema verifica se as informações estão de acordo com as validações   |                  
|                    |  5 - Persiste a transação e uma mensagem de confirmação é enviada.   | 

## Fluxo Alternativo I - Informações incoerentes 
| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: | 
| |  
| | 4.1 - O sistema Identifica que alguns dos campos não foram preenchidos de maneira corre e mostra uma mensagem de erro, por exemplo: valor da transação deve ser positivo  |
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
|  3.3. O Usuário confirma a exclusão. |
| | 4.3. O Sistema remove a transação.
|| 5.3. O Sistema atualiza automaticamente o gráfico e a tabela de transações, refletindo a exclusão.

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...
