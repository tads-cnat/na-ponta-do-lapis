# CDU 01. Login

- **Ator principal**: Visitante 

- **Atores secundários**: 

- **Resumo**:  O caso de uso Login permite que um usuário já cadastrado acesse o sistema utilizando suas credenciais. O processo começa quando o usuário informa seu e-mail e senha na tela de login. O sistema verifica se os dados correspondem a um usuário existente e se a conta está ativa. Se as informações estiverem corretas, o sistema concede acesso e cria uma sessão para o usuário. Caso contrário, o acesso é negado e uma mensagem de erro é exibida solicitando a correção dos dados.

- **Pré-condição**: Usuário não deve esta autenticado

- **Pós-Condição**: Usuário é autenticado no sistema.

## Fluxo Principal
| **Ações do Ator**                                              | **Ações do Sistema**                                                                  |
| -------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| 1. Usuário acessa a tela de login.                             |                                                                                       |
| 2. Usuário informa o email e a senha nos campos do formulário. |                                                                                       |
|                                                                | 3. Sistema recebe as credenciais.                                                     |
|                                                                | 4. Sistema valida o formato do email e verifica se todos os campos foram preenchidos. |
|                                                                | 5. Sistema verifica a existência do usuário no banco de dados.                        |
|                                                                | 6. Sistema valida a senha informada comparando com a senha armazenada.                |
|                                                                | 7. Sistema autentica o usuário.                                                       |
|                                                                | 8. Sistema cria uma sessão válida para o usuário.                                     |
|                                                                | 9. Sistema redireciona o usuário para a página inicial.                               |


## Fluxo Alternativo I - Credenciais invalidas ou inexistentes
| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: | 
| 1.1 - Usuario informa email ou senha incorreto | |  
| | 1.2 - Sistema retorna um feedback informando que a email, senha ou usuario não esta cadastrado |

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...
