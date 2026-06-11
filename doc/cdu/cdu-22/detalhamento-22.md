# CDU 22. Convidar membro à família

- **Ator principal**: Usuário administrador da família.

- **Atores secundários**: Usuário convidado.
- **Resumo**:  Na página “Grupo Familiar” o usuário aperta o botão “convidar” para convidar um novo membro à sua família, caso ainda não tenha membros a família será criada.
- **Pré-condição**: Usuário deve estar cadastrado e autenticado no sistema.
- **Pós-Condição**: Um convite é enviado ao usuário convidado; caso necessário, uma família é criada e o usuário criador recebe o papel admin família; o membro só é vinculado à família após aceitar o convite.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O usuário aperta o botão "convidar". | |
| | 2 - Se o usuário já tiver uma família, o sistema retorna um formulário. |
| 3 - O usuário preenche o formulário com as informações do novo membro e aperta "enviar". | |
| | 4 - O sistema envia o convite para o outro usuário e retorna um pop-up escrito "convite enviado com sucesso". |

## Fluxo Alternativo I - Adicionar primeiro membro

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
| | 2.1 - O sistema retorna um fomulário para criar a família. |
| 3.1 - O usuário preenche e aperta o botão "criar". | |
| | 4.1 - Aparece um pop-up confirmando a criação da família, o sistema persiste os dados de criação da família e o usuário recebe o papel admin família. |
| 5.1 - (retorna para o passo 2 do fluxo principal) | |

## Fluxo Alternativo II - Aceitar convite

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 4.2 - O sistema mostra uma notificação no sino de notificações. |
| 5.2 - O usuário clica no icone da família. | |
| | 6.2 - O sistema apresenta o(s) convite(s) da(s) respectiva(s) família(s) ao usuário. |
| 7.2 - O usuário aceita o convite da família. | |
| | 8.2 - Usuário é registrado na família, seus dados são persistidos e o sistema retorna um pop-up "Bem-Vindo à Família". |

## Fluxo Alternativo III - Recusar convite

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 4.3 - O sistema mostra uma notificação no sino de notificações. |
| 5.3 - O usuário clica no icone da família. | |
| | 6.3 - O sistema apresenta o(s) convite(s) da(s) respectiva(s) família(s) ao usuário. |
| 7.3 - O usuário clica no botão "recusar" e recusa o convite da família. | |
| | 8.3 - A página volta ao formato padrão sem o convite e o remetente recebe um email notificando a recusa. |

## Fluxo Alternativo IV - Dado do formulário inválido

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 4.4 - O sistema retorna uma mensagem "Dado(s) Inválido(s)" e mostra novamente o formulário com o/os campo/os inválido/os em vermelho com uma pequena mensagem em vermelho embaixo do campo. |
| 5.4 - (retorna para o passo 3 do fluxo principal) | |

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

## Diagrama de Classes de Projeto
