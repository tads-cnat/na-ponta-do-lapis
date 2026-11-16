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
  @Input() initialRecovery = false;

  loginForm: FormGroup;
  recoveryForm: FormGroup;
  loading = false;
  readonly recoveryLoading = signal(false);
  readonly recoveryQuestionLoading = signal(false);
  readonly showRecoveryForm = signal(false);
  readonly recoveryQuestion = signal<string | null>(null);
  readonly recoveryStep = signal<'answer' | 'new-password' | 'complete'>('answer');
  readonly mascoteEstado = signal<MascoteEstado>('idle');
  readonly lookProgress = signal(0.5);
  readonly isHoveringSubmit = signal(false);

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router, private messageService: MessageService) {
    this.loginForm = this.fb.group({
      email: [null, [Validators.required, Validators.minLength(3)]],
      password: [null, [Validators.required]]
    });
    this.recoveryForm = this.fb.group({
      email: [null, [Validators.required, Validators.email]],
      answer: [''],
      newPassword: [''],
      confirmNewPassword: ['']
    });
  }

  ngOnInit(): void {
    if (this.initialRecovery) {
      this.showRecoveryForm.set(true);
      this.recoveryStep.set('answer');
      this.recoveryQuestion.set(null);
      this.recoveryForm.reset({ email: '', answer: '', newPassword: '', confirmNewPassword: '' });
    }
  }

  public toggleRecoveryForm(): void {
    this.showRecoveryForm.update(show => !show);
    this.recoveryQuestion.set(null);
    this.recoveryStep.set('answer');
    if (this.showRecoveryForm()) {
      const email = (this.loginForm.get('email')?.value as string) ?? '';
      this.recoveryForm.reset({
        email: email.includes('@') ? email.trim() : '',
        answer: '',
        newPassword: '',
        confirmNewPassword: ''
      });
    } else {
      this.recoveryForm.reset();
    }
  }

  public buscarPerguntaSeguranca(): void {
    const emailControl = this.recoveryForm.get('email');
    if (!emailControl || emailControl.invalid || this.recoveryQuestionLoading()) {
      emailControl?.markAsTouched();
      return;
    }

    this.recoveryQuestionLoading.set(true);
    const email = (emailControl.value as string).trim();
    this.authService.getRecoveryQuestion(email).subscribe({
      next: (response) => {
        this.recoveryQuestionLoading.set(false);
        if (!response.question) {
          this.messageService.add({
            severity: 'error',
            summary: 'Não foi possível iniciar a recuperação',
            detail: 'Confira o e-mail ou configure uma pergunta de segurança no perfil.',
            life: 5000
          });
          return;
        }
        this.recoveryQuestion.set(response.question);
      },
      error: () => {
        this.recoveryQuestionLoading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Não foi possível buscar a pergunta',
          detail: 'Tente novamente mais tarde.',
          life: 4000
        });
      }
    });
  }

  public confirmarRespostaSeguranca(): void {
    const email = (this.recoveryForm.get('email')?.value as string ?? '').trim();
    const answer = (this.recoveryForm.get('answer')?.value as string ?? '').trim();
    if (!this.recoveryQuestion() || !email || !answer || this.recoveryLoading()) return;

    this.recoveryLoading.set(true);
    this.authService.verifyRecoveryAnswer(email, answer).subscribe({
      next: (response) => {
        this.recoveryLoading.set(false);
        if (!response.verified) {
          this.messageService.add({
            severity: 'error',
            summary: 'Resposta incorreta',
            detail: 'Confira a resposta e tente novamente.',
            life: 4000
          });
          return;
        }
        this.recoveryStep.set('new-password');
        this.recoveryForm.patchValue({ newPassword: '', confirmNewPassword: '' });
      },
      error: () => {
        this.recoveryLoading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Não foi possível confirmar a resposta',
          detail: 'Tente novamente.',
          life: 4000
        });
      }
    });
  }

  public salvarNovaSenhaRecuperada(): void {
    const newPassword = (this.recoveryForm.get('newPassword')?.value as string ?? '').trim();
    const confirmNewPassword = (this.recoveryForm.get('confirmNewPassword')?.value as string ?? '').trim();
    if (newPassword.length < 6 || newPassword !== confirmNewPassword || this.recoveryLoading()) return;

    this.recoveryLoading.set(true);
    this.authService.changeRecoveredPassword(newPassword).subscribe({
      next: (response) => {
        this.recoveryLoading.set(false);
        this.recoveryStep.set('complete');
        this.messageService.add({
          severity: 'success',
          summary: 'Senha alterada',
          detail: response.message,
          life: 5000
        });
      },
      error: () => {
        this.recoveryLoading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Sessão de recuperação expirada',
          detail: 'Inicie a recuperação novamente.',
          life: 5000
        });
        this.recoveryStep.set('answer');
        this.recoveryQuestion.set(null);
      }
    });
  }

  public voltarParaLoginAposRecuperacao(): void {
    const email = (this.recoveryForm.get('email')?.value as string ?? '').trim();
    this.showRecoveryForm.set(false);
    this.recoveryQuestion.set(null);
    this.recoveryStep.set('answer');
    this.recoveryForm.reset();
    if (email) this.loginForm.patchValue({ email, password: '' });
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
