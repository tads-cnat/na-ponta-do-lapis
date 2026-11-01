# Relatório de Cobertura do Projeto - Na Ponta do Lápis

Este documento apresenta a análise de cobertura do sistema **Na Ponta do Lápis** (Spring Boot), dividida entre a **Cobertura de Requisitos** (baseada nos Casos de Uso - CDUs) e a **Cobertura de Código** (medida via JaCoCo).

---


## 1. Cobertura de Código (JaCoCo - Spring Boot)

Análise gerada automaticamente através do **JaCoCo Maven Plugin** no ambiente do projeto.

### Resumo Executivo das Métricas Gerais

* **Instruções (Instructions):** **43%** de cobertura (4.323 de 7.604 instruções cobertas).
* **Ramos/Desvios (Branches):** **9%** de cobertura (35 de 354 desvios testados).
* **Linhas de Código (Lines):** **39,7%** de cobertura (964 de 1.599 linhas cobertas).
* **Métodos:** **49,1%** de cobertura (237 de 483 métodos testados).
* **Classes:** **75,5%** de cobertura (80 de 106 classes com alguma execução).

### Análise Detalhada por Pacote Principal

| Pacote Java | Cobertura de Instruções (%) | Cobertura de Branches (%) | Linhas Não Cobertas | Status / Diagnóstico |
| :--- | :---: | :---: | :---: | :--- |
| `com.npl.na_ponta_do_lapis.service` | **5%** | **0%** | 415 / 461 | 🔴 **Crítico** (Regras de negócio sem testes) |
| `com.npl.na_ponta_do_lapis.web.controller` | **8%** | **0%** | 187 / 226 | 🔴 **Crítico** (Endpoints REST não integrados) |
| `com.npl.na_ponta_do_lapis.web.dto` | **67%** | **77%** | 51 / 153 | 🟡 **Médio** (Estruturas de transporte) |
| `com.npl.na_ponta_do_lapis.entity` | **84%** | **83%** | 31 / 215 | 🟢 **Bom** (Entidades de domínio) |
| `com.npl.na_ponta_do_lapis.config` | **95%** | **5%** | 21 / 208 | 🟢 **Bom** (Configurações Spring) |
| `com.npl.na_ponta_do_lapis.entity.enums` | **97%** | **N/A** | 1 / 28 | 🟢 **Bom** (Enumerações) |

---


### Imagens do Relatório HTML Gerado pelo JaCoCo

### Cobertura Geral do Projeto
![Relatório JaCoCo](geral.png)
### Cobertura dos Services
![Relatório JaCoCo](services.png)
### Cobertura dos Controllers
![Relatório JaCoCo](controller.png)


## 2. Comandos para Reprodução Local

Para gerar este relatório novamente na sua máquina local, execute:

```bash
# Limpa compilações anteriores, roda os testes e gera o relatório JaCoCo
mvn clean test jacoco:report
```

O relatório HTML detalhado estará disponível na pasta:
`target/site/jacoco/index.html`

---

## 3. Links e Referências no GitHub

* **Relatório JaCoCo (HTML):** [Visualizar target/site/jacoco](https://github.com/seu-usuario/na-ponta-do-lapis/tree/main/target/site/jacoco)