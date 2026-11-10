import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NgClass } from '@angular/common';
import { AppMoedaPipe } from '../../../../shared/pipes/app-moeda.pipe';
import { IContas, ehCartaoCredito } from '../../../../model/IContas.models';
import { IconeBancoComponent } from '../../../../shared/components/icone-banco/icone-banco.component';
import { obterCorOficialBanco, temBancoDetectado } from '../../../../shared/utils/banco-icon.helper';

@Component({
  selector: 'app-carrossel-cartoes',
  standalone: true,
  imports: [NgClass, AppMoedaPipe, IconeBancoComponent],
  template: `
    <div class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100 overflow-hidden">

      <!-- TOPO -->
      <div class="flex items-center justify-between mb-8">

        <div>
          <div class="flex items-center gap-2.5">
            <h2 class="section-title-npl m-0">
              Suas Contas
            </h2>
            <span class="badge-npl-amarelo">
              <span class="w-1.5 h-1.5 rounded-full bg-[#EEC046]"></span>
              {{ contas.length }} {{ contas.length === 1 ? 'conta' : 'contas' }}
            </span>
          </div>
          <p class="text-slate-500 mt-0.5 text-xs">
            Navegue entre as contas cadastradas
          </p>
        </div>

        <!-- SETAS -->
        <div class="flex items-center gap-3">

          <button
            type="button"
            [disabled]="contas.length <= 1"
            (click)="contas.length > 1 && onAnterior.emit()"
            [ngClass]="contas.length <= 1
              ? 'bg-slate-100 text-slate-400 opacity-40 cursor-not-allowed shadow-none'
              : 'bg-[#7843CD] text-white hover:bg-[#6214CC] cursor-pointer shadow-sm hover:scale-105 active:scale-95'"
            class="w-11 h-11 rounded-full transition-all flex items-center justify-center"
            [attr.aria-disabled]="contas.length <= 1"
            [title]="contas.length <= 1 ? 'Navegação desabilitada (apenas 1 conta cadastrada)' : 'Conta anterior'">
            <i class="pi pi-chevron-left text-base"></i>
          </button>

          <button
            type="button"
            [disabled]="contas.length <= 1"
            (click)="contas.length > 1 && onProxima.emit()"
            [ngClass]="contas.length <= 1
              ? 'bg-slate-100 text-slate-400 opacity-40 cursor-not-allowed shadow-none'
              : 'bg-[#7843CD] text-white hover:bg-[#6214CC] cursor-pointer shadow-sm hover:scale-105 active:scale-95'"
            class="w-11 h-11 rounded-full transition-all flex items-center justify-center"
            [attr.aria-disabled]="contas.length <= 1"
            [title]="contas.length <= 1 ? 'Navegação desabilitada (apenas 1 conta cadastrada)' : 'Próxima conta'">
            <i class="pi pi-chevron-right text-base"></i>
          </button>

        </div>

      </div>

      <!-- ÁREA DO CARROSSEL -->
      <div class="relative h-[280px] flex items-center justify-center overflow-hidden">

        @if (!carregando && contas.length > 0) {

          @for (conta of contas; track conta.id; let i = $index) {

            <div
              (click)="onContaSelecionada.emit(i)"
              class="absolute transition-all duration-500 ease-in-out cursor-pointer"
              [ngClass]="obterClassesPosicao(i)">

              <!-- CARD -->
              <div
                class="relative w-[400px] max-w-[82vw] h-[220px] rounded-[2rem] overflow-hidden shadow-2xl transition-transform hover:scale-[1.02]"
                [title]="conta.nome"
                [style.background]="obterCorCartao(conta)">

                <!-- EFEITOS -->
                <div class="absolute top-[-30px] right-[-20px] w-52 h-52 rounded-full border border-white/20"></div>
                <div class="absolute top-[-70px] right-[20px] w-72 h-72 rounded-full border border-white/10"></div>
                <div class="absolute inset-0 bg-black/10"></div>

                <!-- CONTEÚDO -->
                <div class="relative z-10 h-full p-7 flex flex-col justify-between text-white">

                  <div class="flex items-start justify-between gap-4">

                    <div class="min-w-0 flex-1">
                      @if (conta.sincronizada) {
                        <span class="inline-flex items-center gap-1.5 text-[11px] bg-[#EEC046] text-slate-900 px-2.5 py-0.5 rounded-full font-bold tracking-wide shadow-xs">
                          <svg class="w-3.5 h-3.5 inline-block shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2.5">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M3 21h18M3 10h18M5 10v11M9 10v11M15 10v11M19 10v11M12 3l9 5H3l9-5z"/>
                          </svg>
                          <span>Open Finance</span>
                        </span>
                      }
                      <h3 class="text-2xl font-bold mt-2 leading-snug line-clamp-2 break-words"
                          [title]="conta.nome">
                        {{ conta.nome }}
                      </h3>
                    </div>

                    <div class="shrink-0 flex items-center justify-center transition-transform hover:scale-105 select-none filter drop-shadow-[0_4px_8px_rgba(0,0,0,0.35)] drop-shadow-[0_1px_2px_rgba(0,0,0,0.25)]">
                      @if (temBanco(conta.nome)) {
                        <app-icone-banco
                          [nome]="conta.nome"
                          [tamanho]="46"
                          [formato]="'quadrado'">
                        </app-icone-banco>
                      } @else {
                        <div class="w-11 h-11 flex items-center justify-center text-white">
                          <i [class]="(ehCartao(conta) ? 'pi pi-credit-card' : 'pi pi-wallet') + ' text-3xl text-white'"></i>
                        </div>
                      }
                    </div>

                  </div>

                  <!-- RODAPÉ DO CARD: SALDO / FATURA -->
                  <div class="flex items-end justify-between pt-2 border-t border-white/20">
                    <div>
                      <p class="text-[11px] font-medium text-white/80 uppercase tracking-wider">
                        {{ ehCartao(conta) ? 'Fatura Atual' : 'Saldo Disponível' }}
                      </p>
                      <p class="text-xl font-bold tracking-tight">
                        {{ ehCartao(conta) ? '-' : '' }} {{ conta.saldo | appMoeda }}
                      </p>
                    </div>
                    @if (ehCartao(conta) && (conta.limiteDisponivel || conta.limiteTotal)) {
                      <div class="text-right text-[11px] text-white/90">
                        <span class="opacity-80">Disp.: </span>
                        <span class="font-bold">{{ (conta.limiteDisponivel ?? (conta.limiteTotal! - conta.saldo)) | appMoeda }}</span>
                      </div>
                    }
                  </div>

                </div>

              </div>

            </div>

          }

        }

        @if (carregando) {
          <div class="h-full flex items-center justify-center text-slate-400">
            Carregando contas...
          </div>
        }

        @if (!carregando && contas.length === 0) {
          <div class="h-full flex items-center justify-center text-slate-400">
            Nenhuma conta cadastrada
          </div>
        }

      </div>

      <!-- INDICADORES -->
      @if (contas.length > 1) {
        <div class="flex justify-center items-center gap-3 mt-5">

          @for (conta of contas; track conta.id; let i = $index) {
            <button
              type="button"
              (click)="onContaSelecionada.emit(i)"
              class="rounded-full transition-all duration-300"
              [ngClass]="
                i === contaSelecionadaIndex
                  ? 'w-10 h-3 bg-[#EEC046] shadow-xs'
                  : 'w-3 h-3 bg-slate-300 hover:bg-slate-400'
              ">
            </button>
          }

        </div>
      }

    </div>
  `
})
export class CarrosselCartoesComponent {

