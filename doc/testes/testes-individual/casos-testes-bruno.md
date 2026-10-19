# 1 Introdução

Este documento reúne os casos de teste elaborados para validar os principais fluxos da aplicação Na Ponta do Lápis. Os testes aqui descritos abrangem os casos de uso de gerenciamento de contas e transações da fatura, com o objetivo de assegurar que cada funcionalidade opere conforme os modelados no sistema.

Cada caso de teste inclui informações detalhadas sobre o cenário a ser avaliado, os dados de entrada necessários e resultados esperados.

## Testes Funcionais

### CDU - Manter Conta

#### Fluxo Principal - Criar conta

| Nome | Saldo Inicial | Cor | Moeda | Resultado esperado | Resultado obtido | Situação |
| --- | --- | --- | --- | --- | --- | --- |
| Nubank Principal | 1500.00 | #8A05BE | BRL | Conta criada com sucesso! | - | - |
| Carteira em Dólar | 50.00 | #008000 | USD | Conta criada com sucesso! | - | - |
| - | 500.00 | #FF0000 | BRL | Erro: O nome da conta não pode ser vazio. | - | - |
| Reserva de Emergência | - | #0000FF | BRL | Erro: O saldo é obrigatório. | - | - |
| Caixinha | 150.00 | - | EUR | Erro: A cor não pode ser vazia. | - | - |
| Poupança | 1000.00 | #12345678 | BRL | Erro: A cor não pode ter mais de 7 caracteres. | - | - |
| Conta Corrente | 0.00 | #000000 | - | Erro: A moeda é obrigatória. | - | - |
| [Nome com mais de 100 caracteres...] | 10.00 | #123456 | BRL | Erro: Erro de integridade / Limite de caracteres excedido (100). | - | - |

#### Fluxo Secundário - Atualizar conta (Patch/Put)

| Conta ID | Novo Nome | Novo Saldo | Nova Cor | Nova Moeda | Resultado esperado | Resultado obtido | Situação |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Nubank Secundário | 200.00 | #8A05BE | BRL | Conta atualizada com sucesso! | - | - |
| 1 | - | 500.00 | - | - | Conta atualizada com sucesso (Atualização parcial via Patch). | - | - |
| 2 | - | - | - | - | Erro via Patch: Pelo menos um campo deve ser fornecido para atualização parcial. | - | - |
| 999 | Conta Teste | 0.00 | #000000 | USD | Erro: Conta de ID 999 não existe. | - | - |

#### Fluxo Secundário - Deletar conta

| ID da Conta | Condição | Resultado esperado | Resultado obtido | Situação |
| --- | --- | --- | --- | --- |
| 1 | Conta sem transações vinculadas | Conta excluída com sucesso (204 No Content). | - | - |
| 2 | Conta com transações vinculadas | Erro: Não é possível excluir este item porque ele está sendo usado em outros registros. | - | - |
| 999 | Conta inexistente | Erro: Conta de ID: 999 não existe. | - | - |

### CDU - Adicionar transação pela fatura

> **Nota:** Este fluxo cobre a rota de salvar em lote (`/transacoes/lote?contaId=X`), recebendo uma lista de `TransacaoFaturaDTO`. O sistema define automaticamente como DESPESA e estado REALIZADA.

#### Fluxo Principal - Criar transações na fatura em lote

| ID da Conta | Descrição | Valor | Data | Categoria (Nome) | Resultado esperado | Resultado obtido | Situação |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Supermercado Extra | 250.00 | 15/09/2026 | Alimentação | Transações salvas com sucesso! (Criadas como DESPESA e REALIZADA) | - | - |
| 1 | Uber | 15.50 | 16/09/2026 | Transporte | Transações salvas com sucesso! | - | - |
| - | Assinatura Netflix | 39.90 | 10/09/2026 | Entretenimento | Erro: Parâmetro `contaId` é obrigatório / Conta não encontrada. | - | - |
| 999 | Compra online | 120.00 | 12/09/2026 | Outros | Erro: Conta de ID: 999 não existe. | - | - |
| 1 | Farmácia | 50.00 | 15/09/2026 | CategoriaInexistente | Erro: Categoria de Nome: CategoriaInexistente não encontrado! | - | - |
| 2 | - | 150.00 | 18/09/2026 | Lazer | Erro de integridade (Descrição não pode ser nula no banco). | - | - |
| 2 | Padaria | - | 18/09/2026 | Alimentação | Erro de integridade (Valor não pode ser nulo). | - | - |
| 2 | Compra Genérica | 100.00 | - | Outros | Erro: Formato de data inválido / Data não pode ser nula no banco. | - | - |
