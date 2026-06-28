import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Usuario } from 'app/model/IUsuario.models';
import { ITransacoes } from 'app/model/ITransacoes.model';
@Injectable({
  providedIn: 'root',
})
export class FamiliaService {
  private BASE_URL:string = "http://localhost:8080"

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
  vincularUsuarioAFamilia(nickname: string, familiaId: number): Observable<any> {
    return this.http.post<any>(
      `${this.BASE_URL}/familias/${familiaId}/membros`,
      null,
      { params: { username: nickname } }
    );
  }
  promoverParaAdmin(familiaId: number, userId: number): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.BASE_URL}/familias/${familiaId}/membros/${userId}`, {});
  }
  buscarUsuariosPorNickname(nickname: string): Observable<Usuario> {
    return this.http.get<any>(`${this.BASE_URL}/usuario/username/${nickname}`);
  }
  buscarTransacoesPorFamilia(familiaId: number): Observable<ITransacoes[]> {
    return this.http.get<ITransacoes[]>(`${this.BASE_URL}/transacoes/familia/${familiaId}`);
  }
}
