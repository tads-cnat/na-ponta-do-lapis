# Documento Geral de Casos de Teste - Pedro

## Introdução
Neste documento reúnem-se casos de testes elaborados para os principais casos de uso da aplicação Na Ponta do Lápis. Os testes buscam assegurar a coerência das funcionalidades, assim, atendendo os requisitos estabelecidos.

Cada caso de teste inclui informações detalhadas sobre o cenário a ser avaliado, os dados de entrada necessários e resultados esperados.

## CDU 02 - Logout

### Fluxo principal

#### Objetivo: 
Deslogar do sistema

#### Variáveis de Entrada:
- Variável 1: Access Token

#### Condições de Negócio:
- Condição 1: O token é alfanumérico

#### Classes de Equivalência
| Classes Válidas             | Classes Inválidas           |
|-----------------------------|-----------------------------|
| Possui `access token` | Não possui `access token` |

#### Testes funcionais
| Access token | Resultado esperado | Resultado obitido | Situação | 
| :-----: | :-----: | :-----: | :-------------------: |
| "ASDFDGFGHSD67688GFGDSFGDKJ..." | Local Storage limpo | ---- | ---- |
| *vazio* | erro(token não encontrado) | ---- | ---- |

---

## CDU 12 - Alterar tema

### Fluxo principal

#### Objetivo: 
Mudar o tema do sistema entre claro e escuro

#### Variáveis de Entrada:
- Variável 1: Tema

#### Condições de Negócio:
- Condição 1: O tema é uma string
- Condição 2: O tema dever ser "escuro" ou "claro"

#### Classes de Equivalência
| Classes Válidas             | Classes Inválidas           |
|-----------------------------|-----------------------------|
| Possui `tema` | Não possui `tema` |
| `tema` válido | `tema` inválido |

#### Testes funcionais
| Tema | Resultado esperado | Resultado obitido | Situação | 
| :-----: | :-----: | :-----: | :-------------------: |
| "escuro" | Tema escuro aplicado | ---- | ---- |
| *vazio* | erro(tema não encontrado) | ---- | ---- |
| "vermelho" | erro(tema inválido) | ---- | ---- |