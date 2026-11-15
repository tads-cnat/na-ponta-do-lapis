import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { HeaderComponent } from '../header/header.component'
import { SidebarComponent } from '../sidebar/sidebar.component'
import { RouterOutlet } from '@angular/router'
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { AuthService } from '../../auth/service/auth.service';
import { PreferenciaMoedaService } from '../../shared/service/preferencia-moeda.service';
import { ThemeService } from '../../service/theme.service';

@Component({
  selector: 'app-main-layout',
  imports: [HeaderComponent, SidebarComponent, RouterOutlet, PrimeNGModuleModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  private authService = inject(AuthService);
  private preferenciaMoedaService = inject(PreferenciaMoedaService);
  private themeService = inject(ThemeService);

  ngOnInit(): void {
    this.themeService.ativarAreaInterna();
    this.authService.meusDados().subscribe({
      next: (usuario) => {
        this.preferenciaMoedaService.carregarMoedaDoUsuario(usuario);
      },
      error: () => {
        // Silencioso se token expirado ou interceptor tratar
      }
    });
  }

  ngOnDestroy(): void {
    this.themeService.desativarAreaInterna();
  }
}
