import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';
import { StorageService } from '../../auth/service/storage.service';
import { TokenPayload } from '../../model/IToken.models';
import { Usuario } from '../../model/IUsuario.models';
import { MessageService } from 'primeng/api';
import { SidebarService } from '../../service/sidebar-service.service';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { NotificacaoService } from '../../shared/service/notificacao.service';
import { Notificacao } from '../../model/INotificacao.model';
import { MedalhaConquistaComponent } from '../../shared/components/medalha-conquista/medalha-conquista.component';

@Component({
  selector: 'app-header',
  imports: [CommonModule, PrimeNGModuleModule, IconComponent, MedalhaConquistaComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit {
  isMobile: boolean = false;
  sidebarService: SidebarService = inject(SidebarService);
  public notificacaoService: NotificacaoService = inject(NotificacaoService);
  nomeUsuario: string | undefined = undefined;
  usuarioLogado: Usuario | null = null;

  notificacoes: Notificacao[] = [];
  totalNaoLidas: number = 0;

  constructor(
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.carregarDadosUsuario();
    if (window.innerWidth < 769) {
      this.isMobile = true;
    }

    this.notificacaoService.notificacoes$.subscribe((lista) => {
      this.notificacoes = lista;
      this.cdr.detectChanges();
    });

    this.notificacaoService.totalNaoLidas$.subscribe((total) => {
      this.totalNaoLidas = total;
      this.cdr.detectChanges();
    });

    this.notificacaoService.carregarNotificacoes();
  }

  public abrirNotificacoes(event: Event, popover: any): void {
    popover.toggle(event);
    this.notificacaoService.carregarNotificacoes();
  }

  public marcarComoLida(notificacao: Notificacao): void {
    if (!notificacao.lida) {
      this.notificacaoService.marcarComoLida(notificacao.id).subscribe();
    }
    if (notificacao.linkRedirecionamento) {
      this.router.navigateByUrl(notificacao.linkRedirecionamento);
    }
  }

  public marcarTodasComoLidas(): void {
    this.notificacaoService.marcarTodasComoLidas().subscribe();
  }

  public excluirNotificacao(event: Event, id: number): void {
    event.stopPropagation();
    this.notificacaoService.excluirNotificacao(id).subscribe();
  }

  public obterIconeTipo(tipo: string): string {
    switch (tipo) {
      case 'META_ATINGIDA':
        return 'pi pi-check-circle text-emerald-600 bg-emerald-50';
      case 'META_ESTOURADA':
        return 'pi pi-exclamation-triangle text-rose-600 bg-rose-50';
      case 'META_ALERTA_50':
      case 'META_ALERTA_80':
        return 'pi pi-flag text-amber-600 bg-amber-50';
      case 'CONQUISTA_DESBLOQUEADA':
        return 'pi pi-trophy text-[#7843CD] bg-[#7843CD]/10';
      case 'FAMILIA_CONVITE':
        return 'pi pi-users text-blue-600 bg-blue-50';
      default:
        return 'pi pi-bell text-slate-600 bg-slate-100';
    }
  }

  public extrairIdConquista(mensagem: string): string {
    const m = (mensagem || '').toLowerCase();
    if (m.includes('primeiro passo') || m.includes('primeira meta')) return 'PRIMEIRA_META';
    if (m.includes('cofrinho') || m.includes('primeiro aporte')) return 'PRIMEIRO_APORTE';
    if (m.includes('bronze')) return 'POUPADOR_BRONZE';
    if (m.includes('alvo') || m.includes('primeira meta')) return 'META_BATIDA_1';
    if (m.includes('mestre')) return 'MESTRE_DAS_METAS';
    if (m.includes('blindado')) return 'ORCAMENTO_BLINDADO';
    if (m.includes('dívida') || m.includes('divida')) return 'LIVRE_DE_DIVIDAS';
    if (m.includes('família') || m.includes('familia')) return 'FAMILIA_UNIDA';
    return 'PRIMEIRA_META';
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
