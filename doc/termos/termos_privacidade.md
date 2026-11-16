# Termos de Uso e Política de Privacidade
**Plataforma:** Na Ponta do Lápis  
**Versão:** 2.0 | **Data:** 16 de novembro de 2026

Bem-vindo ao **Na Ponta do Lápis**. Estes documentos estabelecem as regras para o uso da nossa plataforma de gestão financeira pessoal e familiar, em conformidade com a **Lei Geral de Proteção de Dados (LGPD — Lei nº 13.709/2018)**. Ao criar uma conta ou acessar o sistema, você declara estar ciente e de acordo com as condições descritas abaixo.

---

## 1. Termos de Uso

### 1.1. Propósito da Plataforma
O **Na Ponta do Lápis** é um software de organização financeira pessoal e familiar desenvolvido para auxiliar usuários no controle de receitas, despesas, cartões, faturas e metas financeiras. O sistema oferece ferramentas de visualização, relatórios e educação financeira prática.

### 1.2. Responsabilidade do Usuário
Você é responsável pela exatidão dos dados inseridos e pela manutenção do sigilo de suas credenciais de acesso. O compartilhamento de senha individual é desencorajado para garantir a integridade dos seus relatórios e a segurança das suas informações.

### 1.3. Isenção de Responsabilidade Financeira
> **Aviso Importante:** A plataforma fornece dados analíticos baseados nas informações inseridas ou sincronizadas pelo usuário. **Não realizamos consultoria de investimentos, assessoria financeira ou gestão de ativos.** Todas as decisões de consumo ou investimento são de inteira responsabilidade do usuário.

### 1.4. Propriedade Intelectual
Toda a interface, logotipia, identidade visual e código-fonte do "Na Ponta do Lápis" são de propriedade dos seus desenvolvedores, sendo vedada a reprodução total ou parcial sem autorização prévia.

---

## 2. Política de Privacidade e LGPD

### 2.1. Dados Pessoais Coletados e Finalidade
Para o funcionamento pleno do sistema, tratamos apenas os dados necessários:
* **Dados Cadastrais:** Nome completo, nome de usuário, e-mail, telefone (opcional), moeda padrão e foto de perfil (opcional). O registro de aceite destes termos (versão e data/hora) é armazenado para auditoria e segurança jurídica.
* **Autenticação via Google (Opcional):** Caso opte por entrar com sua conta Google (*Google Identity Services*), recebemos apenas seu nome, e-mail verificado e URL da foto de perfil para criação ou vinculação da conta.
* **Dados Financeiros:** Contas bancárias/cartões cadastrados, transações, categorias, marcadores, metas e grupos familiares criados pelo usuário.
* **Open Finance (Meu Pluggy — Opcional):** Mediante conexão explícita do usuário em *Perfil → Integrações*, importamos saldos e movimentações bancárias das instituições autorizadas. O vínculo pode ser revogado a qualquer momento no mesmo painel.
* **Leitura Inteligente de Faturas (IA e OCR — Opcional):** PDFs enviados passam por extração e OCR local no próprio servidor (sem envio externo por padrão). O envio do conteúdo ou imagem da fatura para provedores externos de IA configurados pelo próprio usuário (Google Gemini, OpenAI, Anthropic Claude, DeepSeek ou Groq) ocorre **exclusivamente mediante consentimento explícito**. Chaves de API informadas temporariamente são protegidas com criptografia AES-GCM.

### 2.2. Operadores e Infraestrutura
Utilizamos provedores técnicos estritamente para operação do serviço:
* **Banco de Dados e Cache:** PostgreSQL (armazenamento principal e backups diários com retenção rotativa de 7 dias) e Redis (cache temporário de curta duração para autenticação e categorias).
* **Armazenamento de Imagens:** Cloudinary (hospedagem opcional de foto de perfil e de família).

Suas informações são privadas e **jamais serão vendidas ou compartilhadas com terceiros para fins de marketing ou publicidade**.

### 2.3. Cookies e Armazenamento Local
Utilizamos apenas mecanismos estritamente necessários para o funcionamento da plataforma (sem rastreadores publicitários de terceiros):
* `localStorage`: token de sessão JWT, dados básicos de sessão, preferência de tema (claro/escuro) e moeda padrão.
* **Cookie de sessão criptografado (AES-GCM):** utilizado apenas caso o usuário informe uma chave temporária para leitura de faturas com IA, podendo ser encerrado a qualquer momento.

### 2.4. Direitos do Titular (Art. 18 da LGPD)
Você pode exercer seus direitos a qualquer momento:
* **Acesso e Retificação:** visualize e edite seus dados cadastrais diretamente em *Perfil → Ajustes*.
* **Revogação de Consentimento:** desconecte contas Open Finance ou remova chaves de IA em *Perfil → Integrações*.
* **Eliminação ("Direito ao Esquecimento"):** exclua definitivamente sua conta e todos os dados financeiros associados em *Perfil → Ajustes → Privacidade e Dados (LGPD) → Excluir conta*. A remoção na base ativa é imediata; eventuais cópias em backups rotativos expiram automaticamente em até 7 dias.

### 2.5. Encarregado pelo Tratamento de Dados (DPO — Art. 41 da LGPD)
Para dúvidas, solicitações ou exercício de direitos relacionados à privacidade, entre em contato pelo canal oficial: **privacidade@napontadolapis.app**.

---

## 3. Disposições Finais
Reservamo-nos o direito de atualizar estes termos para refletir melhorias técnicas ou legais. Em caso de alterações relevantes, solicitaremos ciência da nova versão.

---
**Na Ponta do Lápis © 2026** — *Desenvolvido para simplificar sua vida financeira.* Natal, RN - Brasil
