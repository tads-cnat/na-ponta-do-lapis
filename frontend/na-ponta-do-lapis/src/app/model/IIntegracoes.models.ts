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
<<<<<<< HEAD
<<<<<<< HEAD
  syncsRestantesHoje?: number;
  sincronizacoesRestantesHoje?: number;
  limiteDiario?: number;
  clientIdMascarado?: string | null;
  totalContasSincronizadas?: number;
  totalItensConectados?: number;
  limiteItensConexao?: number;
=======
  syncsRestantesHoje: number;
=======
  syncsRestantesHoje?: number;
>>>>>>> 2b2d120 (fix(open-finance): remove trava artificial de 5 syncs diarios e reflete cota de 5 bancos da pluggy)
  sincronizacoesRestantesHoje?: number;
  limiteDiario?: number;
  clientIdMascarado?: string | null;
  totalContasSincronizadas?: number;
<<<<<<< HEAD
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
  totalItensConectados?: number;
  limiteItensConexao?: number;
>>>>>>> 2b2d120 (fix(open-finance): remove trava artificial de 5 syncs diarios e reflete cota de 5 bancos da pluggy)
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

