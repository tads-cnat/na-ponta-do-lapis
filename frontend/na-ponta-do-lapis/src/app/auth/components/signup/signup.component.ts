import { Component, Input } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../service/storage.service';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-signup',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, CommonModule, FormsModule, RouterLink],
  providers: [MessageService],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css',
})
export class SignupComponent {
  @Input() embedded = false;
  registerForm: FormGroup;
  termos: any = null;
  loading = false;
  showSecurityFields = false;

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
      perguntaSeguranca: ['', [Validators.maxLength(200)]],
      respostaSeguranca: ['', [Validators.maxLength(200)]]
    })
  }

  public get botaoPrincipalLabel(): string {
    return this.showSecurityFields ? 'Finalizar cadastro' : 'Criar Conta';
  }

  public register() {
    if (this.loading) return;

    if (!this.showSecurityFields) {
      const formValido = this.registerForm.get('nomeCompleto')?.valid
        && this.registerForm.get('email')?.valid
        && this.registerForm.get('username')?.valid
        && this.registerForm.get('password')?.valid
        && this.registerForm.get('passwordConfirmation')?.valid
        && !!this.termos;

      if (!formValido) {
        this.registerForm.markAllAsTouched();
        return;
      }

      this.showSecurityFields = true;
      return;
    }

    const pergunta = (this.registerForm.get('perguntaSeguranca')?.value ?? '').trim();
    const resposta = (this.registerForm.get('respostaSeguranca')?.value ?? '').trim();

    if (!pergunta || !resposta) {
      this.registerForm.get('perguntaSeguranca')?.markAsTouched();
      this.registerForm.get('respostaSeguranca')?.markAsTouched();
      this.messageService.add({
        severity: 'warn',
        summary: 'Pergunta e resposta obrigatórias',
        detail: 'Defina uma pergunta e a resposta para recuperar sua conta.',
        life: 3000
      });
      return;
    }

    this.loading = true;

    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Registro Realizado com sucesso!',
          life: 2000
        });
        setTimeout(() => {
          this.router.navigate(["/login"]);
        }, 2000);
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao registrar usuário',
          life: 2000
        });
      }
    });
  }

  public cadastrarComGoogle() {
    if (this.loading) return;
    this.loading = true;

    this.authService.iniciarFluxoGoogle(
      (idToken) => {
        this.authService.loginWithGoogle(idToken).subscribe({
          next: (res) => {
            this.loading = false;
            StorageService.salvarToken(res.token);
            this.messageService.add({
              severity: 'success',
              summary: 'Conta conectada com Google!',
              life: 2000
            });
            setTimeout(() => {
              this.router.navigateByUrl('app/dashboard');
            }, 1000);
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
      },
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

  public voltar() {
    this.router.navigateByUrl('');
  }
}

