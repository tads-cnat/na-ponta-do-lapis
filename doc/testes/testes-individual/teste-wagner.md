# 1 Introdução

Este documento reúne os casos de teste elaborados para validar os principais fluxos da aplicação Na Ponta do Lápis. Os testes aqui descritos abrangem os casos de uso de login e cadastro de usuário, com o objetivo de assegurar que cada funcionalidade opere conforme modelado no sistema.

Cada caso de teste inclui informações detalhadas sobre o cenário a ser avaliado, os dados de entrada necessários e resultados esperados.

## Testes Funcionais

### CDU 01 - Login

> **Nota:** Os testes usam o usuário da carga inicial (`data.sql`): e-mail `lucas@gmail.com` e senha `123456`. O botão "Entrar" permanece desabilitado enquanto o formulário estiver inválido.

#### Fluxo Principal - Realizar login

| E-mail | Senha | Resultado esperado | Resultado obtido | Situação |
| --- | --- | --- | --- | --- |
| lucas@gmail.com | 123456 | Login Realizado! (token gerado e redirecionamento para o dashboard) | - | - |
| - | 123456 | Erro: O e-mail é obrigatório. | - | - |
| lucas@gmail.com | - | Erro: A senha é obrigatória. | - | - |
| lucasgmail.com | 123456 | Erro: Informe um formato de e-mail válido. | - | - |

#### Fluxo Alternativo I - Credenciais inválidas ou inexistentes

| E-mail | Senha | Resultado esperado | Resultado obtido | Situação |
| --- | --- | --- | --- | --- |
| naoexiste@gmail.com | 123456 | Erro: Email ou senha inválido. | - | - |
| lucas@gmail.com | SenhaErrada1 | Erro: Email ou senha inválido. | - | - |

### CDU 03 - Cadastro Usuário

> **Nota:** O botão "Criar Conta" só é habilitado com o formulário válido e o aceite dos termos e políticas de privacidade marcado. O telefone é opcional. O usuário `lucas@gmail.com` (username `Lucaasshq`) já existe na carga inicial (`data.sql`).

#### Fluxo Principal - Cadastrar usuário

| Nome completo | Username | Telefone | E-mail | Senha | Confirmação | Resultado esperado | Resultado obtido | Situação |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Maria Souza | mariasouza | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha1234 | Conta criada com sucesso! | - | - |
| Maria Souza | mariasouza | - | maria@gmail.com | Senha1234 | Senha1234 | Conta criada com sucesso! (telefone opcional) | - | - |
| - | mariasouza | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha1234 | Erro: O nome deve ter no mínimo 3 caracteres. | - | - |
| Jo | mariasouza | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha1234 | Erro: O nome deve ter no mínimo 3 caracteres. | - | - |
| [Nome com mais de 100 caracteres...] | mariasouza | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha1234 | Erro: Limite de caracteres excedido (100). | - | - |
| Maria Souza | - | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha1234 | Erro: O username é obrigatório. | - | - |
| Maria Souza | mari | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha1234 | Erro: O username deve ter no mínimo 5 caracteres. | - | - |
| Maria Souza | [Username com mais de 100 caracteres...] | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha1234 | Erro: Limite de caracteres excedido (100). | - | - |
| Maria Souza | mariasouza | [Telefone com 21 caracteres...] | maria@gmail.com | Senha1234 | Senha1234 | Erro: Limite de caracteres excedido (20). | - | - |
| Maria Souza | mariasouza | (84) 99999-9999 | - | Senha1234 | Senha1234 | Erro: O e-mail é obrigatório. | - | - |
| Maria Souza | mariasouza | (84) 99999-9999 | mariagmail.com | Senha1234 | Senha1234 | Erro: E-mail inválido. | - | - |
| Maria Souza | mariasouza | (84) 99999-9999 | maria@gmail.com | - | - | Erro: A senha é obrigatória. | - | - |
| Maria Souza | mariasouza | (84) 99999-9999 | maria@gmail.com | Abc123 | Abc123 | Erro: A senha deve ter pelo menos 8 caracteres. | - | - |
| Maria Souza | mariasouza | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha4321 | Erro: As senhas não são iguais. | - | - |

#### Fluxo Alternativo I - E-mail ou username já em uso

| Nome completo | Username | Telefone | E-mail | Senha | Confirmação | Resultado esperado | Resultado obtido | Situação |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Maria Souza | mariasouza | (84) 99999-9999 | lucas@gmail.com | Senha1234 | Senha1234 | Erro: E-mail já existente. | - | - |
| Maria Souza | Lucaasshq | (84) 99999-9999 | maria@gmail.com | Senha1234 | Senha1234 | Erro: Username já cadastrado. (nenhum usuário duplicado é criado) | - | - |