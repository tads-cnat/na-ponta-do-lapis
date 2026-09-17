# Relatório de Avaliação Heurística

**Metodologia:** Lista Eureca — Diretrizes de Design para Criação e Avaliação de Interfaces (Matos, Freire, Duarte, Vieira, 2024)

---

## Totalização por Tipo

| Tipo | Categoria | Qtd. |
| ---- | --------- | ---- |
| AF1 | AF | 14 |
| AF2 | AF | 1 |
| AF4 | AF | 2 |
| AF6 | AF | 1 |
| AF7 | AF | 1 |
| AF9 | AF | 5 |
| AF11 | AF | 1 |
| CO3 | CO | 4 |
| CO5 | CO | 1 |
| CO6 | CO | 2 |
| CO7 | CO | 1 |
| CO8 | CO | 1 |
| CO9 | CO | 1 |
| FM1 | FM | 1 |
| FM2 | FM | 1 |
| FM3 | FM | 1 |
| FM4 | FM | 1 |
| FM5 | FM | 3 |
| FM6 | FM | 4 |
| FM9 | FM | 2 |
| FM11 | FM | 2 |
| FM16 | FM | 1 |
| NA1 | NA | 2 |
| NA4 | NA | 1 |
| PU2 | PU | 1 |
| PU3 | PU | 1 |
| PU4 | PU | 2 |
| PU5 | PU | 1 |
| PD2 | PD | 1 |
| PD3 | PD | 1 |
| PD4 | PD | 1 |
| PD5 | PD | 1 |
| PD6 | PD | 1 |
| AC3 | AC | 1 |
| AC4 | AC | 2 |
| AC5 | AC | 1 |
| AC6 | AC | 1 |
| AC7 | AC | 1 |
| AC8 | AC | 1 |
| AC9 | AC | 1 |
| AC10 | AC | 1 |
| AC11 | AC | 1 |
| AC12 | AC | 1 |
| AC13 | AC | 1 |
| AC14 | AC | 1 |
| AC15 | AC | 1 |
| AC16 | AC | 1 |
| PS1 | PS | 1 |
| PS3 | PS | 1 |

---

## Totalização por Categoria

| Categoria | Nome | Qtd. de Ocorrências |
| --------- | ---- | ------------------- |
| AF | Aspectos Funcionais | 25 |
| CO | Comunicação | 10 |
| FM | Formatação | 16 |
| NA | Navegação | 3 |
| PU | Particularidades do Usuário | 5 |
| PD | Particularidades do Dispositivo | 5 |
| AC | Acessibilidade | 15 |
| PS | Privacidade e Segurança de Dados (LGPD) | 2 |
| **Total geral** | — | **81** |

---

## Tabela de Problemas Identificados

