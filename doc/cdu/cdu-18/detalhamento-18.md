# CDU 18. Manter Transação 

- **Ator principal**: Cliente
- **Atores secundários**: Sistema de Banco de Dados
- **Resumo**: O sistema exibe um formulário com os campos necessários (*descrição, *valor, categoria, *status de pagamento/recebimento, *data, marcador, *conta) para criar e editar, ja para excluir, uma janela de confirmação é exibida transação. O Cliente preenche todos os campos obrigatórios e confirma a operação.
- **Pré-condição**: O Cliente deve estar autenticado no sistema para registrar uma transação.
- **Pós-condição**: O sistema persiste os dados da transação no banco de dados e exibe uma mensagem de confirmação.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-: | :-: |
| 1 - Cliente clica no botão ["Adicionar transação"](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1545-21663&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654&t=4Hx8oZOgHUnVNuYC-1) | |  
| | 2 - Sistema exibe um [formulário](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1545-21657&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) com os campos (*descrição, *valor, *categoria, *status de pagamento/recebimento, *data, marcador, *conta) necessários para completar o cadastro de transação |
| 3 - Cliente preenche os campos obrigatórios e clica em "Salvar" | |
| | 4 - Sistema valida os dados informados |
| | 5 - Sistema persiste a transação no banco de dados e exibe mensagem de confirmação: "Transação salva com sucesso" |


## Fluxo Alternativo I - Editar Transação

| Ações do ator | Ações do sistema |
| :-: | :-: |
| 1.2 - Cliente seleciona uma transação existente e clica no ícone de edição | |
| | 2.2 - Sistema exibe o [formulário](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1545-21658&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) pré-preenchido com os dados da transação |
| 3.2 - Cliente altera os dados desejados e clica em "Salvar" | |
| | 4.2 - Sistema valida os dados e persiste as alterações |
| | 5.2 - Sistema exibe mensagem de confirmação: "Transação atualizada com sucesso" |

## Fluxo Alternativo II - Excluir Transação

| Ações do ator | Ações do sistema |
| :-: | :-: |
| 1.3 - Cliente seleciona uma transação existente e clica no ícone de lixeira para excluir | |
| | 2.3 - Sistema exibe um [janela de confirmação](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1545-21659&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) com a mensagem: "Tem certeza que deseja remover esta transação?" |
| 3.3 - Cliente confirma a exclusão | |
| | 4.3 - Sistema remove a transação do banco de dados |
| | 5.3 - Sistema atualiza automaticamente o gráfico e a tabela de transações, refletindo a exclusão |
| | 6.3 - Sistema exibe mensagem de confirmação: "Transação excluída com sucesso" |

## Fluxo Exceção - Validação de Dados com Erro

| Ações do ator | Ações do sistema |
| :-: | :-: |
| | 4.1 - Sistema identifica que um ou mais campos foram preenchidos de forma incorreta e exibe mensagem de erro (ex: "Campo obrigatório", "Valor deve ser positivo") |
| | 4.2 - Sistema retorna ao passo 3 do fluxo principal para corrigir os dados |

## Diagrama de Interação (Sequência ou Comunicação)

![Diagrama de Atividade](diagrama_atividade.png)

## Diagrama de Classes de Projeto

![Diagrama de Projeto](diagrama_projeto.png)
