<<<<<<< HEAD
import { AfterViewInit, Component, ElementRef, Input, ViewChild, signal } from '@angular/core';
=======
import { Component, Input } from '@angular/core';
>>>>>>> 6f509eb (feat(frontend): adiciona animacoes na landing page e transicao entre login e cadastro)
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../service/storage.service';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { GoogleConsentDialogComponent } from '../google-consent-dialog/google-consent-dialog.component';

@Component({
  selector: 'app-signup',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, CommonModule, FormsModule, RouterLink, GoogleConsentDialogComponent],
  providers: [MessageService],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css',
})
<<<<<<< HEAD
export class SignupComponent implements AfterViewInit {
  @Input() embedded = false;
  @ViewChild('googleBtnContainer') googleBtnContainer?: ElementRef<HTMLDivElement>;
=======
export class SignupComponent {
  @Input() embedded = false;
>>>>>>> 6f509eb (feat(frontend): adiciona animacoes na landing page e transicao entre login e cadastro)
  registerForm: FormGroup;
  termos: any = null;
  loading = false;
  readonly googleButtonReady = signal(false);
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
      passwordConfirmation: ['', [Validators.required, Validators.minLength(8)]]
    })
  }

  ngAfterViewInit(): void {
    const container = this.googleBtnContainer?.nativeElement;
    if (!container) return;
    this.authService.renderizarBotaoGoogle(
      container,
      'signup_with',
      (idToken) => this.autenticarComGoogleToken(idToken),
      () => this.googleButtonReady.set(true)
    );
  }

  public register() {
    if (this.loading) return;
    this.loading = true;

    this.authService.register(this.registerForm.value).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Registro Realizado com sucesso!',
          life: 2000
        })
        setTimeout(() => {
          this.router.navigate(["/login"]);
        }, 2000);
      },
      error: (error: Error) => {
        this.loading = false;
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao registrar usuário',
          life: 2000
        })
      }
    })
  }

<<<<<<< HEAD
  public cadastrarComGoogle() {
    if (this.loading) return;
    this.loading = true;

    this.authService.iniciarFluxoGoogle(
      (idToken) => this.autenticarComGoogleToken(idToken),
      (reason) => {
        this.loading = false;
        this.messageService.add({
          severity: 'warn',
          summary: 'Google indisponível',
          detail: reason,
          life: 3500
        });
      }
    );
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

=======
>>>>>>> e610144 (style(frontend): implementa Design System NPL e padroniza UI/UX em todas as telas)
  public voltar() {
    this.router.navigateByUrl('');
  }
}

