import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class MetasService {
  private BASE_URL:string = "http://localhost:8080"

  constructor(private http:HttpClient){}

  public listarMetas(): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/metas`)
  }



}
