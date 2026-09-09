# Documentação de Testes Funcionais

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

### 2.1. CDU 023 - Tornar Administrador da Família

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
