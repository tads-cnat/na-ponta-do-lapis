import { AfterViewInit, Component, ElementRef, Input, ViewChild, signal } from '@angular/core';
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
  @Input() embedded = false;
  @ViewChild('googleBtnContainer') googleBtnContainer?: ElementRef<HTMLDivElement>;
  registerForm: FormGroup;
  loading = false;
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

  public register() {
    if (this.loading || this.registerForm.invalid) return;
    this.loading = true;

    const { termos, ...dados } = this.registerForm.value;
    const payload = { ...dados, termosAceitos: !!termos };

    this.authService.register(payload).subscribe({
      next: () => {
        this.loading = false;
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

