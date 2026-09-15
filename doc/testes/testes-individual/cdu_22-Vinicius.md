### TeSte do CDU 22 - Convidar Membro à Família -
Este documento especifica os testes que devem ser realizados para o caso de uso 22 - Convidar membro à família. Ele contém as informações necessárias para a construção dos scripts de teste, como preparação do ambiente, dados de entrada, classes de equivalência e resultados esperados, seguindo o padrão da documentação do projeto Na ponta do Lápis.
s
#### Especificação do CDU
- **Ator principal:** Usuário administrador da família.
- **Atores secundários:** Usuário convidado.
- **Resumo:** Na página “Grupo Familiar” o usuário aperta o botão “convidar” para convidar um novo membro à sua família; caso ainda não tenha membros, a família será criada.
- **Pré-condição:** Usuário deve estar cadastrado, autenticado no sistema e administrador da família.
- **Pós-condição:** Um convite é enviado ao usuário convidado; caso necessário, uma família é criada e o usuário criador recebe o papel admin família; o membro só é vinculado à família após aceitar o convite.

---

#### Casos Essenciais derivados da Partição de Equivalência
Abaixo estão os casos de teste estruturados para a funcionalidade de convite e gestão familiar, cobrindo cenários válidos e inválidos dos campos e fluxos do CDU.

##### Gestão de Convites e Grupo Familiar
| Usuário Logado (Perfil) | Existência de Família | Dados do Formulário (E-mail) | Resposta do Convidado | Resultado Esperado | Situação |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Administrador da Família | Possui Família | `convidado@email.com` (Válido) | N/A | HTTP 201/200, convite enviado com sucesso, pop-up "convite enviado com sucesso" | Não executado |
| Administrador da Família | Não Possui Família | Formulário de criação preenchido corretamente | N/A | Família criada, usuário recebe papel de admin, pop-up de confirmação e redirecionamento para convidar membro | Não executado |
| Administrador da Família | Possui Família | `""` (Vazio / Inválido) | N/A | HTTP 400, mensagem "Dado(s) Inválido(s)", exibindo campos em vermelho com feedback textual | Não executado |
| Usuário Convidado | Possui Convite Pendente | N/A | "Aceitar" (Notificação / Sininho) | Vínculo familiar persistido com sucesso, pop-up "Bem-Vindo à Família" | Não executado |
| Usuário Convidado | Possui Convite Pendente | N/A | "Recusar" (Notificação / Sininho) | Página retorna ao formato padrão sem o convite, remetente notificado por e-mail | Não executado |
| Usuário Comum (Sem privilégio) | Possui / Não Possui Família | Qualquer dado | N/A | HTTP 403 (Acesso Negado) / Ocultação da funcionalidade | Não executado |

---

#### Classes de Equivalência
- **Variáveis de Decisão:** `existencia_familia`, `dados_formulario_membro`, `resposta_convite`.

| Variável | Condições | Classes válidas | Classes inválidas | Resultado esperado |
| :--- | :--- | :--- | :--- | :--- |
| **existencia_familia** | Verifica se o usuário logado já possui uma família vinculada | Usuário já possui uma família cadastrada como administrador; ou usuário não possui família (acionando criação automática) | Usuário comum sem privilégios de administrador tentando convidar | Se sem privilégio, nega acesso (HTTP 403); se não possui família, direciona para o fluxo de criação da família |
| **dados_formulario_membro** | Informações de identificação do novo membro preenchidas no formulário | E-mail estruturalmente correto correspondente a um usuário cadastrado no sistema | E-mail vazio, formato inválido, string corrompida, usuário inexistente | Retorna HTTP 400, mensagem "Dado(s) Inválido(s)", exibindo campos em vermelho com feedback textual |
| **resposta_convite** | Interação do usuário convidado com a notificação recebida | Ação explícita de "Aceitar" ou "Recusar" a partir do ícone do sininho de notificações | Interação inválida, manipulação de estado inexistente de convite | Se aceito, persiste o vínculo familiar e exibe pop-up de boas-vindas; se recusado, limpa o convite e envia e-mail ao remetente |

---

#### Fluxo Principal e Alternativos (Validação de Execução)

##### 1. Fluxo Principal - Convidar Novo Membro
Passo a passo para chegar ao resultado:
1. O usuário aperta o botão "+" na caixa de membros.
2. Como o usuário já tem uma família, o sistema retorna um formulário.
3. O usuário preenche o formulário com as informações do novo membro e aperta "enviar".
4. O sistema envia o convite para o outro usuário e retorna um pop-up escrito "convite enviado com sucesso".

- **Resultado Esperado:** Convite enviado com sucesso e exibição do pop-up correspondente.

##### 2. Fluxo Alternativo I - Adicionar Primeiro Membro (Criação de Família)
Passo a passo para chegar ao resultado:
1. O usuário aperta o botão "+" na caixa de membros.
2. Como o usuário não tem família, o sistema retorna um formulário para criar a família.
3. O usuário preenche e aperta o botão "criar".
4. Aparece um pop-up confirmando a criação da família, o sistema persiste os dados de criação da família e o usuário recebe o papel admin família.
5. O sistema retorna para o passo 2 do fluxo principal.

- **Resultado Esperado:** Família criada, usuário promovido a admin e redirecionamento para o envio do convite.

##### 3. Fluxo Alternativo II - Aceitar Convite
Passo a passo para chegar ao resultado:
1. O sistema mostra uma notificação no sino de notificações.
2. O usuário clica no ícone do sininho.
3. O sistema apresenta o(s) convite(s) da(s) respectiva(s) família(s) ao usuário.
4. O usuário clica no botão "aceitar" e aceita o convite da família.
5. O usuário é registrado na família, seus dados são persistidos e o sistema retorna um pop-up "Bem-Vindo à Família".

- **Resultado Esperado:** Vínculo estabelecido e mensagem de boas-vindas exibida.

##### 4. Fluxo Alternativo III - Recusar Convite
Passo a passo para chegar ao resultado:
1. O sistema mostra uma notificação no sino de notificações.
2. O usuário clica no ícone do sininho.
3. O sistema apresenta o(s) convite(s) da(s) respectiva(s) família(s) ao usuário.
4. O usuário clica no botão "recusar" e recusa o convite da família.
5. A página volta ao formato padrão sem o convite e o remetente recebe um e-mail notificando a recusa.

- **Resultado Esperado:** Convite cancelado e e-mail de notificação enviado ao remetente.

##### 5. Fluxo Alternativo IV - Dado do Formulário Inválido
Passo a passo para chegar ao resultado:
1. O usuário preenche o formulário de convite com dados incorretos ou vazios e clica em "enviar".
2. O sistema retorna uma mensagem "Dado(s) Inválido(s)" e mostra novamente o formulário com o/os campo/os inválido/os em vermelho com uma pequena mensagem em vermelho embaixo do campo.
3. O sistema retorna para o passo 3 do fluxo principal.

- **Resultado Esperado:** Bloqueio do envio e sinalização visual dos erros de validação.