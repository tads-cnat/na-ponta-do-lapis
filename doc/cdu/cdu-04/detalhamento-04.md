# CDU 04. Recuperar senha 

- **Ator principal**: Usuario
- **Atores secundários**: Sistema de envio de e-mail	 
- **Resumo**: caso de uso Recuperação de Senha permite que um usuário que esqueceu sua senha consiga redefini-la. O processo começa quando o usuário, na tela de login, clica em "Esqueceu a senha" e informa seu e-mail de recuperação. O sistema envia um link para o e-mail fornecido, que redireciona o usuário para uma página onde ele poderá criar uma nova senha. Após a atualização, a nova senha poderá ser utilizada para acessar o sistema.
- **Pré-condição**: Usuario com acesso a internet e conta existente
- **Pós-Condição**: Senha é recuperada

## Fluxo Principal
|                           Ações do ator                          |                                          Ações do sistema                                          |
| :--------------------------------------------------------------: | :------------------------------------------------------------------------------------------------: |
| 1 - Usuário acessa a tela de login e clica em "Esqueceu a senha" |                                                                                                    |
|           2 - Usuário informa seu e-mail de recuperação          |                                                                                                    |
|                                                                  |       3 - Sistema verifica se o e-mail está cadastrado e envia um link para redefinir a senha      |
|                                                                  | 4 - Usuário recebe o e-mail, clica no link e é redirecionado para a página de atualização de senha |
|                  5 - Usuário cria uma nova senha                 |                                                                                                    |
|                                                                  |                 6 - Sistema atualiza a senha e confirma a alteração para o usuário                 |


## Fluxo Alternativo I - Email não cadastrado
|                  Ações do ator                 |                                   Ações do sistema                                   |
| :--------------------------------------------: | :----------------------------------------------------------------------------------: |
| 1.1 - Usuário informa um e-mail não cadastrado |                                                                                      |
|                                                | 1.2 - Sistema retorna mensagem: "E-mail não encontrado. Verifique e tente novamente" |

  
## Fluxo Alternativo III - Link expirado ou inválido
|                    Ações do ator                    |                                  Ações do sistema                                  |
| :-------------------------------------------------: | :--------------------------------------------------------------------------------: |
| 1.5 - Usuário clica em um link expirado ou inválido |                                                                                    |
|                                                     | 1.6 - Sistema retorna mensagem: "Link expirado ou inválido. Solicite um novo link" |


> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...