
import { Injectable } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { SocialLogin } from '@capgo/capacitor-social-login';
import { jwtDecode } from 'jwt-decode';
import { TokenPayload } from '../../model/IToken.models';
import { CacheLocalService } from '../../shared/service/cache-local.service';


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

  static temTokenValido(): boolean {
    const payload = this.getUsuarioDecodificado();
    if (!payload?.exp) {
      return false;
    }
    const agoraSegundos = Math.floor(Date.now() / 1000);
    return payload.exp > agoraSegundos + 5;
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
    CacheLocalService.limparTudo();
    if (Capacitor.isNativePlatform()) {
      SocialLogin.logout({ provider: 'google' }).catch(() => {});
    }
  }
}