import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginRequest } from '../../model/ILoginRequest';
import { Token } from '../../model/IToken';



@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private BASE_URL:string = "http://localhost:8080/auth"

  constructor(private http:HttpClient){}

  public login(loginRequest:LoginRequest): Observable<Token>{
    return this.http.post<Token>(`${this.BASE_URL}/login`, loginRequest)
  }

}
