import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env';
import { Marcador } from '@models/Marcador';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MarcadorService {
  http = inject(HttpClient);
  
  private BASE_URL:string = environment.apiBaseUrl;

  public listar(): Observable<Marcador[]>{
    return this.http.get<Marcador[]>(`${this.BASE_URL}/marcadores/me`)
  }

  public salvar(marcador:any): Observable<any>{
    return this.http.post<Marcador>(`${this.BASE_URL}/marcadores`, marcador)
  }

  public excluir(marcadorId:number): Observable<void>{
    return this.http.delete<void>(`${this.BASE_URL}/marcadores/${marcadorId}`)
  }

}
