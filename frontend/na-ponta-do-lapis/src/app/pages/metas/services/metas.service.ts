import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env';
import { Meta } from '@models/IMetas.models'


@Injectable({
  providedIn: 'root',
})
export class MetasService {
  private BASE_URL:string = environment.apiBaseUrl;

  constructor(private http:HttpClient){}

  public listarMetas(): Observable<Meta[]> {
    return this.http.get<Meta[]>(`${this.BASE_URL}/metas`)
  }

  public buscarMeta(id: number): Observable<Meta> {
    return this.http.get<Meta>(`${this.BASE_URL}/metas/${id}`)
  }
  
  public criarMeta(meta: Meta): Observable<Meta> {
    return this.http.post<Meta>(`${this.BASE_URL}/metas`, meta)
  }

  public atualizarMeta(id: number, meta: Meta): Observable<Meta> {
    return this.http.put<Meta>(`${this.BASE_URL}/metas/${id}`, meta)
  }
  
  public deletarMeta(id: number): Observable<Meta> {
    return this.http.delete<Meta>(`${this.BASE_URL}/metas/${id}`)
  }

}
