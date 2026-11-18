import { Injectable, effect, inject, signal } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { App as CapacitorApp } from '@capacitor/app';
import { StatusBar, Style } from '@capacitor/status-bar';
import { SidebarService } from '../../service/sidebar-service.service';
import { ThemeService } from '../../service/theme.service';
import { environment } from '../../../environments/environment';

/**
 * Integração nativa com Capacitor (Android/iOS). No navegador web não executa ações nativas.
 */
@Injectable({
  providedIn: 'root',
})
export class CapacitorService {
  private readonly sidebarService = inject(SidebarService);
  private readonly themeService = inject(ThemeService);
  readonly versaoApp = signal<string>(
    environment.production ? environment.appVersion : `${environment.appVersion} (dev)`
  );

  constructor() {
    if (!Capacitor.isNativePlatform()) return;

    if (typeof document !== 'undefined') {
      document.documentElement.style.setProperty('--npl-native-top', '32px');
      document.documentElement.style.setProperty('--npl-native-bottom', '20px');
    }

    this.carregarVersaoNativa();
    this.configurarStatusBar();
    this.configurarBotaoVoltar();

    effect(() => {
      const escuro = this.themeService.escuro();
      // Em @capacitor/status-bar: Style.Light = ícones escuros (para fundo claro); Style.Dark = ícones claros (para fundo escuro)
      StatusBar.setStyle({ style: escuro ? Style.Dark : Style.Light }).catch(() => {});
    });
  }

  private async carregarVersaoNativa(): Promise<void> {
    try {
      const info = await CapacitorApp.getInfo();
      const sufixoHomolog = info.id?.endsWith('.homolog') ? ' · Homolog' : '';
      this.versaoApp.set(`${info.version} (${info.build})${sufixoHomolog}`);
    } catch {
      // Mantém versão padrão de environment
    }
  }

  private async configurarStatusBar(): Promise<void> {
    try {
      await StatusBar.setOverlaysWebView({ overlay: true });
      await StatusBar.setStyle({ style: this.themeService.escuro() ? Style.Dark : Style.Light });
    } catch {
      // Ignora em plataformas sem suporte a StatusBar
    }
  }

  private configurarBotaoVoltar(): void {
    CapacitorApp.addListener('backButton', ({ canGoBack }) => {
      if (typeof window !== 'undefined' && window.innerWidth < 769 && this.sidebarService.aberto()) {
        this.sidebarService.fecharSidebar();
        return;
      }
      if (canGoBack) {
        window.history.back();
        return;
      }
      CapacitorApp.exitApp();
    });
  }
}

