import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env';
import { MetaRequest, MetaResponse, TipoMetaResponse } from '@models/IMetas.models'


@Injectable({
  providedIn: 'root',
})
export class MetasService {
  private BASE_URL:string = environment.apiBaseUrl;

  constructor(private http:HttpClient){}

  public listarMetas(): Observable<MetaResponse[]> {
    return this.http.get<MetaResponse[]>(`${this.BASE_URL}/metas`)
  }

  public buscarMeta(id: number): Observable<MetaResponse> {
    return this.http.get<MetaResponse>(`${this.BASE_URL}/metas/${id}`)
  }
  
  public criarMeta(meta: MetaRequest): Observable<MetaResponse> {
    return this.http.post<MetaResponse>(`${this.BASE_URL}/metas`, meta)
  }

  public atualizarMeta(id: number, meta: MetaRequest): Observable<MetaResponse> {
    return this.http.put<MetaResponse>(`${this.BASE_URL}/metas/${id}`, meta)
  }
  
  public deletarMeta(id: number): Observable<MetaResponse> {
    return this.http.delete<MetaResponse>(`${this.BASE_URL}/metas/${id}`)
  }

  public listarTiposMeta(): Observable<TipoMetaResponse[]> {
    return this.http.get<TipoMetaResponse[]>(`${this.BASE_URL}/metas/tipos`)
  }

}
