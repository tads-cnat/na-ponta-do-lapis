# Teste CDU 16 - Manter Marcador (Temporário)

Este documento especifica os testes que devem ser realizados para o caso de uso 16 - Manter Marcador (Temporário). Ele contém as informações necessárias para a construção dos scripts de teste, como preparação do ambiente, dados de entrada, classes de equivalência e resultados esperados, seguindo o padrão da documentação do projeto.

## Especificação do CDU
- **Ator principal:** Visitante (Usuário anônimo).
- **Atores secundários:** N/A.
- **Resumo:** O visitante pode criar, atualizar e remover marcadores para separar em grupos os seus gastos de forma temporária na sessão, sem reflexo no banco de dados.
- **Pré-condição:** Usuário acessando a aplicação como visitante (sessão anônima ativa).
- **Pós-condição:** Alterações refletidas apenas na sessão/armazenamento temporário do navegador do usuário, mantendo o banco de dados inalterado.

---

## Casos Essenciais derivados da Partição de Equivalência
Abaixo estão os casos de teste estruturados para a funcionalidade de gerenciamento temporário de marcadores, cobrindo criação, restrição de duplicidade, edição e remoção (com verificação de itens vinculados).

### Gestão de Marcadores Temporários
| Ação Executada | Estado Inicial / Contexto | Dados de Entrada / Ação | Resultado Esperado | Situação |
| :--- | :--- | :--- | :--- | :--- |
| **Criação Válida** | Sessão anônima sem marcadores pré-existentes | Informar nome inédito para o marcador (ex: "Lazer") e confirmar | Marcador criado com sucesso na sessão, sem persistência no DB, feedback visual exibido | Não executado |
| **Criação Inválida (Duplicado)** | Sessão anônima com o marcador "Lazer" já criado | Tentar criar um novo marcador com o mesmo nome exato ("Lazer") | Ação rejeitada pelo sistema, alerta exibido solicitando um novo nome, retorno ao fluxo de criação | Não executado |
| **Edição de Marcador** | Sessão anônima com o marcador "Lazer" existente | Alterar o nome do marcador para "Entretenimento" | Sistema valida o novo nome e atualiza o marcador na sessão com sucesso | Não executado |
| **Remoção (Marcador Vazio)** | Sessão anônima com marcador criado, mas sem gastos associados | Acionar a exclusão do marcador vazio | Marcador apagado imediatamente da sessão do usuário | Não executado |
| **Remoção (Marcador com Dados - Cascata)** | Sessão anônima com marcador contendo gastos/lançamentos associados | Iniciar e confirmar a exclusão do marcador preenchido | Sistema remove o marcador e executa a remoção em cascata dos dados associados na sessão | Não executado |

---

## Classes de Equivalência
- **Variáveis de Decisão:** `nome_marcador_criacao`, `nome_marcador_edicao`, `estado_vinculo_marcador_remocao`.

| Variável | Condições | Classes válidas | Classes inválidas | Resultado esperado |
| :--- | :--- | :--- | :--- | :--- |
| **nome_marcador_criacao** | Verifica unicidade do nome do marcador na sessão | Nome de marcador inédito (não cadastrado na sessão atual) | Nome de marcador já existente na sessão ativa | Se válido, cria o marcador; se inválido, exibe alerta de nome duplicado e solicita novo nome |
| **nome_marcador_edicao** | Valida a alteração de um marcador existente | Novo nome válido e não duplicado para o marcador | Nome em branco ou duplicado de outro marcador na sessão | Atualiza com sucesso ou rejeita a alteração mantendo o estado anterior |
| **estado_vinculo_marcador_remocao** | Verifica dependências antes de excluir o marcador | Marcador sem registros vinculados ou marcador com registros para limpeza em cascata | N/A (Regra cobre ambos os estados previstos no CDU) | Apaga imediatamente se vazio, ou remove em cascata os dados dependentes se possuir registros |

---

## Fluxo Principal e Alternativos (Validação de Execução)

### 1. Fluxo Principal - Criação de Marcador
Passo a passo para chegar ao resultado:
1. O visitante tenta criar um novo marcador informando o nome desejado.
2. O sistema valida se não existe outro marcador com o mesmo nome na sessão.
3. Após a validação bem-sucedida, o novo marcador é criado de forma temporária.

- **Resultado Esperado:** Marcador adicionado à sessão do usuário anônimo sem afetar a base de dados.

### 2. Fluxo de Exceção - Nome de Marcador Duplicado
Passo a passo para chegar ao resultado:
1. O visitante tenta criar um marcador com um nome que já existe na sessão.
2. O sistema identifica a duplicidade e rejeita a criação.
3. Um alerta é exibido para o visitante informar um novo nome, retornando ao passo de validação.

- **Resultado Esperado:** Bloqueio da duplicidade e exibição correta do alerta visual.

### 3. Fluxo Alternativo I - Editar Marcador
Passo a passo para chegar ao resultado:
1. O visitante tenta editar um marcador existente na sessão.
2. O sistema valida o novo nome informado para o marcador.
3. O marcador é atualizado com o novo valor de forma temporária.

- **Resultado Esperado:** Informações do marcador atualizadas com sucesso na sessão.

### 4. Fluxo Alternativo II - Remover Marcador
Passo a passo para chegar ao resultado:
1. O visitante inicia a exclusão de um marcador na sessão.
2. O sistema verifica se o marcador está vazio ou contém dados vinculados.
3. Se estiver vazio, é apagado imediatamente; caso contrário, o sistema remove em cascata os dados daquele marcador.

- **Resultado Esperado:** Exclusão limpa do marcador e dos elementos dependentes estritamente no escopo da sessão temporária.
