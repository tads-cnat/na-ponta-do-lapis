import { HttpClient } from '@angular/common/http';
import ht from '@angular/common/locales/ht';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TransacoesService {
  private BASE_URL:string = "http://localhost:8080"

  constructor(private http:HttpClient){}

  public listarTransacoes(): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/transacoes`)
  }

  public deletarTransacaoPorId(id:number): Observable<void>{
    return this.http.delete<void>(`${this.BASE_URL}/transacoes/${id}`)
  }
}
