# CDU 16. Manter Marcador

- **Ator principal**: Cliente.

- **Atores secundários**: N/A.
- **Resumo**: O Cliente pode criar marcador, remover e atualizar para separar em grupos os seus gastos.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: N/A.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente tenta [cria](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1136-6871&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) um novo marcador. | |
| | 2 - O sistema verifica se não exite outro marcador com o mesmo nome. |
| | 3 - Após a validação um novo marcador é criado. |

## Fluxo de Excessão

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 2.1 - Caso haja um marcador com o nome informado ela é rejeitada. |
| | 3.1 - Um alerta aparece para o Cliente informar um novo nome. |
| | 4.1 - Retorna ao passo 2. |

## Fluxo Alternativo I - Editar

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
| 3.1 - O Cliente tenta [editar](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1137-10599&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) um marcador existente. | |
| | 4.1 - O sistema valida o novo nome da marcador. |
| | 5.1 - A marcador é atualizado. |

## Fluxo Alternativo II - Remover

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.2 - O Cliente inicia a [exlusão](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1137-9522&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) de um marcador. | |
| | 4.2 - O sistema verifica se o marcador esta vinculado a alguma transação. |
| | 5.2 - Se estiver vazio é apagado imediatamente. |
| | 6.2 - Caso esteja vinculado a alguma transação o sistema remove o vinculo com as transações e define a relação como NULL. |




## Diagrama de Interação (Sequência ou Comunicação)


### Listar Marcador
![Diagrama de sequência](./imgs/CDU16-4.png)
