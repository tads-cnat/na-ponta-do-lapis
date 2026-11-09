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
<<<<<<< HEAD
<<<<<<< HEAD
  return conta.tipoConta === 'CARTAO_CREDITO';
=======
  if (conta.tipoConta === 'CARTAO_CREDITO') return true;
  const n = (conta.nome || '').toLowerCase();
  return n.includes('cartão') || n.includes('cartao') || n.includes('credit') || n.includes('card');
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
  return conta.tipoConta === 'CARTAO_CREDITO';
>>>>>>> f17e760 (feat(contas): melhora layout, diferenciacao de cartao/conta e cores de bancos)
}

export interface IContasRequest {
    nome: string;
    saldo: number;
    cor: string;
    moeda: Moeda;
    tipoConta?: TipoConta;
    limiteTotal?: number | null;
}
