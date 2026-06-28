# CDU 19. Manter Conta Financeira

- **Ator principal**: Cliente

- **Atores secundários**: não existe.
- **Resumo**: O sistema exibe uma [página](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=759-1244&t=U0qzQP3s9Su2RBkk-4) contendo as contas criadas até então, e as ações que podem ser feitas, caso já haja outras contas. No caso negativo, o sistema exibe uma mensagem de ausência de contas criadas e aconselha a criação de uma para poder realizar as demais ações. Uma das contas já está selecionada e serão mostrados seu nome, saldo, moeda e cor. O usuário clica no botão de “Adicionar Conta” para realizar a operação. Um formulário aparece e deve ser preenchido corretamente nos campos obrigatórios. Caso algum campo não tenha sido preenchido da forma certa, o sistema avisa que não pode criar a conta devido à falta de informações. Também as operações de atualizar e excluir conta podem ser feitas alternativamente.
- **Pré-condição**: O cliente deve estar devidamente autenticado
- **Pós-Condição**: Uma nova conta aparece na página de “Contas Financeiras”, possibilitando as demais ações da página.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O cliente clica no botão "Adicionar Conta" | |  
| | 2 - O sistema abre um [formulário](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1196-13892&t=U0qzQP3s9Su2RBkk-4) pedindo as informações necessárias para o prosseguimento da operação (nome*, saldo*, cor* e moeda* - * : obrigatórias) |
| 3 - O cliente preenche as informações e envia | |
| | 4 - O sistema valida a operação e mostra uma mensagem de êxito: "Conta criada com sucesso" |

## Fluxo Alternativo I - Informações da conta

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.1 - O cliente seleciona uma conta | |  
| | 2.1 - O sistema mostra uma [visão detalhada](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1599-21555&t=U0qzQP3s9Su2RBkk-4) das informações da conta selecionada |

## Fluxo Alternativo II - Alteração de dados de conta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.2 - O cliente seleciona uma conta e clica na opção de "Editar Conta" | |  
| | 2.2 - O sistema exibe um [formulário](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1196-13282&t=U0qzQP3s9Su2RBkk-4) equivalente ao de "Adicionar conta", mas preenchido com as informações da conta selecionada |
| 3.2 - O cliente altera o que desejar e envia os dados | |
| | 4.2 - O sistema faz a validação e mostra uma mensagem de êxito: "Conta alterada com sucesso" |

## Fluxo Alternativo III - Exclusão de conta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.3 - O cliente seleciona uma conta e clica na opção de "Excluir conta" | |  
| | 2.3 - O sistema [envidencia](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=943-4471&t=U0qzQP3s9Su2RBkk-4) qual conta foi selecionada e solicita a confirmação do cliente |
| 3.3 - O cliente confirma a solicitação de exclusão | |
| | 4.3 - O sistema exclui a conta e apresenta uma mensagem de sucesso: "A Conta '(nome da conta)' foi excluída!" |

## Fluxo Alternativo IV - Exibição de evolução gráfica do saldo

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.4 - O cliente seleciona uma conta | |  
| | 2.4 - O sistema exibe um [gráfico](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=935-7407&t=U0qzQP3s9Su2RBkk-4) mostrando a evolução do saldo da conta selecionada ao longo do tempo, podendo ser filtrado mensalmente ou semanalmente |

## Fluxo Alternativo V - Histórico de transações

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.5 - O cliente seleciona uma conta | |
| | 2.5 - O sistema exibe uma [tabela](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=1599-21556&t=U0qzQP3s9Su2RBkk-4) ordenada das transações mais recentes feitas pela conta |

## Fluxo de Exceção I - Informações incongruentes

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 4.1 - O sistema verifica se alguma(s) informação(ões) não foi(foram) preenchida(s) corretamente, como campos vazios ou, especificamente inserir um nome de conta já existente, e mostra uma mensagem de erro: "Alguma(s) informação(ões) não preenchida(s) ou inválida(s)!" e retorna o formulário com o(s) campo(s) inválido(s) pedindo o correto preenchimento |  
| | (retorna ao passo 3 do fluxo principal) |

## Fluxo de Exceção II - Conta sem histórico de transações

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 2.6 - O sistema ao invés de mostrar as últimas transações feitas, exibe a seguinte mensagem: "Esta conta ainda não possui transações registradas" |

## Diagrama de Interação (Sequência ou Comunicação)

## Diagrama de Classes de Projeto
