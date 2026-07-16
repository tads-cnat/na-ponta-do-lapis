

## Tabela 1 — Fonte (Necessidades e Regras de Negócio) × Requisitos Funcionais

| Fonte | RF01 | RF02 | RF03 | RF04 | RF05 | RF06 | RF07 | RF08 | RF09 | RF10 | RF11 | RF12 | RF13 | RF14 | RF15 | RF16 | RF17 | RF18 | RF19 | RF20 | RF21 | RF22 | RF23 | RF24 | RF25 | RF26 | RF27 | RF28 | RF29 | RF30 | RF31 | RF32 | RF33 | RF34 | RF35 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| *N01 — Aprender a controlar as primeiras rendas (Estudantes)* | X | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | x |
| *N02 — Entender conceitos básicos de finanças pessoais (Estudantes)* | X |  |  |  | X | X |  |  | X |  |  |  |  | X | X |  |  |  |  |  |  |  |  |  |  |  |  |  | x | x | x | x | x |  |  |
| *N03 — Criar hábitos financeiros saudáveis desde cedo (Estudantes)* |  |  | X | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| *N04 — Controlar despesas mensais - aluguel, transporte, etc. (Trabalhadores)* | X | X |  |  | X | X | X | X |  | X | X | X | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | x | x | x | x | x |  | x |
| *N05 — Estabelecer metas de economia ou investimentos (Trabalhadores)* |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  | x |  |
| *N06 — Integrar finanças pessoais e familiares (Trabalhadores)* |  |  |  |  |  |  |  |  |  | X | X |  |  |  |  | X | X | X | X | X | X | X | X |  |  |  |  |  |  |  |  |  |  |  |  |
| *N07 — Incluir posts sobre educação financeira (Administrador)* |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X | X | X | X |  |  |  |  |  |  |  |
| *N08 — Atribuir o papel de Administrador a outro usuário (Administrador)* ⚠️ |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| *RN01 — Não logados só podem acessar os posts* | X | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |
| *RN02 — Usuário só pode ter uma família* |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| *RN03 — Usuário só pode participar de uma família* |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  | X |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| *RN04 — Só o fundador pode excluir o grupo familiar* |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| *RN05 — Só administradores podem convidar para a família* |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| *RN06 — Só administradores podem remover membros da família* |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |
| *RN07 — Só administradores podem promover membros a administrador* |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |
| *RN08 — Um usuário não pode ter 2+ contas financeiras com o mesmo nome* |  |  |  |  |  |  |  |  |  | X |  |  | X |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |
| *RN09 — Só administradores podem gerenciar posts* |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  |  | X |  | X | X |  |  |  |  |  |  |  |

## Tabela 2 — Requisitos Funcionais × Requisitos Não-Funcionais

| Requisito Funcional | NF01<br>Interoperabilidade | NF02<br>Adaptabilidade (BD) | NF03<br>Confidencialidade | NF04<br>Responsividade | NF05<br>Acessibilidade | NF06<br>Navegação | NF07<br>Tema Visual |
| --- | --- | --- | --- | --- | --- | --- | --- |
| *RF01 — Cadastro de usuário* |  | x | X | x | x |  | x |
| *RF02 — Login de usuário* |  | x | X | x | x |  | x |
| *RF03 — Logout de usuário* |  | x | X | x | x |  | x |
| *RF04 — Editar conta de usuário* |  | x | X | x | x |  | x |
| *RF05 — Cadastrar transação* | X | x |  | x | x |  | x |
| *RF06 — Visualizar transação* |  | x |  | x | x | X | x |
| *RF07 — Excluir transação* |  | x |  | x | x |  | x |
| *RF08 — Editar transação* |  | x |  | x | x |  | x |
| *RF09 — Visualização de relatórios* | X | x |  | x | x | X | x |
| *RF10 — Cadastrar conta financeira* |  | x |  | x | x |  | x |
| *RF11 — Visualizar conta financeira* |  | x |  | x | x |  | x |
| *RF12 — Excluir conta financeira* |  | x |  | x | x |  | x |
| *RF13 — Editar conta financeira* |  | x |  | x | x |  | x |
| *RF14 — Visualizar categoria* |  | x |  | x | x |  | x |
| *RF15 — Vincular categoria* |  | x |  | x | x |  | x |
| *RF16 — Cadastrar família* |  | x |  | x | x |  | x |
| *RF17 — Visualizar família* |  | x |  | x | x |  | x |
| *RF18 — Excluir família* |  | x |  | x | x |  | x |
| *RF19 — Editar família* |  | x |  | x | x |  | x |
| *RF20 — Convidar membro para família* |  | x |  | x | x |  | x |
| *RF21 — Aceitar convite de família* |  | x |  | x | x |  | x |
| *RF22 — Remover membro da família* |  | x |  | x | x |  | x |
| *RF23 — Promover membro a admin da família* |  | x |  | x | x |  | x |
| *RF24 — Sistema de metas* |  | x |  | x | x |  | x |
| *RF25 — Cadastrar posts* |  | x |  | x | x |  | x |
| *RF26 — Visualizar posts* |  | x |  | x | x | X | x |
| *RF27 — Excluir posts* |  | x |  | x | x |  | x |
| *RF28 — Editar posts* |  | x |  | x | x |  | x |
| *RF29 — Cadastrar marcador* |  | x |  | x | x |  | x |
| *RF30 — Vincular marcador* |  | x |  | x | x |  | x |
| *RF31 — Visualizar marcador* |  | x |  | x | x |  | x |
| *RF32 — Excluir marcador* |  | x |  | x | x |  | x |
| *RF33 — Editar marcador* |  | x |  | x | x |  | x |
| *RF34 — Visualizar cotação* | X | x |  | x | x |  | x |
| *RF35 — Login com Google* | X | x | X | x | x |  | x |

