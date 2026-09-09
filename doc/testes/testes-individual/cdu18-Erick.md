# Documentação de Testes Funcionais

## 1. Introdução

Este documento reúne os casos de teste desenvolvidos para a validação funcional da aplicação **Na ponta do Lápis**, no âmbito do PDS Corporativo. A documentação abrange a especificação detalhada dos cenários de teste aplicados exclusivamente ao **CDU 018 - Manter Transação**, utilizando as técnicas de Partição de Equivalência e Análise do Valor Limite. O objetivo é analisar apenas o fluxo desse caso de uso para garantir a integridade do sistema no gerenciamento de transações financeiras, cobrindo as operações de criação, edição e exclusão.

### 1.1 Visão geral

O documento apresenta a estrutura do planejamento de testes focado no **CDU 018 - Manter Transação**, contemplando:
- Especificação do caso de uso, atores e pré/pós-condições;
- Matrizes de casos de teste derivados de partições de equivalência;
- Mapeamento de classes de equivalência para campos obrigatórios e opcionais;
- Passo a passo detalhado dos fluxos principal, alternativos e de exceção;
- Análise do Valor Limite para os campos numéricos e textuais;
- Validações e regras de negócio no nível de modelos de dados.

---

## 2. Testes Funcionais

### 2.1. CDU 018 - Manter Transação

Este documento especifica os testes que devem ser realizados para o caso de uso **18 - Manter Transação**. Ele contém as informações necessárias para a construção dos scripts de teste, como preparação do ambiente, dados de entrada, classes de equivalência, análise de valor limite e resultados esperados, seguindo o padrão da documentação do projeto Na ponta do Lápis.

#### Especificação do CDU

* **Ator principal:** Cliente
* **Atores secundários:** Sistema de Banco de Dados
* **Resumo:** O sistema exibe um formulário com os campos necessários (*descrição, *valor, categoria, *status de pagamento/recebimento, *data, marcador, *conta) para criar e editar, já para excluir, uma janela de confirmação é exibida. O Cliente preenche todos os campos obrigatórios e confirma a operação.
* **Pré-condição:** O Cliente deve estar autenticado no sistema para registrar uma transação.
* **Pós-condição:** O sistema persiste os dados da transação no banco de dados e exibe uma mensagem de confirmação.

---

#### Casos Essenciais derivados da Partição de Equivalência

Abaixo estão os casos de teste estruturados para a criação de transações, cobrindo as partições válidas e inválidas dos campos principais (descrição, valor, data, etc.).

##### Criação de Transação

| Descrição | Valor | Categoria | Status (Pagamento / Recebimento) | Data | Conta | Resultado Esperado | Situação |
|---|---|---|---|---|---|---|---|
| "Compra de Supermercado" | 150.50 | Alimentação | Pagamento | 2026-06-06 | Conta Corrente | HTTP 201, transação criada com sucesso, mensagem "Transação salva com sucesso" | Não executado |
| "" (Vazio) | 50.00 | Transporte | Pagamento | 2026-06-06 | Conta Corrente | HTTP 400, erro de validação: "Campo obrigatório" (descrição) | Não executado |
| "Salário" | 0.00 | Renda | Recebimento | 2026-06-06 | Conta Corrente | HTTP 400, erro de validação: "Valor deve ser positivo" (ou regra específica da aplicação) | Não executado |
| "Compra Inválida" | -50.00 | Lazer | Pagamento | 2026-06-06 | Conta Corrente | HTTP 400, erro de validação: "Valor deve ser positivo" | Não executado |
| "Conta de Luz" | 120.00 | Moradia | Pagamento | 2026-02-30 (Data inválida no calendário) | Conta Corrente | HTTP 400, erro de validação de data | Não executado |
| "Freelance" | 500.00 | Trabalho | Status Inválido ("XYZ") | 2026-06-06 | Conta Corrente | HTTP 400, erro de validação de domínio fechado (status) | Não executado |
| "Consultoria" | 300.00 | Trabalho | Recebimento | 2026-06-06 | ID de Conta Inexistente (9999) | HTTP 400, erro de validação de chave estrangeira (conta) | Não executado |

