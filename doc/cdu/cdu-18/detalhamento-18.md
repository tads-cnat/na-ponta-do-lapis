# CDU 18. Manter Transação (Persistente)

- **Ator principal**: Cliente
- **Atores secundários**: Sistema de Banco de Dados
- **Resumo**: O sistema exibe um formulário com os campos necessários (descrição, valor, categoria, tipo, status de pagamento/recebimento, data, marcador, conta) para criar, editar ou excluir uma transação. O Cliente preenche todos os campos obrigatórios e confirma a operação.
- **Pré-condição**: O Cliente deve estar autenticado no sistema para registrar uma transação.
- **Pós-condição**: O sistema persiste os dados da transação no banco de dados e exibe uma mensagem de confirmação.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-: | :-: |
| 1 - Cliente clica no botão "Transações" ou no botão de acesso rápido | |  
| | 2 - Sistema exibe um formulário com os campos (descrição, valor, categoria, tipo, status de pagamento/recebimento, data, marcador, conta) necessários para completar o cadastro de transação |
| 3 - Cliente preenche os campos obrigatórios e clica em "Salvar" | |
| | 4 - Sistema valida os dados informados |
| | 5 - Sistema persiste a transação no banco de dados e exibe mensagem de confirmação: "Transação salva com sucesso" |

## Fluxo Alternativo I - Validação de Dados com Erro

| Ações do ator | Ações do sistema |
| :-: | :-: |
| | 4.1 - Sistema identifica que um ou mais campos foram preenchidos de forma incorreta e exibe mensagem de erro (ex: "Campo obrigatório", "Valor deve ser positivo") |
| | 4.2 - Sistema retorna ao passo 3 do fluxo principal para corrigir os dados |

## Fluxo Alternativo II - Editar Transação

| Ações do ator | Ações do sistema |
| :-: | :-: |
| 1.2 - Cliente seleciona uma transação existente e clica no ícone de edição | |
| | 2.2 - Sistema exibe o formulário pré-preenchido com os dados da transação |
| 3.2 - Cliente altera os dados desejados e clica em "Salvar" | |
| | 4.2 - Sistema valida os dados e persiste as alterações |
| | 5.2 - Sistema exibe mensagem de confirmação: "Transação atualizada com sucesso" |

## Fluxo Alternativo III - Excluir Transação

| Ações do ator | Ações do sistema |
| :-: | :-: |
| 1.3 - Cliente seleciona uma transação existente e clica no ícone de lixeira para excluir | |
| | 2.3 - Sistema exibe um pop-up de confirmação com a mensagem: "Deseja realmente excluir esta transação?" |
| 3.3 - Cliente confirma a exclusão | |
| | 4.3 - Sistema remove a transação do banco de dados |
| | 5.3 - Sistema atualiza automaticamente o gráfico e a tabela de transações, refletindo a exclusão |
| | 6.3 - Sistema exibe mensagem de confirmação: "Transação excluída com sucesso" |

## Fluxo Alternativo IV - Importar Transação via PDF

| Ações do ator | Ações do sistema |
| :-: | :-: |
| 1.4 - Cliente clica no botão "Importar PDF" no formulário de transações | |
| | 2.4 - Sistema exibe um diálogo para seleção de arquivo |
| 3.4 - Cliente seleciona um arquivo PDF e clica em "Abrir" | |
| | 4.4 - Sistema lê e processa o arquivo PDF extraindo os dados da transação |
| | 5.4 - Sistema pré-preenche o formulário com os dados extraídos |
| 6.4 - Cliente revisa os dados e clica em "Salvar" | |
| | 7.4 - Sistema valida e persiste a transação (segue para passo 5 do fluxo principal) |


## Diagrama de Interação (Sequência ou Comunicação)

![Diagrama de Atividade](diagrama_atividade.png)

## Diagrama de Classes de Projeto

![Diagrama de Projeto](diagrama_projeto.png)
