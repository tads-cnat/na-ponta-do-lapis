# CDU 36. Gerenciar Chaves de IA Pessoais (BYOK)

- **Ator principal**: Cliente.
- **Atores secundários**: Provedores de IA (Google Gemini, OpenAI, Anthropic Claude).
- **Resumo**: Permite que o usuário cadastre e gerencie suas próprias chaves de API de Inteligência Artificial (*Bring Your Own Key — BYOK*) para uso na leitura inteligente de faturas PDF. As chaves são armazenadas criptografadas no banco (`ChaveIaUsuario`) e o usuário pode cadastrar múltiplas chaves, definir qual é a padrão por provedor e excluir chaves existentes.
- **Pré-condição**: O Cliente deve estar autenticado.
- **Pós-Condição**: A chave de IA é salva criptografada, marcada como padrão ou removida conforme a operação realizada.

## Fluxo Principal (Adicionar Chave de IA)

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - O Cliente acessa **Perfil → Integrações** (ou o modal de configuração de IA na importação de faturas). | |
| | 2 - O sistema lista as chaves cadastradas do usuário (`GET /api/v1/integracoes/ia/chaves`) exibindo apelido, provedor (`GEMINI`, `OPENAI`, `CLAUDE`), máscara de segurança da chave e indicador de chave padrão. |
| 3 - O Cliente seleciona o provedor, informa um apelido e a chave de API e confirma o cadastro. | |
| | 4 - O sistema criptografa a chave (`POST /api/v1/integracoes/ia/chaves`), persiste no banco de dados e atualiza a listagem. |

## Fluxo Alternativo I - Definir Chave como Padrão

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.1 - O Cliente clica em **"Tornar padrão"** em uma chave cadastrada. | |
| | 3.2 - O sistema executa `PATCH /api/v1/integracoes/ia/chaves/{id}/padrao` e define aquela chave como padrão para futuras análises de faturas. |

## Fluxo Alternativo II - Excluir Chave de IA

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 3.2 - O Cliente clica em excluir em uma chave cadastrada e confirma. | |
| | 3.3 - O sistema remove o registro (`DELETE /api/v1/integracoes/ia/chaves/{id}`) e atualiza a listagem. |

