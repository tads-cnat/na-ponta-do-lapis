export type TipoMeta = 'POUPANCA' | 'GASTO' | 'DIVIDA';
<<<<<<< HEAD
<<<<<<< HEAD
export type StatusMeta = 'EM_ANDAMENTO' | 'CONCLUIDA' | 'ESTOURADA' | 'EXPIRADA' | 'CANCELADA';
=======
export type StatusMeta = 'EM_ANDAMENTO' | 'CONCLUIDA' | 'ESTOURADA' | 'CANCELADA';
>>>>>>> 5fa35d6 (feat(metas): consolida 3 tipos essenciais, limpa tipo legado e implementa notificacoes e aportes)
=======
export type StatusMeta = 'EM_ANDAMENTO' | 'CONCLUIDA' | 'ESTOURADA' | 'EXPIRADA' | 'CANCELADA';
>>>>>>> f1d5028 (feat(metas): implementa tratamento de metas expiradas, fechamento de prazo e ritmo sugerido)

export interface TipoMetaResponse {
    id: string;
    nome: string;
}

export interface MetaRequest {
    nome: string;
    descricao?: string;
    valor: number;
    valorInicial?: number;
    fotoUrl?: string;
    dataLimite?: string | Date;
    tipoMeta: TipoMeta;
    contaId?: number;
    categoriaId?: number;
    familiar?: boolean;
}

export interface MetaResponse {
    id: number;
    nome: string;
    descricao?: string;
    valorTotal: number;
    valorAtual: number;
    fotoUrl?: string;
    dataLimite?: string;
    tipoMeta: TipoMeta;
    status: StatusMeta;
    usuarioId: number;
    usuarioNome?: string;
    contaId?: number;
    contaNome?: string;
    categoriaId?: number;
    categoriaNome?: string;
    familiaId?: number;
    familiar: boolean;
    progresso: number;
}

export interface AporteMetaRequest {
    valor: number;
    contaOrigemId?: number;
    observacao?: string;
}

export interface AporteMetaResponse {
    id: number;
    metaId: number;
    usuarioId: number;
    usuarioNome: string;
    contaOrigemId?: number;
    contaOrigemNome?: string;
    valor: number;
    observacao?: string;
    dataAporte: string;
}

