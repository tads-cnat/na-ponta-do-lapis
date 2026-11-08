import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env';
import {
  IChaveIaRequest,
  IChaveIaResponse,
  IOpenFinanceConectarRequest,
  IOpenFinanceItem,
  IOpenFinanceStatus
} from '../model/IIntegracoes.models';

@Injectable({
  providedIn: 'root'
})
export class IntegracoesService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  // --- Open Finance ---
  public obterStatusOpenFinance(): Observable<IOpenFinanceStatus> {
    return this.http.get<IOpenFinanceStatus>(`${this.baseUrl}/integracoes/open-finance/status`);
  }

  public conectarOpenFinance(payload: IOpenFinanceConectarRequest): Observable<IOpenFinanceStatus> {
    return this.http.post<IOpenFinanceStatus>(`${this.baseUrl}/integracoes/open-finance/conectar`, payload);
  }

  public desconectarOpenFinance(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/integracoes/open-finance/desconectar`);
  }

  public sincronizarOpenFinanceManual(): Observable<IOpenFinanceStatus> {
    return this.http.post<IOpenFinanceStatus>(`${this.baseUrl}/integracoes/open-finance/sincronizar`, {});
  }

  public gerarConnectToken(): Observable<{ connectToken: string }> {
    return this.http.post<{ connectToken: string }>(`${this.baseUrl}/integracoes/open-finance/connect-token`, {});
  }

  public listarItensOpenFinance(): Observable<IOpenFinanceItem[]> {
    return this.http.get<IOpenFinanceItem[]>(`${this.baseUrl}/integracoes/open-finance/items`);
  }

  public adicionarItemOpenFinance(itemId: string): Observable<IOpenFinanceStatus> {
    return this.http.post<IOpenFinanceStatus>(`${this.baseUrl}/integracoes/open-finance/items`, { itemId });
  }

  public removerItemOpenFinance(itemId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/integracoes/open-finance/items/${itemId}`);
  }

  // --- Chaves de IA ---
  public listarChavesIa(): Observable<IChaveIaResponse[]> {
    return this.http.get<IChaveIaResponse[]>(`${this.baseUrl}/integracoes/ia/chaves`);
  }

  public cadastrarChaveIa(payload: IChaveIaRequest): Observable<IChaveIaResponse> {
    return this.http.post<IChaveIaResponse>(`${this.baseUrl}/integracoes/ia/chaves`, payload);
  }

  public definirChaveIaPadrao(id: number): Observable<IChaveIaResponse> {
    return this.http.put<IChaveIaResponse>(`${this.baseUrl}/integracoes/ia/chaves/${id}/padrao`, {});
  }

  public excluirChaveIa(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/integracoes/ia/chaves/${id}`);
  }
}

