import { Injectable, signal } from '@angular/core';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

/**
 * Gerencia instalação do PWA (beforeinstallprompt) e detecção de novas versões do Service Worker (/sw.js).
 */
@Injectable({
  providedIn: 'root',
})
export class PwaService {
  private deferredPrompt: BeforeInstallPromptEvent | null = null;
  private waitingWorker: ServiceWorker | null = null;

  readonly podeInstalar = signal(false);
  readonly atualizacaoDisponivel = signal(false);

  constructor() {
    if (typeof window === 'undefined') return;

    window.addEventListener('beforeinstallprompt', (event: Event) => {
      event.preventDefault();
      this.deferredPrompt = event as BeforeInstallPromptEvent;
      this.podeInstalar.set(true);
    });

    window.addEventListener('appinstalled', () => {
      this.deferredPrompt = null;
      this.podeInstalar.set(false);
    });

    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.getRegistration().then((reg) => {
        if (!reg) return;
        if (reg.waiting && navigator.serviceWorker.controller) {
          this.waitingWorker = reg.waiting;
          this.atualizacaoDisponivel.set(true);
        }
        reg.addEventListener('updatefound', () => {
          const worker = reg.installing;
          if (!worker) return;
          worker.addEventListener('statechange', () => {
            if (worker.state === 'installed' && navigator.serviceWorker.controller) {
              this.waitingWorker = worker;
              this.atualizacaoDisponivel.set(true);
            }
          });
        });
      }).catch(() => {});
    }
  }

  async instalarApp(): Promise<boolean> {
    if (!this.deferredPrompt) return false;
    await this.deferredPrompt.prompt();
    const { outcome } = await this.deferredPrompt.userChoice;
    this.deferredPrompt = null;
    this.podeInstalar.set(false);
    return outcome === 'accepted';
  }

  aplicarAtualizacao(): void {
    if (this.waitingWorker) {
      this.waitingWorker.postMessage({ type: 'SKIP_WAITING' });
    }
    document.location.reload();
  }
}
