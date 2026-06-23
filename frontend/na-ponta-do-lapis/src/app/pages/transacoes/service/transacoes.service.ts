import { HttpClient } from '@angular/common/http';
import ht from '@angular/common/locales/ht';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Categoria, ITransacaoRequest, ITransacoes,  } from '../../../model/ITransacoes.model';
import { environment}  from '@env';

@Injectable({
  providedIn: 'root',
})
export class TransacoesService {
  private BASE_URL:string = environment.apiBaseUrl;

  constructor(private http:HttpClient){}

  public listarTransacoes(): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/transacoes/me`)
  }

  public listarTransacoesPorDescricao(descricao:string): Observable<ITransacoes[]> {
    return this.http.get<ITransacoes[]>(`${this.BASE_URL}/transacoes/buscar?descricao=${descricao}`)
  }

    public buscarTransacaoPorId(id:number): Observable<ITransacaoRequest> {
    return this.http.get<ITransacaoRequest>(`${this.BASE_URL}/transacoes/${id}`)
  }

  public deletarTransacaoPorId(id:number): Observable<void>{
    return this.http.delete<void>(`${this.BASE_URL}/transacoes/${id}`)
  }

  public editarTransacao(id:number ,transacao:ITransacaoRequest) {
    return this.http.put<ITransacoes>(`${this.BASE_URL}/transacoes/${id}`, transacao)
  }

  public adicionarTransacao(transacao:ITransacaoRequest): Observable<ITransacoes> {
    return this.http.post<ITransacoes>(`${this.BASE_URL}/transacoes`, transacao)
  }

  public listarContasUsuarioLogado(): Observable<any>{
    return this.http.get<any>(`${this.BASE_URL}/contas/me`)
  }

  public listarCategorias(): Observable<Categoria[]>{
    return this.http.get<Categoria[]>(`${this.BASE_URL}/categorias`)
  }

  public listarMarcadores(): Observable<any>{
    return this.http.get<any>(`${this.BASE_URL}/marcadores/me`)
  }


}
