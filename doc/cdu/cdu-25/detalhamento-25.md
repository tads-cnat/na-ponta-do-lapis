# CDU 25. Tour Funcionalidades

- **Ator principal**: Cliente.
- **Atores secundários**: N/A.
- **Resumo**: O sistema apresenta um guia visual e interativo, destacando partes da tela para ensinar e guiar o cliente na primeira vez.
- **Pré-condição**: O Cliente deve estar autenticado e acessando o site na sua **primeira vez**.
- **Pós-Condição**: O sistema encerra o guia, e **não** exibe o tour automaticamente nos próximos acessos.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O cliente acessa a conta criada pela primeira vez. | |
| | 2 - O sistema escurece o fundo da tela, destaca o primeiro elemento da interface e exibe um balão com a explicação da funcionalidade. |
| 3 - O Cliente lê a instrução e clica em **"Próximo"**. | |
| | 4 - O sistema move o foco e o balão explicativo para o próximo elemento da interface. |
| | 4.1 - Passo 3 e 4 se repetem até a última etapa do tour. |
| 5 - O Cliente chega ao último passo do guia e clica em **"Concluir"** | |
| | 6 - O sistema remove os destaques visuais, e libera a interface para navegação normal. |