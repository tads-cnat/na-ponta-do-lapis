import { HttpClient } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginRequest, SignupRequest } from '../../model/ILoginRequest';
import { Token } from '../../model/IToken.models';
import { Usuario } from '../../model/IUsuario.models';
import { environment } from '@env';



export interface GoogleConfigResponse {
  enabled: boolean;
  clientId: string;
}

export interface GoogleAuthResponse {
  token: string | null;
  requerTermos: boolean;
  nome?: string | null;
  email?: string | null;
  fotoUrl?: string | null;
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
  private activeGoogleCredentialHandler: ((idToken: string) => void) | null = null;

  constructor(private http:HttpClient, private ngZone: NgZone){}

  public login(loginRequest:LoginRequest): Observable<Token>{
    return this.http.post<Token>(`${this.BASE_URL}/auth/login`, loginRequest)
  }

  public getGoogleConfig(): Observable<GoogleConfigResponse> {
    return this.http.get<GoogleConfigResponse>(`${this.BASE_URL}/auth/google/config`);
  }

  public loginWithGoogle(idToken: string, termosAceitos = false): Observable<GoogleAuthResponse> {
    return this.http.post<GoogleAuthResponse>(`${this.BASE_URL}/auth/google`, { idToken, termosAceitos });
  }

  public register(signupRequest:SignupRequest): Observable<{ message: string; requiresEmailVerification?: boolean }> {
    return this.http.post<{ message: string; requiresEmailVerification?: boolean }>(`${this.BASE_URL}/auth/register`, signupRequest)
  }

  public verifyEmail(token: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.BASE_URL}/auth/verify-email`,
      { token }
    );
  }

  public resendVerificationEmail(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.BASE_URL}/auth/verify-email/resend`,
      { email }
    );
  }

  public getRecoveryConfig(): Observable<{ emailEnabled: boolean }> {
    return this.http.get<{ emailEnabled: boolean }>(
      `${this.BASE_URL}/auth/recover-password/config`
    );
  }

  public requestPasswordRecoveryEmail(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.BASE_URL}/auth/recover-password/email`,
      { email }
    );
  }

  public resetPasswordWithToken(token: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.BASE_URL}/auth/recover-password/reset`,
      { token, newPassword }
    );
  }

  public meusDados(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.BASE_URL}/usuario/me`)
  }

  public renderizarBotaoGoogle(
    container: HTMLElement,
    textMode: 'continue_with' | 'signup_with',
    onCredential: (idToken: string) => void,
    onRendered?: () => void,
    onUnavailable?: (reason: string) => void
  ): void {
    this.getGoogleConfig().subscribe({
      next: async (config) => {
        if (!config.enabled || !config.clientId) {
          onUnavailable?.('Login com Google ainda não configurado no servidor (GOOGLE_CLIENT_ID).');
          return;
        }
        try {
          await this.carregarScriptGoogle();
          const gis = window.google?.accounts?.id;
          if (!gis) {
            onUnavailable?.('Serviço do Google indisponível no momento.');
            return;
          }
          this.activeGoogleCredentialHandler = onCredential;
          gis.initialize({
            client_id: config.clientId,
            callback: (res) => {
              this.ngZone.run(() => {
                if (res?.credential) {
                  this.activeGoogleCredentialHandler?.(res.credential);
                } else {
                  onUnavailable?.('Não foi possível obter a credencial do Google.');
                }
              });
            },
          });
          container.innerHTML = '';
          const measuredWidth = container.clientWidth || container.parentElement?.clientWidth || 0;
          const viewportFallback = typeof window !== 'undefined' ? Math.min(360, window.innerWidth - 88) : 300;
          const width = Math.max(220, Math.min(400, measuredWidth || viewportFallback));
          gis.renderButton(container, {
            type: 'standard',
            theme: 'outline',
            size: 'large',
            text: textMode,
            shape: 'rectangular',
            logo_alignment: 'left',
            locale: 'pt-BR',
            width,
          });
          gis.prompt();
          onRendered?.();
        } catch {
          onUnavailable?.('Não foi possível carregar o script de login do Google.');
        }
      },
      error: () => onUnavailable?.('Erro ao consultar configuração do Google no servidor.'),
    });
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
