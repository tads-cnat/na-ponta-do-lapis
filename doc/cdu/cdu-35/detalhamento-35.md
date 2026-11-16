# CDU 35. Conectar e Sincronizar Contas via Open Finance (Pluggy)

- **Ator principal**: Cliente.
- **Atores secundários**: API Pluggy (Open Finance).
- **Resumo**: Na aba **Integrações** do Perfil (`/app/perfil`), o cliente pode conectar suas credenciais/itens do provedor **Pluggy Open Finance** para importar e sincronizar automaticamente contas bancárias e transações. O sistema armazena as credenciais de forma criptografada (`AES/GCM`) e permite sincronização manual sob demanda ou automática via agendador (`OpenFinanceScheduler` com bloqueio distribuído `ShedLock`).
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: Contas e transações provenientes das instituições conectadas no Open Finance são importadas e atualizadas no sistema.

## Fluxo Principal

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente acessa **Perfil → Integrações** (`/app/perfil`). | |
| | 2 - O sistema consulta o status atual da integração (`GET /api/v1/integracoes/open-finance/status` e `/items`). |
| 3 - O Cliente informa as credenciais (`Client ID` e `Client Secret` da Pluggy) ou adiciona um `Item ID` bancário e clica em conectar/sincronizar. | |
| | 4 - O sistema valida a conexão junto à API Pluggy (`POST /api/v1/integracoes/open-finance/conectar`), persiste as credenciais criptografadas em `UsuarioPluggyIntegracao` e importa contas e transações (`POST /api/v1/integracoes/open-finance/sincronizar`). |
| | 5 - O sistema exibe o resumo de contas e transações sincronizadas e a data da última sincronização. |

## Fluxo Alternativo I - Desconectar Integração Open Finance

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.1 - O Cliente clica em **"Desconectar"** na seção Open Finance (ou remove um item específico em `DELETE /items/{itemId}`). | |
| | 3.2 - O sistema remove a integração (`DELETE /api/v1/integracoes/open-finance/desconectar`) e encerra as sincronizações automáticas daquela credencial. |

