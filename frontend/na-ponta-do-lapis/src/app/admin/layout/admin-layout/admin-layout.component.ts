import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TooltipModule } from 'primeng/tooltip';
import { AuthService } from '../../../auth/service/auth.service';
import { StorageService } from '../../../auth/service/storage.service';
import { Usuario } from '../../../model/IUsuario.models';
import { ThemeService } from '../../../service/theme.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TooltipModule, IconComponent],
  template: `
    <div class="admin-shell min-h-screen flex bg-[#F4F5F8] text-slate-900 border-t-2 border-t-[#7843CD]">
      <!-- Sidebar Backoffice (roxo profundo da marca para contraste imediato com o app normal) -->
      <aside
        class="fixed inset-y-0 left-0 z-40 h-dvh bg-[#1E1433] border-r border-white/10 text-purple-100 flex flex-col transition-all duration-200 lg:sticky lg:top-0 lg:h-screen lg:shrink-0 lg:translate-x-0 w-[236px]"
        [class.lg:w-[236px]]="sidebarAberta()"
        [class.lg:w-[74px]]="!sidebarAberta()"
        [class.-translate-x-full]="!menuAberto()"
        [class.translate-x-0]="menuAberto()">
        <div
          class="h-16 px-3 flex items-center border-b border-white/10 shrink-0"
          [class.justify-between]="sidebarAberta()"
          [class.lg:justify-center]="!sidebarAberta()">
          <a
            routerLink="/admin/dashboard"
            class="flex items-center min-w-0 pl-1 no-underline"
            [class.lg:hidden]="!sidebarAberta()">
            <img src="logo.svg" alt="Na Ponta do Lápis" class="h-11 w-auto object-contain shrink-0" />
          </a>

          <!-- Botão recolher/expandir no desktop -->
          <button
            type="button"
            (click)="sidebarAberta.set(!sidebarAberta())"
            [pTooltip]="sidebarAberta() ? 'Recolher menu' : 'Expandir menu'"
            tooltipPosition="right"
            aria-label="Alternar menu lateral"
            class="hidden lg:flex w-10 h-10 items-center justify-center rounded-xl text-purple-200/80 hover:text-white hover:bg-white/10 border-0 bg-transparent transition-colors cursor-pointer shrink-0">
            <app-icon [name]="sidebarAberta() ? 'recolher' : 'expandir'" class="w-5 h-5" />
          </button>

          <!-- Botão fechar no mobile -->
          <button
            type="button"
            class="lg:hidden w-11 h-11 flex items-center justify-center rounded-xl text-purple-200/80 hover:text-white hover:bg-white/10 border-0 bg-transparent cursor-pointer shrink-0"
            (click)="menuAberto.set(false)"
            aria-label="Fechar menu">
            <app-icon name="close" class="w-5 h-5" />
          </button>
        </div>

        <nav class="flex-1 px-3 pt-6 pb-4 flex flex-col gap-1.5 overflow-y-auto overflow-x-hidden min-h-0">
          @for (item of navItems; track item.route) {
            <a
              [routerLink]="item.route"
              [pTooltip]="!sidebarAberta() ? item.label : ''"
              tooltipPosition="right"
              routerLinkActive="bg-[#7843CD] text-white! font-semibold shadow-sm hover:bg-[#6A35BE]!"
              [routerLinkActiveOptions]="{ exact: item.exact }"
              (click)="menuAberto.set(false)"
              class="h-[46px] flex items-center gap-3 px-4 rounded-xl text-sm text-purple-100/85 hover:bg-white/10 hover:text-white transition-colors no-underline"
              [class.lg:justify-center]="!sidebarAberta()"
              [class.lg:px-0]="!sidebarAberta()">
              <i [class]="'pi ' + item.icon + ' text-base w-5 text-center shrink-0'"></i>
              <span class="truncate" [class.lg:hidden]="!sidebarAberta()">{{ item.label }}</span>
            </a>
          }
        </nav>

        <div class="p-3 border-t border-white/10 flex flex-col gap-1.5 shrink-0">
          <a
            routerLink="/app/dashboard"
            [pTooltip]="!sidebarAberta() ? 'Voltar ao App' : ''"
            tooltipPosition="right"
            class="h-[44px] flex items-center gap-3 px-4 rounded-xl text-sm font-medium text-purple-100/85 hover:bg-white/10 hover:text-white transition-colors no-underline"
            [class.lg:justify-center]="!sidebarAberta()"
            [class.lg:px-0]="!sidebarAberta()">
            <i class="pi pi-arrow-left text-sm w-5 text-center shrink-0"></i>
            <span class="truncate" [class.lg:hidden]="!sidebarAberta()">Voltar ao App</span>
          </a>
          <button
            type="button"
            (click)="sair()"
            [pTooltip]="!sidebarAberta() ? 'Sair da conta' : ''"
            tooltipPosition="right"
            class="h-[44px] flex items-center gap-3 px-4 rounded-xl text-sm font-medium text-rose-300 hover:bg-rose-500/20 hover:text-rose-200 border-0 bg-transparent transition-colors cursor-pointer text-left"
            [class.lg:justify-center]="!sidebarAberta()"
            [class.lg:px-0]="!sidebarAberta()">
            <i class="pi pi-sign-out text-sm w-5 text-center shrink-0"></i>
            <span class="truncate" [class.lg:hidden]="!sidebarAberta()">Sair da conta</span>
          </button>
        </div>
      </aside>

      <!-- Backdrop mobile -->
      @if (menuAberto()) {
        <div
          class="fixed inset-0 bg-black/40 z-30 lg:hidden"
          (click)="menuAberto.set(false)"></div>
      }

      <!-- Conteúdo -->
      <div class="flex-1 flex flex-col min-w-0">
        <header class="h-16 bg-[#FBF9FF]/90 backdrop-blur-md border-b border-purple-200/70 px-4 lg:px-8 flex items-center justify-between sticky top-0 z-20 shrink-0">
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="lg:hidden w-10 h-10 rounded-xl text-slate-700 hover:text-[#7843CD] hover:bg-purple-100/70 border-0 bg-transparent flex items-center justify-center cursor-pointer"
              (click)="menuAberto.set(true)"
              aria-label="Abrir menu">
              <app-icon name="menu" class="w-5 h-5" />
            </button>
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-purple-100 text-[#7843CD] border border-purple-200 text-xs font-bold">
              <i class="pi pi-shield text-[11px]"></i>
              Painel Administrativo
            </span>
          </div>
          <a
            routerLink="/app/perfil"
            title="Ir para o perfil"
            class="flex items-center gap-2.5 px-1.5 sm:px-2.5 py-1 rounded-xl cursor-pointer no-underline hover:bg-purple-100/60 transition-colors min-w-0">
            <img
              class="h-9 w-9 rounded-full border-2 border-[#EEC046] shadow-2xs object-cover shrink-0"
              [src]="usuarioLogado()?.fotoPerfil || 'user_anonimo.png'"
              referrerpolicy="no-referrer"
              (error)="usarFotoPadrao($event)"
              [alt]="usuarioLogado()?.nome || adminEmail" />
            <div class="hidden sm:flex flex-col min-w-0">
              <span class="text-sm font-semibold text-slate-800 truncate max-w-[180px] leading-tight">
                {{ nomeExibicao() }}
              </span>
              <span class="text-[11px] text-slate-500 truncate max-w-[220px] leading-tight">
                {{ usuarioLogado()?.email || adminEmail }}
              </span>
            </div>
          </a>
        </header>

        <main class="flex-1 p-4 lg:p-8 max-w-7xl w-full mx-auto">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
})
export class AdminLayoutComponent implements OnInit, OnDestroy {
  private readonly themeService = inject(ThemeService);
  private readonly authService = inject(AuthService);

  readonly menuAberto = signal(false);
  readonly sidebarAberta = signal(true);
  readonly usuarioLogado = signal<Usuario | null>(null);
  readonly adminEmail = StorageService.getUsuarioDecodificado()?.sub ?? 'Administrador';

  readonly navItems = [
    { label: 'Dashboard', icon: 'pi-chart-line', route: '/admin/dashboard', exact: true },
    { label: 'Usuários', icon: 'pi-users', route: '/admin/usuarios', exact: false },
    { label: 'Famílias', icon: 'pi-home', route: '/admin/familias', exact: false },
    { label: 'Catálogos Globais', icon: 'pi-tags', route: '/admin/catalogos', exact: false },
    { label: 'Comunicação', icon: 'pi-bell', route: '/admin/comunicacao', exact: false },
    { label: 'Auditoria', icon: 'pi-history', route: '/admin/auditoria', exact: false },
  ];

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    this.themeService.ativarAreaInterna();
    this.authService.meusDados().subscribe({
      next: (res) => this.usuarioLogado.set(res),
      error: () => {},
    });
  }

  ngOnDestroy(): void {
    this.themeService.desativarAreaInterna();
  }

  nomeExibicao(): string {
    const nome = this.usuarioLogado()?.nome?.trim();
    return nome ? nome.split(' ')[0] : 'Admin';
  }

  usarFotoPadrao(event: Event): void {
    const img = event.target as HTMLImageElement | null;
    if (img) {
      img.src = 'user_anonimo.png';
    }
  }

  sair(): void {
    StorageService.logout();
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
