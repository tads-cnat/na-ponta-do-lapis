# CDU 17. Manter Meta (Persistente)

- **Ator principal**: Usuario.
- **Atores secundários**: N/A.	 
- **Resumo**: Ao apertar no botão "Meta", é apresentado um quadro com todas as metas ja criadas e seus devidos progressos, sendo possivel a adição, edição e exclusão das mesmas. 
- **Pré-condição**: O usuario deverá está autenticado
- **Pós-Condição**: As açõoes do usuario serão salvas do banco de dados.

## Fluxo Principal
| Ações do ator | Ações do sistema |
| :-----------: | :--------------: | 
| 0 - No menu lateral o usuario seleciona a opção "Metas" | |  
| | 1 - O sistema apresenta um quadro com todas as metas incluindo a porcentagem de progresso, descrição e prazo. alem disso as opções de edição e exclusão |
| 2 - Ao pressionar o botão de adicionar nova meta | |
| | 3 - o sistema apresenta um formulario para definir a descrição da meta, o valor, prazo e uma imagem |
| 4 - O usuario preenche os dados e clicka no botão "Salvar" | |
|| 5 - O sistema valida as informações e salva na persistencia 


## Fluxo Alternativo I - Edição
| Ações do ator | Ações do sistema |
| :-----------: | :--------------: | 
| 2.1 - O usuario aperta no botão de editar |  |  
||3.1 - O sistema mostra o mesmo formulario do passo 3 porem ja preenchido com as informações previamente inseridas|
| Retorna ao passo 4||

## Fluxo Alternativo II - Exclusão
| Ações do ator | Ações do sistema |
| :-----------: | :--------------: |  
| 2.2 - O usuario aperta no botão de Excluir |  |  
|| 3.2 - É exibido uma tela de confirmação de exclusão|
|4.2 - O usuario confirma a exclusão | |
||5.2 - O Sistema remove a meta da persistência|

> Obs. as seções a seguir apenas serão utilizadas na segunda unidade do PDSWeb (segundo orientações do gerente do projeto).

## Diagrama de Interação (Sequência ou Comunicação)

> Substituir pela imagem correspondente...

## Diagrama de Classes de Projeto

> Substituir pela imagem contendo as classes (modelo, visão e templates) que implementam o respectivo CDU...