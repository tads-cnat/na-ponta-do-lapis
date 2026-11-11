export type TipoNotificacao =
  | 'META_ALERTA_50'
  | 'META_ALERTA_80'
  | 'META_ATINGIDA'
  | 'META_ESTOURADA'
  | 'CONQUISTA_DESBLOQUEADA'
  | 'FAMILIA_CONVITE'
  | 'SISTEMA';

export interface Notificacao {
  id: number;
  titulo: string;
  mensagem: string;
  tipo: TipoNotificacao;
  lida: boolean;
  linkRedirecionamento?: string;
  dataCriacao: string;
}

export interface ContadorNaoLidasResponse {
  totalNaoLidas: number;
}

