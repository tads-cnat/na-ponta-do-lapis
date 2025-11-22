# CDU 03. Cadastro

- **Ator principal**: Visitante

- **Atores secundários**: NT
- **Resumo**: O caso de uso Cadastro de Usuário permite que uma nova pessoa crie uma conta no sistema. O processo começa quando o usuário preenche um formulário com suas informações básicas (nome, e-mail e senha). O sistema valida os dados, verifica se o e-mail é único e, se tudo estiver certo, cria a conta. Dependendo da política de segurança, o usuário pode precisar confirmar o e-mail antes de ativar o acesso. Depois disso, o sistema está pronto para autenticar o novo usuário no login.
- **Pré-condição**: Usurio deve estar com acesso a internet e não deve ter conta criada com o mesmo email
- **Pós-Condição**: Usuário consegue cria conta no sistema

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - Usuário preenche formulário de cadastro (nome completo, email e senha) | |
| | 2 - Sistema valida as informações e, se tudo estiver correto, mostra feedback: "Conta criada com sucesso"|

## Fluxo Alternativo II - Email já em uso

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.3 - Usuário preenche formulário com e-mail já cadastrado | |
| | 1.4 - Sistema retorna mensagem: "E-mail já existente"|

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...
