```mermaid
classDiagram
    direction LR

    class Papel {
        <<enumeration>>
        USUARIO
        ADMIN_FAMILIA
        ADMIN_SITE
    }

    class EstadoTransacao {
        <<enumeration>>
        PENDENTE
        REALIZADA
    }

    class TipoTransacao {
        <<enumeration>>
        RECEITA
        DESPESA
    }

    class TipoConta {
        <<enumeration>>
        CREDITO
        DEBITO
        CREDITO_DEBITO
    }

    class Usuario {
        -String nome
        -String email
        -String username
        -String senha
        -String fotoPerfil
        -Papel papel
        -Familia familia
        +getEmail() String
        +getNome() String
    }

    class Familia {
        -String nome
        -String fotoFamilia
        +getNome() String
    }

    class ContaFinanceira {
        -String nome
        -BigDecimal saldo
        -TipoConta tipo
        -Usuario usuario
        +getSaldo() BigDecimal
    }

    class Transacao {
        -String descricao
        -BigDecimal valor
        -LocalDateTime dataHora
        -EstadoTransacao estado
        -TipoTransacao tipo
        -TipoCategoria categoria
        -ContaFinanceira contaFinanceira
        +getValor() BigDecimal
    }

    class TipoCategoria {
        -String nome
        +getNome() String
    }

    class TipoMeta {
        -String nome
        +getNome() String
    }

    class Meta {
        -String nome
        -String descricao
        -BigDecimal valor
        -String fotoUrl
        -LocalDate dataInicio
        -LocalDate dataLimite
        -LocalDate dataEncerramento
        -TipoMeta tipoMeta
        +getValor() BigDecimal
    }

    class Marcador {
    }

    class Post {
        -String autor
        -String titulo
        -String conteudo
        -String imagem
        -LocalDate data
        -Usuario usuario
        +getTitulo() String
    }

    %% Relacionamentos de Entidades
    Familia "1" -- "*" Usuario : possui
    Usuario "1" -- "*" ContaFinanceira : gerencia
    Usuario "1" -- "*" Post : publica
    ContaFinanceira "1" -- "*" Transacao : registra
    Meta "*" -- "1" TipoMeta : define
    Transacao "*" -- "1" TipoCategoria : classificada
    
    %% Relacionamentos com Enums (Padronização)
    Usuario "*" -- "1" Papel : autoridade
    ContaFinanceira "*" -- "1" TipoConta : modalidade
    Transacao "*" -- "1" EstadoTransacao : status
    Transacao "*" -- "1" TipoTransacao : natureza
