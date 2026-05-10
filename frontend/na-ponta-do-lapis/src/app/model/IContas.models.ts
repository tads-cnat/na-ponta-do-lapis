export interface IContas {
    id: number | null,
    nome: string,
    saldo: number,
    cor: string,
    usuario: {
      id: number;
      nome: string;
    };
}

export interface IContasRequest {
    nome: string;
    saldo: number;
    cor: string;
}
