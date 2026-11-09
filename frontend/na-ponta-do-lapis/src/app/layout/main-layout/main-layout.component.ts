import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { RouterOutlet } from '@angular/router';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { ThemeService } from '../../service/theme.service';

@Component({
  selector: 'app-main-layout',
  imports: [HeaderComponent, SidebarComponent, RouterOutlet, PrimeNGModuleModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  private readonly themeService = inject(ThemeService);

  ngOnInit(): void {
    this.themeService.ativarAreaInterna();
  }

  ngOnDestroy(): void {
    this.themeService.desativarAreaInterna();
  }
}
