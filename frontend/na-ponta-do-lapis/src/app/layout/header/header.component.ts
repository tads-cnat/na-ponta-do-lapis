import { ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { AuthService } from '../../auth/service/auth.service';
import { StorageService } from '../../auth/service/storage.service';
import { Usuario } from '../../model/IUsuario.models';
import { MessageService } from 'primeng/api';
import { SidebarService } from '../../service/sidebar-service.service';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { NotificacaoService } from '../../shared/service/notificacao.service';
import { Notificacao } from '../../model/INotificacao.model';
import { MedalhaConquistaComponent } from '../../shared/components/medalha-conquista/medalha-conquista.component';

@Component({
  selector: 'app-header',
  imports: [CommonModule, PrimeNGModuleModule, RouterLink, IconComponent, MedalhaConquistaComponent],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit {
  sidebarService: SidebarService = inject(SidebarService);
  public notificacaoService: NotificacaoService = inject(NotificacaoService);
  private destroyRef = inject(DestroyRef);

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

    this.notificacaoService.notificacoes$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((lista) => {
        this.notificacoes = lista;
        this.cdr.detectChanges();
      });

    this.notificacaoService.totalNaoLidas$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((total) => {
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

  public obterIcone(tipo: string): string {
    switch (tipo) {
      case 'META_ATINGIDA':
        return 'pi-check-circle text-emerald-600';
      case 'META_ESTOURADA':
        return 'pi-exclamation-triangle text-rose-600';
      case 'META_ALERTA_50':
      case 'META_ALERTA_80':
        return 'pi-flag text-amber-600';
      case 'CONQUISTA_DESBLOQUEADA':
        return 'pi-trophy text-[#7843CD]';
      case 'FAMILIA_CONVITE':
        return 'pi-users text-blue-600';
      default:
        return 'pi-bell text-slate-600';
    }
  }

  public obterBgIcone(tipo: string): string {
    switch (tipo) {
      case 'META_ATINGIDA':
        return 'bg-emerald-50 border border-emerald-100/80';
      case 'META_ESTOURADA':
        return 'bg-rose-50 border border-rose-100/80';
      case 'META_ALERTA_50':
      case 'META_ALERTA_80':
        return 'bg-amber-50 border border-amber-100/80';
      case 'CONQUISTA_DESBLOQUEADA':
        return 'bg-[#7843CD]/10 border border-[#7843CD]/20';
      case 'FAMILIA_CONVITE':
        return 'bg-blue-50 border border-blue-100/80';
      default:
        return 'bg-slate-100 border border-slate-200/80';
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

  public carregarDadosUsuario(): void {
    this.authService.meusDados().subscribe({
      next: (res: Usuario) => {
        this.usuarioLogado = res;
        this.nomeUsuario = this.usuarioLogado?.nome?.split(' ')[0];
        this.cdr.detectChanges();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Erro ao se conectar ao servidor.',
          life: 3000,
        });
      },
    });
  }

  logout(): void {
    StorageService.logout();
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }

  usarFotoPadrao(event: any): void {
    event.target.src = 'user_anonimo.png';
  }
}
