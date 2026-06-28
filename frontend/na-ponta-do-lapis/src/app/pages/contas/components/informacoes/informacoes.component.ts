import { Component, Input } from '@angular/core';
import { IContas, Moeda } from '../../../../model/IContas.models';

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

        <!-- GRID 2 colunas -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">

          <!-- SALDO -->
          <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5 min-w-0">

            <p class="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-3">
              Saldo Atual
            </p>

            <span class="text-2xl font-bold text-slate-900 break-words leading-tight">
              {{
                conta.saldo.toLocaleString(
                  localeDaMoeda(conta.moeda),
                  { style: 'currency', currency: conta.moeda }
                )
              }}
            </span>

          </div>

          <!-- MOEDA -->
          <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5 min-w-0">

            <p class="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-3">
              Moeda
            </p>

            <div class="flex items-center gap-3">

              <!-- Badge com símbolo -->
              <span
                class="inline-flex items-center justify-center
                       w-11 h-11 rounded-xl
                       bg-orange-100 text-orange-600
                       text-base font-bold shrink-0">
                {{ simboloMoeda(conta.moeda) }}
              </span>

              <div class="flex flex-col min-w-0">
                <span class="text-lg font-bold text-slate-900 truncate">
                  {{ conta.moeda }}
                </span>
                <span class="text-sm text-slate-500 truncate">
                  {{ nomeMoeda(conta.moeda) }}
                </span>
              </div>

            </div>

          </div>

          <!-- COR -->
          <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5">

            <p class="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-3">
              Cor do Cartão
            </p>

            <div class="flex items-center gap-3">
              <div
                class="w-11 h-11 rounded-xl border border-slate-200 shrink-0"
                [style.background]="conta.cor">
              </div>
              <span class="text-sm text-slate-500 font-mono uppercase">
                {{ conta.cor }}
              </span>
            </div>

          </div>

        </div>

      </div>
    }
  `
})
export class InformacoesComponent {

  @Input() conta: IContas | null = null;

  localeDaMoeda(moeda: Moeda | null): string {
    switch (moeda) {
      case 'USD': return 'en-US';
      case 'EUR': return 'de-DE';
      default:    return 'pt-BR';
    }
  }

  simboloMoeda(moeda: Moeda | null): string {
    const simbolos: Record<string, string> = { BRL: 'R$', USD: 'US$', EUR: '€' };
    return simbolos[moeda ?? 'BRL'] ?? moeda ?? '';
  }

  nomeMoeda(moeda: Moeda | null): string {
    const nomes: Record<string, string> = {
      BRL: 'Real brasileiro',
      USD: 'Dólar americano',
      EUR: 'Euro'
    };
    return nomes[moeda ?? 'BRL'] ?? 'Moeda desconhecida';
  }

}
