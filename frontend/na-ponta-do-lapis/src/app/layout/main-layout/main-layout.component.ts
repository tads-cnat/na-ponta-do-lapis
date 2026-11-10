import { Component, OnInit, inject } from '@angular/core';
import { HeaderComponent } from '../header/header.component'
import { SidebarComponent } from '../sidebar/sidebar.component'
import { RouterOutlet } from '@angular/router'
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { AuthService } from '../../auth/service/auth.service';
import { PreferenciaMoedaService } from '../../shared/service/preferencia-moeda.service';

@Component({
  selector: 'app-main-layout',
  imports: [HeaderComponent, SidebarComponent, RouterOutlet, PrimeNGModuleModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent implements OnInit {
  private authService = inject(AuthService);
  private preferenciaMoedaService = inject(PreferenciaMoedaService);

  ngOnInit(): void {
    this.authService.meusDados().subscribe({
      next: (usuario) => {
        this.preferenciaMoedaService.carregarMoedaDoUsuario(usuario);
      },
      error: () => {
        // Silencioso se token expirado ou interceptor tratar
      }
    });
  }
}
