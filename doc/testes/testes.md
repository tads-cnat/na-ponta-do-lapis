# Documento Geral de Casos de Teste

## Introdução
Neste documento reúnem-se casos de testes elaborados para os principais casos de uso da aplicação Na Ponta do Lápis. Os testes buscam assegurar a coerência das funcionalidades, assim, atendendo os requisitos estabelecidos.

Cada caso de teste inclui informações detalhadas sobre o cenário a ser avaliado, os dados de entrada necessários e resultados esperados.

## CDU 02 - Logout

### Fluxo principal

#### Objetivo: 
Deslogar do sistema

#### Variáveis de Entrada:
- Variável 1: Access Token

#### Condições de Negócio:
- Condição 1: O token é alfanumérico

#### Classes de Equivalência
| Classes Válidas             | Classes Inválidas           |
|-----------------------------|-----------------------------|
| Possui `access token` | Não possui `access token` |

#### Testes funcionais
| Access token | Resultado esperado | Resultado obitido | Situação | 
| :-----: | :-----: | :-----: | :-------------------: |
| "ASDFDGFGHSD67688GFGDSFGDKJ..." | Local Storage limpo | ---- | ---- |
| *vazio* | erro(token não encontrado) | ---- | ---- |

---

## CDU 12 - Alterar tema

### Fluxo principal

#### Objetivo: 
Mudar o tema do sistema entre claro e escuro

#### Variáveis de Entrada:
- Variável 1: Tema

#### Condições de Negócio:
- Condição 1: O tema é uma string
- Condição 2: O tema dever ser "escuro" ou "claro"

#### Classes de Equivalência
| Classes Válidas             | Classes Inválidas           |
|-----------------------------|-----------------------------|
| Possui `tema` | Não possui `tema` |
| `tema` válido | `tema` inválido |

#### Testes funcionais
| Tema | Resultado esperado | Resultado obitido | Situação | 
| :-----: | :-----: | :-----: | :-------------------: |
| "escuro" | Tema escuro aplicado | ---- | ---- |
| *vazio* | erro(tema não encontrado) | ---- | ---- |
| "vermelho" | erro(tema inválido) | ---- | ---- |

---

## Documento de Casos de Teste do CDU 20 - Atualizar Perfil

## Introdução

Neste documento reúne-se casos de testes elaborados para o fluxo principal do caso de uso. Os testes buscam assegurar a coerência das funcionalidades, assim, atendendo os requisitos estabelecidos.
Cada caso de teste inclui informações sobre o cenário a ser avaliado, os dados de entrada necessários e resultados esperados.

## Especificação do CDU

- **Ator principal**: Cliente.

- **Atores secundários**: N/A.
- **Resumo**: O cliente acessa a área de configurações para alterar informações do seu perfil como foto e email.
- **Pré-condição**: O cliente deve estar devidamente autenticado.
- **Pós-Condição**: Suas informações são atualizadas e  alteradas no sistema.

## Fluxo principal

### Objetivo

Atualizar informações básicas do perfil do cliente autenticado

### Variáveis de Entrada

- Variável 1: Nome
- Variável 2: Username
- Variável 3: Email

### Condições de Negócio

- A(s) variável(is) que será(ão) alterada(s) não pode(rão) ser enviada(s) vazia(s)

