# Documento de Visão

## Histórico de Revisões

|    Data    | Versão |   Descrição    |               Autores                |
| :--------: | :----: | :------------: | :----------------------------------: |
| 24/09/2025 |  1.0   | Versão inicial | Bruno, Eduardo, Lucas, Pedro, Wagner |
| 25/01/2026 |  2.0   | Segunda versão  | Bruno, Eduardo, Lucas, Pedro, Wagner |
| 28/03/2026 |  3.0   | Terceira versão | Bruno, Eduardo, Eric, Lucas, Pedro, Vinicius, Wagner |

## 1. Visão Geral do Sistema Proposto

Criar um ambiente acolhedor para jovens iniciando a vida adulta, onde os mesmos possam ter uma melhor gestão de gastos e entender como desenvolver uma boa saúde financeira pensando no desenvolvimento de suas carreiras e vida.

## 2. Descrição do Problema

|              |                                                             |
| :----------: | :---------------------------------------------------------: |
| **Problema** |         Dificuldade em organizar os gastos pessoais         |
|  **Afeta**   | Todos que almejam ter uma boa gestão financeira |
| **Impacta**  |                    Liberdade financeira                     |
| **Solução**  |        Criar uma ferramenta que auxilia a organização das finanças         |

## 3. Descrição dos Usuários

|    Usuário    |                     Descrição                     |             Responsabilidades             |
| :-----------: | :-----------------------------------------------: | :---------------------------------------: |
|  Estudantes   |           Jovens no início da carreira            | Planejar e controlar seus gastos pessoais |
| Trabalhadores |              Trabalhadores no geral               | Planejar finanças pessoais ou familiares  |
| Administrador | Usuário que irá gerenciar partes estatica do site | Criar posts para o site |

## 4. Descrição do Ambiente dos Usuários

Os usuários utilizarão o sistema através de um Navegador (Sistema Web), onde ficará acessível, indiferente de seu sistema operacional, tanto em desktop quanto também em dispositivo móvel.

## 5. Principais Necessidades dos Usuários

> Apresentadas no formato de tópicos

1. **Estudantes**

- Aprender a controlar suas primeiras rendas (mesadas, bolsas, estágios).

- Entender conceitos básicos de finanças pessoais (entrada, saída, economia).
- Criar hábitos financeiros saudáveis desde cedo.

2. **Trabalhadores no Geral**

- Controlar despesas mensais com aluguel, transporte, alimentação, etc.

- Estabelecer metas de economia ou investimentos.
- Integrar finanças pessoais e familiares.

3. **Administrador**

- Realizar a inclusão de posts sobre educação financeira de forma prática.

- Atribuir o papel de Administrador a outro usuário.

## 6. Alternativas Concorrentes

1. **Minhas Finanças**

- Estética de componentes do site agradável;

- Paleta de cores harmônica, alinhada com o conteúdo apresentado;
- Gráficos para saldo, entrada, despesa e etc.

2. **Excel**

- Quantidade de recurso;

- Organização e alinhamento;
- Ícones para funcionalidades;
- Personalização de planilha.

3. **Minhas Economias**

- Curso em vídeo ensinando sobre o dinheiro, com dicas e exemplos práticos;

- Integração com vários bancos;

4. **Mobills**

- Dashboard personalizável;

- Interface minimalista;
- Planejamento mensal;
- Tem um dashboard.

## 7. Regras de Negócio

|  ID   |  Regra  |  Descrição   |
| :---: | :-----: | :----------: |
| RN01  | Exclusão no grupo familiar | Somente o fundador do grupo pode  excluir o grupo familiar. |
| RN02  |   Adição no grupo familiar   |     Somente os administradores podem adicionar outra pessoa ao grupo da família.      |
| RN03  |   Remoção de membros   |     Somente os administradores podem remover do grupo familiar.      |

## 8. Requisitos Funcionais

