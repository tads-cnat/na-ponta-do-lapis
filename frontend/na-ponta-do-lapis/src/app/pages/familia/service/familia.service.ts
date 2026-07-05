import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Usuario } from '../../../model/IUsuario.models';
import { ITransacoes } from '../../../model/ITransacoes.model';
import { IContas } from '../../../model/IContas.models';
import { environment } from '@env';
@Injectable({
  providedIn: 'root',
})
export class FamiliaService {
  private BASE_URL:string = environment.apiBaseUrl;

  constructor(private http:HttpClient){}

  verificarUsuarioExistente(nickname: string): Observable<boolean> {
    return this.http.get<any>(`${this.BASE_URL}/usuario/username/${nickname}`).pipe(
      map(() => true),    
      catchError(() => of(false)) 
    );
  }
  criarFamilia(nomeFamilia: string, foto?: string): Observable<Familia> {
    return this.http.post<any>(`${this.BASE_URL}/familias`, {
      foto: foto,
      nome: nomeFamilia
    });
  }
  usuarioTemFamilia(nickname: string): Observable<boolean> {
    return this.http.get<any>(`${this.BASE_URL}/usuario/username/${nickname}`).pipe(
      map((user: any) => user.idFamilia != null),
      catchError(() => of(false))
    );
  }
  buscarFamiliaPorNome(nomeFamilia: string): Observable<Familia> {
    const familias = this.http.get<Familia[]>(`${this.BASE_URL}/familias`);
    return familias.pipe(
      map(familias => familias.find(familia => familia.nome === nomeFamilia)!)
    ); 
  }
  vincularUsuarioAFamilia(nickname: string, familiaId: number): Observable<Usuario> {
    return this.http.post<any>(
      `${this.BASE_URL}/familias/${familiaId}/membros`,
      null,
      { params: { username: nickname } }
    );
  }
  removerUsuarioDaFamilia(familiaId: number, username: string): Observable<void> {
    return this.http.delete<any>(`${this.BASE_URL}/familias/${familiaId}/membros`, {
      params: { username: username }
    });
  }
  promoverParaAdmin(familiaId: number, userId: number): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.BASE_URL}/familias/${familiaId}/membros/${userId}`, {});
  }
  buscarUsuariosPorNickname(nickname: string): Observable<Usuario> {
    return this.http.get<any>(`${this.BASE_URL}/usuario/username/${nickname}`);
  }
  buscarTransacoesPorFamilia(familiaId: number): Observable<ITransacoes[]> {
    return this.http.get<ITransacoes[]>(`${this.BASE_URL}/familias/${familiaId}/transacoes`);
  }
  buscarMembrosPorFamilia(familiaId: number): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.BASE_URL}/familias/${familiaId}/membros`);
  }
  buscarContasPorFamilia(familiaId: number): Observable<IContas[]> {
    return this.http.get<IContas[]>(`${this.BASE_URL}/familias/${familiaId}/contas`);
  }
  buscarTransacoesFamiliaPorDescricao(familiaId: number, descricao: string): Observable<ITransacoes[]> {
    return this.http.get<ITransacoes[]>(`${this.BASE_URL}/familias/${familiaId}/transacoes/descricao`, {
      params: { descricao }
    });
  }
}
