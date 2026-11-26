import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env';

export interface SerieDiariaDTO {
  data: string;
  total: number;
}

export interface ConsultorDislikeDTO {
  id: number;
  usuarioNome: string;
  usuarioEmail: string | null;
  provedor: string | null;
  perguntaResumo: string | null;
  respostaResumo: string | null;
  criadoEm: string | null;
}

export interface AdminDashboardResumo {
  totalUsuarios: number;
  usuariosAtivos: number;
  usuariosSuspensos: number;
  ativosVerificados: number;
  ativosNaoVerificados: number;
  novosUltimos7Dias: number;
  novosUltimos30Dias: number;
  emailsVerificados: number;
  usuariosAtivados: number;
  totalFamilias: number;
  usuariosEmFamilia: number;
  convitesPendentes: number;
  usuariosComIa: number;
  usuariosComOpenFinance: number;
  totalTransacoes: number;
  totalMetas: number;
  taxaAtivacao24h: number;
  usuariosAtivados24h: number;
  usuariosBaseAtivacao24h: number;
  indiceAutomacao: number;
  transacoesManuais: number;
  transacoesFaturaPdf: number;
  transacoesOpenFinance: number;
  fatorViralFamiliar: number;
  retencaoSegundoMes: number;
  usuariosRetidosM2: number;
  usuariosElegiveisM2: number;
  retencaoD7: number;
  usuariosRetidosD7: number;
  usuariosElegiveisD7: number;
  usuariosRiscoChurn: number;
  funilComConta: number;
  taxaConclusaoMetas: number;
  metasConcluidas: number;
  stickinessWauMau: number;
  wau: number;
  mau: number;
  periodoDias: number;
  novosNoPeriodo: number;
  novosPeriodoAnterior: number;
  serieCadastros: SerieDiariaDTO[];
  consultorLikes: number;
  consultorDislikes: number;
  consultorTaxaAprovacao: number;
  usuariosComAssinaturas: number;
  usuariosUsaramConsultor: number;
  ultimosDislikesConsultor: ConsultorDislikeDTO[];
}

export interface AdminUsuarioResumo {
  id: number;
  nome: string;
  email: string;
  username: string;
  papel: 'USUARIO' | 'ADMIN_SITE';
  ativo: boolean;
  emailVerificado: boolean;
  onboardingConcluido: boolean;
  moedaPadrao: string;
  familiaNome: string | null;
  criadoEm: string | null;
  ultimoAcessoEm: string | null;
}

export interface AdminUsuarioDetalhe {
  id: number;
  nome: string;
  email: string;
  username: string;
  telefone: string | null;
  fotoPerfil: string | null;
  papel: 'USUARIO' | 'ADMIN_SITE';
  ativo: boolean;
  suspensoEm: string | null;
  motivoSuspensao: string | null;
  emailVerificado: boolean;
  onboardingConcluido: boolean;
  moedaPadrao: string;
  versaoTermos: string | null;
  termosAceitosEm: string | null;
  criadoEm: string | null;
  ultimoAcessoEm: string | null;
  familiaId: number | null;
  familiaNome: string | null;
  totalContas: number;
  totalTransacoes: number;
  totalMetas: number;
  totalConquistas: number;
  possuiChaveIa: boolean;
  possuiOpenFinance: boolean;
}

export interface AdminFamiliaResumo {
  id: number;
  nome: string;
  totalMembros: number;
  adminFamiliaNome: string | null;
  adminFamiliaEmail: string | null;
  convitesPendentes: number;
}

export interface AdminCategoriaResumo {
  id: number;
  nome: string;
  totalTransacoes: number;
}

export type CategoriaConquista = 'METAS' | 'ECONOMIA' | 'DISCIPLINA' | 'FAMILIA';

export interface AdminConquistaResumo {
  id: string;
  titulo: string;
  descricao: string;
  icone: string;
  pontosXp: number;
  categoria: CategoriaConquista;
  totalDesbloqueios: number;
}

export interface AdminAuditLog {
  id: number;
  adminUsuarioId: number;
  adminEmail: string;
  acao: string;
  entidadeAlvo: string;
  alvoId: string | null;
  alvoDescricao: string | null;
  motivo: string | null;
  detalhesJson: string | null;
  ipOrigem: string | null;
  criadoEm: string;
}

