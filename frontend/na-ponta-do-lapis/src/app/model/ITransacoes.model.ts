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
  valorComSinal?: number | null;
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
    parcelado?: boolean
    totalParcelas?: number | null
    numeroParcela?: number | null
    valorTotalCompra?: number | null
    criarParcelasAnteriores?: boolean
}

export interface ITransacaoFatura {
  descricao: string;
  valor: number;
  data: string | Date;
  categoria: string;
  tipo?: Tipo;
  numeroParcela?: number | null;
  totalParcelas?: number | null;
  valorTotalCompra?: number | null;
  selecionada?: boolean;
  duplicata?: boolean;
  duplicataInfo?: string;
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
  precisaHabilitarIa: boolean;
  padraoNovoAprendido?: boolean;
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
  senha?: string;
  manterNaSessao?: boolean;
  usarIa?: boolean;
  enviarPdfParaIa?: boolean;
  consentimentoLgpd?: boolean;
  chaveIaId?: number;
  salvarChave?: boolean;
}

export interface FaturaValidacaoPreviaResponse {
  valido: boolean;
  protegidoPorSenha: boolean;
  emissor: string | null;
  codigo: string | null;
  mensagem: string | null;
}

export interface FaturaSessaoResponse {
  temChaveSalva: boolean;
  provedor: ProvedorIa | null;
}



