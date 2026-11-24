import { HttpClient } from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { SocialLogin } from '@capgo/capacitor-social-login';
import { firstValueFrom, Observable, shareReplay } from 'rxjs';
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
  private googleConfig$: Observable<GoogleConfigResponse> | null = null;
  private googleScriptPromise: Promise<void> | null = null;
  private googleNativeInitPromise: Promise<void> | null = null;
  private activeGoogleCredentialHandler: ((idToken: string) => void) | null = null;

  constructor(private http:HttpClient, private ngZone: NgZone){}

  public login(loginRequest:LoginRequest): Observable<Token>{
    return this.http.post<Token>(`${this.BASE_URL}/auth/login`, loginRequest)
  }

  public getGoogleConfig(): Observable<GoogleConfigResponse> {
    if (!this.googleConfig$) {
      this.googleConfig$ = this.http
        .get<GoogleConfigResponse>(`${this.BASE_URL}/auth/google/config`)
        .pipe(shareReplay({ bufferSize: 1, refCount: false }));
    }
    return this.googleConfig$;
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

  public concluirOnboarding(): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.BASE_URL}/usuario/me`, { onboardingConcluido: true });
  }

  public renderizarBotaoGoogle(
    container: HTMLElement,
    textMode: 'continue_with' | 'signup_with',
    onCredential: (idToken: string) => void,
    onRendered?: () => void,
    onUnavailable?: (reason: string) => void
  ): void {
    if (Capacitor.isNativePlatform()) {
      const initPromise = firstValueFrom(this.getGoogleConfig()).then(async (config) => {
        if (!config.enabled || !config.clientId) {
          throw new Error('Login com Google ainda não configurado no servidor (GOOGLE_CLIENT_ID).');
        }
        await this.inicializarGoogleNativo(config.clientId);
      });
      this.renderizarBotaoGoogleNativo(container, textMode, onCredential, onUnavailable, initPromise);
      onRendered?.();
      initPromise.catch((err) => {
        console.error('[Google Native Init]', err);
      });
      return;
    }

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

  private inicializarGoogleNativo(webClientId: string): Promise<void> {
    if (!this.googleNativeInitPromise) {
      this.googleNativeInitPromise = SocialLogin.initialize({
        google: { webClientId },
      }).catch((err) => {
        this.googleNativeInitPromise = null;
        throw err;
      });
    }
    return this.googleNativeInitPromise;
  }

  private renderizarBotaoGoogleNativo(
    container: HTMLElement,
    textMode: 'continue_with' | 'signup_with',
    onCredential: (idToken: string) => void,
    onUnavailable?: (reason: string) => void,
    initPromise?: Promise<void>
  ): void {
    const texto = textMode === 'signup_with' ? 'Inscrever-se com o Google' : 'Continuar com o Google';
    container.innerHTML = '';

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className =
      'w-full min-h-11 px-4 py-2.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 active:bg-slate-100 text-slate-800 font-semibold text-sm flex items-center justify-center gap-3 shadow-2xs transition-colors cursor-pointer';
    btn.innerHTML = `
      <svg class="w-5 h-5 shrink-0" viewBox="0 0 24 24" aria-hidden="true">
        <path fill="#4285F4" d="M23.745 12.27c0-.7-.06-1.4-.19-2.07H12v4.51h6.6c-.29 1.52-1.14 2.82-2.4 3.68v3.05h3.88c2.27-2.09 3.665-5.17 3.665-9.17Z"/>
        <path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.88-3.05c-1.08.72-2.45 1.16-4.05 1.16-3.12 0-5.77-2.11-6.72-4.96H1.29v3.14C3.26 21.3 7.31 24 12 24Z"/>
        <path fill="#FBBC05" d="M5.28 14.24c-.24-.72-.38-1.49-.38-2.24s.14-1.52.38-2.24V6.62H1.29C.47 8.24 0 10.06 0 12s.47 3.76 1.29 5.38l3.99-3.14Z"/>
        <path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.26 2.7 1.29 6.62l3.99 3.14c.95-2.85 3.6-4.96 6.72-4.96Z"/>
      </svg>
      <span>${texto}</span>
    `;

    btn.addEventListener('click', async () => {
      if (btn.disabled) return;
      btn.disabled = true;
      try {
        if (initPromise) {
          await initPromise;
        }
        const res = await SocialLogin.login({
          provider: 'google',
          options: { scopes: ['email', 'profile'] },
        });
        const idToken = res.result.responseType === 'online' ? res.result.idToken : null;
        this.ngZone.run(() => {
          if (idToken) {
            onCredential(idToken);
          } else {
            onUnavailable?.('Não foi possível obter a credencial do Google.');
          }
        });
      } catch (err) {
        console.error('[Google Native Login]', err);
        const detalhe =
          err instanceof Error
            ? err.message
            : typeof err === 'object' && err !== null && 'message' in err
              ? String((err as { message: unknown }).message)
              : '';
        this.ngZone.run(() => {
          onUnavailable?.(
            detalhe
              ? `Erro no login com Google: ${detalhe}`
              : 'Login com Google cancelado ou indisponível no dispositivo.'
          );
        });
      } finally {
        btn.disabled = false;
      }
    });

    container.appendChild(btn);
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
