import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { Route, Router } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';
import { StorageService } from '../../auth/service/storage.service';
import { TokenPayload } from '../../model/IToken.models';
import { Usuario } from '../../model/IUsuario.models';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-header',
  imports: [PrimeNGModuleModule],
  templateUrl: './header.component.html',
  providers: [MessageService],
  styleUrl: './header.component.css',
})
export class HeaderComponent {

  constructor(private authService:AuthService ,private router:Router, private messageService: MessageService){}

  usuarioLogado:Usuario | null = null;

  ngOnInit():void {
    this.carregarDadosUsuario();
   }

  public carregarDadosUsuario(){
   const usuario:TokenPayload | null = StorageService.getUsuarioDecodificado()
   this.authService.meusDados().subscribe({
    next: (res:Usuario) => {
      this.usuarioLogado = res;
      console.log(res)
    },
    error: (res:Error) => {
        this.messageService.add({
            severity: 'error',
            summary: 'Erro',
            detail: 'Erro ao se conectar ao servidor.',
            life: 3000
          })
    }
   })
  }

  logout(){
    StorageService.logout()
    this.router.navigateByUrl("/login")
  }

  usarFotoPadrao(event:any) {
    event.target.src = 'user_anonimo.png'
  }

  
}
