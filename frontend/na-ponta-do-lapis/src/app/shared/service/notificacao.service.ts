import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subscription, timer, tap } from 'rxjs';
import { environment } from '@env';
import { Notificacao, ContadorNaoLidasResponse } from '../../model/INotificacao.model';
import { CelebracaoService } from './celebracao.service';

@Injectable({
  providedIn: 'root'
})
export class NotificacaoService implements OnDestroy {
  private readonly baseUrl = `${environment.apiBaseUrl}/notificacoes`;

  private notificacoesSubject = new BehaviorSubject<Notificacao[]>([]);
  public notificacoes$ = this.notificacoesSubject.asObservable();

  private totalNaoLidasSubject = new BehaviorSubject<number>(0);
  public totalNaoLidas$ = this.totalNaoLidasSubject.asObservable();

  private idsConhecidos = new Set<number>();
  private primeiraCarga = true;
  private pollingSub?: Subscription;

  constructor(
    private http: HttpClient,
    private celebracaoService: CelebracaoService
  ) {
    this.iniciarPolling();
  }

  public iniciarPolling(): void {
    if (this.pollingSub) return;
    // Polling periódico a cada 8 segundos
    this.pollingSub = timer(0, 8000).subscribe(() => {
      this.carregarNotificacoes();
    });
  }

  public carregarNotificacoes(forcarCelebracao = false): void {
    this.http.get<Notificacao[]>(this.baseUrl).subscribe({
      next: (notificacoes) => {
        this.processarNovasNotificacoes(notificacoes, forcarCelebracao);
        this.notificacoesSubject.next(notificacoes);
        const naoLidas = notificacoes.filter((n) => !n.lida).length;
        this.totalNaoLidasSubject.next(naoLidas);
      },
      error: () => {
        // Falha silenciosa para não poluir console em caso de token expirado
      }
    });
  }

  public recarregarImediatamente(): void {
    this.carregarNotificacoes(true);
  }

  private processarNovasNotificacoes(notificacoes: Notificacao[], forcarCelebracao: boolean): void {
    if (this.primeiraCarga) {
      notificacoes.forEach((n) => this.idsConhecidos.add(n.id));
      this.primeiraCarga = false;
      return;
    }

    const novas = notificacoes.filter((n) => !this.idsConhecidos.has(n.id));
    novas.forEach((n) => {
      this.idsConhecidos.add(n.id);

      // Gatilho triunfal de celebração com confetes caso seja conquista ou meta
      if (n.tipo === 'CONQUISTA_DESBLOQUEADA') {
        // Tenta inferir ID da conquista ou título
        const matchTitulo = n.mensagem.match(/'([^']+)'/);
        const tituloConquista = matchTitulo ? matchTitulo[1] : n.titulo;
        const matchXp = n.mensagem.match(/\+(\d+)\s*XP/);
        const xp = matchXp ? parseInt(matchXp[1], 10) : 50;

        this.celebracaoService.celebrarConquista(tituloConquista, n.mensagem, xp, this.inferirIdConquista(tituloConquista));
      } else if (n.tipo === 'META_ATINGIDA') {
        this.celebracaoService.celebrarMetaAtingida(n.titulo.replace('Meta Concluída: ', ''), 0);
      }
    });
  }

  private inferirIdConquista(titulo: string): string {
    const t = titulo.toLowerCase();
    if (t.includes('primeiro passo') || t.includes('primeira meta')) return 'PRIMEIRA_META';
    if (t.includes('cofrinho') || t.includes('primeiro aporte')) return 'PRIMEIRO_APORTE';
    if (t.includes('bronze')) return 'POUPADOR_BRONZE';
    if (t.includes('alvo') || t.includes('primeira meta')) return 'META_BATIDA_1';
    if (t.includes('mestre')) return 'MESTRE_DAS_METAS';
    if (t.includes('blindado')) return 'ORCAMENTO_BLINDADO';
    if (t.includes('dívida') || t.includes('divida')) return 'LIVRE_DE_DIVIDAS';
    if (t.includes('família') || t.includes('familia')) return 'FAMILIA_UNIDA';
    return 'PRIMEIRA_META';
  }

  public atualizarContadorNaoLidas(): void {
    this.http.get<ContadorNaoLidasResponse>(`${this.baseUrl}/nao-lidas/count`).subscribe({
      next: (res) => this.totalNaoLidasSubject.next(res.totalNaoLidas),
      error: () => {}
    });
  }

  public marcarComoLida(id: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/ler`, {}).pipe(
      tap(() => {
        const atualizadas = this.notificacoesSubject.value.map((n) =>
          n.id === id ? { ...n, lida: true } : n
        );
        this.notificacoesSubject.next(atualizadas);
        const novoTotal = Math.max(0, this.totalNaoLidasSubject.value - 1);
        this.totalNaoLidasSubject.next(novoTotal);
      })
    );
  }

  public marcarTodasComoLidas(): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/ler-todas`, {}).pipe(
      tap(() => {
        const atualizadas = this.notificacoesSubject.value.map((n) => ({ ...n, lida: true }));
        this.notificacoesSubject.next(atualizadas);
        this.totalNaoLidasSubject.next(0);
      })
    );
  }

  public excluirNotificacao(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      tap(() => {
        const item = this.notificacoesSubject.value.find((n) => n.id === id);
        const atualizadas = this.notificacoesSubject.value.filter((n) => n.id !== id);
        this.notificacoesSubject.next(atualizadas);
        if (item && !item.lida) {
          this.totalNaoLidasSubject.next(Math.max(0, this.totalNaoLidasSubject.value - 1));
        }
      })
    );
  }

  ngOnDestroy(): void {
    if (this.pollingSub) {
      this.pollingSub.unsubscribe();
    }
  }
}
