# CDU 25. Visualizar Dashboard 

- **Ator principal**: Cliente.
- **Atores secundários**: N/A.
- **Resumo**: Através da pagina dashboard o cliente visualiza um resumo geral sobre suas transações, gastos, receitas e depesas.
- **Pré-condição**: Cliente autenticado.
- **Pós-Condição**: O dashboard exibe os dados financeiros do cliente.

## Fluxo Principal

| Ações do ator | Ações do sistema |
|---------------|------------------|
| 1. O cliente acessa a página do dashboard. | |
| | 2. O sistema recupera as transações financeiras associadas ao cliente |
| | 3. O sistema processa os dados (total de gastos, receitas e despesas). |
| | 4. O sistema exibe o dashboard com o resumo financeiro. |
|  | |


## Fluxo Alternativo I - Não existe transações cadastradas

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 4.1 O dashboard exibe as informações com valores zerados.|