| Código |                              Nome                              |                                                                Descrição                                                                 | Prioridade |
| :----: | :------------------------------------------------------------: | :--------------------------------------------------------------------------------------------------------------------------------------: | :--------: |
|  RF01  | Sistema de Autenticação de usuario e gerenciamento de usuários |                Permitir que o usuario se autentique no sistema, cadastre uma nova conta, edite seus dados e faça logout.                 |    Alta    |
|  RF02  |                    Gerenciamento de gastos                     |                                     Permitir o registro, edição, exclusão e vizualização de gastos.                                      |    Alta    |
|  RF03  |                   Vizualização de Relatórios                   |                        Permitir ao usuário a vizualização de tabelas, gráficos e relatório de evolução de gastos.                        |    Alta    |
|  RF04  |                        Sistema de Metas                        | O sistema terá um sistema de metas baseada nas entradas e gastos, será definido a partir do perfil do usuario ou de forma personalizada. |   Baixa    |
|  RF05  |                    Gerenciamento de Contas.                    |    Permitir o cadastro, edição, exclusão e vizualização de contas bancárias vinculadas ao sistema, afim de vincular entradas e saidas    |   Média    |
|  RF06  |                    Categorização dos Gastos                    |                   Permitir o cadastro, edição, exclusão , vizualização das categorias e vincular um gasto a categoria.                   |   Média    |
|  RF07  |                   Sistema de Grupo Familiar                    |     Permitir que o usuário crie, edite e exclua grupos familiares, além de visualizar os gastos individuais e totais de cada membro.     |   Baixa    |
|  RF08  |                     Gerenciamento de Posts                     |                            Permitir o administrador crie, edite, exclua posts relacionado a área de finanças.                            |   Baixa    |
|  RF9  |                       Vizualizar Cotação                       |                                              Permitir vizualizar a cotação de várias moedas                                              |   Baixa    |

> **Prioridade**: alta, média ou baixa

## 9. Requisitos Não-funcionais

| Código |          Nome           |                                    Descrição                                    |      Categoria       | Classificação |
| :----: | :---------------------: | :-----------------------------------------------------------------------------: | :------------------: | :-----------: |
|  NF01  |  Linguagens/Frameworks  | Utilização de Java, Spring Boot,Typescript e Angular para desenvolver o projeto | Restrição de projeto |   Desejável   |
|  NF02  |     Banco de Dados      |                                   PostgreSQL                                    | Restrição de projeto |  Obrigatório  |
|  NF03  |      Criptografia       |               As senhas deve ser armazenadas usando criptografia                |      Segurança       |  Obrigatório  |
|  NF04  |     Responsividade      |                    O sistema deve ter a interface responsiva                   |      Interface       |   Desejável   |
|  NF05  |     Acessibilidade      |          O sistema deverá ter algumas funcionalidades de acessibilidade como HTML semântico, botões grandes e chamativo.          |     Usabilidade      |   Desejável   |
|  NF06  | Facilidade de Navegação |      Todas as funcionalidades devem poder ser acessadas com poucos clicks       |     Usabilidade      |  Obrigatório  |
|  RF07  |                     Seleção de tema visual                     |                                        Permitir o usuário alternar entre o tema claro e o escuro.                                        |   Baixa    |

## 10. Riscos ⚠️
Possíveis Riscos:

- Má comunicação entre os membros do projeto durante o desenvolvimento e tomada de decisões.

- Baixo conhecimento de desenvolvimento utilizando os frameworks Angular, Spring Boot e Deploy do sistema.

- Interface ser confusa ou não resolver o problema do usuário final.

- Baixa escalabilidade, sistema funcionar para 10 usuários, mas não suportar uma carga de 1.000 clientes simultâneos.

- Vazamento de dados, autenticação frágil ou falta de conformidade com Lei LGPD

- Incoerência em relação ao projeto proposto com o que foi desenvolvido.

- Projeto demorar tanto que, quando for lançado já vai estar fora do prazo estipulado pelos Stakeholders.

- Baixa confiabilidade e disponibilidade do sistema (abaixo de 99% ao ano), causando perda de reputação da marca.



> **Categoria** Usabilidade, confiabilidade, performance, suportabilidade, restrição de projeto, implementação, interface e requisito físico - segundo classificação [FURP+](https://pt.wikipedia.org/wiki/FURPS).
> **Classificação**: desejável ou obrigatório.
