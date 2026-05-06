import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginRequest } from '../../model/LoginRequest';
import { Token } from '../../model/Token';


const BASE_URL = "http://localhost:8080/auth"

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  constructor(private http:HttpClient){}

  public login(loginRequest:LoginRequest): Observable<Token>{
    return this.http.post<Token>(`${BASE_URL}/login`, loginRequest)
  }

}
