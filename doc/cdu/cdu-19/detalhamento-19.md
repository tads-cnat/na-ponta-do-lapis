# CDU 19. Manter Conta Financeira (Persistente)
 

- **Ator principal**: Usuário

- **Atores secundários**: não existe.	 

- **Resumo**: O sistema exibe uma página contendo as contas criadas até então, e as ações que podem ser feitas, caso já haja outras contas. Uma das contas já estará selecionada e será mostrado seu nome, tipo e saldo, cada tipo terá uma imagem genérica para sua representação. O usuário clica no botão de “Adicionar Conta” para realizar a operação. Um formulário aparecerá e deve ser preenchido corretamente nos campos obrigatórios. Caso algum campo não tenha sido preenchido da forma certa, o sistema avisará que não poderá criar a conta devido à falta de informações. Também as operações de atualizar e excluir poderão ser feitas.

- **Pré-condição**: O usuário deverá estar devidamente autenticado e acessar a aba “Contas Financeiras”

- **Pós-Condição**: Uma nova conta aparecerá na página de “Contas Financeiras”, possibilitando as demais ações de uma conta.


## Fluxo Principal
| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: | 
| 1 - O usuário clica no botão "Adicionar Conta" | |  
| | 2 - O sistema abre um formulário pedindo as informações necessárias para o prosseguimento da operação (nome*, saldo* e tipo* --- *: obrigatórias) | 
|3 - O usuário preenche as informações e clica em "Salvar" ||
||4 - O sistema valida a operação e mostra uma mensagem de êxito: "Conta criada com sucesso"|

## Fluxo Alternativo I - Informações incongruentes
| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: | 
|| 4.1 - O sistema verifica se alguma(s) informação(ões) não foi(foram) preenchida(s) corretamente e mostra uma mensagem de erro: "Alguma(s) informação(ões) não preenchida(s) ou inválida(s)!" e retorna o formulário com o(s) campo(s) inválido(s) pedindo o correto preenchimento |  
|| (retorna ao passo 3 do fluxo principal) |

## Fluxo Alternativo II - Visualizar conta
| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: | 
| 1.2 - O usuário seleciona uma conta através do carrossel de imagens das contas criadas e clica na opção de "visualizar conta" ||  
|| 2.2 - O sistema mostra uma visão detalhada das informações da conta selecionada e o seu histórico de transações ordenado cronologicamente e em formato de tabela|

## Fluxo Alternativo III - Alteração de dados de conta
| Ações do ator | Ações do sistema |
| :-----------: | :--------------: | 
| 1.3 - O usuário seleciona uma conta através do carrossel de imagens das contas criadas e clica na opção de "Editar conta" | |  
| | 2.3 - O sistema exibe um formulário equivalente ao de "Adicionar conta", mas preenchido |
| 3.3 - Parecido com o passo 3 do fluxo principal, o usuário altera e salva os dados que desejar | |
| | 4.3 - O sistema faz a validação, podendo repetir as etapas 4 do fluxo principal ou o 4.1 do fluxo alternativo I |
||5.3 - O sistema mostra uma mensagem de êxito: "Conta alterada com sucesso"|


## Fluxo Alternativo IV - Exclusão de conta
| Ações do ator | Ações do sistema |
| :-----------: | :--------------: | 
| 1.4 - O usuário seleciona uma conta através do carrossel de imagens das contas criadas e clica na opção de "Excluir conta" | |  
| | 2.4 - O sistema solicita a confirmação do usuário |
| 3.4 - O usuário confirma a solicitação de exclusão | |
| | 4.4 - O sistema exclui a conta e apresenta uma mensagem de sucesso: "A Conta '(nome da conta)' foi excluída!" |


> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...