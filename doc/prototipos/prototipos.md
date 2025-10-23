# Protótipos de Interface com o Usuário

## Histórico de Revisões

| Data | Versão | Descrição | Autores |
| :--: | :----: | :-------: | :-----: |
| 09/10/2025 | 0.1 | Versão inicial | Bruno |
| - | - | - |  - |

## 1. Mapa do Site
>Versão de prototipo
```mermaid
---
config:
  theme: mc
  look: neo
---
flowchart TD
    A["Index"] -- <br> --> B("Entrar")
    A --> n1["Cadastro"] & n2["Sobre"] & n4["Educação Financeira"] & n5["Investimento"]
    n1 --> B
    B --> n7["Dashboard"]
    n7 --> n8["Metas"] & n10["Contas"] & n9["Relatorio"] & n6["Transações"] & n11["Ajustes"] & n12["Tags/Categorias"] & n13["Grupo Familiar"]
    n4 --> n14["Posts"] & n15["Consultor IA"]
     A:::Ash
     B:::Ash
     n1:::Ash
     n2:::Ash
     n4:::Ash
     n5:::Ash
     n7:::Aqua
     n8:::Aqua
     n10:::Aqua
     n9:::Aqua
     n6:::Aqua
     n11:::Aqua
     n12:::Aqua
     n13:::Aqua
     n14:::Ash
     n15:::Ash
    classDef Aqua stroke-width:1px, stroke-dasharray:none, stroke:#46EDC8, fill:#DEFFF8, color:#378E7A
    classDef Ash stroke-width:1px, stroke-dasharray:none, stroke:#999999, fill:#EEEEEE, color:#000000
    style A stroke:#757575
    style B stroke:#757575
    style n1 stroke:#757575
    style n2 stroke:#757575
    style n7 stroke:#00C853

```

### Protótipo de baixa fidelidade
[Link para vizualização no Figma](https://www.figma.com/design/hbbNIiCbHjSmDtWXFRDvgs/Na-Ponta-do-Lapis?node-id=87-4&t=MZDT6lo0g5vAUEoe-1)
### A. Tela 1: Index
> Obs. Substituir pela captura das imagens, sejam elas provenientes do Figma (anexar também o link para o Figma) ou já em HTML e CSS...

### B. Tela 2: Login

> Obs. Substituir pela captura das imagens, sejam elas provenientes do Figma (anexar também o link para o Figma) ou já em HTML e CSS...
