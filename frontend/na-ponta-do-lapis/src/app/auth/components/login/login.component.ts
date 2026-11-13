<<<<<<< HEAD
<<<<<<< HEAD
import { AfterViewInit, Component, ElementRef, Input, ViewChild, signal } from '@angular/core';
=======
import { Component, Input } from '@angular/core';
>>>>>>> 6f509eb (feat(frontend): adiciona animacoes na landing page e transicao entre login e cadastro)
=======
import { Component, Input, signal } from '@angular/core';
>>>>>>> f5b5148 (feat(frontend): integra login por nickname, Google Auth, mascote e ajustes de layout no auth)
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../service/storage.service';
import { Token } from '../../../model/IToken.models';
import { MessageService } from 'primeng/api';
import { MascoteEstado, MascoteLoginComponent } from '../mascote-login/mascote-login.component';
<<<<<<< HEAD
import { GoogleConsentDialogComponent } from '../google-consent-dialog/google-consent-dialog.component';

@Component({
  selector: 'app-login',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, RouterLink, MascoteLoginComponent, GoogleConsentDialogComponent],
=======

@Component({
  selector: 'app-login',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, RouterLink, MascoteLoginComponent],
>>>>>>> f5b5148 (feat(frontend): integra login por nickname, Google Auth, mascote e ajustes de layout no auth)
  providers: [MessageService],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
<<<<<<< HEAD
export class LoginComponent implements AfterViewInit {
  @Input() embedded = false;
  @ViewChild('googleBtnContainer') googleBtnContainer?: ElementRef<HTMLDivElement>;
=======
export class LoginComponent {
  @Input() embedded = false;
>>>>>>> 6f509eb (feat(frontend): adiciona animacoes na landing page e transicao entre login e cadastro)

  loginForm: FormGroup;
  loading = false;
<<<<<<< HEAD
  readonly googleButtonReady = signal(false);
  readonly googleConsentVisible = signal(false);
  readonly googleConsentDados = signal<{ idToken: string; nome: string; email: string; fotoUrl: string | null } | null>(null);
=======
>>>>>>> f5b5148 (feat(frontend): integra login por nickname, Google Auth, mascote e ajustes de layout no auth)
  readonly mascoteEstado = signal<MascoteEstado>('idle');
  readonly lookProgress = signal(0.5);
  readonly isHoveringSubmit = signal(false);

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router, private messageService: MessageService) {
    this.loginForm = this.fb.group({
      email: [null, [Validators.required, Validators.minLength(3)]],
      password: [null, [Validators.required]]
    })
  }

  ngAfterViewInit(): void {
    const container = this.googleBtnContainer?.nativeElement;
    if (!container) return;
    this.authService.renderizarBotaoGoogle(
      container,
      'continue_with',
      (idToken) => this.autenticarComGoogleToken(idToken),
      () => this.googleButtonReady.set(true)
    );
  }

  public login() {
    if (this.loading) return;
    this.loading = true;
    this.mascoteEstado.set('loading');

    const payload = {
      ...this.loginForm.value,
      email: ((this.loginForm.value.email as string) ?? '').trim(),
    };

    this.authService.login(payload).subscribe({
      next: (res: Token) => this.finalizarLoginComSucesso(res),
      error: (erro: Error) => {
        this.loading = false;
        this.mascoteEstado.set('error');
        setTimeout(() => {
          if (this.mascoteEstado() === 'error') {
            this.mascoteEstado.set('idle');
          }
        }, 1500);
        console.error(erro)
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'E-mail, nickname ou senha inválido.',
          life: 3000
        })
      }
    })
  }

  public entrarComGoogle() {
    if (this.loading) return;
    this.loading = true;
    this.mascoteEstado.set('loading');

    this.authService.iniciarFluxoGoogle(
<<<<<<< HEAD
      (idToken) => this.autenticarComGoogleToken(idToken),
=======
      (idToken) => {
        this.authService.loginWithGoogle(idToken).subscribe({
          next: (res) => this.finalizarLoginComSucesso(res),
          error: () => {
            this.loading = false;
            this.mascoteEstado.set('error');
            this.messageService.add({
              severity: 'error',
              summary: 'Erro no Google',
              detail: 'Não foi possível autenticar com sua conta Google.',
              life: 3000
            });
          }
        });
      },
>>>>>>> f5b5148 (feat(frontend): integra login por nickname, Google Auth, mascote e ajustes de layout no auth)
      (reason) => {
        this.loading = false;
        this.mascoteEstado.set('idle');
        this.messageService.add({
          severity: 'warn',
          summary: 'Google indisponível',
          detail: reason,
          life: 3500
        });
      }
    );
  }

<<<<<<< HEAD
  private autenticarComGoogleToken(idToken: string, termosAceitos = false) {
    this.loading = true;
    this.mascoteEstado.set('loading');
    this.authService.loginWithGoogle(idToken, termosAceitos).subscribe({
      next: (res) => {
        if (res.requerTermos) {
          this.loading = false;
          this.mascoteEstado.set('idle');
          this.googleConsentDados.set({
            idToken,
            nome: res.nome ?? '',
            email: res.email ?? '',
            fotoUrl: res.fotoUrl ?? null,
          });
          this.googleConsentVisible.set(true);
          return;
        }
        this.googleConsentVisible.set(false);
        this.googleConsentDados.set(null);
        if (res.token) {
          this.finalizarLoginComSucesso({ token: res.token });
        }
      },
      error: () => {
        this.loading = false;
        this.mascoteEstado.set('error');
        this.messageService.add({
          severity: 'error',
          summary: 'Erro no Google',
          detail: 'Não foi possível autenticar com sua conta Google.',
          life: 3000
        });
      }
    });
  }

  public confirmarTermosGoogle() {
    const dados = this.googleConsentDados();
    if (!dados || this.loading) return;
    this.autenticarComGoogleToken(dados.idToken, true);
  }

  public cancelarTermosGoogle() {
    this.googleConsentVisible.set(false);
    this.googleConsentDados.set(null);
    this.loading = false;
    this.mascoteEstado.set('idle');
  }

=======
>>>>>>> f5b5148 (feat(frontend): integra login por nickname, Google Auth, mascote e ajustes de layout no auth)
  private finalizarLoginComSucesso(res: Token) {
    this.loading = false;
    this.mascoteEstado.set('success');
    this.messageService.add({
      severity: 'success',
      summary: 'Login Realizado!',
      detail: 'Bem-vindo ao Na Ponta do Lápis',
      life: 2000
    });

    StorageService.salvarToken(res.token);
    setTimeout(() => {
      this.router.navigateByUrl('app/dashboard');
    }, 1200);
  }

  public onEmailFocus() {
    this.mascoteEstado.set('checking');
    const current = (this.loginForm.get('email')?.value as string) ?? '';
    this.lookProgress.set(Math.min(1, current.length / 28));
  }

  public onEmailInput(value: string) {
    this.mascoteEstado.set('checking');
    this.lookProgress.set(Math.min(1, value.length / 28));
  }

  public onEmailBlur() {
    if (this.mascoteEstado() === 'checking') {
      this.mascoteEstado.set('idle');
    }
  }

  public onPasswordFocus(container: HTMLElement) {
    const input = container.querySelector('input');
    this.mascoteEstado.set(input?.type === 'text' ? 'peeking' : 'hands-up');
  }

  public onPasswordBlur(event: FocusEvent, container: HTMLElement) {
    const nextTarget = event.relatedTarget as Node | null;
    if (nextTarget && container.contains(nextTarget)) {
      return;
    }
    const estado = this.mascoteEstado();
    if (estado === 'hands-up' || estado === 'peeking') {
      this.mascoteEstado.set('idle');
    }
  }

  public onPasswordAreaClick(container: HTMLElement) {
    setTimeout(() => {
      if (!container.contains(document.activeElement)) {
        return;
      }
      const input = container.querySelector('input');
      this.mascoteEstado.set(input?.type === 'text' ? 'peeking' : 'hands-up');
    }, 0);
  }

  public voltar(){
    this.router.navigateByUrl("")
  }

}
