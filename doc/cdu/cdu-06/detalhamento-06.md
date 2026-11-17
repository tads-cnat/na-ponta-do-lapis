# CDU 06. Visualizar Dashboard

- **Ator principal**: Cliente.
- **Atores secundários**: N/A.
- **Resumo**: Ao entrar no sistema ou acessar a opção **"Dashboard"** no menu lateral (`/app/dashboard`), o cliente visualiza o panorama consolidado de sua vida financeira: cards de resumo (Saldo Total, Receitas, Despesas e Balanço do período), gráfico de evolução financeira, gráfico de gastos por categoria, acompanhamento das metas ativas, últimas movimentações e opção de exportar relatório financeiro (PDF ou CSV).
- **Pré-condição**: O cliente deve estar autenticado.
- **Pós-Condição**: Dados financeiros consolidados são exibidos em tempo real na tela (ou exportados em arquivo quando solicitado).

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - Acessa `/app/dashboard` após login ou pelo menu lateral. | |
| | 2 - Sistema carrega contas, transações, categorias e metas do usuário autenticado. |
| | 3 - Sistema calcula e exibe os cards de resumo (Saldo Total, Entradas, Saídas), gráfico de evolução temporal, distribuição de gastos por categoria, progresso das metas e lista de últimas movimentações. |

## Fluxo Alternativo I - Exportar Relatório Financeiro

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.1 - Clica no botão de exportação de relatório no Dashboard. | |
| 1.2 - Seleciona o formato desejado (**PDF** ou **CSV**) e o período. | |
| | 1.3 - Sistema gera o arquivo consolidado com o resumo e as movimentações do período e inicia o download no navegador. |
