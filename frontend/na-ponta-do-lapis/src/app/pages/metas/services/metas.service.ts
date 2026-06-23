import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env';


@Injectable({
  providedIn: 'root',
})
export class MetasService {
  private BASE_URL:string = environment.apiBaseUrl;

  constructor(private http:HttpClient){}

  public listarMetas(): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/metas`)
  }



}
