import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment.development';
import { ICotacao } from '../../../model/ICotacao.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private BASE_URL: string = environment.apiBaseUrl

  constructor(private http: HttpClient){
  }

  public listarCotacoes(moeda: string): Observable<ICotacao> {
    return this.http.get<any>(`${this.BASE_URL}/cotacoes/${moeda}`)
  }

}
