import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Categoria,
  FaturaAnaliseOpcoes,
  FaturaAnaliseResponse,
  ITransacaoFatura,
  ITransacaoRequest,
  ITransacoes,
} from '../../../model/ITransacoes.model';
import { environment }  from '@env';

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

  public adicionarTransacao(transacao:ITransacaoRequest): Observable<ITransacoes[]> {
    return this.http.post<ITransacoes[]>(`${this.BASE_URL}/transacoes`, transacao)
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

  public analisarFatura(
    arquivo: File,
    opcoes: FaturaAnaliseOpcoes = {}
  ): Observable<FaturaAnaliseResponse> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);

    if (opcoes.provedor) formData.append('provedor', opcoes.provedor);
    if (opcoes.apiKey) formData.append('apiKey', opcoes.apiKey);
    formData.append('manterNaSessao', String(opcoes.manterNaSessao ?? false));
    formData.append('forceAiAnalysis', String(opcoes.forceAiAnalysis ?? false));
    formData.append('enviarPdfParaIa', String(opcoes.enviarPdfParaIa ?? false));
    formData.append('consentimentoLgpd', String(opcoes.consentimentoLgpd ?? false));

    return this.http.post<FaturaAnaliseResponse>(
      `${this.BASE_URL}/faturas/analise`,
      formData,
      { withCredentials: true }
    );
  }

  public esquecerChaveIa(): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/faturas/sessao`, { withCredentials: true });
  }

  public salvarFaturaEmLote(transacoes: ITransacaoFatura[], contaId: number): Observable<ITransacoes[]> {
    return this.http.post<ITransacoes[]>(`${this.BASE_URL}/transacoes/lote?contaId=${contaId}`, transacoes);
  }

  public listarParcelamento(grupoId: string): Observable<ITransacoes[]> {
    return this.http.get<ITransacoes[]>(`${this.BASE_URL}/transacoes/parcelamento/${grupoId}`);
  }

  public gerarParcelasFuturas(grupoId: string): Observable<ITransacoes[]> {
    return this.http.post<ITransacoes[]>(`${this.BASE_URL}/transacoes/parcelamento/${grupoId}/futuras`, {});
  }

  public excluirParcelamento(grupoId: string, apenasFuturas: boolean): Observable<void> {
    return this.http.delete<void>(
      `${this.BASE_URL}/transacoes/parcelamento/${grupoId}?apenasFuturas=${apenasFuturas}`
    );
  }
}
