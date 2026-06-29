# CDU 21. Remover Membro da Família 

- **Ator principal**: Usuário administrador do site.
- **Atores secundários**: N/A.
- **Resumo**: Na página "Grupo Familiar" o usuário administrador da família aperta no botão para remover um membro da família.
- **Pré-condição**: Usuário cadastrado, autenticado e administrador da família.
- **Pós-Condição**: O membro é desligado da família no banco da dados.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: | 
| 1 - Aperta o botão "-" na [caixa de membros](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=859-1943&t=DQ2OEuq9dFUHHszq-4). | |  
| | 2 - Retorna um [formulário](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=935-7850&t=DQ2OEuq9dFUHHszq-4) para preencher os dados. | 
| 3 - Preenche os dados e aperta o botão de "remover membro". | |
| | 4 - Retorna mensagem de "Membro Removido". |

