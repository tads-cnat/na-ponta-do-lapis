import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { Route, Router } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';
import { StorageService } from '../../auth/service/storage.service';
import { TokenPayload } from '../../model/IToken.models';
import { Usuario } from '../../model/IUsuario.models';
import { MessageService } from 'primeng/api';
import { SidebarService } from '../../service/sidebar-service.service';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-header',
  imports: [PrimeNGModuleModule, IconComponent],
  templateUrl: './header.component.html',
  providers: [MessageService],
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  isMobile: boolean = false;
  sidebarService: SidebarService = inject(SidebarService);
  nomeUsuario: string | undefined = undefined;
  constructor(private authService:AuthService ,private router:Router, private messageService: MessageService, private cdr: ChangeDetectorRef){}

  usuarioLogado:Usuario | null = null;

  ngOnInit():void {
    this.carregarDadosUsuario();
    if (window.innerWidth < 769) {
      this.isMobile = true;
    }
   }

  public carregarDadosUsuario(){
   const usuario:TokenPayload | null = StorageService.getUsuarioDecodificado()
   this.authService.meusDados().subscribe({
    next: (res:Usuario) => {
      this.usuarioLogado = res;
      this.nomeUsuario = this.usuarioLogado?.nome?.split(' ')[0]
      this.cdr.detectChanges();
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
