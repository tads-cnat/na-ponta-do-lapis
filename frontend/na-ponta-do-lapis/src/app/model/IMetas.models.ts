export type TipoMeta = 'POUPANCA' | 'GASTO' | 'DIVIDA';
export type StatusMeta = 'EM_ANDAMENTO' | 'CONCLUIDA' | 'ESTOURADA' | 'CANCELADA';

export interface TipoMetaResponse {
    id: string;
    nome: string;
}

export interface MetaRequest {
    nome: string;
    descricao?: string;
    valor: number;
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

