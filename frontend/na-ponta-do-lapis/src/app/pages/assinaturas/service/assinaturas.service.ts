import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type FrequenciaRecorrencia = 'SEMANAL' | 'MENSAL' | 'TRIMESTRAL' | 'ANUAL';
export type StatusRecorrencia = 'ATIVA' | 'PAUSADA' | 'CANCELADA';

export interface AssinaturaRecorrente {
  id: number;
  descricao: string;
  valor: number;
  valorMensalEquivalente: number;
  tipo: 'DESPESA' | 'RECEITA';
  frequencia: FrequenciaRecorrencia;
  status: StatusRecorrencia;
  estadoGeracao: 'PENDENTE' | 'REALIZADA';
  diaCobranca: number;
  proximaCobranca: string;
  ultimaCobranca?: string | null;
  contaId: number;
  contaNome: string;
  categoriaId: number;
  categoriaNome: string;
  marcadorId?: number | null;
  marcadorNome?: string | null;
}

export interface AssinaturasResumo {
  totalMensalDespesas: number;
  totalMensalReceitas: number;
  totalAnualDespesas: number;
  ativas: number;
  pausadas: number;
  assinaturas: AssinaturaRecorrente[];
}

export interface AssinaturaRecorrentePayload {
  descricao: string;
  valor: number;
  tipo: 'DESPESA' | 'RECEITA';
  frequencia: FrequenciaRecorrencia;
  estadoGeracao: 'PENDENTE' | 'REALIZADA';
  proximaCobranca: string;
  contaId: number;
  categoriaId: number;
  marcadorId?: number | null;
  lancarPrimeiraAgora?: boolean;
}

export interface ParcelamentoResumo {
  grupoId: string;
  descricao: string;
  valorParcela: number;
  valorTotalRestante: number;
  parcelasPagas: number;
  parcelasRestantes: number;
  totalParcelas: number;
  proximaParcelaData?: string | null;
  ultimaParcelaData?: string | null;
  contaNome?: string | null;
  categoriaNome?: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class AssinaturasService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiBaseUrl}/assinaturas`;

  listar(): Observable<AssinaturasResumo> {
    return this.http.get<AssinaturasResumo>(this.apiUrl);
  }

  listarParcelamentos(): Observable<ParcelamentoResumo[]> {
    return this.http.get<ParcelamentoResumo[]>(`${this.apiUrl}/parcelamentos`);
  }

  criar(payload: AssinaturaRecorrentePayload): Observable<AssinaturaRecorrente> {
    return this.http.post<AssinaturaRecorrente>(this.apiUrl, payload);
  }

  atualizar(id: number, payload: AssinaturaRecorrentePayload): Observable<AssinaturaRecorrente> {
    return this.http.put<AssinaturaRecorrente>(`${this.apiUrl}/${id}`, payload);
  }

  alterarStatus(id: number, status: StatusRecorrencia): Observable<AssinaturaRecorrente> {
    return this.http.patch<AssinaturaRecorrente>(`${this.apiUrl}/${id}/status`, null, {
      params: { status },
    });
  }

  lancarAgora(id: number): Observable<unknown> {
    return this.http.post(`${this.apiUrl}/${id}/lancar`, null);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
