export type ProvedorIa = 'GEMINI' | 'CLAUDE' | 'OPENAI' | 'DEEPSEEK' | 'GROQ';

export interface IChaveIaResponse {
  id: number;
  apelido: string;
  provedor: ProvedorIa;
  chaveMascarada: string;
  padrao: boolean;
  dataCriacao: string;
}

export interface IChaveIaRequest {
  apelido: string;
  provedor: ProvedorIa;
  apiKey: string;
  padrao: boolean;
}

export interface IOpenFinanceStatus {
  conectado: boolean;
  ultimaSincronizacao: string | null;
  syncsRestantesHoje: number;
  sincronizacoesRestantesHoje?: number;
  limiteDiario: number;
  clientIdMascarado?: string | null;
  totalContasSincronizadas?: number;
}

export interface IOpenFinanceConectarRequest {
  clientId: string;
  clientSecret: string;
  itemIds?: string;
}

export interface IOpenFinanceItem {
  itemId: string;
  nomeInstituicao: string;
  logoUrl?: string;
  status: string;
  ultimaAtualizacao?: string;
}

export interface IConnectTokenResponse {
  connectToken: string;
}

