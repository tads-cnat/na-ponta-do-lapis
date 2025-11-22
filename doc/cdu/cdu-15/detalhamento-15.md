# CDU 15. Manter Conta de Administrador

- **Ator principal**: Administrador

- **Atores secundários**: n/a
- **Resumo**: O administrador consegue criar contas para outros administradores, listar as contas, editar e excluir a conta de outros administradores.
- **Pré-condição**: O usuario deverá estar autenticado como administrador do sistema.
- **Pós-Condição**: Apos a operação ser realizada o estado da conta é atualizado no sistema de acordo com a opção selecionada e atualizada a listagem.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O administrador seleciona a opção "Gerenciar Administradores" | |
| | 2 - O sistema exibe um botão "Cadastrar novo Administrador" e logo abaixo uma listagem com todos os administradores ja cadastrados no sistema |
| 3 - O administrador seleciona o opção "Cadastrar novo administrador" ||
| | 4 - O sistema exibe um formulario solicitando: Nome de Usuario, Senha, Nome |
| 5 - O administrador preenche todos os campos solicitados e clicka em "Cadastrar" | |
| | 6 - O sistema válidas o campos preenchidos |
| | 7- Atualiza no Banco de dados |
| | 8 - Uma mensagem de feedback("Sucesso") é enviada ao Administrador |

## Fluxo Alternativo I - Editar Administrador

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
| 3.1 - O administrador seleciona o botão "Editar" proximo ao usuario desejado na listagem | |
|| 4.1 - O sistema exibe um formulario solicitando: Nome de Usuario, Senha, Nome. com os campos já preenchidos |
| 5.1 - O administrador preenche todos os campos e clickar em "Atualizar" | |
|| Retorna ao passo 6 do fluxo principal |

## Fluxo Alternativo II - Excluir Administrador

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.2 - O administrador seleciona o botão "Excluir" proximo ao usuario desejado na listagem |  |
| | 4.2 - O sistema exibe uma mensagem de confirmação de exclusão |
| 5.2 - O administrador confirma a exclusão | |
| | Retorna ao passo 7 do fluxo principal |

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...
