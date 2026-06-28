export enum TipoMeta{
    POUPANCA,
    GASTO,
    DIVIDA,
    PRAZO_FIXO
}

export interface TipoMetaResponse{
    id: string;
    nome: string;
}

export interface MetaRequest{
    nome: string;
    descricao?: string;
    valor: number;
    fotoUrl?: string;
    dataLimite?: Date;
    tipoMeta: TipoMeta;
    contaId?: number;
}

export interface MetaResponse{
    id?: number;
    nome: string;
    descricao?: string;
    valorTotal: number;
    valorAtual?: number;
    fotoUrl?: string;
    dataLimite?: Date;
    tipoMeta: TipoMeta;
    usuarioId: number;
    contaId?: number;
    progresso?: number;
}

