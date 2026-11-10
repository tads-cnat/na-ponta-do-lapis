import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';
import { ITransacoes } from '../../../../model/ITransacoes.model';
import { AppMoedaPipe } from '../../../../shared/pipes/app-moeda.pipe';

@Component({
  selector: 'app-transacoes-conta',
  standalone: true,
  imports: [CommonModule, PrimeNGModuleModule, AppMoedaPipe],
  template: `
    <div
      class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100 flex flex-col min-h-0">

      <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-6">

        <div>
          <h2 class="section-title-npl">
            Últimas transações
          </h2>
          <p class="text-xs text-slate-500 mt-0.5">
            Movimentações recentes
          </p>
        </div>

        <p-button
          label="Mostrar todas"
          icon="pi pi-arrow-right"
          iconPos="right"
          size="small"
          styleClass="btn-npl-secondary text-xs px-3.5 py-2"
          (onClick)="onIrParaTransacoes.emit()">
        </p-button>

      </div>

      <!-- LISTA -->
      <div class="flex-1 min-h-0 overflow-y-auto pr-2 max-h-[480px]">

        <div class="flex flex-col gap-3">

          @for (transacao of transacoesPaginadas; track transacao.id) {

            <div
              class="flex items-center justify-between gap-4 p-4 rounded-2xl hover:bg-slate-50 transition-all duration-200 border border-slate-100">

              <div class="flex items-center gap-4 min-w-0">

                <!-- ÍCONE -->
                <div class="w-12 h-12 rounded-2xl bg-[#F3E8FF] border border-[#E9D5FF] flex items-center justify-center shrink-0">
                  <i
                    class="text-[#7843CD] text-lg pi"
                    [ngClass]="obterIconeCategoria(transacao.categoria?.nome)">
                  </i>
                </div>

                <!-- INFO -->
                <div class="min-w-0 flex flex-col">
                  <span class="font-semibold text-lg truncate">
                    {{ transacao.descricao }}
                  </span>
                  <span class="text-slate-400 text-sm truncate">
                    {{ transacao.categoria?.nome }}
                  </span>
                  <span class="text-slate-400 text-sm">
                    {{ transacao.dataHora | date:'dd/MM/yyyy HH:mm' }}
                  </span>
                </div>

              </div>

              <!-- VALOR -->
              <span
                class="font-bold shrink-0"
                [ngClass]="{
                  'text-[#C24E4E]':    transacao.tipo === 'DESPESA',
                  'text-emerald-600': transacao.tipo === 'RECEITA'
                }">
                {{ transacao.valor | appMoeda }}
              </span>

            </div>

          }

        </div>

      </div>

      <!-- PAGINAÇÃO -->
      @if (ultimasTransacoes.length > transacoesPorPagina) {
        <div class="flex items-center justify-center gap-4 mt-6">

          <button
            type="button"
            (click)="onPaginaAnterior.emit()"
            [disabled]="paginaAtualTransacoes === 0"
            class="w-10 h-10 rounded-full bg-[#7843CD] text-white hover:bg-[#6214CC] disabled:opacity-40 transition flex items-center justify-center shadow-xs cursor-pointer">
            <i class="pi pi-chevron-left text-xs"></i>
          </button>

          <span class="text-slate-600 text-sm font-semibold">
            Página {{ paginaAtualTransacoes + 1 }}
          </span>

          <button
            type="button"
            (click)="onProximaPagina.emit()"
            [disabled]="(paginaAtualTransacoes + 1) * transacoesPorPagina >= ultimasTransacoes.length"
            class="w-10 h-10 rounded-full bg-[#7843CD] text-white hover:bg-[#6214CC] disabled:opacity-40 transition flex items-center justify-center shadow-xs cursor-pointer">
            <i class="pi pi-chevron-right text-xs"></i>
          </button>

        </div>
      }

    </div>
  `
})
export class TransacoesComponent {

  // =========================================
  // INPUTS
  // =========================================

  @Input() transacoesPaginadas: ITransacoes[] = [];
  @Input() ultimasTransacoes: ITransacoes[] = [];
  @Input() transacoesPorPagina: number = 5;
  @Input() paginaAtualTransacoes: number = 0;

  // =========================================
  // OUTPUTS
  // =========================================

  @Output() onProximaPagina    = new EventEmitter<void>();
  @Output() onPaginaAnterior   = new EventEmitter<void>();
  @Output() onIrParaTransacoes = new EventEmitter<void>();

  // =========================================
  // UTILITÁRIO LOCAL
  // =========================================

  /** Mapa de categoria → ícone PrimeIcons.
   *  Pertence aqui pois é usado exclusivamente na lista de transações. */
  obterIconeCategoria(categoria: string | undefined): string {

    const mapa: Record<string, string> = {
      'Supermercado':          'pi-shopping-cart',
      'Restaurantes':          'pi-shop',
      'Academia':              'pi-heart',
      'Assinaturas Streaming': 'pi-play-circle',
      'Combustível':           'pi-car',
      'Manutenção Carro':      'pi-cog',
      'Estacionamento':        'pi-map-marker',
      'Pedágio':               'pi-directions',
      'Cinema':                'pi-video',
      'Jogos Online':          'pi-desktop',
      'Viagens':               'pi-send',
      'Hospedagem':            'pi-home',
      'Farmácia':              'pi-plus-circle',
      'Consultas Médicas':     'pi-user-plus',
      'Exames':                'pi-search',
      'Suplementos':           'pi-bolt',
      'Cursos Online':         'pi-book',
      'Livros':                'pi-bookmark',
      'Material Escolar':      'pi-pencil',
      'Mensalidade Faculdade': 'pi-building-columns'
    };

    return mapa[categoria ?? ''] ?? 'pi-wallet';
  }

}
