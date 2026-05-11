import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { IContas, IContasRequest } from '../../../model/IContas.models';

@Injectable({
  providedIn: 'root',
})

export class ContaFinanceiraService {
  private BASE_URL:string = "http://localhost:8080"

  constructor(private http:HttpClient){
  }

  public listarContasUsuario(): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/contas/me`)
  }

  public adicionarConta(contaFinanceira:IContasRequest): Observable<IContas> {
    return this.http.post<IContas>(`${this.BASE_URL}/contas`, contaFinanceira)
  }

  public atualizarConta(contaFinanceira:IContasRequest, id: number): Observable<IContas> {
    return this.http.put<IContas>(`${this.BASE_URL}/contas/${id}`, contaFinanceira)
  }

  public atualizarAtributoConta(contaFinanceira:IContasRequest, id: number): Observable<IContas> {
    return this.http.patch<IContas>(`${this.BASE_URL}/contas/${id}`, contaFinanceira)
  }

    public deletarContaPorId(id:number): Observable<void>{
      return this.http.delete<void>(`${this.BASE_URL}/contas/${id}`)
    }

  public listarContasUsuarioLogado(): Observable<any>{
    return this.http.get<any>(`${this.BASE_URL}/contas/me`)
  }
}
