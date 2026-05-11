import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../service/storage.service';
import { Token } from '../../../model/IToken.models';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-login',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, RouterLink],
  providers: [MessageService],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {

  loginForm:FormGroup;

  constructor(private fb:FormBuilder, private authService:AuthService, private router:Router, private messageService: MessageService){
    this.loginForm = this.fb.group({
      email: [null, [Validators.required, Validators.email]],
      password: [null, [Validators.required]]
    })
  }

  public login(){
    const loginSucesso = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: (res: Token) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Login Realizado!',
            detail: 'Bem-vindo ao Na Ponta do Lápis',
            life:2000
          })
        

        StorageService.salvarToken(res.token)
        const usuario = StorageService.getUsuarioDecodificado()
        console.log(usuario?.sub, usuario?.email)
        setTimeout(() => {
          this.router.navigateByUrl("app/transacoes")
        }, 1200)
      },
      error: (erro:Error) => {
        console.error(erro)
         this.messageService.add({
            severity: 'error',
            summary: 'Erro',
            detail: 'Email ou senha inválido.',
            life: 3000
          })
      }
    })
  }

}
