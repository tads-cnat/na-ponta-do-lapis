import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PrimeNGModuleModule } from "./shared/primeNg.module";
import { ModalCelebracaoComponent } from './shared/components/modal-celebracao/modal-celebracao.component';
import { PwaService } from './shared/service/pwa.service';
import { CapacitorService } from './shared/service/capacitor.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PrimeNGModuleModule, ModalCelebracaoComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private readonly destroyRef = inject(DestroyRef);
  protected readonly title = signal('na-ponta-do-lapis');
  protected readonly pwaService = inject(PwaService);
  protected readonly capacitorService = inject(CapacitorService);
  protected readonly offline = signal(typeof navigator !== 'undefined' ? !navigator.onLine : false);

  constructor() {
    if (typeof window !== 'undefined') {
      const atualizar = () => this.offline.set(!navigator.onLine);
      window.addEventListener('online', atualizar);
      window.addEventListener('offline', atualizar);
      this.destroyRef.onDestroy(() => {
        window.removeEventListener('online', atualizar);
        window.removeEventListener('offline', atualizar);
      });
    }
  }
}
