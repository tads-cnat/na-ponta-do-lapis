import da from '@angular/common/locales/da';
import to from '@angular/common/locales/to';
import { Injectable } from '@angular/core';
import { TokenPayload } from '../../model/ITokenPayload';
import tok from '@angular/common/locales/tok';
import tr from '@angular/common/locales/tr';
import { jwtDecode } from 'jwt-decode';


const TOKEN = "token"
const USUARIO = "usuario"

@Injectable({
  providedIn: 'root',
})



export class StorageService {

  constructor(){}

  static salvarToken(token:string):void {
    window.localStorage.removeItem(TOKEN)
    window.localStorage.setItem(TOKEN, token)
  }

  static salvarUser(user:any){
    window.localStorage.removeItem(USUARIO)
    window.localStorage.setItem(USUARIO, JSON.stringify(user))
  }

  static getToken(): string | null{
    return localStorage.getItem(TOKEN)
  }

  static getUsuarioDecodificado(): TokenPayload | null {
    const token = this.getToken()
    if (token){
      try {
        return jwtDecode<TokenPayload>(token)
      } catch(Error) {
        return null;
      }
    }
    return null;
  }

  static getPapel(): string {
    const usuario = this.getUsuarioDecodificado();
    if (usuario == null || usuario.papeis.length == 0){
      return "";
    }
    return usuario.papeis[0];
  }

  static eAdminSiteLogado(): boolean {
    if (this.getUsuarioDecodificado() == null){
      return false
    }
    const papel:string = this.getPapel()
    return "ROLE_ADMIN_SITE" == papel;
  }

  static eUsuarioLogado(): boolean {
    if (this.getUsuarioDecodificado() == null){
      return false;
    }
    const papel:string = this.getPapel()
    return "ROLE_USUARIO" == papel; 
  }

  static logout(): void {
    localStorage.removeItem(TOKEN);
    localStorage.removeItem(USUARIO);
  }
}