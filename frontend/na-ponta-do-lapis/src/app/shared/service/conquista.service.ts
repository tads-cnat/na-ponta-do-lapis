import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '@env';
import { PerfilGamificado } from '../../model/IConquista.models';

@Injectable({
  providedIn: 'root'
})
export class ConquistaService {
  private readonly baseUrl = `${environment.apiBaseUrl}/conquistas`;

  private perfilSubject = new BehaviorSubject<PerfilGamificado | null>(null);
  public perfil$ = this.perfilSubject.asObservable();

  constructor(private http: HttpClient) {}

  public carregarPerfilGamificado(): Observable<PerfilGamificado> {
    return this.http.get<PerfilGamificado>(`${this.baseUrl}/me`).pipe(
      tap((perfil) => this.perfilSubject.next(perfil))
    );
  }

  public getPerfilAtual(): PerfilGamificado | null {
    return this.perfilSubject.value;
  }
}