---

#### Classes de Equivalência

* **Campos Obrigatórios:** `descricao`, `valor`, `status_pagamento_recebimento`, `data`, `conta`.
* **Campos Opcionais:** `categoria`, `marcador`.

| Campo | Condições | Classes válidas | Classes inválidas | Resultado esperado |
|---|---|---|---|---|
| `descricao` | Texto obrigatório | String não vazia (qualquer tamanho aceito pelo campo) | String vazia, ausente, tipo inválido (ex: booleano ou numérico puro) | Inválido retorna HTTP 400 com erro indicando campo obrigatório |
| `valor` | Numérico, deve ser estritamente positivo (> 0) | Números decimais positivos maiores que zero ($> 0$) | Zero ($0$), números negativos ($< 0$), texto não numérico, ausente | Inválido retorna HTTP 400, erro de validação de valor positivo |
| `status_pagamento_recebimento` | Domínio fechado / Choices | Valores pertencentes ao conjunto de opções permitidas (ex: "pagamento", "recebimento") | Valores fora do domínio, string vazia, ausente | Inválido retorna HTTP 400, erro de validação de escolha |
| `data` | Data válida no formato adequado | Data válida existente no calendário (passado, presente ou limite permitido) | Datas inexistentes (ex: 31 de fevereiro), formato de string corrompido, ausente | Inválido retorna HTTP 400, erro de validação de formato de data |
| `conta` | Chave estrangeira (FK) obrigatória | ID numérico correspondente a uma conta existente pertencente ao usuário logado | ID inexistente, ID de conta de outro usuário, valor nulo/ausente | Inválido retorna HTTP 400, erro de integridade ou chave estrangeira |
| `categoria` | Campo opcional | String descritiva válida ou identificador numérico de categoria | Tipo de dado incompatível (se aplicável) | Se inválido, retorna HTTP 400; se ausente, assume padrão (se houver) |

---

#### Fluxo Principal e Alternativos (Validação de Execução)

##### 1. Fluxo Principal - Adicionar Transação

**Passo a passo para chegar ao resultado:**
1. O Cliente realiza a autenticação no sistema.
2. O Cliente acessa a interface principal e clica no botão "Adicionar transação".
3. O Sistema exibe o formulário contendo os campos `*descrição`, `*valor`, `categoria`, `*status de pagamento/recebimento`, `*data`, `marcador` e `*conta`.
4. O Cliente preenche todos os campos obrigatórios com dados válidos e clica em "Salvar".
5. O Sistema valida os dados informados e persiste a transação no banco de dados.
6. O Sistema exibe a mensagem de confirmação: "Transação salva com sucesso".

* **Resultado Esperado:** Transação gravada na base de dados com sucesso e mensagem visível ao usuário.

##### 2. Fluxo Alternativo I - Editar Transação

**Passo a passo para chegar ao resultado:**
1. Com o Cliente autenticado, o mesmo localiza uma transação já existente na listagem.
2. O Cliente clica no ícone de edição associado à transação escolhida.
3. O Sistema exibe o formulário pré-preenchido com os dados atuais da transação selecionada.
4. O Cliente altera os dados desejados (por exemplo, atualiza o valor ou a descrição) e clica em "Salvar".
5. O Sistema valida as novas informações e atualiza os registros persistidos no banco de dados.
6. O Sistema exibe a mensagem de confirmação: "Transação atualizada com sucesso".

* **Resultado Esperado:** Dados atualizados corretamente no banco de dados sem duplicação de registros.

##### 3. Fluxo Alternativo II - Excluir Transação

