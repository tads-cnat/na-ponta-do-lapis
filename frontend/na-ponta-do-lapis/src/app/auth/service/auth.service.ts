import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginRequest, SignupRequest } from '../../model/ILoginRequest';
import { Token } from '../../model/IToken.models';
import { Usuario } from '../../model/IUsuario.models';
import { environment } from '@env';



@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private BASE_URL:string = environment.apiBaseUrl;

  constructor(private http:HttpClient){}

  public login(loginRequest:LoginRequest): Observable<Token>{
    return this.http.post<Token>(`${this.BASE_URL}/auth/login`, loginRequest)
  }

  public register(signupRequest:SignupRequest): Observable<SignupRequest> {
    return this.http.post<SignupRequest>(`${this.BASE_URL}/auth/register`, signupRequest)
  }

  public meusDados(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.BASE_URL}/usuario/me`)
  }

}
