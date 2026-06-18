import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChartModule } from 'primeng/chart';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';

@Component({
  selector: 'app-grafico-conta',
  standalone: true,
  imports: [PrimeNGModuleModule, ChartModule, FormsModule],
  template: `
    <div class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100">

      <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-6">

        <div>
          <h2 class="text-3xl font-bold text-slate-900">
            Evolução do saldo
          </h2>
          <p class="text-slate-500 mt-1">
            Histórico financeiro da conta selecionada
          </p>
        </div>

        <p-select
          [options]="periodosGrafico"
          [(ngModel)]="periodoSelecionado"
          (onChange)="onPeriodoMudou.emit(periodoSelecionado)"
          optionLabel="label"
          optionValue="value"
          placeholder="Período"
          styleClass="w-full lg:w-44">
        </p-select>

      </div>

      <div class="h-[360px] rounded-2xl border border-slate-100 bg-slate-50 flex items-center justify-center">
        <span class="text-slate-400 text-lg">
          Gráfico futuramente
        </span>
      </div>

    </div>
  `
})
export class GraficoComponent {

  // =========================================
  // INPUTS
  // =========================================

  @Input() chartData: any;
  @Input() chartOptions: any;

  // =========================================
  // OUTPUTS
  // =========================================

  /** Emite o novo período ('semanal' | 'mensal') ao pai para que
   *  atualizarGraficoSaldo() seja chamado com o dado correto. */
  @Output() onPeriodoMudou = new EventEmitter<string>();

  // =========================================
  // ESTADO LOCAL
  // =========================================

  /** Período é estado da UI do gráfico — vive aqui, não no pai. */
  periodoSelecionado: string = 'semanal';

  readonly periodosGrafico = [
    { label: 'Semanal', value: 'semanal' },
    { label: 'Mensal',  value: 'mensal'  }
  ];

}
