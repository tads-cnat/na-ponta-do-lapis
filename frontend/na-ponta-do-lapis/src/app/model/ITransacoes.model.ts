import { Marcador } from "./IMarcador.models";


export type Tipo = "RECEITA" | "DESPESA";
export type Estado = "PENDENTE" | "REALIZADA";

export interface Categoria {
  id: number;
  nome: string;
}

export interface Conta {
  id: number;
  nome: string;
}

export interface ITransacoes {
  id: number | null;
  descricao: string;
  valor: number;
  tipo: Tipo | null;
  estado: Estado | null;
  dataHora: string;
  categoria: Categoria | null;
  conta: Conta | null;
  marcador: Marcador | null
}

export interface ITransacaoRequest {
    descricao: string;
    valor: number;
    tipo:string;
    estado:string;
    idCategoria:number;
    idContaFinanceira:number;
    dataHora:string
    marcador: Marcador | null
}


