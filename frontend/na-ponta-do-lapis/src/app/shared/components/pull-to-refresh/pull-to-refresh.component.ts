import { Component, EventEmitter, HostListener, Output, inject, signal } from '@angular/core';
import { CapacitorService } from '../../service/capacitor.service';

@Component({
  selector: 'app-pull-to-refresh',
  standalone: true,
  template: `
    <div class="relative w-full">
      @if (alturaPuxada() > 8 || atualizando()) {
        <div
          class="flex items-center justify-center overflow-hidden transition-all duration-150"
          [style.height.px]="atualizando() ? 48 : alturaPuxada()"
          [style.opacity]="atualizando() ? 1 : Math.min(1, alturaPuxada() / 48)">
          <div
            class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#F3E8FF] border border-[#DDD0FB] text-[#7843CD] text-xs font-semibold shadow-2xs">
            <i
              class="pi text-xs"
              [class.pi-spin]="atualizando()"
              [class.pi-spinner]="atualizando()"
              [class.pi-arrow-down]="!atualizando() && !prontoParaSoltar()"
              [class.pi-refresh]="!atualizando() && prontoParaSoltar()"></i>
            <span>
              {{
                atualizando()
                  ? 'Atualizando...'
                  : prontoParaSoltar()
                    ? 'Solte para atualizar'
                    : 'Puxe para atualizar'
              }}
            </span>
          </div>
        </div>
      }
      <ng-content></ng-content>
    </div>
  `,
})
export class PullToRefreshComponent {
  protected readonly Math = Math;
  private readonly capacitorService = inject(CapacitorService);

  @Output() refresh = new EventEmitter<void>();

  readonly alturaPuxada = signal(0);
  readonly atualizando = signal(false);
  readonly prontoParaSoltar = signal(false);

  private inicioY: number | null = null;
  private readonly limiteDisparo = 68;
  private readonly maximoPuxada = 92;
  private vibrouLimite = false;

  @HostListener('touchstart', ['$event'])
  aoIniciarToque(event: TouchEvent): void {
    if (this.atualizando() || event.touches.length !== 1) return;
    if (typeof window !== 'undefined' && window.scrollY > 2) return;
    if (typeof document !== 'undefined' && document.querySelector('.p-dialog-mask')) return;
    this.inicioY = event.touches[0].clientY;
    this.vibrouLimite = false;
  }

  @HostListener('touchmove', ['$event'])
  aoMoverToque(event: TouchEvent): void {
    if (this.inicioY === null || this.atualizando() || event.touches.length !== 1) return;
    if (typeof window !== 'undefined' && window.scrollY > 2) {
      this.resetar();
      return;
    }

    const delta = event.touches[0].clientY - this.inicioY;
    if (delta <= 0) {
      this.alturaPuxada.set(0);
      this.prontoParaSoltar.set(false);
      return;
    }

    const puxada = Math.min(this.maximoPuxada, Math.round(delta * 0.45));
    this.alturaPuxada.set(puxada);
    const atingiu = puxada >= this.limiteDisparo;
    this.prontoParaSoltar.set(atingiu);
    if (atingiu && !this.vibrouLimite) {
      this.vibrouLimite = true;
      this.capacitorService.vibrarImpactoLeve();
    }
  }

  @HostListener('touchend')
  @HostListener('touchcancel')
  aoSoltarToque(): void {
    if (this.inicioY === null) return;
    const deveAtualizar = this.prontoParaSoltar() && !this.atualizando();
    this.resetar();
    if (!deveAtualizar) return;

    this.atualizando.set(true);
    this.refresh.emit();
    setTimeout(() => this.atualizando.set(false), 750);
  }

  private resetar(): void {
    this.inicioY = null;
    this.alturaPuxada.set(0);
    this.prontoParaSoltar.set(false);
    this.vibrouLimite = false;
  }
}

