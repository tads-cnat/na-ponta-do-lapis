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
| Trabalhadores |    Trabalhadores no geral    |             ...             |


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
| Código |                     Nome                      |                                                             Descrição                                                              | Prioridade |
| :----: | :-------------------------------------------: | :--------------------------------------------------------------------------------------------------------------------------------: | :--------: |
|  RF01  |                Login e Logout                 | Login: conjunto de credencias e procedimentos usados para identificar,    logout: processo de encerrar uma sessão ativa no sistema |    Alta    |
|  RF02  |  Cadastrar usuário e editar dados do usuário  |                                 Criação de conta do usuário no sistema e edição de dados do mesmo                                  |    Alta    |
|  RF03  |        Criar gastos, editar e excluir         |                                Criar gasto do usuário com informações detalhadas como tipo e valor                                 |    Alta    |
|  RF04  | Visualização de tabelas e gráficos dos gastos |                               Uma área no sistema que o usuário possa ver a evolução de seus gastos                                |   média    |




> **Prioridade**: alta, média ou baixa


## 9. Requisitos Não-funcionais


| Código |             Nome              |                          Descrição                          |      Categoria       | Classificação |
| :----: | :---------------------------: | :---------------------------------------------------------: | :------------------: | :-----------: |
|  NF01  | Ferramenta de desenvolvimento |  Utilização de Python e Django para desenvolver o projeto   | Restrição de projeto |  Obrigatório  |
|  NF02  |        Banco de Dados         |                           sqlite                            | Restrição de projeto |  Obrigatório  |
|  NF03  |         Criptografia          |     As senhas deve ser armazenadas usando criptografia      | Restrição de projeto |  Obrigatório  |
|  NF04  |          Usabilidade          | O sistema deve ser acessível em celular e design responsivo | Restrição de projeto |   Desejável   |


> **Categoria** Usabilidade, confiabilidade, performance, suportabilidade, restrição de projeto, implementação, interface e requisito físico - segundo classificação [FURP+](https://pt.wikipedia.org/wiki/FURPS).


> **Classificação**: desejável ou obrigatório.

