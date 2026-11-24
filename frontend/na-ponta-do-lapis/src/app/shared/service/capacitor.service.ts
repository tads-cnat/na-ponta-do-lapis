import { Injectable, afterNextRender, effect, inject, signal } from '@angular/core';
import { Capacitor } from '@capacitor/core';
import { App as CapacitorApp } from '@capacitor/app';
import { Haptics, ImpactStyle, NotificationType } from '@capacitor/haptics';
import { SplashScreen } from '@capacitor/splash-screen';
import { StatusBar, Style } from '@capacitor/status-bar';
import { SidebarService } from '../../service/sidebar-service.service';
import { ThemeService } from '../../service/theme.service';

/**
 * Integração nativa com Capacitor (Android/iOS). No navegador web não executa ações nativas.
 */
@Injectable({
  providedIn: 'root',
})
export class CapacitorService {
  private readonly sidebarService = inject(SidebarService);
  private readonly themeService = inject(ThemeService);
  readonly versaoApp = signal<string | null>(null);
  private splashOcultada = false;

  constructor() {
    if (!Capacitor.isNativePlatform()) return;

    if (typeof document !== 'undefined') {
      document.documentElement.style.setProperty('--npl-native-top', '32px');
      document.documentElement.style.setProperty('--npl-native-bottom', '20px');
    }

    this.carregarVersaoNativa();
    this.configurarStatusBar();
    this.configurarBotaoVoltar();

    afterNextRender(() => this.ocultarSplashScreen());
    setTimeout(() => this.ocultarSplashScreen(), 2500);

    effect(() => {
      const escuro = this.themeService.escuro();
      // Em @capacitor/status-bar: Style.Light = ícones escuros (para fundo claro); Style.Dark = ícones claros (para fundo escuro)
      StatusBar.setStyle({ style: escuro ? Style.Dark : Style.Light }).catch(() => {});
    });
  }

  vibrarImpactoLeve(): void {
    if (!Capacitor.isNativePlatform()) return;
    Haptics.impact({ style: ImpactStyle.Light }).catch(() => {});
  }

  vibrarExclusao(): void {
    if (!Capacitor.isNativePlatform()) return;
    Haptics.impact({ style: ImpactStyle.Medium }).catch(() => {});
  }

  vibrarSucesso(): void {
    if (!Capacitor.isNativePlatform()) return;
    Haptics.notification({ type: NotificationType.Success }).catch(() => {});
  }

  private ocultarSplashScreen(): void {
    if (this.splashOcultada) return;
    this.splashOcultada = true;
    SplashScreen.hide({ fadeOutDuration: 220 }).catch(() => {});
  }

  private async carregarVersaoNativa(): Promise<void> {
    try {
      const info = await CapacitorApp.getInfo();
      const ehHomolog = info.id?.endsWith('.homolog');
      this.versaoApp.set(
        ehHomolog ? `${info.version} (${info.build}) · Homolog` : info.version
      );
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
      if (this.fecharDialogAberto()) {
        return;
      }
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

  private fecharDialogAberto(): boolean {
    if (typeof document === 'undefined') return false;
    const guiaSkip = document.querySelector<HTMLButtonElement>('[data-tour-skip]');
    if (guiaSkip) {
      guiaSkip.click();
      return true;
    }
    const dialogs = document.querySelectorAll('.p-dialog-mask');
    if (!dialogs.length) return false;
    const topo = dialogs[dialogs.length - 1];
    const closeBtn = topo.querySelector<HTMLButtonElement>('.p-dialog-close-button, button[aria-label="Close"]');
    if (closeBtn) {
      closeBtn.click();
      return true;
    }
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    return true;
  }
}

