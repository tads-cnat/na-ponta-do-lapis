# CDU 26. Adicionar Transação (Fatura PDF com IA)

- **Ator principal**: Cliente.
- **Atores secundários**: Provedores de IA (Google Gemini, OpenAI ou Anthropic Claude — via chave BYOK do usuário).
- **Resumo**: O Cliente pode importar transações a partir de uma fatura de cartão de crédito em PDF. O sistema realiza validação prévia do documento (`POST /api/v1/faturas/validar-previa`), suporta **PDF protegido por senha**, extrai os lançamentos localmente (Regex) ou com auxílio de **IA generativa** mediante **consentimento LGPD** (`POST /api/v1/faturas/analise`), sinaliza possíveis duplicatas e permite revisar/editar cada item antes de salvar em lote (`POST /api/v1/transacoes/lote`).
- **Pré-condição**: O Cliente deve estar autenticado e possuir ao menos uma conta financeira cadastrada.
- **Pós-Condição**: As transações selecionadas na revisão são cadastradas em lote na conta escolhida (com geração opcional das parcelas futuras).

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente clica em **"Nova Transação"** e seleciona **"Importar Fatura PDF"**. | |
| | 2 - O sistema abre o modal de importação e consulta a sessão/chaves de IA salvas (`GET /api/v1/faturas/sessao`). |
| 3 - O Cliente seleciona o arquivo PDF da fatura. | |
| | 4 - O sistema executa a validação prévia (`POST /api/v1/faturas/validar-previa`), identifica o emissor do cartão e verifica se o PDF exige senha ou ativação de IA. |
| 5 - O Cliente configura as opções de análise (extração local ou com IA escolhendo provedor/chave BYOK e aceitando o termo LGPD) e clica em analisar. | |
| | 6 - O sistema processa a fatura (`POST /api/v1/faturas/analise`), extrai data, descrição, valor, categoria sugerida e informações de parcelamento, e destaca possíveis duplicatas já existentes na conta. |
| 7 - Na tela de revisão, o Cliente seleciona a conta financeira de destino, ajusta/desmarca lançamentos se desejar e confirma a importação. | |
| | 8 - O sistema salva os lançamentos em lote (`POST /api/v1/transacoes/lote`), gera parcelas futuras quando aplicável e exibe confirmação de sucesso. |

## Fluxo Alternativo I - PDF Protegido por Senha

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 4.1 - A validação prévia detecta que o PDF está criptografado (`precisaSenhaPdf`). |
| 4.2 - O Cliente informa a senha do PDF e revalida o arquivo. | |
| | 4.3 - O sistema desbloqueia o PDF em memória e prossegue para o passo 5. |
