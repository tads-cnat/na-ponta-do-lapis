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
  marcador: Marcador | null;
  grupoParcelamento: string | null;
  numeroParcela: number | null;
  totalParcelas: number | null;
  valorTotalCompra: number | null;
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

export interface ITransacaoFatura {
  descricao: string;
  valor: number;
  data: string | Date;
  categoria: string;
  numeroParcela?: number | null;
  totalParcelas?: number | null;
  valorTotalCompra?: number | null;
}

export type ProvedorIa = 'GEMINI' | 'CLAUDE' | 'OPENAI' | 'DEEPSEEK' | 'GROQ';

export type FonteFatura = 'REGEX' | 'IA' | 'NENHUMA';

export interface FaturaAnaliseResponse {
  transacoes: ITransacaoFatura[];
  fonte: FonteFatura;
  emissor: string | null;
  somaCalculada: number;
  valorTotalFatura: number | null;
  diferenca: number | null;
  alertaSoma: boolean;
  padraoVersao: number | null;
  pdfEscaneado: boolean;
  ocrUtilizado: boolean;
  consentimentoPdfIa: boolean;
  precisaConsentimentoPdf: boolean;
  mensagem: string | null;
}

export interface FaturaErro {
  error: string;
  codigo: string;
  provedores?: string[];
}

export interface FaturaAnaliseOpcoes {
  provedor?: ProvedorIa;
  apiKey?: string;
  manterNaSessao?: boolean;
  forceAiAnalysis?: boolean;
  enviarPdfParaIa?: boolean;
  consentimentoLgpd?: boolean;
}

export interface CompraParceladaRequest {
  descricao: string;
  valorTotal: number;
  totalParcelas: number;
  dataPrimeiraParcela: string;
  idContaFinanceira: number;
  idCategoria: number;
  marcadorId: number | null;
}
