### Teste CDU 21 - Remover Membro da Família
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