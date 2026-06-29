import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Usuario } from '../../../model/IUsuario.models';
import { environment } from '@env';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private BASE_URL: string = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  public obterPerfilCompleto(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.BASE_URL}/usuario/me`);
  }

  /**
   * Dispara uma atualização parcial via PATCH para o ID especificado na URL
   */
  /**
   * Dispara uma atualização parcial via PATCH para o perfil do usuário logado
   */
  public atualizarParcial(dadosParciais: Partial<Usuario>): Observable<Usuario> {
    // Agora dispara direto para /usuario/me sem precisar caçar ou passar IDs!
    return this.http.patch<Usuario>(`${this.BASE_URL}/usuario/me`, dadosParciais);
  }
}