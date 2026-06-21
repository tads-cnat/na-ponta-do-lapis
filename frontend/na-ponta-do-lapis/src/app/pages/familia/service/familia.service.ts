import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/internal/operators/map';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class FamiliaService {
  private BASE_URL:string = "http://localhost:8080"

  constructor(private http:HttpClient){}

  verificarUsuarioExistente(nickname: string): Observable<boolean> {
     return this.http.get<any[]>(`${this.BASE_URL}/usuario/{nickname}`).pipe(
      map(res => res.length === 0)
     );
  }

}
