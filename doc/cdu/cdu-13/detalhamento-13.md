# CDU 13. Manter Post 

- **Ator principal**: Administrador
- **Atores secundários**: n/a	 
- **Resumo**: O administrador consegue gerenciar as postagens do site, executando operações de CRUD como criar um novo post, remover, editar e vizualizar. Para a criação de um post o administrador deverá preencher um formulario com titulo, corpo e imagem do post. Apos isso conseguira confirmar e efetuar a postagem de fato.
- **Pré-condição**: Estar autenticado como administrador do sistema.
- **Pós-Condição**: O estado do post é atualizado no sistema de acordo com a operação realizada(Criar,editar ou remover), uma mensagem de feedback é enviada ao administador confirmando a operação realizada.

## Fluxo Principal
| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: | 
| 1 - O administrador inicia o caso de uso clickando no botão "Gerenciar posts". | |  
| | 2 - O sistema exibe uma listagem de todos os posts e um botão de criar novo post. |
| 3 - O usuario clicka do botão de criar novo post.||
|| 4 - O sistema apresenta um fomulario contendo os campos: titulo, corpo e imagem.|
| 5 - O administrador preenche todos os campos. ||
| 6 - O administrador clicka no botão "Publicar". ||
|| 7 - As informações preenchidas são válidadas.|
|| 8 - O post é salvo na persistencia e a listagem de posts é atualizada.|
|| 9 - Uma mensagem de feedback com sucesso é enviada. |

## Fluxo Alternativo I - Editar Post
| Ações do ator | Ações do sistema |
| :-----------------: |:-----------------: | 
| 3.1 - O usuario seleciona a opção "Editar" proximo ao post desejado |  |
|| 4.1 - O sistema apresenta um fomulario contendo os campos: titulo, corpo e imagem. Ja preenchidas|
|| Retorna ao passo 5 do fluxo principal  |

## Fluxo Alternativo II - Excluir Post
| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: | 
| 3.2 - O usuario seleciona a opção "Excluir" proximo ao post desejado | |  
| | 4.2 - O sistema exibe um pop-up de confirmação de exclusão |
| 5.2 - O usuario seleciona o botão "Confirmar exclusão" ||
|| 6.2 - O post é removido da persistencia e a listagem de posts é atualizada|
|| 7.2 - Uma mensagem de feedback que a exclusão foi executada com sucesso é exibida|

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...