export interface AdminPageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export function extrairMensagemErroAdmin(err: unknown, fallback: string): string {
  const e = err as { error?: unknown; message?: string } | null | undefined;
  const body = e?.error;
  if (typeof body === 'string' && body.trim()) {
    try {
      const parsed = JSON.parse(body) as { message?: string };
      if (parsed?.message) return parsed.message;
    } catch {
      return body.trim();
    }
  }
  if (body && typeof body === 'object') {
    const obj = body as Record<string, unknown>;
    if (typeof obj['message'] === 'string' && obj['message'].trim()) {
      return obj['message'];
    }
    const valores = Object.values(obj).filter((v): v is string => typeof v === 'string' && v.trim().length > 0);
    if (valores.length > 0) {
      return valores.join(' • ');
    }
  }
  return fallback;
}

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/admin`;

  obterResumoDashboard(dias = 30): Observable<AdminDashboardResumo> {
    const params = new HttpParams().set('dias', String(dias));
    return this.http.get<AdminDashboardResumo>(`${this.baseUrl}/dashboard/resumo`, { params });
  }

  listarUsuarios(filtros: {
    q?: string;
    papel?: string;
    ativo?: string;
    emailVerificado?: string;
    onboardingConcluido?: string;
    riscoChurn?: boolean;
    page?: number;
    size?: number;
  }): Observable<AdminPageResponse<AdminUsuarioResumo>> {
    let params = new HttpParams()
      .set('page', String(filtros.page ?? 0))
      .set('size', String(filtros.size ?? 20));
    if (filtros.q?.trim()) params = params.set('q', filtros.q.trim());
    if (filtros.papel) params = params.set('papel', filtros.papel);
    if (filtros.ativo === 'true' || filtros.ativo === 'false') params = params.set('ativo', filtros.ativo);
    if (filtros.emailVerificado === 'true' || filtros.emailVerificado === 'false') {
      params = params.set('emailVerificado', filtros.emailVerificado);
    }
    if (filtros.onboardingConcluido === 'true' || filtros.onboardingConcluido === 'false') {
      params = params.set('onboardingConcluido', filtros.onboardingConcluido);
    }
    if (filtros.riscoChurn) {
      params = params.set('riscoChurn', 'true');
    }
    return this.http.get<AdminPageResponse<AdminUsuarioResumo>>(`${this.baseUrl}/usuarios`, { params });
  }

  obterUsuario(id: number): Observable<AdminUsuarioDetalhe> {
    return this.http.get<AdminUsuarioDetalhe>(`${this.baseUrl}/usuarios/${id}`);
  }

  alterarSuspensao(id: number, ativo: boolean, motivo: string): Observable<AdminUsuarioDetalhe> {
    return this.http.patch<AdminUsuarioDetalhe>(`${this.baseUrl}/usuarios/${id}/suspensao`, { ativo, motivo });
  }

  verificarEmail(id: number): Observable<AdminUsuarioDetalhe> {
    return this.http.patch<AdminUsuarioDetalhe>(`${this.baseUrl}/usuarios/${id}/verificar-email`, {});
  }

  alterarPapel(id: number, papel: 'USUARIO' | 'ADMIN_SITE', motivo: string): Observable<AdminUsuarioDetalhe> {
    return this.http.patch<AdminUsuarioDetalhe>(`${this.baseUrl}/usuarios/${id}/papel`, { papel, motivo });
  }

  resetarOnboarding(id: number): Observable<AdminUsuarioDetalhe> {
    return this.http.patch<AdminUsuarioDetalhe>(`${this.baseUrl}/usuarios/${id}/reset-onboarding`, {});
  }

  excluirUsuario(id: number, confirmacaoEmail: string, motivo: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/usuarios/${id}`, {
      body: { confirmacaoEmail, motivo },
    });
  }

  listarFamilias(page = 0, size = 20): Observable<AdminPageResponse<AdminFamiliaResumo>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AdminPageResponse<AdminFamiliaResumo>>(`${this.baseUrl}/familias`, { params });
  }

  listarCategorias(): Observable<AdminCategoriaResumo[]> {
    return this.http.get<AdminCategoriaResumo[]>(`${this.baseUrl}/categorias`);
  }

  criarCategoria(nome: string): Observable<AdminCategoriaResumo> {
    return this.http.post<AdminCategoriaResumo>(`${this.baseUrl}/categorias`, { nome });
  }

  atualizarCategoria(id: number, nome: string): Observable<AdminCategoriaResumo> {
    return this.http.put<AdminCategoriaResumo>(`${this.baseUrl}/categorias/${id}`, { nome });
  }

  excluirCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/categorias/${id}`);
  }

  listarConquistas(): Observable<AdminConquistaResumo[]> {
    return this.http.get<AdminConquistaResumo[]>(`${this.baseUrl}/conquistas`);
  }

  criarConquista(payload: Omit<AdminConquistaResumo, 'totalDesbloqueios'>): Observable<AdminConquistaResumo> {
    return this.http.post<AdminConquistaResumo>(`${this.baseUrl}/conquistas`, payload);
  }

  atualizarConquista(id: string, payload: Omit<AdminConquistaResumo, 'totalDesbloqueios'>): Observable<AdminConquistaResumo> {
    return this.http.put<AdminConquistaResumo>(`${this.baseUrl}/conquistas/${id}`, payload);
  }

  enviarNotificacao(payload: {
    titulo: string;
    mensagem: string;
    linkRedirecionamento?: string;
    usuarioId?: number | null;
    segmento?: 'TODOS' | 'RISCO_CHURN' | 'INDIVIDUAL';
  }): Observable<{ destinatarios: number; global: boolean }> {
    return this.http.post<{ destinatarios: number; global: boolean }>(`${this.baseUrl}/notificacoes/enviar`, payload);
  }

  listarAuditoria(filtros: {
    acao?: string;
    adminEmail?: string;
    page?: number;
    size?: number;
  }): Observable<AdminPageResponse<AdminAuditLog>> {
    let params = new HttpParams()
      .set('page', String(filtros.page ?? 0))
      .set('size', String(filtros.size ?? 25));
    if (filtros.acao) params = params.set('acao', filtros.acao);
    if (filtros.adminEmail?.trim()) params = params.set('adminEmail', filtros.adminEmail.trim());
    return this.http.get<AdminPageResponse<AdminAuditLog>>(`${this.baseUrl}/auditoria`, { params });
  }
}
