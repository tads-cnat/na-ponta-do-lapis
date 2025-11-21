# CDU 14. Manter Cotação

- **Ator principal**: Administrador do site

- **Atores secundários**: não existe.

- **Resumo**: O sistema exibe uma página contendo uma lista de cotações criadas até então, caso já haja outras criadas. Cada registro mostra o nome, valor e data de cada uma. O Administrador do site clica no botão de “Registrar nova cotação” para realizar a operação. Um formulário aparece e deve ser preenchido corretamente nos campos obrigatórios. Caso algum campo não tenha sido preenchido da forma certa, o sistema avisa que não pode finalizar o processo devido à falta de informações.

- **Pré-condição**: O administrador do site deverá estar devidamente autenticado e acessar a aba “Cotação”

- **Pós-Condição**: Uma nova cotação será registrada e aparecerá na página de “Cotação” através de uma lista cotações criadas ordenada cronologicamente, da mais recente à mais antiga.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Administrador do site clica no botão "Registrar nova cotação" | |  
| | 2 - O sistema abre um formulário pedindo as informações necessárias para o prosseguimento da operação (nome*, valor* e data* - * : obrigatórias) |
| 3 - O Administrador do site preenche as informações e clica em "Salvar" ||
|| 4 - O sistema valida a operação e mostra uma mensagem de êxito: "Cotação registrada com sucesso" |

## Fluxo Alternativo I - Informações incongruentes

| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: |
|| 4.1 - O sistema verifica se alguma(s) informação(ões) não foi(foram) preenchida(s) corretamente e mostra uma mensagem de erro: "Alguma(s) informação(ões) não preenchida(s) ou inválida(s)!" e retorna o formulário com o(s) campo(s) inválido(s) pedindo o correto preenchimento |  
|| (retorna ao passo 3 do fluxo principal) |

## Fluxo Alternativo II - Visualizar Cotação

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1.2 - O Administrador do site seleciona uma cotação através da lista de cotações já criadas e clica na opção de "Visualizar cotação" ||  
|| 2.2 - O sistema mostra uma visão detalhada das informações da cotação selecionada em formato de tabela |

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...
