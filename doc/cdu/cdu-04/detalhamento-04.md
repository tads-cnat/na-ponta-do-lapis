# CDU 04. Recuperar Senha

- **Ator principal**: Cliente / Visitante.
- **Atores secundários**: N/A.
- **Resumo**: Permite que um usuário que esqueceu sua senha a redefina diretamente na tela de autenticação por meio da sua **Pergunta de Segurança** cadastrada. O usuário informa seu e-mail, responde à pergunta de segurança exibida pelo sistema e, após validação da resposta (com token temporário de recuperação em cookie `HttpOnly`), define sua nova senha.
- **Pré-condição**: Usuário possuir conta cadastrada com pergunta e resposta de segurança configuradas.
- **Pós-Condição**: A senha do usuário é atualizada no banco de dados e ele pode autenticar-se imediatamente.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - Na tela de Login, clica em **"Esqueceu a senha?"**. | |
| | 2 - Sistema exibe a etapa de recuperação solicitando o e-mail da conta. |
| 3 - Informa o e-mail cadastrado e avança. | |
| | 4 - Sistema consulta `POST /api/v1/auth/recover-password/question` e exibe a pergunta de segurança associada à conta. |
| 5 - Informa a resposta da pergunta de segurança e confirma. | |
| | 6 - Sistema valida a resposta em `POST /api/v1/auth/recover-password`, emite o cookie temporário `HttpOnly` (`npl_pwd_recovery`, válido por 5 minutos) e avança para a etapa de nova senha. |
| 7 - Informa a nova senha e a confirmação da nova senha e clica em salvar. | |
| | 8 - Sistema valida o token de recuperação em `POST /api/v1/auth/recover-password/change`, atualiza o hash da senha, limpa o cookie temporário e exibe mensagem de sucesso. |

## Fluxo Alternativo I - E-mail sem pergunta configurada ou inexistente

| Ações do ator | Ações do sistema |
| :-----------------: | :------------------: |
| 3.1 - Usuário informa um e-mail não encontrado ou sem pergunta de segurança (ex.: conta criada apenas via Google). | |
| | 3.2 - Sistema informa que não há pergunta de segurança disponível para o e-mail informado. |

## Fluxo Alternativo II - Resposta de segurança incorreta

| Ações do ator | Ações do sistema |
| :-----------------: | :------------------: |
| 5.1 - Usuário informa uma resposta que não confere com a cadastrada. | |
| | 5.2 - Sistema retorna aviso (*"Resposta incorreta. Confira a resposta e tente novamente."*) mantendo o usuário na etapa de resposta. |
