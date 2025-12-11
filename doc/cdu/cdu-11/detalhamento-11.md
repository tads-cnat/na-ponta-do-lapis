# CDU 16. Manter Marcador (Temporário)

- **Ator principal**: Visitante
- **Atores secundários**: Não tem
- **Resumo**: O visitante pode criar marcador, remover e atualizar para separar em grupos os seus gastos, porém nenhuma das ações refletirá na DB, somente na sessão do usuario anônimo de forma temporária.
- **Pré-condição**: N/A
- **Pós-Condição**: N/A

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O visitante tenta cria uma nova marcador. | |
| | 2 - O sistema valida se não exite outra marcador com o mesmo nome. |
| | 3 - Após a validação uma nova marcador é criada. |

## Fluxo de Excessão

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 2.1 - Caso haja uma marcador com o nome informado ela é rejeitada. |
| | 3.1 - Um alerta aparece para o visitante informar um novo nome. |
| | 4.1 - Retorna ao passo 2. |

## Fluxo Alternativo I - Editar

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
| 3.1 - O visitante tenta editar uma marcador existente. | |
| | 4.1 - O sistema valida o novo nome da marcador. |
| | 5.1 - A marcador é atualizada. |

## Fluxo Alternativo II - Remover

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.2 - O visitante inicia a exlusão de uma marcador. | |
| | 4.2 - O sistema verifica se a marcador esta vazia. |
| | 5.2 - Se estiver vazia é apagada imediatamente. |
| | 6.2 - Caso não esteja o sistema remove em cascata os dados daquela marcador. |

