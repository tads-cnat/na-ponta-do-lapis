import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { AuthService } from '../../service/auth.service';
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
  registerForm: FormGroup;
  termos: any = null;
  loading = false;

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
}

