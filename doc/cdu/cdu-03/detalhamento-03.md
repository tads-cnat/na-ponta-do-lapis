# CDU 03. Cadastro Usuário

- **Ator principal**: Visitante

- **Atores secundários**: NT
- **Resumo**: Permite que uma nova pessoa crie uma conta no sistema. O processo começa quando o usuário preenche um formulário com suas informações básicas (nome, e-mail e senha). O sistema valida os dados, verifica se o e-mail é único e, se tudo estiver certo, cria a conta.
- **Pré-condição**: NT
- **Pós-Condição**: Usuário consegue cria conta no sistema

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - Preenche formulário de [cadastro](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1545-21655&t=gZFvJtLbjEaf6iYW-0&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) (nome completo, username, telefone, email e senha) | |
| | 2 - Valida as informações e, se tudo estiver correto, mostra feedback: "Conta criada com sucesso"|

## Fluxo Alternativo II - Email já em uso

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.3 -  Preenche formulário com e-mail já cadastrado | |
| | 1.4 - Sistema retorna mensagem: "E-mail já existente"|

