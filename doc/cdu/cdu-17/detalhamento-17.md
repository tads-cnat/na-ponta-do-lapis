# CDU 17. Manter Meta 

- **Ator principal**: Cliente.

- **Atores secundários**: N/A.
- **Resumo**: Ao apertar no botão "Meta", é apresentado um quadro com todas as metas ja criadas e seus devidos progressos, sendo possivel a adição, edição e exclusão das mesmas.
- **Pré-condição**: O cliente deverá estar autenticado.
- **Pós-Condição**: As açõoes do cliente serão salvas do banco de dados.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 0 - No menu lateral o cliente seleciona a opção "Metas" | |
| | 1 - O sistema apresenta um [lista](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=927-2886&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654&fuid=1249119361476075397) com todas as metas incluindo a porcentagem de progresso, descrição e prazo. alem disso as opções de edição e exclusão |
| 2 - Ao pressionar o botão de adicionar nova meta | |
| | 3 - o sistema apresenta um [formulario](figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=927-2887&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654&fuid=1249119361476075397) para definir a descrição da meta, o valor, prazo e uma imagem |
| 4 - O cliente preenche os dados e clicka no botão "Salvar" | |
|| 5 - O sistema valida as informações e salva na persistencia |

## Fluxo Alternativo I - Edição

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 2.1 - O cliente aperta no botão de editar |  |  
|| 3.1 - O sistema mostra o mesmo [formulario](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=945-4569&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) do passo 3 porém ja preenchido com as informações previamente inseridas |
| Retorna ao passo 4 | |

## Fluxo Alternativo II - Exclusão

| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |
| 2.2 - O cliente aperta no botão de Excluir | |
|| 3.2 - É exibido uma [tela de confirmação de exclusão](https://www.figma.com/proto/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=945-4872&m=dev&scaling=min-zoom&content-scaling=fixed&page-id=625%3A100&starting-point-node-id=1545%3A21654) |
| 4.2 - O cliente confirma a exclusão | |
| | 5.2 - O Sistema remove a meta da persistência|