- O nome deve ter pelo menos três caracteres e menos que 100 caracteres
- O Email deve estar no formato válido para emails (#@#.#) e menos de 254 caracteres
- O username deve ter menos de 100 caracteres

### Classes de Equivalência

| Variável | Classes Válidas | Classes Inválidas |
| -------- | --------------- | ----------------- |
| **Nome** | 1. Preenchido; 2. Mais que três caracteres e menos de 100 caracteres. | 3. Vazio; 4. 3 > Nome ou Nome > 100. |
| **Username** | 5. Preenchido; 6. Menos de 100 caracteres. | 7. Vazio; 8. Username > 100. |
| **Email** | 9. Preenchido; 10. Formato correto. 11. Menos de 254 caracteres | 12. Vazio; 13. Não estar no formato; 14. Email > 254. |

### Testes funcionais

#### Usuário de Referência

- Nome: Clarice Lispector

- Username: @Clarice
- Email: clarice@gmail.com

##### Os detaques em negrito significam as alterações, portanto, campos não destacados não estãos sendo alterados

---

| ID | Nome | Username | Email | Resultado Esperado | Situação | Classe(s) Coberta(s) |
| -- | ---- | -------- | ----- | ------------------ | -------- | -------------------- |
| **CT01** | **Cla** | @Clarice | clarice@gmail.com | Nome alterado com sucesso! | Pendente | 1, 2 |
| **CT02** | Clarice Lispector | **@ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | clarice@gmail.com | Username alterado com sucesso! | Pendente | 5, 6 |
| **CT03** | Clarice Lispector | @Clarice | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm** | Email alterado com sucesso! | Pendente | 9, 10, 11 |
| **CT04** | **Cla** | **@ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | clarice@gmail.com | Nome e Username alterados com sucesso! | Pendente | 1, 2, 5, 6 |
| **CT05** | **Cla** | @Clarice | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm** | Nome e Email alterados com sucesso! | Pendente | 1, 2, 9, 10, 11 |
| **CT06** | Clarice Lispector | **@ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm** | Username e Email alterados com sucesso! | Pendente | 5, 6, 9, 10, 11 |
| **CT07** | **ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | **@CClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIsso** | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm** | Nome, Username e Email alterados com sucesso! | Pendente | 1, 2, 5, 6, 9, 10, 11 |
| **CT08** | *vazio* | @Clarice | clarice@gmail.com | Erro: Nome não pode ser vazio | Pendente | 3 |
| **CT09** | **Cl** | @Clarice | clarice@gmail.com | Erro: Nome deve conter pelo menos três caracteres e menos que 100 caracteres | Pendente | 4 |
| **CT10** | **ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIssoX** | @Clarice | clarice@gmail.com | Erro: Nome deve conter pelo menos três caracteres e menos que 100 caracteres | Pendente | 4 |
| **CT11** | Clarice Lispector | *vazio* | clarice@gmail.com | Erro: Username vazio | Pendente | 7 |
| **CT12** | Clarice Lispector | **@ClariceAMaiorDeTodasElaQuerDescobrirOsLimitesDeCaracteresDesseSiteElaEhDoidaDemaisNãoAguentaMaisIssoX** | clarice@gmail.com | Erro: Username deve ter menos de 100 caracteres | Pendente | 8 |
| **CT13** | Clarice Lispector | @Clarice | *vazio* | Erro: Email não pode ser vazio | Pendente | 11 |
| **CT14** | Clarice Lispector | @Clarice | claricegmailcom | Erro: Email fora de formato | Pendente | 12 |
| **CT15** | Clarice Lispector | @Clarice | **clariceaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb.cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccoommmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm**| Erro: O Email deve ter menos de 254 caracteres| Pendente | 12 |

---

## Documento de Casos de Teste do CDU 22 - Convidar Membro à Família

## Introdução

Neste documento reúnem-se os casos de testes elaborados para o fluxo principal e fluxos de exceção do caso de uso. Os testes buscam assegurar a coerência das funcionalidades, atendendo aos requisitos estabelecidos. Cada caso de teste inclui informações sobre o cenário a ser avaliado, os dados de entrada necessários e os resultados esperados.

## Especificação do CDU

- **Ator principal:** Usuário administrador da família.

- **Atores secundários:** Usuário convidado.
- **Resumo:** Na página “Grupo Familiar” o usuário aperta o botão “convidar” para convidar um novo membro à sua família; caso ainda não tenha membros, a família será criada.
- **Pré-condição:** Usuário deve estar cadastrado, autenticado no sistema e ser administrador da família.
- **Pós-condição:** Um convite é enviado ao usuário convidado; caso necessário, uma família é criada e o usuário criador recebe o papel admin família; o membro só é vinculado à família após aceitar o convite.

## Fluxo Principal

### Objetivo

Preencher um formulário para enviar um convite de entrada a um grupo familiar para um usuário do sistema

### Variáveis de Entrada

- Variável 1: Username
- Variável 2: Email

### Condições de Negócio

- Pelo menos um dos campos (Username ou Email) deve ser preenchido, o sistema deve validar se ambos estão vazios.

- O Username ou Email informado deve pertencer a um usuário válido e já cadastrado no sistema.
- O Email deve estar no formato válido para emails (#@#.#)
- O usuário convidado não pode fazer parte da família atual do administrador.
- Caso ambos os campos sejam preenchidos corretamente, o sistema primeiramente irá tentar convidar pelo Username antes do Email

### Classes de Equivalência

| Variável | Classes Válidas | Classes Inválidas |
| -------- | --------------- | ----------------- |
| **Username** | 1. Preenchido; 2. Usuário existe; 3. Usuário não é membro da família. 4. Vazio (apenas se o Email estiver preenchido). | 5. Vazio (se o Email também estiver vazio); 6. Usuário inexistente; 7. Usuário já é membro da família atual. |
| **Email** | 8. Preenchido; 9. Formato correto; 10. Usuário existe; 11. Usuário não é membro da família; | 12. Vazio (se o Username também estiver vazio); 13. Não estar no formato; 14. Usuário inexistente. 15. Usuário já é membro da família atual. |

### Testes Funcionais

| ID | Username | Email | Resultado Esperado | Situação | Classe(s) Coberta(s) |
| -- | -------- | ----- | ------------------ | -------- | -------------------- |
| **CT01** | @Clarice | *vazio* | Convite enviado com sucesso. Família criada | Pendente | 1, 2, 3 |
| **CT02** | *vazio* | clarice@gmail.com | Convite enviado com sucesso. Família criada | Pendente | 4, 8, 9, 10, 11 |
| **CT03** | @Clarice | clarice@gmail.com | Convite enviado com sucesso. Família criada | Pendente | 1, 2, 3 |
| **CT04** | *vazio* | *vazio* | Erro: "Preencha o Username ou o Email para convidar". Convite não enviado. | Pendente | 5, 12 |
| **CT05** | @123 | *vazio* | Usuário inexistente. Convite não enviado. | Pendente | 6 |
| **CT06** | @Clarice | *vazio* | Erro: "Este usuário já pertence à sua família." | Pendente | 7 |
| **CT07** | *vazio* | testeemail | Erro de validação de formato de e-mail. Convite não enviado. | Pendente | 13 |
| **CT08** | *vazio* | JogosEGames@gmail.com | Usuário inválido. Convite não enviado. | Pendente | 14 |
| **CT09** | *vazio* | clarice@gmail.com | Erro: "Este usuário já pertence à sua família." | Pendente | 15 |

---

## Teste CDU 17 - Manter Meta
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

## Teste CDU 21 - Remover Membro da Família
Este documento especifica os testes que devem ser realizados para o caso de uso 21 - Remover Membro da Família. Ele contém as informações necessárias para a construção dos scripts de teste, como preparação do ambiente, dados de entrada, classes de equivalência e resultados esperados, seguindo o padrão da documentação do projeto Na ponta do Lápis.

#### Especificação do CDU
- **Ator principal:** Usuário administrador da família.
- **Atores secundários:** N/A.
- **Resumo:** Na página "Grupo Familiar" o usuário administrador da família aperta no botão para remover um membro da família.
- **Pré-condição:** Usuário cadastrado, autenticado e administrador da família.
- **Pós-condição:** O membro é desligado da família no banco de dados.

---

#### Casos Essenciais derivados da Partição de Equivalência
Abaixo estão os casos de teste estruturados para a funcionalidade de remoção de membro, cobrindo cenários válidos e de restrição de regra de negócio do CDU.

##### Gestão de Remoção de Membros
| Usuário Logado (Perfil) | Vínculo e Status do Membro | Dados do Formulário / Ação | Resultado Esperado | Situação |
| :--- | :--- | :--- | :--- | :--- |
| Administrador da Família | Membro ativo pertencente à mesma família | Dados do membro selecionados corretamente e botão "remover membro" acionado | HTTP 200/204, membro desligado da família no banco de dados, mensagem "Membro Removido" | Não executado |
| Administrador da Família | Membro inexistente ou já removido | Tentativa de remoção de ID inválido / não vinculado | HTTP 400 ou HTTP 404, erro de validação: "Membro não encontrado ou inválido" | Não executado |
| Administrador da Família | O próprio administrador (tentando se remover) | Acionamento da remoção do próprio perfil de admin | HTTP 400, mensagem de erro impedindo a auto-remoção do único administrador | Não executado |
| Usuário Comum (Sem privilégio) | Membro ativo da família | Tentativa de acesso à funcionalidade de exclusão | HTTP 403 (Acesso Negado) / Botão de remoção ocultado ou desabilitado | Não executado |

---

#### Classes de Equivalência
- **Variáveis de Decisão:** `perfil_usuario_logado`, `status_vinculo_membro`, `dados_formulario_remocao`.

| Variável | Condições | Classes válidas | Classes inválidas | Resultado esperado |
| :--- | :--- | :--- | :--- | :--- |
| **perfil_usuario_logado** | Verifica se o usuário possui permissão administrativa | Usuário autenticado com papel de Administrador da Família | Usuário comum sem privilégios administrativos, usuário não autenticado | Se sem privilégio, nega acesso (HTTP 403); se não autenticado, redireciona para login (HTTP 401) |
| **status_vinculo_membro** | Verifica se o alvo da remoção pertence ao grupo familiar | Membro ativo vinculado diretamente à mesma família do administrador logado | Membro de outra família, usuário inexistente, ou auto-remoção do administrador | Se inválido ou de outra família, retorna HTTP 404/400 com erro correspondente |
| **dados_formulario_remocao** | Informações do formulário preenchidas para efetivar a exclusão | Dados de identificação do membro corretos e preenchidos | Campos em branco, dados corrompidos ou alterados via manipulação de requisição | Retorna HTTP 400, mensagem indicando dados inválidos ou falha no processamento |

---

#### Fluxo Principal e Alternativos (Validação de Execução)

##### 1. Fluxo Principal - Remover Membro da Família
Passo a passo para chegar ao resultado:
1. O usuário administrador aperta o botão "-" na caixa de membros na página "Grupo Familiar".
2. O sistema retorna um formulário para preencher os dados de seleção do membro.
3. O usuário preenche os dados necessários e aperta o botão de "remover membro".
4. O sistema processa a exclusão e retorna a mensagem de "Membro Removido".

- **Resultado Esperado:** Membro desligado com sucesso do grupo familiar na base de dados e feedback visual exibido ao administrador.

##### 2. Fluxo Alternativo I - Tentativa de Remoção por Usuário Comum
Passo a passo para chegar ao resultado:
1. O usuário sem privilégios administrativos tenta acessar a opção de remoção de membros na interface.
2. O sistema identifica a ausência de permissão (ou oculta o botão "-" na caixa de membros).
3. Caso a requisição seja forçada via URL/API direta, o sistema bloqueia a ação.

- **Resultado Esperado:** Acesso negado (HTTP 403) e proteção da integridade do grupo familiar.

##### 3. Fluxo Alternativo II - Dados de Formulário Inválidos
Passo a passo para chegar ao resultado:
1. O administrador aciona a remoção, mas submete o formulário com dados incorretos, campos vazios ou ID de membro inexistente.
2. O sistema valida as informações enviadas.
3. O sistema bloqueia a transação e exibe uma mensagem de erro informando a falha nos dados.

- **Resultado Esperado:** Nenhuma alteração é efetuada no banco de dados e os erros de validação são evidenciados na interface.

---

## Documentação de Testes Funcionais - CDU 18

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

---

## Documentação de Testes Funcionais - CDU 23

## 1. Introdução

Este documento reúne os casos de teste desenvolvidos para a validação funcional da aplicação **Na ponta do Lápis**, no âmbito do PDS Corporativo. A documentação abrange a especificação detalhada dos cenários de teste aplicados exclusivamente ao **CDU 023 - Tornar Administrador da Família**, utilizando a técnica de Partição de Equivalência para validar as permissões e fluxos de alteração de papel do usuário. O objetivo é analisar apenas o fluxo desse caso de uso para garantir que a atribuição de privilégios de administração familiar ocorra conforme as regras de negócio estabelecidas.

### 1.1 Visão geral

O documento apresenta a estrutura do planejamento de testes focado no **CDU 023 - Tornar Administrador da Família**, contemplando:
- Especificação do caso de uso, atores e pré/pós-condições;
- Matrizes de casos de teste derivados de partições de equivalência;
- Mapeamento de classes de equivalência para contexto, permissões e estado do membro;
- Passo a passo detalhado dos fluxos principal e alternativo;
- Validações e regras de negócio no nível de modelos de dados e permissões.

---

## 2. Testes Funcionais

### 2.1. CDU 23 - Tornar Administrador da Família

Este documento especifica os testes que devem ser realizados para o caso de uso **23 - Tornar Administrador da Família**. Ele contém as informações necessárias para a construção dos scripts de teste, como preparação do ambiente, dados de entrada, classes de equivalência e resultados esperados, seguindo o padrão da documentação do projeto Na ponta do Lápis.

#### Especificação do CDU

* **Ator principal:** Administrador da Família
* **Atores secundários:** Cliente
* **Resumo:** O Administrador acessa a aba de "Membros" e seleciona um dos membros de sua família. Vendo suas informações detalhadas e históricos, ele clica na opção de "Tornar Administrador da Família" e a operação é efetuada. Caso o membro selecionado já seja um administrador da família, a opção estará desabilitada.
* **Pré-condição:** Administrador da Família devidamente autenticado.
* **Pós-condição:** O cliente envolvido se torna um Administrador da Família.

---

#### Casos Essenciais derivados da Partição de Equivalência

Abaixo estão os casos de teste estruturados para a funcionalidade de promoção a Administrador da Família, cobrindo cenários válidos e de restrição de regra de negócio.

##### Alteração de Papel de Membro da Família

| Usuário Autenticado | Membro Selecionado | Estado do Membro | Vínculo Familiar | Resultado Esperado | Situação |
|---|---|---|---|---|---|
| Administrador A | Cliente B | Membro Comum (`is_admin=False`) | Mesma família | HTTP 200, membro promovido a administrador, mensagem "O Cliente B agora é um Administrador da Família" | Não executado |
| Administrador A | Cliente C | Já é Administrador (`is_admin=True`) | Mesma família | Botão desabilitado na interface (opaca). Tativa via requisição direta retorna HTTP 400 com mensagem "A operação não pode ser feita pois este membro já é um Administrador da Família" | Não executado |
| Cliente Comum B | Cliente D | Membro Comum (`is_admin=False`) | Mesma família | HTTP 403 (Acesso Negado), opção não visível/disponível no sistema | Não executado |
| Administrador A | Cliente E | Membro Comum (`is_admin=False`) | Família Diferente / Sem Vínculo | HTTP 404 ou HTTP 403, membro não encontrado na família do administrador logado | Não executado |

---

#### Classes de Equivalência

* **Campos / Variáveis de Decisão:** `perfil_usuario_logado`, `estado_membro_selecionado`, `vinculo_familia`.

| Variável | Condições | Classes válidas | Classes inválidas | Resultado esperado |
|---|---|---|---|---|
| `perfil_usuario_logado` | Usuário deve ser Administrador da Família | Usuário com permissão de Administrador da Família (`is_admin_familia=True`) | Usuário com perfil de Cliente Comum, usuário não autenticado | Se inválido, nega acesso (HTTP 401/403) ou oculta a funcionalidade |
| `estado_membro_selecionado` | Membro selecionado deve ter papel de Cliente comum | Membro pertencente à família que **não** possui papel de administrador (`is_admin=False`) | Membro que **já é** Administrador da Família (`is_admin=True`) | Se já for administrador, exibe botão desabilitado ou erro indicando que a operação é inviável |
| `vinculo_familia` | O membro deve pertencer à mesma família do Administrador | Membro associado à mesma família do usuário autenticado | Membro de outra família, usuário não associado à família | Se de outra família, nega acesso/operação (HTTP 404/403) |

---

#### Fluxo Principal e Alternativos (Validação de Execução)

##### 1. Fluxo Principal - Tornar Administrador da Família

**Passo a passo para chegar ao resultado:**
1. O Administrador da Família realiza a autenticação no sistema.
2. O Administrador da Família acessa a aba de "Membros".
3. O Sistema exibe a listagem dos usuários vinculados à família.
4. O Administrador da Família seleciona um membro que possui o papel de cliente comum.
5. O Sistema exibe a visão detalhada do membro (histórico de transações) acompanhada da opção "Tornar Administrador da Família".
6. O Administrador da Família clica na opção "Tornar Administrador da Família" e confirma a ação.
7. O Sistema processa a alteração no banco de dados e exibe a mensagem: *"O [Nome do Cliente] agora é um Administrador da Família"*.

* **Resultado Esperado:** O perfil do membro é atualizado para Administrador da Família na base de dados e a mensagem de confirmação é apresentada.

##### 2. Fluxo Alternativo I - O Usuário já é um Administrador da Família

**Passo a passo para chegar ao resultado:**
1. O Administrador da Família realiza a autenticação no sistema.
2. O Administrador da Família acessa a aba de "Membros".
3. O Sistema exibe a listagem dos usuários vinculados à família.
4. O Administrador da Família seleciona um membro que **já é** um Administrador da Família.
5. O Sistema exibe a visão detalhada do membro e apresenta a opção "Tornar Administrador da Família" desabilitada (evidenciada por cor de fonte menos opaca).
6. Caso o Administrador tente clicar/selecionar a opção, o Sistema exibe a mensagem: *"A operação não pode ser feita pois este membro já é um Administrador da Família"*.
7. O Sistema permanece na tela de detalhes do membro.

* **Resultado Esperado:** Impede a execução repetida da operação, mantendo a integridade dos papéis dos membros da família.

---

#### Regras dos Models que Impactam o CDU

* `MembroFamilia.papel` (ou `is_admin`): define a atribuição de permissões administrativas dentro do grupo familiar no banco de dados.
* `Familia`: o isolamento de dados garante que um Administrador da Família só possa visualizar e alterar o papel de membros pertencentes ao seu próprio grupo familiar.
* `MembroFamilia.status`: impede a alteração de papel para membros com status inativo ou removidos do grupo familiar.

---
