import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import { StorageService } from '../../service/storage.service';
import { Token } from '../../../model/IToken';

@Component({
  selector: 'app-login',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {

  loginForm:FormGroup;

  constructor(private fb:FormBuilder, private authService:AuthService, private router:Router){
    this.loginForm = this.fb.group({
      email: [null, [Validators.required, Validators.email]],
      password: [null, [Validators.required]]
    })
  }

  public login(){
    this.authService.login(this.loginForm.value).subscribe({
      next: (res: Token) => {
        StorageService.salvarToken(res.token)
        const usuario = StorageService.getUsuarioDecodificado()
        console.log(usuario?.sub, usuario?.email)
        this.router.navigateByUrl("/transacoes")
      },
      error: (erro:Error) => {
        console.error(erro)
      }
    })
  }

}
