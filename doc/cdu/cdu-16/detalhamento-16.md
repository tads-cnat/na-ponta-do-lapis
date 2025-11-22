# CDU 16. Manter Categoria (Persistente)

- **Ator principal**: Usuário

- **Atores secundários**: Não tem
- **Resumo**: O usuário pode criar categorias, remover e atualizar para separar em grupos os seus gastos.
- **Pré-condição**: O usuário deve estar autenticado.
- **Pós-Condição**: ...

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O usuário tenta cria uma nova categoria. | |
| | 2 - O sistema valida se não exite outra categoria com o mesmo nome. |
| | 3 - Após a validação uma nova categoria é criada. |

## Fluxo de Excessão

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 2.1 - Caso haja uma categoria com o nome informado ela é rejeitada. |
| | 3.1 - Um alerta aparece para o usuário informar um novo nome. |
| | 4.1 - Retorna ao passo 2. |

## Fluxo Alternativo I - Editar

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
| 3.1 - O usuário tenta editar uma categoria existente. | |
| | 4.1 - O sistema valida o novo nome da categoria. |
| | 5.1 - A categoria é atualizada. |

## Fluxo Alternativo II - Remover

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.2 - O usuário inicia a exlusão de uma categoria. | |
| | 4.2 - O sistema verifica se a categoria esta vazia. |
| | 5.2 - Se estiver vazia é apagada imediatamente. |
| | 6.2 - Caso não esteja o sistema remove em cascata os dados daquela categoria. |

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...
