# CDU 19. Manter Conta Financeira

- **Ator principal**: Cliente ou Administrador da família

- **Atores secundários**: não existe.
- **Resumo**: O sistema exibe uma página contendo as contas criadas até então, e as ações que podem ser feitas, caso já haja outras contas. No caso negativo, o sistema exibe uma mensagem de ausência de contas criadas e aconselha a criação de uma para poder realizar as demais ações. Posteriormente uma das contas já estará selecionada e serão mostrados seu nome, tipo e saldo. O usuário clica no botão de “Adicionar Conta” para realizar a operação. Um formulário aparece e deve ser preenchido corretamente nos campos obrigatórios. Caso algum campo não tenha sido preenchido da forma certa, o sistema avisa que não pode criar a conta devido à falta de informações. Também as operações de visualizar histórico de transações, atualizar e excluir conta podem ser feitas, alternativamente.
- **Pré-condição**: O usuário deve estar devidamente autenticado
- **Pós-Condição**: Uma nova conta aparece na página de “Contas Financeiras”, possibilitando as demais ações da página.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O usuário clica no botão "Adicionar Conta" | |  
| | 2 - O sistema abre um [formulário](../prototipos/Editar%20conta.png) pedindo as informações necessárias para o prosseguimento da operação (nome*, saldo*, cor* e moeda* - * : obrigatórias) |
| 3 - O usuário preenche as informações e envia | |
| | 4 - O sistema valida a operação e mostra uma mensagem de êxito: "Conta criada com sucesso" |

## Fluxo Alternativo I - Informações da conta

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.1 - O usuário seleciona uma conta | |  
| | 2.1 - O sistema mostra uma visão detalhada das informações da conta selecionada |

## Fluxo Alternativo II - Alteração de dados de conta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.2 - O usuário seleciona uma conta e clica na opção de "Editar Conta" | |  
| | 2.2 - O sistema exibe um formulário equivalente ao de "Adicionar conta", mas preenchido com as informações da conta selecionada |
| 3.2 - O usuário altera o que desejar e envia os dados | |
| | 4.2 - O sistema faz a validação e mostra uma mensagem de êxito: "Conta alterada com sucesso" |

## Fluxo Alternativo III - Exclusão de conta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.4 - O usuário seleciona uma conta através do [carrossel de imagens](../imgs/cdu.png) das contas criadas e clica na opção de "Excluir conta" | |  
| | 2.4 - O sistema envidencia qual conta foi selecionada e solicita a confirmação do usuário |
| 3.4 - O usuário confirma a solicitação de exclusão | |
| | 4.4 - O sistema exclui a conta e apresenta uma mensagem de sucesso: "A Conta '(nome da conta)' foi excluída!" |

## Fluxo Alternativo IV - Exibição de evolução gráfica do saldo

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.3 - O usuário seleciona um dos campos da conta selecionada, ou seja, centralizada na tela e altera e confirma a alteração | |  
| | 2.3 - O sistema valida a alteração e mostra uma mensagem de êxito: "Dado alterado com sucesso" |

## Fluxo de Exceção I - Informações incongruentes

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 4.1 - O sistema verifica se alguma(s) informação(ões) não foi(foram) preenchida(s) corretamente, como campos vazios ou, especificamente inserir um nome de conta já existente, e mostra uma mensagem de erro: "Alguma(s) informação(ões) não preenchida(s) ou inválida(s)!" e retorna o formulário com o(s) campo(s) inválido(s) pedindo o correto preenchimento |  
| | (retorna ao passo 3 do fluxo principal) |

## Fluxo de Exceção II - Conta sem histórico de transações

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 2.6 - O sistema mostra uma visão detalhada das informações da conta selecionada e exibe a seguinte mensagem: "Sem Transações Esta conta ainda não possui transações registradas", além de convidar o usuário a ir a aba de transações para cadastrar uma e poder mostrar o histórico |

## Diagrama de Interação (Sequência ou Comunicação)

## Diagrama de Classes de Projeto
