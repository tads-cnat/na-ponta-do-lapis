import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface MensagemConsultor {
  role: 'user' | 'assistant';
  content: string;
  provedorUsado?: string;
  perguntaOrigem?: string;
  feedbackId?: number;
  feedbackUtil?: boolean | null;
}

export interface ConsultorStatus {
  pronto: boolean;
  temChaveUsuario: boolean;
  temSessao: boolean;
  temChavePlataforma: boolean;
  provedorPadrao: string;
}

export interface ConsultorRequest {
  pergunta: string;
  historico: Array<{ role: 'user' | 'assistant'; content: string }>;
  provedor?: string;
  apiKey?: string;
  chaveIaId?: number;
  manterNaSessao?: boolean;
}

export interface ConsultorResponse {
  resposta: string;
  provedorUsado: string;
  usouChavePlataforma: boolean;
}

const BOTAO_VISIVEL_KEY = 'npl_grafith_botao_visivel';

@Injectable({
  providedIn: 'root',
})
export class ConsultorService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/consultor`;

  readonly aberto = signal(false);
  readonly perguntaPendente = signal<string | null>(null);
  readonly exibirBotaoFlutuante = signal<boolean>(this.carregarPreferenciaBotao());

  definirExibirBotaoFlutuante(visivel: boolean): void {
    this.exibirBotaoFlutuante.set(visivel);
    try {
      localStorage.setItem(BOTAO_VISIVEL_KEY, String(visivel));
    } catch {}
  }

  private carregarPreferenciaBotao(): boolean {
    try {
      const salvo = localStorage.getItem(BOTAO_VISIVEL_KEY);
      if (salvo === 'false') return false;
    } catch {}
    return true;
  }

  abrir(): void {
    this.aberto.set(true);
  }

  abrirComPergunta(pergunta: string): void {
    const limpa = (pergunta || '').trim();
    if (limpa) {
      this.perguntaPendente.set(limpa);
    }
    this.aberto.set(true);
  }

  consumirPerguntaPendente(): string | null {
    const p = this.perguntaPendente();
    if (p) {
      this.perguntaPendente.set(null);
    }
    return p;
  }

  fechar(): void {
    this.aberto.set(false);
  }

  status(): Observable<ConsultorStatus> {
    return this.http.get<ConsultorStatus>(`${this.apiUrl}/status`, { withCredentials: true });
  }

  perguntar(payload: ConsultorRequest): Observable<ConsultorResponse> {
    return this.http.post<ConsultorResponse>(`${this.apiUrl}/perguntar`, payload, { withCredentials: true });
  }

  enviarFeedback(payload: {
    feedbackId?: number;
    util: boolean;
    provedor?: string;
    perguntaResumo?: string;
    respostaResumo?: string;
  }): Observable<{ id: number; util: boolean }> {
    return this.http.post<{ id: number; util: boolean }>(`${this.apiUrl}/feedback`, payload, { withCredentials: true });
  }
}
