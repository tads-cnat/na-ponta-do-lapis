# CDU 03. Cadastro de Usuário

- **Ator principal**: Visitante
- **Atores secundários**: Google Identity Services (opcional).
- **Resumo**: Permite que uma nova pessoa crie uma conta no sistema. O visitante pode utilizar o botão **"Continuar com o Google"** no topo (ver [CDU-34](../cdu-34/detalhamento-34.md)) ou preencher o formulário com nome completo, nome de usuário, e-mail, celular (opcional), senha, confirmação de senha, pergunta e resposta de segurança (para recuperação de senha) e aceite dos Termos de Uso e Política de Privacidade (LGPD). Ao concluir o cadastro, o sistema cria automaticamente a `"Conta Principal"` do usuário.
- **Pré-condição**: Não possuir sessão ativa ou desejar registrar nova conta.
- **Pós-Condição**: Usuário é cadastrado no sistema com sua `"Conta Principal"` inicial e pode realizar login.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - Acessa a tela de [Cadastro](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1545-21655&t=gZFvJtLbjEaf6iYW-0&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) (`/signup`). | |
| 2 - Preenche nome completo, nome de usuário, e-mail, telefone (opcional), senha, confirmação de senha, pergunta/resposta de segurança, marca o aceite dos Termos de Uso (LGPD) e clica em **"Criar Conta"**. | |
| | 3 - Valida os campos, verifica unicidade de e-mail e nome de usuário, codifica a senha e resposta de segurança com BCrypt, cria a `"Conta Principal"` padrão (`BRL`) e retorna confirmação (*"Registro Realizado com sucesso!"*). |
| | 4 - Redireciona o usuário para a tela de Login (`/login`). |

## Fluxo Alternativo I - E-mail ou Nome de Usuário já em uso

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 2.1 - Preenche formulário com e-mail ou nome de usuário já cadastrado. | |
| | 2.2 - Sistema rejeita o cadastro e retorna mensagem informando que o e-mail ou nome de usuário já está em uso. |

## Fluxo Alternativo II - Cadastro rápido com Google (LGPD)

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.1 - Clica em **"Continuar com o Google"** no topo do formulário. | |
| | 1.2 - Executa o fluxo de criação de conta via Google com modal de confirmação de dados e aceite LGPD ([CDU-34](../cdu-34/detalhamento-34.md)). |
