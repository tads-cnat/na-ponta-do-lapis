import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '@env';
import { Notificacao, ContadorNaoLidasResponse } from '../../model/INotificacao.model';

@Injectable({
  providedIn: 'root'
})
export class NotificacaoService {
  private readonly baseUrl = `${environment.apiBaseUrl}/notificacoes`;

  private notificacoesSubject = new BehaviorSubject<Notificacao[]>([]);
  public notificacoes$ = this.notificacoesSubject.asObservable();

  private totalNaoLidasSubject = new BehaviorSubject<number>(0);
  public totalNaoLidas$ = this.totalNaoLidasSubject.asObservable();

  constructor(private http: HttpClient) {}

  public carregarNotificacoes(): void {
    this.http.get<Notificacao[]>(this.baseUrl).subscribe({
      next: (notificacoes) => {
        this.notificacoesSubject.next(notificacoes);
        const naoLidas = notificacoes.filter((n) => !n.lida).length;
        this.totalNaoLidasSubject.next(naoLidas);
      },
      error: () => {
        // Falha silenciosa para não quebrar a navegação principal
      }
    });
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
}

