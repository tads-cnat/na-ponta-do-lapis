# CDU 34. Autenticar/Cadastrar com Google e Consentimento LGPD

- **Ator principal**: Visitante / Cliente.
- **Atores secundários**: Google Identity Services (GIS).
- **Resumo**: Permite que o usuário entre ou crie sua conta utilizando sua conta Google através do botão oficial do Google (`gis.renderButton`). Caso o e-mail já esteja cadastrado, o login ocorre imediatamente. Caso seja o **primeiro acesso** (conta nova), o sistema exibe um modal de **Consentimento LGPD** detalhando os dados do Google que serão utilizados (nome completo, e-mail verificado e foto de perfil) e exigindo o aceite dos Termos de Uso e Política de Privacidade antes de criar o registro no banco.
- **Pré-condição**: Variável `GOOGLE_CLIENT_ID` configurada no servidor (`GET /api/v1/auth/google/config` retornando `enabled: true`).
- **Pós-Condição**: Usuário autenticado com token JWT válido (e conta + `"Conta Principal"` criadas caso seja novo usuário).

## Fluxo Principal (Usuário já cadastrado)

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| 1 - Clica no botão **"Continuar com o Google"** na tela de Login ou Cadastro e seleciona sua conta Google (podendo escolher *"Usar outra conta"*). | |
| | 2 - O Google Identity Services retorna o `idToken` assinado para o frontend. |
| | 3 - O frontend envia `POST /api/v1/auth/google` com `{ idToken, termosAceitos: false }`. |
| | 4 - O backend valida o `idToken` junto ao Google (`tokeninfo`), localiza o usuário existente pelo e-mail e retorna `{ token, requerTermos: false }`. |
| | 5 - O frontend armazena o JWT e redireciona para `/app/dashboard`. |

## Fluxo Alternativo I - Primeiro Acesso / Conta Nova (Consentimento LGPD)

| Ações do ator | Ações do sistema |
| :-----------------: | :-----------------: |
| | 4.1 - O backend identifica que o e-mail ainda não possui conta e, como `termosAceitos` é `false`, **não cria o usuário**, retornando `{ requerTermos: true, nome, email, fotoUrl }`. |
| | 4.2 - O frontend exibe o modal **Confirmar criação de conta com Google** (`GoogleConsentDialogComponent`) mostrando nome, e-mail, foto e a lista de dados utilizados (LGPD). |
| 4.3 - O usuário marca *"Li e concordo com os Termos de Uso e a Política de Privacidade (LGPD)"* e clica em **"Confirmar e criar conta"**. | |
| | 4.4 - O frontend reenvia `POST /api/v1/auth/google` com `{ idToken, termosAceitos: true }`. |
| | 4.5 - O backend cria o usuário, cria a `"Conta Principal"` padrão, retorna o JWT e redireciona o usuário para `/app/dashboard`. |

