import { DestroyRef, Injectable, NgZone, inject, signal } from '@angular/core';
import { App as CapacitorApp } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';
import { MessageService } from 'primeng/api';

@Injectable({
  providedIn: 'root',
})
export class ConexaoService {
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly ngZone = inject(NgZone);
  private reconexaoTimer: ReturnType<typeof setTimeout> | null = null;

  readonly offline = signal(typeof navigator !== 'undefined' ? !navigator.onLine : false);
  readonly verificandoConexao = signal(false);

  constructor() {
    if (typeof window !== 'undefined') {
      const atualizar = () => this.revalidarAoRetornar();

      window.addEventListener('online', atualizar);
      window.addEventListener('offline', atualizar);
      this.destroyRef.onDestroy(() => {
        window.removeEventListener('online', atualizar);
        window.removeEventListener('offline', atualizar);
      });
    }

    if (Capacitor.isNativePlatform()) {
      const listenerPromise = CapacitorApp.addListener('appStateChange', ({ isActive }) => {
        if (!isActive || typeof navigator === 'undefined') return;
        this.ngZone.run(() => {
          this.revalidarAoRetornar();
          if (this.reconexaoTimer) {
            clearTimeout(this.reconexaoTimer);
          }
          // Aguarda a WebView restabelecer o estado de rede após desbloqueio do aparelho
          this.reconexaoTimer = setTimeout(() => {
            this.reconexaoTimer = null;
            this.revalidarAoRetornar();
          }, 400);
        });
      });

      this.destroyRef.onDestroy(() => {
        if (this.reconexaoTimer) {
          clearTimeout(this.reconexaoTimer);
        }
        listenerPromise.then((handle) => handle.remove()).catch(() => {});
      });
    }
  }

  private revalidarAoRetornar(): void {
    if (typeof navigator === 'undefined') return;
    const estavaOffline = this.offline();
    const estaOffline = !navigator.onLine;
    this.offline.set(estaOffline);
    if (estaOffline) {
      this.messageService.clear();
    } else if (estavaOffline) {
      window.location.reload();
    }
  }

  marcarOffline(): void {
    this.offline.set(true);
    this.messageService.clear();
  }

  tentarReconectar(): void {
    if (typeof navigator === 'undefined') return;
    this.verificandoConexao.set(true);
    setTimeout(() => {
      const estaOffline = !navigator.onLine;
      this.offline.set(estaOffline);
      this.verificandoConexao.set(false);
      if (!estaOffline) {
        window.location.reload();
      }
    }, 350);
  }
}

