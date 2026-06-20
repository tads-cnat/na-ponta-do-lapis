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

      <!-- HEADER -->
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
          (onChange)="mudarPeriodo($event.value)"
          optionLabel="label"
          optionValue="value"
          placeholder="Período"
          styleClass="w-full lg:w-44">
        </p-select>

      </div>

      <!-- CHART -->
      @if (chartData && chartData.datasets?.length) {
        <p-chart
          type="line"
          [data]="chartData"
          [options]="chartOptions"
          [plugins]="chartPlugins"
          [style]="{ height: '360px' }"
          styleClass="w-full">
        </p-chart>
      } @else {
        <div
          class="h-[360px] rounded-2xl border border-slate-100 bg-slate-50
                 flex items-center justify-center">
          <span class="text-slate-400 text-sm">
            Selecione uma conta para visualizar o histórico
          </span>
        </div>
      }

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

  /** Emite o novo período ('semanal' | 'mensal') ao ContasComponent
   *  para que atualizarGraficoSaldo() seja recalculado com os dados corretos. */
  @Output() onPeriodoMudou = new EventEmitter<string>();

  // =========================================
  // ESTADO LOCAL (UI do seletor)
  // =========================================

  periodoSelecionado: string = 'mensal';

  readonly periodosGrafico = [
    { label: 'Semanal', value: 'semanal' },
    { label: 'Mensal',  value: 'mensal'  }
  ];

  // =========================================
  // PLUGIN INLINE — crosshair vertical no hover
  // =========================================

  /**
   * Plugin Chart.js para desenhar a linha vertical tracejada ao passar o mouse,
   * replicando o estilo da imagem de referência.
   * Definido aqui porque é estritamente uma responsabilidade de apresentação
   * do gráfico, sem nenhuma lógica de domínio.
   */
  readonly chartPlugins = [
    {
      id: 'verticalCrosshair',
      afterDraw(chart: any): void {
        const active = chart.tooltip?._active;
        if (!active?.length) return;

        const ctx   = chart.ctx as CanvasRenderingContext2D;
        const x     = active[0].element.x as number;
        const yScale = chart.scales['y'];

        ctx.save();
        ctx.beginPath();
        ctx.setLineDash([4, 4]);
        ctx.moveTo(x, yScale.top);
        ctx.lineTo(x, yScale.bottom);
        ctx.lineWidth   = 1;
        ctx.strokeStyle = 'rgba(249, 115, 22, 0.55)';
        ctx.stroke();
        ctx.restore();
      }
    }
  ];

  // =========================================
  // HANDLERS
  // =========================================

  mudarPeriodo(novoValor: string): void {
    this.periodoSelecionado = novoValor;
    this.onPeriodoMudou.emit(novoValor);
  }

}
