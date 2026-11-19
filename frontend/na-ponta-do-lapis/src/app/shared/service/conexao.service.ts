import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { App as CapacitorApp } from '@capacitor/app';
import { Capacitor } from '@capacitor/core';
import { MessageService } from 'primeng/api';

@Injectable({
  providedIn: 'root',
})
export class ConexaoService {
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);

  readonly offline = signal(typeof navigator !== 'undefined' ? !navigator.onLine : false);
  readonly verificandoConexao = signal(false);

  constructor() {
    if (typeof window !== 'undefined') {
      const atualizar = () => {
        const estaOffline = !navigator.onLine;
        this.offline.set(estaOffline);
        if (estaOffline) {
          this.messageService.clear();
        }
      };

      window.addEventListener('online', atualizar);
      window.addEventListener('offline', atualizar);
      this.destroyRef.onDestroy(() => {
        window.removeEventListener('online', atualizar);
        window.removeEventListener('offline', atualizar);
      });
    }

    if (Capacitor.isNativePlatform()) {
      const listenerPromise = CapacitorApp.addListener('appStateChange', ({ isActive }) => {
        if (isActive && typeof navigator !== 'undefined') {
          const estaOffline = !navigator.onLine;
          this.offline.set(estaOffline);
          if (estaOffline) {
            this.messageService.clear();
          }
        }
      });

      this.destroyRef.onDestroy(() => {
        listenerPromise.then((handle) => handle.remove()).catch(() => {});
      });
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

