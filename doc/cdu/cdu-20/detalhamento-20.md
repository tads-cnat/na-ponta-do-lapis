# CDU 20. Atualizar Perfil

- **Ator principal**: Cliente.

- **Atores secundários**: N/A.
- **Resumo**: O cliente acessa a área de configurações para alterar informações do seu perfil como foto e email.
- **Pré-condição**: O cliente deve estar devidamente autenticado.
- **Pós-Condição**: Suas informações são atualizadas e  alteradas no sistema.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O cliente acessa as Configurações. | |
| 2 - O cliente seleciona a opção Editar Perfil. | |
| 3 - O cliente altera as informações desejadas (foto, email). | |
| 4 - O cliente confirma a atualização clicando em Salvar. | |
| | 5 - O sistema valida os dados informados. |
| | 6 - O sistema atualiza o perfil no banco de dados. |
| | 7 - O sistema exibe mensagem de sucesso ao cliente. |

## Fluxo Alternativo I - Dados inválidos

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
| 4.1 O cliente tenta salvar informações inválidas (ex.: email já existente, formato errado, arquivo de imagem inválido). | |
| | 4.2 O sistema exibe mensagem de erro informando os campos inválidos. |
| 4.3 O cliente corrige as informações. | |
| 4.4 Retorna ao passo 4 do Fluxo Principal. | |
