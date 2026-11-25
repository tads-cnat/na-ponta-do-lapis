import { Component, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { StorageService } from '../../../auth/service/storage.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="min-h-screen flex bg-slate-50 text-slate-900">
      <!-- Sidebar Backoffice (fixa em 100vh no desktop para manter Sair sempre visível) -->
      <aside
        class="fixed inset-y-0 left-0 z-40 w-64 h-dvh bg-slate-950 text-slate-100 flex flex-col transition-transform duration-200 lg:sticky lg:top-0 lg:h-screen lg:shrink-0 lg:translate-x-0"
        [class.-translate-x-full]="!menuAberto()"
        [class.translate-x-0]="menuAberto()">
        <div class="h-20 px-5 flex items-center justify-between border-b border-slate-800 shrink-0">
          <a routerLink="/admin/dashboard" class="flex items-center no-underline">
            <img src="logo.svg" alt="Na Ponta do Lápis" class="h-12 w-auto" />
          </a>
          <button
            type="button"
            class="lg:hidden text-slate-400 hover:text-white cursor-pointer"
            (click)="menuAberto.set(false)"
            aria-label="Fechar menu">
            <i class="pi pi-times"></i>
          </button>
        </div>

        <nav class="flex-1 px-3 py-4 flex flex-col gap-1 overflow-y-auto min-h-0">
          @for (item of navItems; track item.route) {
            <a
              [routerLink]="item.route"
              routerLinkActive="bg-[#7843CD] text-white! font-semibold shadow-sm"
              [routerLinkActiveOptions]="{ exact: item.exact }"
              (click)="menuAberto.set(false)"
              class="flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm text-slate-300 hover:bg-slate-800/80 hover:text-white transition-colors">
              <i [class]="'pi ' + item.icon + ' text-sm'"></i>
              <span>{{ item.label }}</span>
            </a>
          }
        </nav>

        <div class="p-3 border-t border-slate-800 flex flex-col gap-1.5 shrink-0">
          <a
            routerLink="/app/dashboard"
            class="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold text-slate-300 hover:bg-slate-800 hover:text-white transition-colors">
            <i class="pi pi-arrow-left text-xs"></i>
            <span>Voltar ao App</span>
          </a>
          <button
            type="button"
            (click)="sair()"
            class="flex items-center gap-2.5 px-3.5 py-2.5 rounded-xl text-xs font-semibold text-rose-400 hover:bg-rose-500/10 hover:text-rose-300 transition-colors cursor-pointer text-left">
            <i class="pi pi-sign-out text-xs"></i>
            <span>Sair da conta</span>
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
        <header class="h-16 bg-white border-b border-slate-200 px-4 lg:px-8 flex items-center justify-between sticky top-0 z-20 shrink-0">
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="lg:hidden w-9 h-9 rounded-lg border border-slate-200 flex items-center justify-center text-slate-700 cursor-pointer"
              (click)="menuAberto.set(true)"
              aria-label="Abrir menu">
              <i class="pi pi-bars"></i>
            </button>
            <span class="text-sm font-bold text-slate-800">
              Painel Administrativo
            </span>
          </div>
          <div class="flex items-center gap-2.5">
            <span class="hidden sm:inline text-xs text-slate-500 truncate max-w-[240px]">
              {{ adminEmail }}
            </span>
            <span class="w-8 h-8 rounded-full bg-purple-100 text-[#7843CD] font-bold text-xs flex items-center justify-center uppercase shrink-0">
              {{ adminInicial }}
            </span>
          </div>
        </header>

        <main class="flex-1 p-4 lg:p-8 max-w-7xl w-full mx-auto">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
})
export class AdminLayoutComponent {
  readonly menuAberto = signal(false);
  readonly adminEmail = StorageService.getUsuarioDecodificado()?.sub ?? 'Administrador';
  readonly adminInicial = (this.adminEmail.trim()[0] ?? 'A').toUpperCase();

  readonly navItems = [
    { label: 'Dashboard', icon: 'pi-chart-line', route: '/admin/dashboard', exact: true },
    { label: 'Usuários', icon: 'pi-users', route: '/admin/usuarios', exact: false },
    { label: 'Famílias', icon: 'pi-home', route: '/admin/familias', exact: false },
    { label: 'Catálogos Globais', icon: 'pi-tags', route: '/admin/catalogos', exact: false },
    { label: 'Comunicação', icon: 'pi-bell', route: '/admin/comunicacao', exact: false },
    { label: 'Auditoria', icon: 'pi-history', route: '/admin/auditoria', exact: false },
  ];

  constructor(private readonly router: Router) {}

  sair(): void {
    StorageService.logout();
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
