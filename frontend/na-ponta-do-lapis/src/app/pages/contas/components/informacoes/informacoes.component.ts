import { Component, Input } from '@angular/core';
import { IContas } from '../../../../model/IContas.models';

@Component({
  selector: 'app-informacoes-conta',
  standalone: true,
  imports: [],
  template: `
    @if (conta) {
      <div class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100">

        <div class="flex items-center justify-between mb-6">
          <div>
            <h2 class="text-2xl font-bold text-slate-900">
              Informações da Conta
            </h2>
            <p class="text-slate-500">
              Resumo da conta selecionada
            </p>
          </div>
        </div>

        <!-- GRID -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

          <!-- SALDO -->
          <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5 min-w-0">

            <span class="text-2xl text-slate-700 font-bold">
              Saldo Atual
            </span>

            <div class="mt-3">
              <span class="text-2xl font-bold text-slate-900 break-words leading-tight">
                {{
                  conta.saldo.toLocaleString('pt-BR', {
                    style: 'currency',
                    currency: 'BRL'
                  })
                }}
              </span>
            </div>

          </div>

          <!-- COR -->
          <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5">

            <span class="text-slate-700 font-bold">
              Cor da Conta
            </span>

            <div class="mt-4 flex items-center">
              <div
                class="w-10 h-10 rounded-full border border-slate-200 shrink-0"
                [style.background]="conta.cor">
              </div>
            </div>

          </div>

        </div>

      </div>
    }
  `
})
export class InformacoesComponent {

  @Input() conta: IContas | null = null;

}
