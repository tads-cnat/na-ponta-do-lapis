export enum TipoMeta{
    POUPANCA,
    GASTO,
    DIVIDA,
    PRAZO_FIXO
}

export interface Meta{
    "id"?: number;
    "nome": string;
    "descricao": string;
    "valorTotal": number;
    "valorAtual": number;
    "fotoUrl": string;
    "dataLimite": Date;
    "tipoMeta": TipoMeta;
    "usuarioId": number;
    "contaId": number;
    "progresso": number;
}

