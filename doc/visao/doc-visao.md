# Documento de Visão

## Histórico de Revisões

|    Data    | Versão |   Descrição    |               Autores                |
|:----------:| :----: | :------------: | :----------------------------------: |
| 24/09/2025 |  1.0   | Versão inicial | Bruno, Eduardo, Lucas, Pedro, Wagner |
| 25/01/2026 |  2.0   | Segunda versão  | Bruno, Eduardo, Lucas, Pedro, Wagner |
| 28/03/2026 |  3.0   | Terceira versão | Bruno, Eduardo, Eric, Lucas, Pedro, Vinicius, Wagner |
| 10/05/2026 |  4.0   | Quarta versão   | Bruno, Eduardo, Eric, Lucas, Pedro, Vinicius, Wagner |

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

|  ID  |             Regra              |                                     Descrição                                      |
|:----:|:------------------------------:|:----------------------------------------------------------------------------------:|
| RN01 | Utilização das funcionalidades |                 Os usuários não logados só podem acessar os posts.                 |
| RN02 |       Criação de família       |                        O usuário só poderá ter uma família.                        |
| RN03 |    Pertencimento à família     |                   O usuário só poderá participar de uma família.                   |
| RN04 |   Exclusão no grupo familiar   |            Somente o fundador do grupo pode  excluir o grupo familiar.             |
| RN05 |    Adição no grupo familiar    |     Somente os administradores podem convidar outra pessoa ao grupo familiar.      |
| RN06 |       Remoção de membros       |       Somente os administradores da família podem remover do grupo familiar.       |
| RN07 |  Promoção para administrador   | Somente os administradores da família podem promover membros para administradores. | 
| RN08 |    Nome da conta financeira    |  Um mesmo usuário não pode ter duas ou mais contas financeiras com o mesmo nome.   |
| RN09 |     Gerenciamento de posts     |         Somente administradores podem adicionar, editar ou excluir posts.          |

## 8. Requisitos Funcionais

| Código |                              Nome                              |                                                                 Descrição                                                                  | Prioridade |
|:------:|:--------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------------------------------:|:----------:|
|  RF01  |                      Cadastro de usuário                       |                                        O usuário poderá se cadastrar e criar uma conta no sistema.                                         |    Alta    |
|  RF02  |                        Login de usuário                        |                                              O usuário poderá logar em sua conta no sistema.                                               |    Alta    |
|  RF03  |                       Logout de usuário                        |                                             O usuário poderá deslogar de sua conta no sistema.                                             |    Alta    |
|  RF04  |                    Editar conta de usuário                     |                                               O usuário poderá editar os dados de sua conta.                                               |    Alta    |
|  RF05  |                      Cadastrar transação                       |                            O usuário poderá cadastrar transações tanto de receita como de despesa em sua conta.                            |    Alta    |
|  RF06  |                      Vizualizar transação                      |                                 O usuário poderá vizualizar todas as transações registradas em sua conta.                                  |    Alta    |
|  RF07  |                       Excluir transação                        |                          O usuário poderá excluir quais transações quiser das que foram regitradas em sua conta.                           |    Alta    |
|  RF08  |                        Editar transação                        |                             O usuário poderá editar os dados das transações que foram regitradas em sua conta.                             |    Alta    |
|  RF09  |                   Vizualização de Relatórios                   |                         Permitir ao usuário a vizualização de tabelas, gráficos e relatório de evolução de gastos.                         |    Alta    |
|  RF10  |                   Cadastrar conta financeira                   |                                     O usuário poderá cadastrar uma nova conta financeira em sua conta.                                     |   Médio    |
|  RF11  |                  Vizualizar conta financeira                   |                                O usuário poderá vizualizar as contas financeiras registardas em sua conta.                                 |   Médio    |
|  RF12  |                    Excluir conta financeira                    |                           O usuário poderá excluir quais contas financeiras quiser das registradas em sua conta.                           |   Médio    |
|  RF13  |                    Editar conta financeira                     |                                  O usuário poderá editar as contas financeiras registradas em sua conta.                                   |   Médio    |
|  RF14  |                      Vizualizar categoria                      |                                     O usuário poderá vizualizar as categorias registradas no sistema.                                      |   Média    |
|  RF15  |                       Vincular categoria                       |                                            O usuário poderá vincular uma categoria à transação.                                            |   Média    |
|  RF16  |                       Cadastrar família                        |                                         O usuário poderá cadastrar uma nova família em sua conta.                                          |   Baixa    |
|  RF17  |                       Vizualizar família                       |                               O usuário poderá vizualizar sua família, os membros e as despesas da família.                                |   Baixa    |
|  RF18  |                        Excluir família                         |                                        O usuário poderá excluir a família registrada em sua conta.                                         |   Baixa    |
|  RF19  |                         Editar família                         |                                    O usuário poderá editar os dados da família registrada em sua conta.                                    |   Baixa    |
|  RF20  |                  Convidar membro para família                  |                           Terá a opção de convidar outro usuário para sua família através do username do mesmo.                            |   Baixa    |
|  RF21  |                 Aceitar convite de uma família                 |                        O usuário que recebeu um convite de uma família poderá aceitar o mesmo e se vincular a ela.                         |   Baixa    |
|  RF22  |                   Remover membro da família                    |                                          Terá a opção de remover um membro registrado na família.                                          |   Baixa    |
|  RF23  |          Promover membro para administador da família          |                                Terá a opção de promover um membro da família para administrador da família.                                |   Baixa    |
|  RF24  |                        Sistema de Metas                        | O sistema terá um sistema de metas baseada nas receitas e despesas, será definido a partir do perfil do usuario ou de forma personalizada. |   Baixa    |
|  RF25  |                        Cadastrar posts                         |                                                     O usuário poderá registrar posts.                                                      |   Baixa    |
|  RF26  |                        Vizualizar posts                        |                                      O usuário poderá vizualizar os posts criados na página de posts.                                      |   Baixa    |
|  RF27  |                         Excluir posts                          |                                         O usuário poderá excluir os posts registrados no sistema.                                          |   Baixa    |
|  RF28  |                          Editar posts                          |                                          O usuário poderá editar os posts registrados no sistema.                                          |   Baixa    |
|  RF29  |                       Cadastrar marcador                       |                                         O usuário poderá cadastrar um novo marcador na sua conta.                                          |   Baixa    |
|  RF30  |                       Vincular marcador                        |                               O usuário poderá vincular uma transação a um marcador registrado em sua conta.                               |   Baixa    |
|  RF31  |                      Vizualizar marcador                       |                                    O usuário poderá vizualizar os marcadores registrados em sua conta.                                     |   Baixa    |
|  RF32  |                        Excluir marcador                        |                                       O usuário poderá excluir um marcador registrado em sua conta.                                        |   Baixa    |
|  RF33  |                        Editar marcador                         |                                  O usuário poderá editar os dados de um marcador registrado em sua conta.                                  |   Baixa    |
|  RF34  |                       Vizualizar Cotação                       |                                               Permitir vizualizar a cotação de várias moedas                                               |   Baixa    |
|  RF35  |                        Login com Google                        |                                     O usuário poderá logar no sistema através da sua conta do Google.                                      |   Baixa    |

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

> **Categoria** Usabilidade, confiabilidade, performance, suportabilidade, restrição de projeto, implementação, interface e requisito físico - segundo classificação [FURP+](https://pt.wikipedia.org/wiki/FURPS).
> **Classificação**: desejável ou obrigatório.
