import { AfterViewInit, Component, DestroyRef, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../service/storage.service';
import { Token } from '../../../model/IToken.models';
import { MessageService } from 'primeng/api';
import { MascoteEstado, MascoteLoginComponent } from '../mascote-login/mascote-login.component';
import { GoogleConsentDialogComponent } from '../google-consent-dialog/google-consent-dialog.component';

@Component({
  selector: 'app-login',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, RouterLink, MascoteLoginComponent, GoogleConsentDialogComponent],
  providers: [MessageService],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit, OnChanges, AfterViewInit {
  private readonly destroyRef = inject(DestroyRef);
  @Input() embedded = false;
  @Input() initialRecovery = false;
  @ViewChild('googleBtnContainer') googleBtnContainer?: ElementRef<HTMLDivElement>;

  loginForm: FormGroup;
  recoveryForm: FormGroup;
  loading = false;
  readonly recoveryLoading = signal(false);
  readonly showRecoveryForm = signal(false);
  readonly recoveryEmailSent = signal(false);
  readonly resendCooldownSeconds = signal(0);
  private resendAttempts = 0;
  private resendTimerId: ReturnType<typeof setInterval> | null = null;
  readonly recoveryToken = signal<string | null>(null);
  readonly recoveryStep = signal<'email' | 'new-password' | 'complete'>('email');
  readonly emailVerificationState = signal<'none' | 'verifying' | 'success' | 'error'>('none');
  readonly emailVerificationMessage = signal('');
  readonly unverifiedEmail = signal<string | null>(null);
  readonly resendVerificationLoading = signal(false);
  readonly googleButtonState = signal<'loading' | 'ready' | 'unavailable'>('loading');
  readonly googleConsentVisible = signal(false);
  readonly googleConsentDados = signal<{ idToken: string; nome: string; email: string; fotoUrl: string | null } | null>(null);
  readonly mascoteEstado = signal<MascoteEstado>('idle');
  readonly lookProgress = signal(0.5);
  readonly isHoveringSubmit = signal(false);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService
  ) {
    this.loginForm = this.fb.group({
      email: [null, [Validators.required, Validators.minLength(3)]],
      password: [null, [Validators.required]]
    });
    this.recoveryForm = this.fb.group({
      email: [null, [Validators.required, Validators.email]],
      newPassword: [''],
      confirmNewPassword: ['']
    });
  }

  ngOnInit(): void {
    this.destroyRef.onDestroy(() => this.limparTimerReenvio());
    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        const tokenParam = params.get('token')?.trim();
        if (this.router.url.startsWith('/confirmar-email')) {
          this.showRecoveryForm.set(true);
          this.executarConfirmacaoEmail(tokenParam ?? null);
          return;
        }
        this.emailVerificationState.set('none');
        if (tokenParam) {
          this.recoveryToken.set(tokenParam);
          this.showRecoveryForm.set(true);
          this.recoveryStep.set('new-password');
          this.recoveryEmailSent.set(false);
          this.recoveryForm.reset({ email: '', newPassword: '', confirmNewPassword: '' });
          return;
        }

        if (this.initialRecovery) {
          this.entrarModoRecuperacao();
        } else {
          this.showRecoveryForm.set(false);
        }
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialRecovery'] && !changes['initialRecovery'].firstChange) {
      const tokenParam = this.route.snapshot.queryParamMap.get('token')?.trim();
      if (tokenParam) {
        return;
      }
      if (this.initialRecovery) {
        this.entrarModoRecuperacao();
      } else {
        this.resetarEstadoRecuperacao();
      }
    }
  }

  private entrarModoRecuperacao(): void {
    const email = (this.loginForm.get('email')?.value as string) ?? '';
    this.showRecoveryForm.set(true);
    this.recoveryEmailSent.set(false);
    this.recoveryToken.set(null);
    this.recoveryStep.set('email');
    this.recoveryForm.reset({
      email: email.includes('@') ? email.trim() : '',
      newPassword: '',
      confirmNewPassword: ''
    });
  }

  private resetarEstadoRecuperacao(): void {
    this.limparTimerReenvio();
    this.resendAttempts = 0;
    this.showRecoveryForm.set(false);
    this.recoveryEmailSent.set(false);
    this.recoveryToken.set(null);
    this.recoveryStep.set('email');
    this.emailVerificationState.set('none');
    this.emailVerificationMessage.set('');
    this.unverifiedEmail.set(null);
    this.recoveryForm.reset();
  }

  private executarConfirmacaoEmail(token: string | null): void {
    if (!token) {
      this.emailVerificationState.set('error');
      this.emailVerificationMessage.set('Link de confirmação inválido ou incompleto.');
      return;
    }
    this.emailVerificationState.set('verifying');
    this.authService.verifyEmail(token).subscribe({
      next: (res) => {
        this.emailVerificationState.set('success');
        this.emailVerificationMessage.set(res.message);
      },
      error: (err) => {
        this.emailVerificationState.set('error');
        this.emailVerificationMessage.set(err?.error?.message || 'Link de confirmação inválido ou expirado.');
      }
    });
  }

  private iniciarTimerReenvio(segundos = 60): void {
    this.limparTimerReenvio();
    this.resendCooldownSeconds.set(segundos);
    this.resendTimerId = setInterval(() => {
      const restante = this.resendCooldownSeconds() - 1;
      if (restante <= 0) {
        this.limparTimerReenvio();
      } else {
        this.resendCooldownSeconds.set(restante);
      }
    }, 1000);
  }

  private limparTimerReenvio(): void {
    if (this.resendTimerId !== null) {
      clearInterval(this.resendTimerId);
      this.resendTimerId = null;
    }
    this.resendCooldownSeconds.set(0);
  }

  public abrirRecuperacao(): void {
    this.entrarModoRecuperacao();
    if (!this.router.url.startsWith('/recuperar-senha')) {
      this.router.navigate(['/recuperar-senha']);
    }
  }

  public usarOutroEmailRecuperacao(): void {
    this.limparTimerReenvio();
    this.recoveryEmailSent.set(false);
    this.recoveryForm.get('email')?.markAsUntouched();
  }

  public enviarEmailRecuperacao(): void {
    const emailControl = this.recoveryForm.get('email');
    if (!emailControl || emailControl.invalid || this.recoveryLoading() || this.resendCooldownSeconds() > 0) {
      emailControl?.markAsTouched();
      return;
    }
    const email = (emailControl.value as string).trim();
    this.recoveryLoading.set(true);
    this.authService.requestPasswordRecoveryEmail(email).subscribe({
      next: (response) => {
        this.recoveryLoading.set(false);
        this.recoveryEmailSent.set(true);
        this.resendAttempts += 1;
        const steps = [60, 120, 300];
        this.iniciarTimerReenvio(steps[Math.min(this.resendAttempts - 1, steps.length - 1)]);
        this.messageService.add({
          severity: 'success',
          summary: 'E-mail enviado',
          detail: response.message,
          life: 5000,
        });
      },
      error: (err) => {
        this.recoveryLoading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao enviar e-mail',
          detail: err?.error?.message || 'Não foi possível enviar o link de recuperação. Tente novamente em instantes.',
          life: 4000,
        });
      },
    });
  }

  public salvarNovaSenhaRecuperada(): void {
    const newPassword = (this.recoveryForm.get('newPassword')?.value as string ?? '').trim();
    const confirmNewPassword = (this.recoveryForm.get('confirmNewPassword')?.value as string ?? '').trim();
    const token = this.recoveryToken();
    if (!token || newPassword.length < 6 || newPassword !== confirmNewPassword || this.recoveryLoading()) return;

    this.recoveryLoading.set(true);
    this.authService.resetPasswordWithToken(token, newPassword).subscribe({
      next: (response) => {
        this.recoveryLoading.set(false);
        this.recoveryStep.set('complete');
        this.recoveryToken.set(null);
        this.messageService.add({
          severity: 'success',
          summary: 'Senha alterada',
          detail: response.message,
          life: 5000
        });
      },
      error: (err) => {
        this.recoveryLoading.set(false);
        const detail = err?.error?.message || 'Inicie a recuperação novamente.';
        this.messageService.add({
          severity: 'error',
          summary: 'Link inválido ou expirado',
          detail,
          life: 5000
        });
        this.recoveryToken.set(null);
        this.recoveryStep.set('email');
        this.recoveryEmailSent.set(false);
      }
    });
  }

  public voltarParaLoginAposRecuperacao(): void {
    const email = (this.recoveryForm.get('email')?.value as string ?? '').trim();
    this.resetarEstadoRecuperacao();
    if (email) this.loginForm.patchValue({ email, password: '' });
    if (this.router.url.includes('/redefinir-senha') || this.router.url.includes('/recuperar-senha') || this.router.url.includes('/confirmar-email')) {
      this.router.navigate(['/login']);
    }
  }

  ngAfterViewInit(): void {
    const container = this.googleBtnContainer?.nativeElement;
    if (!container) return;
    this.authService.renderizarBotaoGoogle(
      container,
      'continue_with',
      (idToken) => this.autenticarComGoogleToken(idToken),
      () => this.googleButtonState.set('ready'),
      () => this.googleButtonState.set('unavailable')
    );
  }

  public login() {
    if (this.loading) return;
    this.loading = true;
    this.unverifiedEmail.set(null);
    this.mascoteEstado.set('loading');

    const payload = {
      ...this.loginForm.value,
      email: ((this.loginForm.value.email as string) ?? '').trim(),
    };

    this.authService.login(payload).subscribe({
      next: (res: Token) => this.finalizarLoginComSucesso(res),
      error: (erro: { status?: number; error?: { emailNaoVerificado?: boolean; email?: string; message?: string } }) => {
        this.loading = false;
        this.mascoteEstado.set('error');
        setTimeout(() => {
          if (this.mascoteEstado() === 'error') {
            this.mascoteEstado.set('idle');
          }
        }, 1500);
        if (erro?.status === 403 && erro?.error?.emailNaoVerificado) {
          this.unverifiedEmail.set(erro.error.email || payload.email);
          this.showRecoveryForm.set(true);
          return;
        }
        console.error(erro)
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'E-mail, nome de usuário ou senha inválido.',
          life: 3000
        })
      }
    })
  }

  public reenviarConfirmacaoNoLogin(): void {
    const email = this.unverifiedEmail();
    if (!email || this.resendVerificationLoading() || this.resendCooldownSeconds() > 0) {
      return;
    }
    this.resendVerificationLoading.set(true);
    this.authService.resendVerificationEmail(email).subscribe({
      next: (res) => {
        this.resendVerificationLoading.set(false);
        this.resendAttempts += 1;
        const steps = [60, 120, 300];
        this.iniciarTimerReenvio(steps[Math.min(this.resendAttempts - 1, steps.length - 1)]);
        this.messageService.add({
          severity: 'success',
          summary: 'E-mail enviado',
          detail: res.message,
          life: 4500
        });
      },
      error: (err) => {
        this.resendVerificationLoading.set(false);
        this.messageService.add({
          severity: 'warn',
          summary: 'Atenção',
          detail: err?.error?.message || 'Não foi possível reenviar o e-mail agora.',
          life: 4000
        });
      }
    });
  }

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

  private finalizarLoginComSucesso(res: Token) {
    this.loading = false;
    this.mascoteEstado.set('success');
    StorageService.salvarToken(res.token);
    this.messageService.add({
      severity: 'success',
      summary: 'Login Realizado!',
      detail: 'Bem-vindo ao Na Ponta do Lápis',
      life: 2000
    });

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
