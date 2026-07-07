# CDU 29. Manter Tipo de Categoria

- **Ator principal**: Cliente.
- **Atores secundários**: N/A.
- **Resumo**: O Cliente pode gerenciar os tipos de categorias utilizados para classificar suas transações, realizando operações de cadastro, edição e exclusão.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: O tipo de categoria é cadastrado, atualizado ou removido do sistema.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente acessa a tela de **Tipos de Categoria**. | |
| | 2 - O sistema exibe a lista de tipos de categorias cadastrados. |
| 3 - O Cliente seleciona a operação desejada (**Cadastrar**, **Editar** ou **Excluir**). | |

### Fluxo Alternativo I – Cadastrar Tipo de Categoria

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 4.1 - O Cliente seleciona **Cadastrar**. | |
| | 5.1 - O sistema exibe o formulário de cadastro. |
| 6.1 - O Cliente informa os dados do tipo de categoria e confirma a operação. | |
| | 7.1 - O sistema valida os dados, salva o novo tipo de categoria e exibe uma mensagem de sucesso. |

### Fluxo Alternativo II – Editar Tipo de Categoria

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 4.2 - O Cliente seleciona um tipo de categoria e clica em **Editar**. | |
| | 5.2 - O sistema exibe o formulário preenchido com os dados atuais. |
| 6.2 - O Cliente altera as informações e confirma a operação. | |
| | 7.2 - O sistema valida os dados, atualiza o tipo de categoria e exibe uma mensagem de sucesso. |

### Fluxo Alternativo III – Excluir Tipo de Categoria

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 4.3 - O Cliente seleciona um tipo de categoria e clica em **Excluir**. | |
| | 5.3 - O sistema solicita a confirmação da exclusão. |
| 6.3 - O Cliente confirma a exclusão. | |
| | 7.3 - O sistema remove o tipo de categoria do banco de dados e exibe uma mensagem de sucesso. |