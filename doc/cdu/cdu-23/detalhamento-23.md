# CDU 23. Tornar Administrador da Família

- **Ator principal**: Administrador da Família.

- **Atores secundários**: Cliente.
- **Resumo**: O Administrador acessa a aba de "Membros" e seleciona um dos membros de sua família. Vendo suas informações detalhadas e históricos, ele clica na opção de "Tornar Administrador da Família" e a operação é efetuada. Caso o membro selecionado já seja um administrador da família, a opção estará desabilitada.
- **Pré-condição**: Administrador da Família devidamente autenticado.
- **Pós-Condição**: O cliente envolvido se torna um Administrador da Família.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Administrador da Família acessa a aba de "Membros" | |
| | 2 - O sistema mostra os usuários ligados à família (membros) |
| 3 - Administrador da Família seleciona um dos membros | |
| | 4 - O sistema mostra uma visão detalhada do membro, incluindo seu histórico de transações e uma opção chamada "Tornar Administrador da Família" |
| 5 - O Administrador da Família seleciona a opção e confirma sua ação | |
| | 6 - O sistema faz a operação e envia uma mensagem: "O (cliente selecionado) agora é um Administrador da Família" |

## Fluxo Alternativo I - O Usuário já é um Administrador da Família

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 6.1 - Enquanto o Administrador da Família tenta selecionar a opção "Tornar Administrador da Família", O sistema mostra uma mensagem: "A operação não pode ser feita pois este membro já é um Administrador da Família", sua inviabilidade é evidenciada pela cor da fonte estar menos opaca |
| | (O sistema permanece na ação 4 do fluxo principal) |

