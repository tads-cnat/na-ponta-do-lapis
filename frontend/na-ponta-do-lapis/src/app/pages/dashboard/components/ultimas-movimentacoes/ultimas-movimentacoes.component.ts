import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { ITransacoes } from '../../../../model/ITransacoes.model';
import { PreferenciaMoedaService } from '../../../../shared/service/preferencia-moeda.service';

@Component({
  selector: 'app-ultimas-movimentacoes',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe],
  template: `
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-100 h-full flex flex-col gap-4">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-bold text-slate-800 m-0">Últimas movimentações</h2>
          <p class="text-xs text-slate-400 m-0 mt-0.5">Lançamentos mais recentes</p>
        </div>
        <button
          type="button"
          (click)="onIrParaTransacoes.emit()"
          class="text-xs font-semibold text-[#7843CD] hover:text-[#6214CC] hover:underline cursor-pointer bg-transparent border-0 p-0">
          Ver todas
        </button>
      </div>

      @if (recentes.length === 0) {
        <div class="py-6 text-center text-sm text-slate-400">
          Nenhuma movimentação registrada ainda.
        </div>
      } @else {
        <ul class="flex flex-col divide-y divide-slate-100 m-0 p-0 list-none">
          @for (item of recentes; track item.id ?? $index) {
            <li class="py-2.5 first:pt-0 last:pb-0 flex items-center justify-between gap-3">
              <div class="flex items-center gap-3 min-w-0">
                <div
                  class="w-9 h-9 rounded-xl flex items-center justify-center shrink-0"
                  [class]="item.tipo === 'RECEITA' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'">
                  <i [class]="item.tipo === 'RECEITA' ? 'pi pi-arrow-up-right text-sm' : 'pi pi-arrow-down-left text-sm'"></i>
                </div>
                <div class="min-w-0">
                  <p class="text-sm font-semibold text-slate-800 truncate m-0">
                    {{ item.descricao || 'Sem descrição' }}
                  </p>
                  <p class="text-xs text-slate-400 truncate m-0">
                    {{ item.categoria?.nome || 'Sem categoria' }} · {{ item.dataHora | date:'dd/MM' }}
                  </p>
                </div>
              </div>

              <span
                class="text-sm font-bold whitespace-nowrap shrink-0"
                [class]="item.tipo === 'RECEITA' ? 'text-emerald-600' : 'text-rose-600'">
                {{ item.tipo === 'RECEITA' ? '+' : '-' }}
                {{ (item.valor || 0) | currency:moedaService.obterMoedaAtiva():'symbol':'1.2-2' }}
              </span>
            </li>
          }
        </ul>
      }
    </div>
  `,
})
export class UltimasMovimentacoesComponent {
  readonly moedaService = inject(PreferenciaMoedaService);
  @Input() transacoes: ITransacoes[] = [];
  @Output() onIrParaTransacoes = new EventEmitter<void>();

  get recentes(): ITransacoes[] {
    if (!this.transacoes?.length) {
      return [];
    }
    return [...this.transacoes]
      .sort((a, b) => {
        const ta = a.dataHora ? new Date(a.dataHora).getTime() : 0;
        const tb = b.dataHora ? new Date(b.dataHora).getTime() : 0;
        return tb - ta;
      })
      .slice(0, 4);
  }
}

