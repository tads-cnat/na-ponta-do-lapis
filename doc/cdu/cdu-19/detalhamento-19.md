# CDU 19. Manter Conta Financeira

- **Ator principal**: Usuário

- **Atores secundários**: não existe.
- **Resumo**: O sistema exibe uma página contendo as contas criadas até então, e as ações que podem ser feitas, caso já haja outras contas. No caso negativo, o sistema exibe uma mensagem de ausência de contas criadas e aconselha a criação de uma para poder realizar as demais ações. Posteriormente uma das contas já estará selecionada e serão mostrados seu nome, tipo e saldo. O usuário clica no botão de “Adicionar Conta” para realizar a operação. Um formulário aparece e deve ser preenchido corretamente nos campos obrigatórios. Caso algum campo não tenha sido preenchido da forma certa, o sistema avisa que não pode criar a conta devido à falta de informações. Também as operações de visualizar histórico de transações, atualizar e excluir conta podem ser feitas, alternativamente.
- **Pré-condição**: O usuário deverá estar devidamente autenticado ou entrar no site como convidado e acessar a aba “Contas Financeiras”
- **Pós-Condição**: Uma nova conta aparecerá na página de “Contas Financeiras”, possibilitando as demais ações da página.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O usuário clica no botão "Adicionar Conta" | |  
| | 2 - O sistema abre um formulário pedindo as informações necessárias para o prosseguimento da operação (nome*, saldo* e tipo* - *: obrigatórias) |
| 3 - O usuário preenche as informações e clica em "Salvar" ||
|| 4 - O sistema valida a operação e mostra uma mensagem de êxito: "Conta criada com sucesso" |

## Fluxo Alternativo I - Informações incongruentes

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
|| 4.1 - O sistema verifica se alguma(s) informação(ões) não foi(foram) preenchida(s) corretamente e mostra uma mensagem de erro: "Alguma(s) informação(ões) não preenchida(s) ou inválida(s)!" e retorna o formulário com o(s) campo(s) inválido(s) pedindo o correto preenchimento |  
|| (retorna ao passo 3 do fluxo principal) |

## Fluxo Alternativo II - Visualizar conta

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.2 - O usuário seleciona uma conta através do carrossel de imagens das contas criadas e clica na opção de "visualizar conta" ||  
|| 2.2 - O sistema mostra uma visão detalhada das informações da conta selecionada e o seu histórico de transações ordenado cronologicamente e em formato de tabela |

## Fluxo Alternativo III - Alteração de dados de conta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.3 - O usuário seleciona uma conta através do carrossel de imagens das contas criadas e clica na opção de "Editar conta" | |  
| | 2.3 - O sistema exibe um formulário equivalente ao de "Adicionar conta", mas preenchido |
| 3.3 - Parecido com o passo 3 do fluxo principal, o usuário altera e salva os dados que desejar | |
| | 4.3 - O sistema faz a validação e, podendo repetir as etapas 4 do fluxo principal ou o 4.1 do fluxo alternativo I, mostra uma mensagem de êxito: "Conta alterada com sucesso" |

## Fluxo Alternativo IV - Exclusão de conta

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 1.4 - O usuário seleciona uma conta através do carrossel de imagens das contas criadas e clica na opção de "Excluir conta" | |  
| | 2.4 - O sistema envidencia qual conta foi selecionada e solicita a confirmação do usuário |
| 3.4 - O usuário confirma a solicitação de exclusão | |
| | 4.4 - O sistema exclui a conta e apresenta uma mensagem de sucesso: "A Conta '(nome da conta)' foi excluída!" |

## Diagrama de Interação (Sequência ou Comunicação)

## Diagrama de Classes de Projeto
