import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { HeaderComponent } from '../header/header.component'
import { SidebarComponent } from '../sidebar/sidebar.component'
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router'
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { AuthService } from '../../auth/service/auth.service';
import { PreferenciaMoedaService } from '../../shared/service/preferencia-moeda.service';
import { ThemeService } from '../../service/theme.service';
import { SidebarService } from '../../service/sidebar-service.service';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { GuiaCondutorComponent } from '../../shared/components/guia-condutor/guia-condutor.component';
import { GuiaCondutorService } from '../../shared/service/guia-condutor.service';

@Component({
  selector: 'app-main-layout',
  imports: [HeaderComponent, SidebarComponent, RouterOutlet, RouterLink, RouterLinkActive, PrimeNGModuleModule, IconComponent, GuiaCondutorComponent],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private preferenciaMoedaService = inject(PreferenciaMoedaService);
  private themeService = inject(ThemeService);
  private guiaCondutorService = inject(GuiaCondutorService);
  public sidebarService = inject(SidebarService);
  private guiaTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.themeService.ativarAreaInterna();
    this.authService.meusDados().subscribe({
      next: (usuario) => {
        this.preferenciaMoedaService.carregarMoedaDoUsuario(usuario);
        const concluido = !!usuario.onboardingConcluido;
        this.guiaCondutorService.marcarPersistido(concluido);
        if (!concluido) {
          this.guiaTimer = setTimeout(() => {
            this.guiaCondutorService.iniciar(usuario.nome);
          }, 450);
        }
      },
      error: () => {
      }
    });
  }

  ngOnDestroy(): void {
    if (this.guiaTimer) {
      clearTimeout(this.guiaTimer);
    }
    this.themeService.desativarAreaInterna();
  }
}
