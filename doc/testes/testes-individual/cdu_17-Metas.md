### 2.1. CDU 17 - Manter Meta
Este documento especifica os testes que devem ser realizados para o caso de uso 17 - Manter Meta. Ele contém as informações necessárias para a construção dos scripts de teste, como preparação do ambiente, dados de entrada, classes de equivalência e resultados esperados, seguindo o padrão da documentação do projeto Na ponta do Lápis.

#### Especificação do CDU
- **Ator principal:** Cliente.
- **Atores secundários:** N/A.
- **Resumo:** Ao apertar no botão "Meta", é apresentado um quadro com todas as metas já criadas e seus devidos progressos, sendo possível a adição, edição e exclusão das mesmas.
- **Pré-condição:** O cliente deverá estar autenticado.
- **Pós-condição:** As ações do cliente serão salvas no banco de dados.

---

#### Casos Essenciais derivados da Partição de Equivalência
Abaixo estão os casos de teste estruturados para a funcionalidade de gestão de metas (criação, edição e exclusão), cobrindo cenários válidos e inválidos dos campos do formulário.

##### Gestão de Metas
| Cliente Autenticado | Nome da Meta | Valor | Data Limite | Tipo de Meta | Conta | Descrição | Ação / Operação | Resultado Esperado | Situação |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Sim | "Viagem de Férias" | 3500.00 | `2027-12-31` (Futura) | Poupança | Conta Corrente | "Meta para viagem de fim de ano" | Criar Nova Meta | Meta criada com sucesso e salva na persistência | Não executado |
| Sim | `""` (Vazio) | 1000.00 | `2026-10-15` | Gasto | Conta Poupança | "Reduzir gastos" | Criar Nova Meta | Erro de validação: "Nome da meta é obrigatório" | Não executado |
| Sim | "Reserva" | -500.00 | `2026-11-30` | Poupança | Conta Corrente | N/A | Criar Nova Meta | Erro de validação: "O valor deve ser positivo" | Não executado |
| Sim | "Investimento" | 2000.00 | `2025-01-01` (Passado) | Prazo Fixo | Conta Corrente | N/A | Criar Nova Meta | Erro de validação: "A data limite deve ser da data atual em diante" | Não executado |
| Sim | "Quitar Cartão" | 1500.00 | `2026-12-01` | Dívida | Conta Inválida / Inexistente | N/A | Criar Nova Meta | Erro de validação de chave estrangeira (conta) | Não executado |
| Sim | "Meta Atualizada" | 4000.00 | `2026-12-31` | Poupança | Conta Corrente | "Atualizado" | Editar Meta Existente | Dados atualizados com sucesso no banco de dados | Não executado |
| Sim | N/A | N/A | N/A | N/A | N/A | N/A | Excluir Meta (Confirmado) | Meta removida da persistência com sucesso | Não executado |
| Não (Não Autenticado) | "Viagem" | 1500.00 | `2026-12-01` | Poupança | Conta Corrente | N/A | Tentar Acessar / Criar Meta | Acesso negado e redirecionamento para o login | Não executado |

---

#### Classes de Equivalência
- **Campos Obrigatórios:** `nome_meta`, `valor`, `data_limite`, `tipo_meta`, `conta`.
- **Campos Opcionais:** `descricao`.

| Campo / Variável | Condições | Classes válidas | Classes inválidas | Resultado esperado |
| :--- | :--- | :--- | :--- | :--- |
| **nome_meta** | Identificação textual da meta | String não vazia contendo o nome da meta | String vazia, nula ou ausente | Erro de validação indicando que o nome da meta é obrigatório |
| **valor** | Valor monetário alvo da meta | Números decimais estritamente positivos ($> 0$) | Zero ($0$), números negativos ($< 0$), texto não numérico ou ausente | Erro de validação de valor monetário positivo |
| **data_limite** | Prazo final para cumprimento da meta | Data igual à data atual ou futura ($data \geq hoje$) | Datas no passado, datas inexistentes, formato corrompido ou ausente | Erro de validação indicando que a data limite deve ser da data atual em diante |
| **tipo_meta** | Categoria da meta baseada em domínio fechado | Valores pertencentes às opções permitidas: "Poupança", "Gasto", "Dívida", "Prazo Fixo" | Valores fora do conjunto permitido, string vazia ou ausente | Erro de validação de domínio fechado para o tipo de meta |
| **conta** | Conta de origem/destino associada (Select de contas cadastradas) | ID numérico correspondente a uma conta existente pertencente ao usuário logado | ID inexistente, conta de outro usuário, valor nulo ou ausente | Erro de validação de chave estrangeira ou conta inválida |
| **descricao** | Detalhes adicionais sobre a meta | String descritiva opcional | Tipo de dado incompatível (se aplicável) | Se ausente, salva sem descrição; se inválido, exibe erro de preenchimento |
| **operacao_exclusao** | Confirmação do ato de exclusão | Confirmação explícita na tela de confirmação de exclusão | Interação inválida ou cancelamento da exclusão | Se confirmado, remove a meta da persistência; se cancelado, mantém o registro inalterado |

---

#### Fluxo Principal e Alternativos (Validação de Execução)

##### 1. Fluxo Principal - Adicionar Nova Meta
Passo a passo para chegar ao resultado:
1. No menu lateral o cliente seleciona a opção "Metas".
2. O sistema apresenta um lista com todas as metas incluindo a porcentagem de progresso, descrição e prazo. Além disso, as opções de edição e exclusão.
3. Ao pressionar o botão de adicionar nova meta, o sistema apresenta um formulário para definir o nome da meta, o valor, a data limite, o tipo de meta (Poupança, Gasto, Dívida, Prazo Fixo), a conta (select de contas cadastradas) e a descrição.
4. O cliente preenche os dados e clica no botão "Salvar".
5. O sistema valida as informações e salva na persistência.

- **Resultado Esperado:** Nova meta gravada na base de dados com sucesso e exibida na listagem.

---

##### 2. Fluxo Alternativo I - Edição de Meta
Passo a passo para chegar ao resultado:
1. No menu lateral o cliente seleciona a opção "Metas".
2. O cliente aperta no botão de editar em uma meta específica.
3. O sistema mostra o mesmo formulário do passo 3, porém já preenchido com as informações previamente inseridas.
4. O cliente altera os dados desejados e clica no botão "Salvar" (retorna ao passo 4 do fluxo principal).
5. O sistema valida as novas informações e atualiza o registro na persistência.

- **Resultado Esperado:** Informações da meta atualizadas com sucesso no banco de dados sem duplicação de registros.

---

##### 3. Fluxo Alternativo II - Exclusão de Meta
Passo a passo para chegar ao resultado:
1. No menu lateral o cliente seleciona a opção "Metas".
2. O cliente aperta no botão de Excluir em uma meta desejada.
3. É exibido uma tela de confirmação de exclusão.
4. O cliente confirma a exclusão.
5. O Sistema remove a meta da persistência e atualiza a listagem na interface.

- **Resultado Esperado:** Meta permanentemente removida da base de dados e dos indicadores visuais do cliente.

---
