# CDU 26. Adicionar Transação (Fatura)

- **Ator principal**: Cliente.
- **Atores secundários**: N/A.
- **Resumo**: O Cliente pode adicionar uma nova transação realizando o upload da fatura em formato PDF. O sistema processa o arquivo, extrai automaticamente as informações e cadastra as transações.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: A transação é cadastrada no sistema com os dados extraídos da fatura em PDF.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente clica no botão **"Adicionar Transação"**. | |
| | 2 - O sistema exibe um pop-up com as opções **"Manual"** e **"Upload de PDF"**. |
| 3 - O Cliente seleciona a opção **"Upload de PDF"**. | |
| | 4 - O sistema exibe a interface para seleção do arquivo PDF. |
| 5 - O Cliente seleciona a fatura em formato PDF e confirma o envio. | |
| | 6 - O sistema realiza o upload do arquivo e processa seu conteúdo. |
| | 7 - O sistema extrai automaticamente as informações da fatura e preenche os dados da transação. |
| | 8 - O sistema exibe os campos contendo informações das faturas para possíveis correções. |
|9 - O cliente verifica e clica no botão confirmar transações. | |
| | 10 - O sistema fecha o pop-up e informa **"Transações salvas com sucesso!"**  |