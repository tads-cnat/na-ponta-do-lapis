import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { IContaFinanceira, IContaFinanceiraRequest } from '../../../model/IContas.models';

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

  public adicionarConta(contaFinanceira:IContaFinanceiraRequest): Observable<IContaFinanceira> {
    return this.http.post<IContaFinanceira>(`${this.BASE_URL}/contas`, contaFinanceira)
  }

  public atualizarConta(contaFinanceira:IContaFinanceiraRequest, id: number): Observable<IContaFinanceira> {
    return this.http.put<IContaFinanceira>(`${this.BASE_URL}/contas/${id}`, contaFinanceira)
  }

  public atualizarAtributoConta(contaFinanceira:IContaFinanceiraRequest, id: number): Observable<IContaFinanceira> {
    return this.http.patch<IContaFinanceira>(`${this.BASE_URL}/contas/${id}`, contaFinanceira)
  }

    public deletarContaPorId(id:number): Observable<void>{
      return this.http.delete<void>(`${this.BASE_URL}/contas/${id}`)
    }

  public listarContasUsuarioLogado(): Observable<any>{
    return this.http.get<any>(`${this.BASE_URL}/contas/me`)
  }
}
