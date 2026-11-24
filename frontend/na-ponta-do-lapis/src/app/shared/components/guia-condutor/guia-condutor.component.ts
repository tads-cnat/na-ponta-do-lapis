import {
  Component,
  DestroyRef,
  HostListener,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { MascoteLoginComponent } from '../../../auth/components/mascote-login/mascote-login.component';
import { GuiaCondutorService } from '../../service/guia-condutor.service';

interface SpotlightBox {
  top: number;
  left: number;
  width: number;
  height: number;
}

@Component({
  selector: 'app-guia-condutor',
  standalone: true,
  imports: [MascoteLoginComponent],
  template: `
    @if (guia.ativo()) {
      <div
        class="fixed inset-0 z-[1200]"
        role="dialog"
        aria-modal="true"
        aria-label="Guia de boas-vindas do sistema">

        <!-- Overlay com recorte spotlight ou backdrop centralizado -->
        @if (spotlight(); as box) {
          <div
            class="fixed inset-0 bg-transparent"
            (click)="guia.pular()"></div>
          <div
            class="fixed pointer-events-none rounded-2xl transition-all duration-300 ease-out ring-2 ring-[#EEC046] border-2 border-[#7843CD]"
            [style.top.px]="box.top"
            [style.left.px]="box.left"
            [style.width.px]="box.width"
            [style.height.px]="box.height"
            style="box-shadow: 0 0 0 9999px rgba(15, 23, 42, 0.65);">
          </div>
        } @else {
          <div
            class="fixed inset-0 bg-slate-950/65 backdrop-blur-[2px] transition-opacity"
            (click)="guia.pular()"></div>
        }

        <!-- Card flutuante com Mascote -->
        <div
          class="fixed z-[1210] w-[min(390px,calc(100vw-24px))] bg-white rounded-3xl border border-purple-100 shadow-2xl p-4 sm:p-5 transition-all duration-300 ease-out"
          [style.top]="cardPos().top"
          [style.left]="cardPos().left"
          [style.transform]="cardPos().transform"
          (click)="$event.stopPropagation()">

          <div class="flex items-start gap-3">
            <div class="shrink-0 -mt-1 flex items-center justify-center rounded-2xl bg-[#F3E8FF] border border-[#DDD0FB] p-1">
              <app-mascote-login
                [compact]="true"
                [estado]="guia.passoAtual().mascoteEstado" />
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between gap-2 mb-1">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-[#FEF9E7] text-[#B45309] border border-[#F9DB70]">
                  Passo {{ guia.indiceAtual() + 1 }} de {{ guia.totalPassos }}
                </span>
                <button
                  type="button"
                  data-tour-skip
                  (click)="guia.pular()"
                  class="text-xs font-semibold text-slate-400 hover:text-slate-700 cursor-pointer bg-transparent border-0 p-1">
                  Pular
                </button>
              </div>

              <h3 class="text-base font-extrabold text-slate-900 m-0 leading-snug">
                @if (guia.indiceAtual() === 0 && guia.nomeUsuario()) {
                  Olá, {{ guia.nomeUsuario() }}! Eu sou o Lápis 👋
                } @else {
                  {{ guia.passoAtual().titulo }}
                }
              </h3>

              <p class="text-xs sm:text-[13px] text-slate-600 mt-1.5 mb-0 leading-relaxed">
                {{ guia.passoAtual().descricao }}
              </p>
            </div>
          </div>

          <!-- Indicadores e ações -->
          <div class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between gap-2">
            <div class="flex items-center gap-1.5" aria-label="Progresso do guia">
              @for (passo of guia.passos; track passo.id; let i = $index) {
                <button
                  type="button"
                  [attr.aria-label]="'Ir para passo ' + (i + 1)"
                  (click)="guia.irPara(i)"
                  class="h-2 rounded-full transition-all border-0 cursor-pointer p-0"
                  [class]="i === guia.indiceAtual() ? 'w-5 bg-[#7843CD]' : 'w-2 bg-slate-200 hover:bg-slate-300'">
                </button>
              }
            </div>

            <div class="flex items-center gap-2 shrink-0">
              @if (guia.indiceAtual() > 0) {
                <button
                  type="button"
                  (click)="guia.voltar()"
                  class="px-3 py-1.5 rounded-xl text-xs font-semibold btn-npl-secondary cursor-pointer">
                  Anterior
                </button>
              }
              <button
                type="button"
                (click)="guia.avancar()"
                class="px-4 py-1.5 rounded-xl text-xs font-bold btn-npl-primary cursor-pointer flex items-center gap-1.5">
                <span>{{ guia.ehUltimoPasso() ? 'Começar!' : 'Próximo' }}</span>
                <i class="pi text-[10px]" [class.pi-check]="guia.ehUltimoPasso()" [class.pi-arrow-right]="!guia.ehUltimoPasso()"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `,
})
export class GuiaCondutorComponent {
  readonly guia = inject(GuiaCondutorService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly spotlight = signal<SpotlightBox | null>(null);
  private reposicionarTimer: ReturnType<typeof setTimeout> | null = null;

  readonly cardPos = computed(() => {
    const box = this.spotlight();
    if (!box || typeof window === 'undefined') {
      return { top: '50%', left: '50%', transform: 'translate(-50%, -50%)' };
    }

    const vw = window.innerWidth;
    const vh = window.innerHeight;
    const cardW = Math.min(390, vw - 24);
    const cardH = 228;
    const gap = 14;

    let left = box.left + box.width / 2 - cardW / 2;
    left = Math.max(12, Math.min(vw - cardW - 12, left));

    // Tenta colocar abaixo do alvo; se não couber, acima; se ainda não couber, ao lado ou centralizado
    let top: number;
    if (box.top + box.height + gap + cardH <= vh - 12) {
      top = box.top + box.height + gap;
    } else if (box.top - gap - cardH >= 12) {
      top = box.top - gap - cardH;
    } else if (vw >= 768 && box.left + box.width + gap + cardW <= vw - 12) {
      left = box.left + box.width + gap;
      top = Math.max(16, Math.min(vh - cardH - 16, box.top));
    } else {
      return { top: '50%', left: '50%', transform: 'translate(-50%, -50%)' };
    }

    return { top: `${Math.round(top)}px`, left: `${Math.round(left)}px`, transform: 'none' };
  });

  constructor() {
    effect(() => {
      const ativo = this.guia.ativo();
      const idx = this.guia.indiceAtual();
      if (!ativo) {
        this.spotlight.set(null);
        return;
      }
      this.agendarReposicionamento(idx);
    });

    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        if (this.guia.ativo()) {
          this.agendarReposicionamento(this.guia.indiceAtual());
        }
      });

    this.destroyRef.onDestroy(() => {
      if (this.reposicionarTimer) clearTimeout(this.reposicionarTimer);
    });
  }

  @HostListener('window:resize')
  @HostListener('window:scroll')
  aoRedimensionar(): void {
    if (this.guia.ativo()) {
      this.atualizarSpotlight();
    }
  }

  @HostListener('document:keydown.escape')
  aoPressionarEsc(): void {
    if (this.guia.ativo()) {
      this.guia.pular();
    }
  }

  private agendarReposicionamento(_idx: number): void {
    if (this.reposicionarTimer) clearTimeout(this.reposicionarTimer);
    this.reposicionarTimer = setTimeout(() => this.atualizarSpotlight(), 140);
  }

  private atualizarSpotlight(): void {
    if (typeof document === 'undefined' || !this.guia.ativo()) {
      this.spotlight.set(null);
      return;
    }

    const targetKey = this.guia.passoAtual().target;
    if (!targetKey) {
      this.spotlight.set(null);
      return;
    }

    const candidatos = Array.from(document.querySelectorAll<HTMLElement>(`[data-tour="${targetKey}"]`));
    const visivel = candidatos.find((el) => {
      const rect = el.getBoundingClientRect();
      return el.offsetParent !== null && rect.width > 0 && rect.height > 0;
    });

    if (!visivel) {
      this.spotlight.set(null);
      return;
    }

    const rect = visivel.getBoundingClientRect();
    const pad = 6;
    this.spotlight.set({
      top: Math.max(4, rect.top - pad),
      left: Math.max(4, rect.left - pad),
      width: rect.width + pad * 2,
      height: rect.height + pad * 2,
    });
  }
}
