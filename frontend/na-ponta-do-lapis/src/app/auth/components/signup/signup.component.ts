import { AfterViewInit, Component, DestroyRef, ElementRef, Input, ViewChild, inject, signal } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../service/storage.service';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { GoogleConsentDialogComponent } from '../google-consent-dialog/google-consent-dialog.component';

function senhasIguaisValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmation = group.get('passwordConfirmation')?.value;
  if (!password || !confirmation) return null;
  return password === confirmation ? null : { senhasDiferentes: true };
}

@Component({
  selector: 'app-signup',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, CommonModule, RouterLink, GoogleConsentDialogComponent],
  providers: [MessageService],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css',
})
export class SignupComponent implements AfterViewInit {
  private readonly destroyRef = inject(DestroyRef);
  @Input() embedded = false;
  @ViewChild('googleBtnContainer') googleBtnContainer?: ElementRef<HTMLDivElement>;
  registerForm: FormGroup;
  loading = false;
  readonly verificationPending = signal(false);
  readonly registeredEmail = signal('');
  readonly resendLoading = signal(false);
  readonly resendCooldownSeconds = signal(0);
  private resendAttempts = 0;
  private resendTimerId: ReturnType<typeof setInterval> | null = null;
  readonly googleButtonState = signal<'loading' | 'ready' | 'unavailable'>('loading');
  readonly googleConsentVisible = signal(false);
  readonly googleConsentDados = signal<{ idToken: string; nome: string; email: string; fotoUrl: string | null } | null>(null);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router) {
    this.registerForm = fb.group({
      nomeCompleto: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(200)]],
      telefone: ['', [Validators.maxLength(20)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      passwordConfirmation: ['', [Validators.required, Validators.minLength(8)]],
      termos: [false, [Validators.requiredTrue]]
    }, { validators: senhasIguaisValidator });
    this.destroyRef.onDestroy(() => this.limparTimerReenvio());
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

  public get botaoPrincipalLabel(): string {
    return 'Criar Conta';
  }

  public register() {
    if (this.loading) return;

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    const { termos, ...dados } = this.registerForm.value;
    const payload = { ...dados, termosAceitos: !!termos };

    this.authService.register(payload).subscribe({
      next: (res) => {
        this.loading = false;
        if (res?.requiresEmailVerification) {
          this.registeredEmail.set(((dados.email as string) ?? '').trim());
          this.verificationPending.set(true);
          this.resendAttempts = 1;
          this.iniciarTimerReenvio(60);
          return;
        }
        this.messageService.add({
          severity: 'success',
          summary: 'Registro realizado com sucesso!',
          life: 2000
        });
        setTimeout(() => {
          this.router.navigate(["/login"]);
        }, 2000);
      },
      error: (error: { error?: { message?: string; detail?: string }; message?: string }) => {
        this.loading = false;
        const detail = error?.error?.message || error?.error?.detail || error?.message || 'Não foi possível concluir o cadastro.';
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao registrar usuário',
          detail,
          life: 3500
        });
      }
    });
  }

  public reenviarConfirmacaoEmail(): void {
    const email = this.registeredEmail();
    if (!email || this.resendLoading() || this.resendCooldownSeconds() > 0) {
      return;
    }
    this.resendLoading.set(true);
    this.authService.resendVerificationEmail(email).subscribe({
      next: (res) => {
        this.resendLoading.set(false);
        this.resendAttempts += 1;
        const steps = [60, 120, 300];
        const wait = steps[Math.min(this.resendAttempts - 1, steps.length - 1)];
        this.iniciarTimerReenvio(wait);
        this.messageService.add({
          severity: 'success',
          summary: 'E-mail reenviado',
          detail: res.message,
          life: 4000
        });
      },
      error: (err) => {
        this.resendLoading.set(false);
        this.messageService.add({
          severity: 'warn',
          summary: 'Atenção',
          detail: err?.error?.message || 'Não foi possível reenviar o e-mail agora.',
          life: 4000
        });
      }
    });
  }

  public irParaLogin(): void {
    this.limparTimerReenvio();
    this.verificationPending.set(false);
    this.router.navigate(['/login']);
  }

  private iniciarTimerReenvio(segundos: number): void {
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

  private autenticarComGoogleToken(idToken: string, termosAceitos = false) {
    this.loading = true;
    this.authService.loginWithGoogle(idToken, termosAceitos).subscribe({
      next: (res) => {
        if (res.requerTermos) {
          this.loading = false;
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
        this.loading = false;
        if (res.token) {
          StorageService.salvarToken(res.token);
          this.messageService.add({
            severity: 'success',
            summary: 'Conta conectada com Google!',
            life: 2000
          });
          setTimeout(() => {
            this.router.navigateByUrl('app/dashboard');
          }, 1000);
        }
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao cadastrar com Google',
          life: 2500
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
  }

  public voltar() {
    this.router.navigateByUrl('');
  }
}

