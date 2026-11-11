export type CategoriaConquista = 'METAS' | 'ECONOMIA' | 'DISCIPLINA' | 'FAMILIA';

export interface ConquistaItem {
  id: string;
  titulo: string;
  descricao: string;
  icone: string;
  pontosXp: number;
  categoria: CategoriaConquista;
  desbloqueada: boolean;
  dataDesbloqueio?: string | null;
}

export interface PerfilGamificado {
  totalXp: number;
  nivel: number;
  tituloNivel: string;
  xpAtualNoNivel: number;
  xpNecessarioParaProximoNivel: number;
  progressoNivel: number;
  totalMetasConcluidas: number;
  totalConquistasDesbloqueadas: number;
  totalConquistasExistentes: number;
  conquistas: ConquistaItem[];
}