| N | Tipo | Problema | Solução Proposta | Gravidade | Esforço | Status | Tela |
| - | ---- | -------- | ---------------- | --------- | ------- | ------ | ---- |
| 1 | AF1 | Botão de logar com o Google não funciona | Habilitar ou ocultar a função | Média | Moderado | Não iniciado | Login |
| 2 | PU4 | Botão de logar com o Google não funciona | Implementar essa função | Média | Moderado | Não iniciado | Login |
| 3 | AF1 | Botão de 'esqueceu a senha' não funciona | Implementar recuperação de senha | Alta | Moderado | Não iniciado | Login |
| 4 | AF1 | Campo de Email tem case sensitive | Remover o case sensitive do campo Email | Média | Leve | Não iniciado | Login |
| 5 | AF1 | Botão de filtrar transação não funciona | Habilitar ou ocultar a função | Média | Leve | Não iniciado | Transações |
| 6 | AF1 | Botão de Ordenar não funciona | Implementar a funcionalidade | Baixa | Leve | Não iniciado | Transações |
| 7 | FM4 | Botão de Ordenar não funciona | Implementar a funcionalidade | Baixa | Leve | Não iniciado | Transações |
| 8 | AF1 | Filtro por marcador não implementado | Implementar a funcionalidade | Baixa | Leve | Não iniciado | Transações |
| 9 | FM3 | Filtro por marcador não implementado | Implementar a funcionalidade | Baixa | Leve | Não iniciado | Transações |
| 10 | AF1 | Exclusão em massa ainda não implementada | Botão de exclusão ao usar checkboxes | Baixa | Leve | Não iniciado | Transações |
| 11 | AF9 | Sem confirmação ao adicionar transação manualmente | Pop-up de confirmação | Baixa | Leve | Não iniciado | Transações |
| 12 | AF9 | Sem confirmação ao adicionar marcador | Pop-up de confirmação | Baixa | Leve | Não iniciado | Transações |
| 13 | AF9 | Sem confirmação ao editar transação | Pop-up de confirmação | Média | Leve | Não iniciado | Transações |
| 14 | AF9 | Sem confirmação ao excluir transação | Pop-up de confirmação | Alta | Leve | Não iniciado | Transações |
| 15 | AF9 | Sem confirmação ao adicionar transação via upload | Pop-up de confirmação | Alta | Leve | Não iniciado | Transações |
| 16 | AF4 | Sem recuperação após excluir meta/transação | Pop-up de confirmação ou lista de últimas edições | Alta | Leve | Não iniciado | Transações e metas |
| 17 | FM5 | Botão de exclusão como primeiro item, não o último | Colocar botão mais à direita | Média | Leve | Não iniciado | Transações/Metas |
| 18 | PD4 | Tabela com muitas informações, principalmente mobile | Adicionar botão de detalhar | Média | Moderado | Não iniciado | Transações |
| 19 | FM6 | Botão de adicionar (modal membro) fora do padrão do site | Mudar a cor para verde | Baixa | Leve | Não iniciado | Família |
| 20 | CO3 | Campo de nome da família sem input estilizado/indicação clara | Incluir campo de texto com label e placeholder | Média | Leve | Não iniciado | Família |
| 21 | CO7 | Campo de nome da família sem input estilizado/indicação clara | Incluir campo de texto com label e placeholder | Média | Leve | Não iniciado | Família |
| 22 | AF1 | Convite da família ainda não implementado | Implementar junto ao sistema de notificação | Média | Grande | Não iniciado | Família |
| 23 | CO6 | Termo 'Dashboard' não condiz com o contexto | Substituir por termo em português | Baixa | Leve | Não iniciado | Dashboard |
| 24 | AF1 | Cotação parou de funcionar | Corrigir solução atual ou usar cache diário | Média | Moderado | Não iniciado | Dashboard |
| 25 | AC4 | Gráfico 'Gastos por categoria' com baixo contraste | Ajustar paleta de cores; ocultar/agrupar valores 0,0% | Média | Leve | Não iniciado | Dashboard |
| 26 | FM11 | Gráfico 'Gastos por categoria' com baixo contraste | Ajustar paleta de cores; ocultar/agrupar valores 0,0% | Média | Leve | Não iniciado | Dashboard |
| 27 | FM6 | Botão de salvar conta com tom de verde diferente | Ajustar tom de verde | Baixa | Leve | Não iniciado | Contas |
| 28 | FM9 | Componentes da página desalinhados | Alinhar componentes | Baixa | Leve | Não iniciado | Contas |
| 29 | CO3 | Carrossel 'Suas Contas' sem indicação clara de seleção/quantidade | Indicador numérico e destaque do cartão ativo | Baixa | Leve | Não iniciado | Contas |
| 30 | PU4 | Carrossel 'Suas Contas' sem indicação clara de seleção/quantidade | Indicador numérico e destaque do cartão ativo | Baixa | Leve | Não iniciado | Contas |
| 31 | AF1 | Lógica do cálculo de metas ainda inconclusa | Implementar a lógica | Alta | Leve | Em desenvolvimento | Metas |
| 32 | FM6 | Botão de salvar mudanças diferente dos demais | Ajustar cor para verde | Baixa | Leve | Não iniciado | Perfil |
| 33 | AF1 | Mudar foto do perfil não implementado | Implementar a funcionalidade | Média | Moderado | Não iniciado | Perfil |
| 34 | AF1 | Recuperar senha não implementado (Perfil) | Implementar a funcionalidade | Alta | Grande | Não iniciado | Perfil |
| 35 | CO3 | Sem retorno visual de sucesso na mudança de senha | Pop-up de confirmação | Alta | Leve | Não iniciado | Perfil |
| 36 | FM2 | Campos de alteração de senha com baixo contraste | Aumentar contraste dos inputs | Baixa | Leve | Não iniciado | Perfil |
| 37 | FM11 | Campos de alteração de senha com baixo contraste | Aumentar contraste dos inputs | Baixa | Leve | Não iniciado | Perfil |
| 38 | AF1 | Sininho de notificação não implementado | Implementar a funcionalidade | Média | Grande | Não iniciado | Cabeçalho |
| 39 | FM6 | Cores do item ativo do menu lateral inconsistentes | Padronizar cor de item ativo | Baixa | Leve | Não iniciado | Geral (Menu Lateral) |
| 40 | AF1 | Clique na foto/nome não abre a área de perfil | Linkar elemento | Baixa | Leve | Não iniciado | Cabeçalho |
| 41 | AF2 | Poucas alternativas de acesso às abas do site | Barra de pesquisa no cabeçalho/lateral e hiperlinks | Baixa | Leve | Não iniciado | Todo o sistema |
| 42 | AF4 | Dados digitados não persistem ao sair de modais | Salvar parcialmente dados digitados | Baixa | Leve | Não iniciado | Formulários do site |
| 43 | AF6 | Sem sugestões de preenchimento em campos de busca | Autopreenchimento com base em pesquisas frequentes | Baixa | Moderado | Não iniciado | Campo de pesquisa |
| 44 | AF7 | Sem escrita inteligente conforme contexto digitado | Implementar em conformidade com PU3 | Baixa | Moderado | Não iniciado | Formulários do site |
| 45 | AF11 | Falta de recursos de busca para seções do site | Implementar buscas além do campo de texto | Baixa | Moderado | Não iniciado | Campo de pesquisa |
| 46 | CO3 | Mudança de senha não retorna mensagem de êxito | Mostrar mensagem/pop-up de sucesso | Alta | Leve | Não iniciado | Perfil |
| 47 | CO5 | Site não demonstra ser seguro | Textos que indiquem confiança em segurança | Alta | Leve | Não iniciado | Todo o sistema |
| 48 | CO6 | Poucas formas de enfatizar a própria marca | Aumentar menções ao nome/sigla | Baixa | Leve | Não iniciado | Todo o sistema |
| 49 | CO8 | Sistema não orienta como deve ser usado | Rodapé com seção de dúvidas/descrição de uso | Média | Leve | Não iniciado | Todo o sistema |
| 50 | CO9 | Nenhuma forma de feedback do site | Botão de ajuda/suporte/avaliação | Média | Leve | Não iniciado | Todo o sistema |
| 51 | FM1 | Campos de editar perfil esmaecidos, sem indicação de edição | Ícone de editar e maior contraste | Média | Leve | Não iniciado | Perfil |
| 52 | FM5 | Ícones da barra lateral fora do padrão de menus retráteis | Substituir por ícones mais atuais | Baixa | Leve | Não iniciado | Barra lateral |
| 53 | FM5 | Ação de excluir vem primeiro (esquerda→direita) | Colocar botão mais à direita | Média | Leve | Não iniciado | Transações |
| 54 | FM9 | 'Esqueceu sua senha?' não centralizado com botão salvar | Alinhar ao centro | Baixa | Leve | Não iniciado | Perfil |
| 55 | FM16 | Sem tela de carregamento em mobile | Implementar tela de carregamento com marca | Média | Leve | Não iniciado | Todo o sistema |
| 56 | NA1 | Ausência de guia condutor para novo usuário se cadastrar | Criar mecanismo que chame o usuário a se cadastrar | Média | Leve | Não iniciado | Todo o sistema |
| 57 | NA1 | Sistema não indica suas principais funcionalidades | Guia condutor na primeira vez que acessar cada parte | Média | Leve | Não iniciado | Todo o sistema |
| 58 | NA4 | Sem indicação do caminho percorrido (migalhas de pão) | Adicionar 'migalhas de pão' no topo do conteúdo | Baixa | Leve | Não iniciado | Todo o sistema |
| 59 | PU2 | Sem alternativa de temas claro e escuro | Botão que alterna entre temas | Média | Moderado | Não iniciado | Todo o sistema |
| 60 | PU3 | Sistema não pede autorização de uso de dados sensíveis | Pop-ups de notificação para aval do usuário | Alta | Leve | Não iniciado | Todo o sistema |
| 61 | PU5 | Sistema não sugere/pergunta preferências do usuário | Questionário de preferências de uso | Média | Moderado | Não iniciado | Todo o sistema |
| 62 | PD2 | Versão móvel não formatada para tela pequena | Projetar versão adequada para dispositivos móveis | Média | Moderado | Em desenvolvimento | Todo o sistema |
| 63 | PD3 | Sistema não responde a mudanças de proporção/tamanho | Refazer implementações visuais responsivas | Média | Leve | Em desenvolvimento | Todo o sistema |
| 64 | PD5 | Elementos pequenos demais no mobile | Ampliar elementos para facilitar interação | Alta | Leve | Não iniciado | Todo o sistema |
| 65 | PD6 | Sistema não se adapta à mudança de orientação de tela | Rever elementos visuais para mudança de orientação | Média | Moderado | Não iniciado | Todo o sistema |
| 66 | AC3 | Sistema não possui ícones de ajuda e campo de busca | Implementar elementos essenciais ausentes | Média | Leve | Não iniciado | Todo o sistema |
| 67 | AC4 | Sistema não possui modo de alto contraste | Implementar modo de alto contraste | Alta | Moderado | Não iniciado | Todo o sistema |
| 68 | AC5 | Sistema não possui modo de ampliação de tela | Implementar funcionalidade de lupa | Média | Leve | Não iniciado | Todo o sistema |
| 69 | AC6 | Sistema não destaca recursos de acessibilidade | Ícone fixo destacando acessibilidade | Alta | Leve | Não iniciado | Todo o sistema |
| 70 | AC7 | Sem contextualização de seção via áudio | Adicionar recursos de áudio e indicação visual de seção | Média | Moderado | Não iniciado | Todo o sistema |
| 71 | AC8 | Sistema não permite formatação de fontes | Implementar formatação de fontes | Média | Leve | Não iniciado | Todo o sistema |
| 72 | AC9 | Sistema não destaca adequadamente links clicáveis | Destacar melhor os links clicáveis | Média | Leve | Não iniciado | Todo o sistema |
| 73 | AC10 | Sistema não permite mudança de saturação de cores | Implementar formatação de saturação | Alta | Moderado | Não iniciado | Todo o sistema |
| 74 | AC11 | Sistema não permite ativar/desativar animações | Adicionar função no ícone de acessibilidade | Alta | Leve | Não iniciado | Todo o sistema |
| 75 | AC12 | Sistema não permite ativar/desativar sons | Adicionar função no ícone de acessibilidade | Baixa | Leve | Não iniciado | Todo o sistema |
| 76 | AC13 | Sistema não apresenta recurso de leitura (máscara) | Adicionar recurso no ícone de acessibilidade | Média | Moderado | Não iniciado | Todo o sistema |
| 77 | AC14 | Sistema não apresenta/destaca comunicação em Libras | Adicionar recurso no ícone de acessibilidade | Alta | Moderado | Não iniciado | Todo o sistema |
| 78 | AC15 | Sistema não centraliza mensagens de erro | Centralizar as mensagens | Média | Leve | Não iniciado | Todo o sistema |
| 79 | AC16 | Sistema é monolinguístico | Botão de mudança de idioma | Alta | Moderado | Não iniciado | Todo o sistema |
| 80 | PS1 | Sem configuração de exposição de dados sensíveis | Configuração de privacidade no perfil | Alta | Leve | Não iniciado | Perfil |
| 81 | PS3 | Sistema não informa medidas de segurança (LGPD) | Informar em rodapé fixo e ajustes de perfil | Alta | Moderado | Não iniciado | Todo o sistema |

---

## Síntese Geral

- **Total de problemas registrados no relatório:** 81
- **Categoria com mais ocorrências:** AF – Aspectos Funcionais (25 ocorrências)
- **Categoria com menos ocorrências:** PS – Privacidade e Segurança de Dados (2 ocorrências)
- **Tipo específico mais recorrente:** AF1 – Funcionalidade (14 ocorrências, 17,3% do total de ocorrências)

### Distribuição por Status

| Status | Qtd. |
| ------ | ---- |
| Não iniciado | 76 |
| Em desenvolvimento | 3 |

### Distribuição por Gravidade

| Gravidade | Qtd. |
| --------- | ---- |
| Alta | 19 |
| Média | 34 |
| Baixa | 26 |

### Distribuição por Esforço de Alteração

| Esforço | Qtd. |
| ------- | ---- |
| Grande | 3 |
| Moderado | 20 |
| Leve | 56 |
