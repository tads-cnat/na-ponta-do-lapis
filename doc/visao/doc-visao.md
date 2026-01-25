# Documento de Visão

## Histórico de Revisões

| Data       | Versão | Descrição        | Autores                                  |
| ---------- | ------ | ---------------- | ---------------------------------------- |
| 24/09/2025 | 0.1    | Versão inicial   | Bruno, Eduardo, Lucas, Pedro, Wagner     |

---

## 1. Visão Geral do Sistema Proposto

Criar um ambiente acolhedor para jovens que estão iniciando a vida adulta, possibilitando uma melhor gestão de gastos e o entendimento de como desenvolver uma boa saúde financeira, com foco no crescimento profissional e pessoal.

---

## 2. Descrição do Problema

|            | Descrição                                                     |
| ---------- | ------------------------------------------------------------- |
| **Problema** | Dificuldade em organizar os gastos pessoais                  |
| **Afeta**    | Pessoas sem uma administração financeira adequada           |
| **Impacta**  | Liberdade financeira                                        |
| **Solução**  | Criar uma ferramenta que auxilie no controle de despesas    |

---

## 3. Descrição dos Usuários

| Usuário        | Descrição                                   | Responsabilidades                                  |
| -------------- | ------------------------------------------- | -------------------------------------------------- |
| Estudantes     | Jovens no início da carreira                | Planejar e controlar seus gastos pessoais          |
| Trabalhadores  | Trabalhadores em geral                      | Planejar finanças pessoais ou familiares           |
| Administrador  | Usuário responsável pela gestão do sistema | Criar posts e atualizar dados estáticos do sistema |

---

## 4. Descrição do Ambiente dos Usuários

Os usuários utilizarão o sistema por meio de um navegador web, sendo acessível tanto em desktops (independente do sistema operacional) quanto em dispositivos móveis.

---

## 5. Principais Necessidades dos Usuários

1. **Estudantes**
   - Controlar suas primeiras rendas (mesadas, bolsas, estágios).
   - Entender conceitos básicos de finanças pessoais.
   - Criar hábitos financeiros saudáveis desde cedo.

2. **Trabalhadores**
   - Controlar despesas mensais (aluguel, transporte, alimentação etc.).
   - Estabelecer metas de economia ou investimento.
   - Integrar finanças pessoais e familiares.

3. **Administrador**
   - Publicar conteúdos sobre educação financeira.
   - Atualizar dados estáticos do sistema, como cotação de moedas.

---

## 6. Alternativas Concorrentes

1. **Minhas Finanças**
   - Interface agradável;
   - Paleta de cores harmônica;
   - Gráficos de entradas, despesas e saldo.

2. **Excel**
   - Flexibilidade de recursos;
   - Organização e personalização de planilhas;
   - Uso de fórmulas e gráficos.

3. **Minhas Economias**
   - Cursos em vídeo sobre educação financeira;
   - Integração com diversos bancos.

4. **Mobills**
   - Dashboard personalizável;
   - Interface minimalista;
   - Planejamento financeiro mensal.

---

## 7. Regras de Negócio

| ID   | Regra            | Descrição                                |
| ---- | ---------------- | ---------------------------------------- |
| RN01 | Cadastro de usuário | Usuários devem possuir cadastro válido |
| RN02 | Controle de gastos  | Todo gasto deve estar vinculado a um usuário |

---

## 8. Requisitos Funcionais

| Código | Nome                              | Descrição                                                                 | Prioridade |
| ------ | --------------------------------- | -------------------------------------------------------------------------- | ---------- |
| RF01   | Autenticação de Usuário           | Permitir cadastro, login, edição de dados e logout                         | Alta       |
| RF02   | Gerenciamento de Gastos           | Registrar, editar, excluir e visualizar gastos                             | Alta       |
| RF03   | Visualização de Relatórios        | Exibir gráficos e relatórios de evolução financeira                        | Alta       |
| RF04   | Sistema de Metas                  | Definir metas financeiras personalizadas                                   | Baixa      |
| RF05   | Gerenciamento de Contas           | Gerenciar contas bancárias vinculadas                                      | Média      |
| RF06   | Categorização de Gastos           | Criar e gerenciar categorias de gastos                                     | Média      |
| RF07   | Grupo Familiar                    | Criar e gerenciar grupos familiares e seus gastos                          | Baixa      |
| RF08   | Tema Visual                       | Alternar entre tema claro e escuro                                         | Baixa      |
| RF09   | Gerenciamento de Posts            | Criar, editar e excluir posts educativos                                   | Baixa      |
| RF10   | Visualização de Cotação           | Exibir cotação de diferentes moedas                                        | Baixa      |

> **Prioridade:** Alta, Média ou Baixa

---

## 9. Requisitos Não Funcionais

| Código | Nome                      | Descrição                                                              | Categoria              | Classificação |
| ------ | ------------------------- | ---------------------------------------------------------------------- | ---------------------- | ------------- |
| NF01   | Ferramenta de Desenvolvimento | Utilização de Python e Django                                       | Restrição de Projeto   | Obrigatório   |
| NF02   | Banco de Dados             | Utilização de SQLite                                                  | Restrição de Projeto   | Obrigatório   |
| NF03   | Criptografia               | Senhas devem ser armazenadas de forma criptografada                   | Segurança              | Obrigatório   |
| NF04   | Responsividade             | Sistema deve possuir design responsivo                                | Usabilidade            | Desejável     |
| NF05   | Acessibilidade             | Funcionalidades básicas de acessibilidade                             | Usabilidade            | Desejável     |
| NF06   | Facilidade de Navegação    | Funcionalidades acessíveis com poucos cliques                         | Usabilidade            | Obrigatório   |

> **Categoria:** Usabilidade, Confiabilidade, Performance, Suportabilidade, Restrição de Projeto, Implementação, Interface e Requisito Físico — segundo a classificação **FURP+**.  
> **Classificação:** Desejável ou Obrigatório.

