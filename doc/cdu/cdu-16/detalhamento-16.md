# CDU 16. Manter Marcador

- **Ator principal**: Cliente

- **Atores secundários**: Não tem
- **Resumo**: O Cliente pode criar marcador, remover e atualizar para separar em grupos os seus gastos.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: ...

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente tenta cria um novo marcador. | |
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
| 3.1 - O Cliente tenta editar um marcador existente. | |
| | 4.1 - O sistema valida o novo nome da marcador. |
| | 5.1 - A marcador é atualizado. |

## Fluxo Alternativo II - Remover

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.2 - O Cliente inicia a exlusão de um marcador. | |
| | 4.2 - O sistema verifica se o marcador esta vinculado a alguma transação. |
| | 5.2 - Se estiver vazio é apagado imediatamente. |
| | 6.2 - Caso esteja vinculado a alguma transação o sistema remove o vinculo com as transações e define a relação como NULL. |



## Diagrama de Interação (Sequência ou Comunicação)

### Criar Marcador
![Diagrama de sequência](./imgs/CDU16-1.png)

### Excluir Marcador
![Diagrama de sequência](./imgs/CDU16-2.png)

### Editar Marcador
![Diagrama de sequência](./imgs/CDU16-3.png)

### Listar Marcador
![Diagrama de sequência](./imgs/CDU16-4.png)

