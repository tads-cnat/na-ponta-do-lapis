# Documento de Visão


## Histórico de Revisões


|    Data    | Versão |   Descrição    |               Autores                |
| :--------: | :----: | :------------: | :----------------------------------: |
| 24/09/2025 |  0.1   | Versão inicial | Bruno, Eduardo, Lucas, Pedro, Wagner |




## 1. Visão Geral do Sistema Proposto


Criar um ambiente acolhedor para jovens iniciando a vida adulta, onde os mesmos possam ter uma melhor gestão de gastos e entender como desenvolver uma boa saúde financeira pensando no desenvolvimento de suas carreiras e vida.


## 2. Descrição do Problema
|              |                                                             |
| :----------: | :---------------------------------------------------------: |
| **Problema** |         Dificuldade em organizar os gastos pessoais         |
|  **Afeta**   | Todos que não tiveram uma adequada administração financeira |
| **Impacta**  |                    Liberdade financeira                     |
| **Solução**  |        Criar uma ferramenta que auxilia as despesas         |


## 3. Descrição dos Usuários


|    Usuário    |          Descrição           |      Responsabilidades      |
| :-----------: | :--------------------------: | :-------------------------: |
|  Estudantes   | Jovens no início da carreira | Cria planejamento de gastos |
| Trabalhadores |    Trabalhadores no geral    | Cria planejamento de gastos |


## 4. Descrição do Ambiente dos Usuários


| Usuário | Ambiente operacional |
| :-----: | :------------------: |
| Desktop |         Web          |
| Mobile  |         Web          |


## 5. Principais Necessidades dos Usuários


> Apresentadas no formato de tópicos
1. **Papel 1**
  - ...
  - ...
1. **Papel 2**
  - ...
  - ...




## 6. Alternativas Concorrentes


1. **Minhas Finanças**
  - Estética de componentes do site agradável;
  - Paleta de cores harmônica, alinhada com o conteúdo apresentado;
  - Gráficos para saldo, entrada, despesa e etc.
1. **Excel**
  - Quantidade de recurso;
  - Organização e alinhamento;
  - Ícones para funcionalidades;
  - Personalização de planilha.
1. **Minhas Economias**
  - Curso em vídeo ensinando sobre o dinheiro, com dicas e exemplos práticos;
  - Integração com vários bancos;
1. **Mobills**
  - Dashboard personalizável;
  - Interface minimalista;
  - Planejamento mensal;
  - Tem um dashboard.


## 7. Regras de Negócio


|  ID   |  Regra  |  Descrição   |
| :---: | :-----: | :----------: |
| RN01  | nome... | Descrição... |
| RN02  |   ...   |     ...      |


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
|  RF08  |                     Seleção de tema visual                     |                                        Permitir o usuário alternar entre o tema claro e o escuro.                                        |   Baixa    |
|  RF09  |                     Gerenciamento de Posts                     |                            Permitir o administrador crie, edite, exclua posts relacionado a área de finanças.                            |   Baixa    |
|  RF10  |                       Vizualizar Cotação                       |                                              Permitir vizualizar a cotação de várias moedas                                              |   Baixa    |


> **Prioridade**: alta, média ou baixa


## 9. Requisitos Não-funcionais


| Código |             Nome              |                              Descrição                               |      Categoria       | Classificação |
| :----: | :---------------------------: | :------------------------------------------------------------------: | :------------------: | :-----------: |
|  NF01  | Ferramenta de desenvolvimento |       Utilização de Python e Django para desenvolver o projeto       | Restrição de projeto |  Obrigatório  |
|  NF02  |        Banco de Dados         |                                SQLite                                | Restrição de projeto |  Obrigatório  |
|  NF03  |         Criptografia          |          As senhas deve ser armazenadas usando criptografia          |      Segurança       |  Obrigatório  |
|  NF04  |        Renponsividade         |                O sistema deve ter o design responsivo                |     Usabilidade      |   Desejável   |
|  NF05  |        Acessibilidade         |    O sistema deverá ter algumas funcionalidades a acessibilidade     |     Usabilidade      |   Desejável   |
|  NF06  |    Facilidade de Navegação    | Todas as funcionalidades devem poder ser acessadas com poucos clicks |     Usabilidade      |  Obrigatório  |


> **Categoria** Usabilidade, confiabilidade, performance, suportabilidade, restrição de projeto, implementação, interface e requisito físico - segundo classificação [FURP+](https://pt.wikipedia.org/wiki/FURPS).


> **Classificação**: desejável ou obrigatório.

