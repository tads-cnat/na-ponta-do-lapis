import { Component, Input, signal } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../service/storage.service';
import { Token } from '../../../model/IToken.models';
import { MessageService } from 'primeng/api';
import { MascoteEstado, MascoteLoginComponent } from '../mascote-login/mascote-login.component';

@Component({
  selector: 'app-login',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, RouterLink, MascoteLoginComponent],
  providers: [MessageService],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  @Input() embedded = false;

  loginForm: FormGroup;
  recoveryForm: FormGroup;
  loading = false;
  recoveryLoading = false;
  showRecoveryForm = false;
  readonly mascoteEstado = signal<MascoteEstado>('idle');
  readonly lookProgress = signal(0.5);
  readonly isHoveringSubmit = signal(false);

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router, private messageService: MessageService) {
    this.loginForm = this.fb.group({
      email: [null, [Validators.required, Validators.minLength(3)]],
      password: [null, [Validators.required]]
    });
    this.recoveryForm = this.fb.group({
      email: [null, [Validators.required, Validators.email]]
    });
  }

  public toggleRecoveryForm(): void {
    this.showRecoveryForm = !this.showRecoveryForm;
    if (this.showRecoveryForm) {
      const email = (this.loginForm.get('email')?.value as string) ?? '';
      this.recoveryForm.reset({ email: email.includes('@') ? email.trim() : '' });
    } else {
      this.recoveryForm.reset();
    }
  }

  public recoverPassword(): void {
    if (this.recoveryForm.invalid || this.recoveryLoading) return;

    this.recoveryLoading = true;
    const email = (this.recoveryForm.get('email')?.value as string).trim();
    this.authService.recoverPassword(email).subscribe({
      next: (response) => {
        this.recoveryLoading = false;
        const detail = response.temporaryPassword
          ? `${response.message} Senha temporária: ${response.temporaryPassword}`
          : response.message;
        this.messageService.add({
          severity: 'success',
          summary: 'Recuperação de senha',
          detail,
          life: 7000
        });
        this.showRecoveryForm = false;
        this.recoveryForm.reset();
      },
      error: () => {
        this.recoveryLoading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Não foi possível recuperar a senha',
          detail: 'Verifique o e-mail informado e tente novamente.',
          life: 4000
        });
      }
    });
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

  private finalizarLoginComSucesso(res: Token) {
    this.loading = false;
    this.mascoteEstado.set('success');
    StorageService.salvarToken(res.token);
    const mustChangePassword = StorageService.getUsuarioDecodificado()?.mustChangePassword === true;
    this.messageService.add({
      severity: 'success',
      summary: mustChangePassword ? 'Senha temporária ativada' : 'Login Realizado!',
      detail: mustChangePassword
        ? 'Troque sua senha no perfil para continuar.'
        : 'Bem-vindo ao Na Ponta do Lápis',
      life: mustChangePassword ? 5000 : 2000
    });

    setTimeout(() => {
      this.router.navigateByUrl(mustChangePassword ? 'app/perfil' : 'app/dashboard');
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
