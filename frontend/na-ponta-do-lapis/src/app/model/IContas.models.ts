export type Moeda = "BRL" | "USD" | "EUR";
export type TipoConta = "CONTA_CORRENTE" | "CARTAO_CREDITO" | "POUPANCA" | "INVESTIMENTO" | "OUTROS";

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
    usuario: Usuario,
    sincronizada?: boolean,
    pluggyAccountId?: string | null,
    tipoConta?: TipoConta,
    limiteTotal?: number | null,
    limiteDisponivel?: number | null,
    valorFaturaFechada?: number | null,
    valorFaturaAberta?: number | null,
    dataVencimentoFatura?: string | null
}

export function ehCartaoCredito(conta?: IContas | null): boolean {
  if (!conta) return false;
  return conta.tipoConta === 'CARTAO_CREDITO';
}

export interface IContasRequest {
    nome: string;
    saldo: number;
    cor: string;
    moeda: Moeda;
    tipoConta?: TipoConta;
    limiteTotal?: number | null;
}