  @Input() contas: IContas[] = [];
  @Input() contaSelecionadaIndex: number = 0;
  @Input() contaAnterior: number = 0;
  @Input() contaProxima: number = 0;
  @Input() carregando: boolean = true;

  @Output() onContaSelecionada = new EventEmitter<number>();
  @Output() onAnterior = new EventEmitter<void>();
  @Output() onProxima = new EventEmitter<void>();

  ehCartao(conta: IContas): boolean {
    return ehCartaoCredito(conta);
  }

  temBanco(nome?: string | null): boolean {
    return temBancoDetectado(nome);
  }

  obterClassesPosicao(i: number): Record<string, boolean> {
    // 1 conta: sempre no centro absoluto
    if (this.contas.length <= 1) {
      return {
        'z-30 opacity-100 scale-100 translate-x-0': true
      };
    }

    // 2 contas: a selecionada fica no centro e a outra fica à direita
    if (this.contas.length === 2) {
      const isSelecionada = i === this.contaSelecionadaIndex;
      return {
        'z-30 opacity-100 scale-100 translate-x-0': isSelecionada,
        'z-20 opacity-40 scale-90 translate-x-[72%]': !isSelecionada
      };
    }

    // 3 ou mais contas: carrossel tridimensional com esquerda, centro e direita
    const isSelecionada = i === this.contaSelecionadaIndex;
    const isAnterior = i === this.contaAnterior;
    const isProxima = i === this.contaProxima;

    return {
      'z-30 opacity-100 scale-100 translate-x-0': isSelecionada,
      'z-20 opacity-40 scale-90 -translate-x-[72%]': isAnterior && !isSelecionada,
      'z-20 opacity-40 scale-90 translate-x-[72%]': isProxima && !isSelecionada && !isAnterior,
      'opacity-0 scale-75 pointer-events-none': !isSelecionada && !isAnterior && !isProxima
    };
  }

  obterCorCartao(conta: IContas): string {
    const corAtual = conta.cor ? conta.cor.toUpperCase().replace('#', '') : '';
    // Se tiver cor customizada que não seja o amarelo padrão antigo (FFDE59)
    if (corAtual && corAtual !== 'FFDE59') {
      return `#${corAtual}`;
    }
    // Busca a cor oficial da marca do banco
    const corOficial = obterCorOficialBanco(conta.nome);
    if (corOficial) {
      return corOficial;
    }
    return conta.cor ? (conta.cor.startsWith('#') ? conta.cor : `#${conta.cor}`) : '#4F46E5';
  }

}
