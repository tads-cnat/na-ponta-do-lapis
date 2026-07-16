# Atributos dos Requisitos

## 1. Definição dos 6 atributos escolhidos

| Atributo | Definição | Valores possíveis |
| --- | --- | --- |
| **Tipo** | Classifica a natureza do requisito, distinguindo capacidades do sistema (funcionais) de restrições de qualidade (não-funcionais). | Funcional, Não-Funcional |
| **Origem/Fonte** | Identifica a necessidade do usuário (Seção 5 do Documento de Visão) ou o critério de qualidade (Seção 9) que deu origem ao requisito, permitindo a rastreabilidade reversa. | Código da necessidade (N01–N08) ou seção do documento |
| **Prioridade** | Indica a importância do requisito para o lançamento de uma versão utilizável do sistema, conforme já classificado no Documento de Visão. | Alta, Média, Baixa |
| **Estabilidade** | Estima a probabilidade de o requisito mudar ao longo do desenvolvimento, com base na maturidade da funcionalidade e na dependência de decisões ainda em aberto (ex.: integrações externas, escopo do MVP). | Estável, Instável |
| **Esforço Estimado** | Estimativa relativa de esforço de implementação, considerando complexidade técnica, integrações e validações envolvidas. | Baixo, Médio, Alto |
| **Status** | Situação atual do requisito no ciclo de vida do projeto. | Proposto, Aprovado, Em desenvolvimento, Implementado, Verificado |

> Os atributos **Tipo**, **Origem/Fonte** e **Prioridade** foram extraídos diretamente do Documento de Visão. Os atributos **Estabilidade**, **Esforço Estimado** e **Status** são estimativas da equipe (não constam explicitamente no documento) e devem ser revisados/atualizados conforme o projeto evolui.

## 2. Tabela de Atributos — Requisitos Funcionais

| ID | Nome | Tipo | Origem/Fonte | Prioridade | Estabilidade | Esforço Estimado | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF01 | Cadastro de usuário | Funcional | N01, N02, N04 | Alta | Estável | Médio | Em desenvolvimento |
| RF02 | Login de usuário | Funcional | N01, N04 | Alta | Estável | Baixo | Em desenvolvimento |
| RF03 | Logout de usuário | Funcional | N03 | Alta | Estável | Baixo | Em desenvolvimento |
| RF04 | Editar conta de usuário | Funcional | N03 | Alta | Estável | Baixo | Em desenvolvimento |
| RF05 | Cadastrar transação | Funcional | N02, N04 | Alta | Estável | Médio | Em desenvolvimento |
| RF06 | Visualizar transação | Funcional | N02, N04 | Alta | Estável | Baixo | Em desenvolvimento |
| RF07 | Excluir transação | Funcional | N04 | Alta | Estável | Baixo | Em desenvolvimento |
| RF08 | Editar transação | Funcional | N04 | Alta | Estável | Baixo | Em desenvolvimento |
| RF09 | Visualização de relatórios | Funcional | N02, N05 | Alta | Estável | Alto | Em desenvolvimento |
| RF10 | Cadastrar conta financeira | Funcional | N04, N06 | Média | Estável | Baixo | Aprovado |
| RF11 | Visualizar conta financeira | Funcional | N04, N06 | Média | Estável | Baixo | Aprovado |
| RF12 | Excluir conta financeira | Funcional | N04 | Média | Estável | Baixo | Aprovado |
| RF13 | Editar conta financeira | Funcional | N04 | Média | Estável | Baixo | Aprovado |
| RF14 | Visualizar categoria | Funcional | N02 | Média | Estável | Baixo | Aprovado |
| RF15 | Vincular categoria | Funcional | N02 | Média | Estável | Baixo | Aprovado |
| RF16 | Cadastrar família | Funcional | N06 | Baixa | Instável | Médio | Proposto |
| RF17 | Visualizar família | Funcional | N06 | Baixa | Instável | Médio | Proposto |
| RF18 | Excluir família | Funcional | N06 | Baixa | Instável | Baixo | Proposto |
| RF19 | Editar família | Funcional | N06 | Baixa | Instável | Baixo | Proposto |
| RF20 | Convidar membro para família | Funcional | N06 | Baixa | Instável | Médio | Proposto |
| RF21 | Aceitar convite de família | Funcional | N06 | Baixa | Instável | Baixo | Proposto |
| RF22 | Remover membro da família | Funcional | N06 | Baixa | Instável | Baixo | Proposto |
| RF23 | Promover membro a admin da família | Funcional | N06 | Baixa | Instável | Baixo | Proposto |
| RF24 | Sistema de metas | Funcional | N05 | Baixa | Instável | Alto | Proposto |
| RF25 | Cadastrar posts | Funcional | N07 | Baixa | Estável | Baixo | Proposto |
| RF26 | Visualizar posts | Funcional | N07 | Baixa | Estável | Baixo | Proposto |
| RF27 | Excluir posts | Funcional | N07 | Baixa | Estável | Baixo | Proposto |
| RF28 | Editar posts | Funcional | N07 | Baixa | Estável | Baixo | Proposto |
| RF29 | Cadastrar marcador | Funcional | N02 (inferida) | Baixa | Instável | Baixo | Proposto |
| RF30 | Vincular marcador | Funcional | N02 (inferida) | Baixa | Instável | Baixo | Proposto |
| RF31 | Visualizar marcador | Funcional | N02 (inferida) | Baixa | Instável | Baixo | Proposto |
| RF32 | Excluir marcador | Funcional | N02 (inferida) | Baixa | Instável | Baixo | Proposto |
| RF33 | Editar marcador | Funcional | N02 (inferida) | Baixa | Instável | Baixo | Proposto |
| RF34 | Visualizar cotação | Funcional | N05 (inferida) | Baixa | Instável | Médio | Proposto |
| RF35 | Login com Google | Funcional | N01 (inferida) | Baixa | Instável | Médio | Proposto |

## 3. Tabela de Atributos — Requisitos Não-Funcionais

| ID | Nome | Tipo | Origem/Fonte | Prioridade | Estabilidade | Esforço Estimado | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| NF01 | Interoperabilidade (Linguagens/Frameworks) | Não-Funcional | Seção 9 (ISO/IEC 25010) | Alta | Estável | Médio | Aprovado |
| NF02 | Adaptabilidade (Banco de Dados) | Não-Funcional | Seção 9 (ISO/IEC 25010) | Alta | Estável | Médio | Aprovado |
| NF03 | Confidencialidade (Criptografia) | Não-Funcional | Seção 9 (ISO/IEC 25010) | Alta | Estável | Médio | Aprovado |
| NF04 | Adaptabilidade (Responsividade) | Não-Funcional | Seção 9 (ISO/IEC 25010) | Média | Estável | Médio | Aprovado |
| NF05 | Assistência ao usuário (Acessibilidade) | Não-Funcional | Seção 9 (ISO/IEC 25010) | Média | Estável | Alto | Aprovado |
| NF06 | Operabilidade (Facilidade de Navegação) | Não-Funcional | Seção 9 (ISO/IEC 25010) | Média | Estável | Médio | Aprovado |
| NF07 | Personalização (Seleção de Tema Visual) | Não-Funcional | Seção 9 (ISO/IEC 25010) | Baixa | Instável | Baixo | Proposto |

