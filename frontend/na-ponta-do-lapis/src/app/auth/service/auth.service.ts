import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginRequest, SignupRequest } from '../../model/ILoginRequest';
import { Token } from '../../model/IToken.models';
import { Usuario } from '../../model/IUsuario.models';
import { environment } from '@env';



export interface GoogleConfigResponse {
  enabled: boolean;
  clientId: string;
}

declare global {
  interface Window {
    google?: {
      accounts?: {
        id?: {
          initialize: (config: {
            client_id: string;
            callback: (response: { credential?: string }) => void;
            ux_mode?: string;
          }) => void;
          prompt: (momentListener?: (notification: { isNotDisplayed: () => boolean; isSkippedMoment: () => boolean }) => void) => void;
          renderButton: (parent: HTMLElement, options: Record<string, unknown>) => void;
        };
      };
    };
  }
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private BASE_URL:string = environment.apiBaseUrl;
  private googleScriptPromise: Promise<void> | null = null;

  constructor(private http:HttpClient){}

  public login(loginRequest:LoginRequest): Observable<Token>{
    return this.http.post<Token>(`${this.BASE_URL}/auth/login`, loginRequest)
  }

  public getGoogleConfig(): Observable<GoogleConfigResponse> {
    return this.http.get<GoogleConfigResponse>(`${this.BASE_URL}/auth/google/config`);
  }

  public loginWithGoogle(idToken: string): Observable<Token> {
    return this.http.post<Token>(`${this.BASE_URL}/auth/google`, { idToken });
  }

  public register(signupRequest:SignupRequest): Observable<SignupRequest> {
    return this.http.post<SignupRequest>(`${this.BASE_URL}/auth/register`, signupRequest)
  }

  public meusDados(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.BASE_URL}/usuario/me`)
  }

  public iniciarFluxoGoogle(
    onCredential: (idToken: string) => void,
    onUnavailable: (reason: string) => void
  ): void {
    this.getGoogleConfig().subscribe({
      next: async (config) => {
        if (!config.enabled || !config.clientId) {
          onUnavailable('Login com Google ainda não configurado no servidor (GOOGLE_CLIENT_ID).');
          return;
        }
        try {
          await this.carregarScriptGoogle();
          const gis = window.google?.accounts?.id;
          if (!gis) {
            onUnavailable('Serviço do Google indisponível no momento.');
            return;
          }
          gis.initialize({
            client_id: config.clientId,
            callback: (res) => {
              if (res?.credential) {
                onCredential(res.credential);
              } else {
                onUnavailable('Não foi possível obter a credencial do Google.');
              }
            },
          });
          gis.prompt((notification) => {
            if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
              onUnavailable('Permita pop-ups/terceiros do Google ou tente novamente.');
            }
          });
        } catch {
          onUnavailable('Não foi possível carregar o script de login do Google.');
        }
      },
      error: () => onUnavailable('Erro ao consultar configuração do Google no servidor.'),
    });
  }

  private carregarScriptGoogle(): Promise<void> {
    if (window.google?.accounts?.id) {
      return Promise.resolve();
    }
    if (!this.googleScriptPromise) {
      this.googleScriptPromise = new Promise<void>((resolve, reject) => {
        const script = document.createElement('script');
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.defer = true;
        script.onload = () => resolve();
        script.onerror = () => reject(new Error('Falha ao carregar GIS'));
        document.head.appendChild(script);
      });
    }
    return this.googleScriptPromise;
  }

}
