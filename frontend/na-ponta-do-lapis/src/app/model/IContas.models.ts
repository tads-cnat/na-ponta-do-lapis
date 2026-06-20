export type Moeda = "BRL" | "USD" | "EUR"
export interface Usuario {
  id: number;
  nome: string;
}
export interface IContas {
    id: number | null,
    nome: string,
    saldo: number,
    cor: string,
    moeda: Moeda,
    usuario: Usuario
}

export interface IContasRequest {
    nome: string;
    saldo: number;
    cor: string;
    moeda: Moeda;
}
