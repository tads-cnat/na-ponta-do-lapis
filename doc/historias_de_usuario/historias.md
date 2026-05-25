# Histórias de Usuário

## Épico: Gestão de Grupo Familiar

### História de Usuário 1 — Convidar membro para a família

Como usuário autenticado, quero convidar outro usuário para participar da minha família para que eu possa compartilhar e gerenciar informações em um grupo familiar.

**Critérios de Aceitação** 
Deve-se notificar o usuário do convite recebido.
Caso usuário já esteja em uma família deve-se retornar um erro ao convida-lo, caso o usuário não exista igualmente mostra-se um erro no ato do convite. 
Deve-se mostrar o(s) convite(s) recebido(s) pelo o usuário convidado e no caso de recusa uma notificação para o(s) administrador(es) da família.

### História de Usuário 2 — Criar família ao adicionar o primeiro membro

Como usuário autenticado sem família cadastrada, quero criar uma família antes de convidar membros para que eu possa iniciar meu grupo familiar.

**Critérios de Aceitação** 
Deve-se retornar uma mensagem de criação da família e mostrar os usuários que pertencem a mesma.

### História de Usuário 3 — Aceitar convite para família

Como usuário convidado, quero aceitar um convite para participar de uma família para que eu possa fazer parte do grupo familiar.

**Critérios de Aceitação** 
Deve-se retornar uma mensagem confirmando a ação, caso aceito o convite ou recusado.
Deve-se mostrar a família que o usuário se tornou integrante.