**Passo a passo para chegar ao resultado:**
1. Com o Cliente autenticado, o mesmo seleciona uma transação existente na listagem.
2. O Cliente clica no ícone de lixeira para solicitar a exclusão.
3. O Sistema exibe uma janela de confirmação contendo a mensagem: "Tem certeza que deseja remover esta transação?".
4. O Cliente clica no botão de confirmação da exclusão.
5. O Sistema remove permanentemente (ou logicamente) a transação do banco de dados.
6. O Sistema atualiza de forma automática o gráfico e a tabela de transações exibidos na interface.
7. O Sistema exibe a mensagem de confirmação: "Transação excluída com sucesso".

* **Resultado Esperado:** Transação removida da listagem e dos indicadores visuais da aplicação.

##### 4. Fluxo de Exceção - Validação de Dados com Erro

**Passo a passo para chegar ao resultado:**
1. O Sistema exibe o formulário de cadastro de transação.
2. O Cliente preenche o formulário deixando campos obrigatórios em branco ou informando valores incorretos (ex: valor negativo) e clica em "Salvar".
3. O Sistema identifica a falha na validação dos campos.
4. O Sistema exibe mensagens de erro específicas abaixo dos campos correspondentes (ex: "Campo obrigatório", "Valor deve ser positivo").
5. O Sistema retorna o fluxo para que o usuário corrija os dados informados.

* **Resultado Esperado:** Registro não persistido na base de dados e exibição clara dos erros de validação.

---

#### Análise de Valor Limite

Para o caso de uso de transações, a análise de valor limite aplica-se principalmente ao campo numérico `valor` e ao comprimento das strings textuais obrigatórias (`descricao`).

##### 1. Campo `valor` (Fronteira de positividade: maior que zero)

Faixa válida: $valor > 0$ (considerando precisão decimal monetária).

| Caso | Entrada (`valor`) | Resultado Esperado |
|---|---|---|
| Abaixo do limite inferior (inválido) | -0.01 (ou -50.00) | HTTP 400, erro de validação: "Valor deve ser positivo" |
| Limite inferior restrito (inválido / zero) | 0.00 | HTTP 400, erro de validação: "Valor deve ser positivo" |
| Imediatamente acima do limite inferior (válido) | 0.01 (menor unidade monetária válida) | Transação criada com sucesso |
| Valor típico intermediário (válido) | 150.00 | Transação criada com sucesso |
| Valor elevado / limite superior de negócio (válido) | 999999.99 | Transação criada com sucesso |

##### 2. Campo `descricao` (Comprimento mínimo)

Faixa válida: String com pelo menos 1 caractere visível.

| Caso | Entrada (`descricao`) | Resultado Esperado |
|---|---|---|
| Abaixo do limite (vazio) | "" (string vazia) | HTTP 400, erro em descrição: "Campo obrigatório" |
| Limite mínimo exato | "A" (1 caractere) | Transação criada com sucesso |
| Tamanho normal / típico | "Pagamento de Internet" | Transação criada com sucesso |

---

#### Regras dos Models que Impactam a Gestão de Transações

* `Transacao.descricao` é um campo textual obrigatório (`max_length` definido pelo projeto); não pode ser nulo ou string vazia.
* `Transacao.valor` armazena valores decimais monetários e deve possuir restrição lógica ou de banco (`positive` / `GT 0`) para evitar valores nulos, zerados ou negativos.
* `Transacao.status_pagamento_recebimento` utiliza um domínio fechado (`choices`) validado tanto na camada de formulário/serializer quanto no banco de dados.
* `Transacao.data` armazena a data da transação e requer validação de formato e consistência temporal.
* `Transacao.conta` é uma chave estrangeira obrigatória (`ForeignKey`) vinculada ao modelo de contas, protegida contra exclusão indevida de contas que possuam lançamentos ativos.
* `Transacao.cliente` (ou usuário associado) é preenchido automaticamente com base na sessão do usuário autenticado para garantir o isolamento de dados entre clientes.
