# CDU 17. Manter Meta

- **Ator principal**: Cliente.
- **Atores secundários**: Sistema de Conquistas e Notificações.
- **Resumo**: Na página de **Metas** (`/app/metas`), o cliente visualiza suas metas financeiras com progresso percentual, valor acumulado, valor alvo, prazo e ilustração temática (baseada no catálogo de tipos de metas `GET /api/v1/metas/tipos`). Além de criar, editar e excluir metas, o cliente pode **registrar aportes financeiros** (`POST /api/v1/metas/{id}/aportes`) e consultar o histórico de aportes (`GET /api/v1/metas/{id}/aportes`). Ao atingir marcos de progresso ou concluir 100% da meta, o sistema dispara notificações e celebração visual com confetes.
- **Pré-condição**: O cliente deverá estar autenticado.
- **Pós-Condição**: As metas e seus respectivos aportes são persistidos no banco de dados, atualizando o progresso e eventuais conquistas.

## Fluxo Principal (Criar Meta)

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1 - No menu lateral o cliente seleciona a opção **"Metas"** (`/app/metas`). | |
| | 2 - O sistema apresenta a lista de metas com barra de progresso, valor atual/alvo, prazo, ilustração do tipo de meta e botões de ação (Aportar, Editar, Excluir). |
| 3 - Clica no botão de adicionar nova meta. | |
| | 4 - O sistema exibe o modal de criação permitindo escolher um tipo/template de meta (ex.: Reserva de Emergência, Viagem, Estudos, etc.) ou personalizar descrição, valor alvo e prazo. |
| 5 - O cliente preenche os dados e clica em **"Salvar"**. | |
| | 6 - O sistema valida as informações (`POST /api/v1/metas`), persiste a meta e atualiza a listagem. |

## Fluxo Alternativo I - Registrar Aporte na Meta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 3.1 - O cliente clica no botão **"Adicionar Aporte"** em um card de meta. | |
| | 4.1 - O sistema abre o modal de aporte exibindo o saldo atual da meta e o histórico de aportes anteriores (`GET /api/v1/metas/{id}/aportes`). |
| 5.1 - O cliente informa o valor do aporte (e observação opcional) e confirma. | |
| | 6.1 - O sistema registra o aporte (`POST /api/v1/metas/{id}/aportes`), recalcula o progresso da meta e, caso atinja 100%, exibe o modal de celebração com confetes e registra notificação/conquista. |

## Fluxo Alternativo II - Edição de Meta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 3.2 - O cliente clica no botão de editar da meta. | |
| | 4.2 - O sistema exibe o formulário preenchido com os dados atuais da meta. |
| 5.2 - O cliente altera os dados desejados e salva (`PUT /api/v1/metas/{id}`). | |

## Fluxo Alternativo III - Exclusão de Meta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 3.3 - O cliente clica no botão de excluir da meta. | |
| | 4.3 - É exibida a confirmação de exclusão. |
| 5.3 - O cliente confirma a exclusão (`DELETE /api/v1/metas/{id}`). | |
| | 6.3 - O sistema remove a meta da persistência e atualiza a tela. |
