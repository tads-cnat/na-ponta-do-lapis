import { Component, Input, OnChanges, effect, inject, untracked } from '@angular/core';
import { IContas } from '../../../../model/IContas.models';
import { ITransacoes } from '../../../../model/ITransacoes.model';
import { ChartModule } from 'primeng/chart';
import { ThemeService } from '../../../../service/theme.service';

@Component({
  selector: 'app-relatorio',
  standalone: true,
  imports: [ChartModule],
  /*
   * FIX 4: Nós de texto soltos ("Cabeçalho da seção", "Seletor de mês",
   * "Área do gráfico de linha (placeholder)") eram renderizados como texto
   * visível na página porque não estavam dentro de tags HTML ou comentários.
   * Convertidos para comentários HTML <!-- --> para manter a documentação
   * sem impacto visual.
   */
  template: `
    <section class="bg-white rounded-2xl p-6 shadow-sm">
      <div class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100">
        <div class="flex justify-between items-center mb-5">
          <h2 class="text-2xl font-bold text-slate-900">Relatório mensal</h2>

          <select
            class="
            border
            border-slate-200
            rounded-lg
            px-3
            py-2
            text-sm
            text-slate-600"
          >
            <option>Ano atual</option>
          </select>
        </div>

        @if (chartData) {
          <p-chart
            type="line"
            [data]="chartData"
            [options]="chartOptions"
            [style]="{
              height: '260px',
            }"
            class="w-full"
          >
          </p-chart>
        }
      </div>
    </section>
  `,
})
export class RelatorioComponent implements OnChanges {
  @Input() transacoes: any[] = [];

  @Input() saldoAtual: number = 0;

  private tema = inject(ThemeService);

  chartData: any;

  chartOptions: any;

  constructor() {
    // Regenera o gráfico quando o tema muda (cores de eixo/grade).
    effect(() => {
      this.tema.paletaGrafico();
      untracked(() => {
        if (this.chartData) this.gerarGrafico();
      });
    });
  }

  ngOnChanges() {
    this.gerarGrafico();
  }

  gerarGrafico() {
    const valores = this.calcularSaldoMensal();

    this.chartData = {
      labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
      datasets: [
        {
          label: 'Saldo',
          data: valores,
          fill: true,
          tension: 0.4,
          borderColor: '#3B82F6',
          backgroundColor: 'rgba(59,130,246,0.15)',
          pointRadius: 4,
        },
      ],
    };
    // No tema claro mantém os padrões do Chart.js; no escuro troca eixo/grade.
    const escuro = this.tema.escuro();
    const paleta = this.tema.paletaGrafico();
    const corTexto = escuro ? paleta.textoSuave : undefined;
    const corGrade = escuro ? paleta.grade : undefined;
    this.chartOptions = {
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
      },
      scales: {
        x: {
          ticks: { color: corTexto },
          grid: { color: corGrade },
        },
        y: {
          grid: { color: corGrade },
          ticks: {
            color: corTexto,
            callback: (value: any) => {
              return 'R$ ' + value.toLocaleString('pt-BR');
            },
          },
        },
      },
    };
  }
  private calcularSaldoMensal(): number[] {
    const movimentacoes = new Array(12).fill(0);

    for (const t of this.transacoes) {
      const data = new Date(t.dataHora);
      const mes = data.getMonth();

      if (t.tipo === 'RECEITA') {
        movimentacoes[mes] += t.valor;
      }

      if (t.tipo === 'DESPESA') {
        movimentacoes[mes] -= t.valor;
      }
    }

    const resultado = new Array(12);

    let saldo = 0;

    for (let i = 0; i < 12; i++) {
      saldo += movimentacoes[i];
      resultado[i] = saldo;
    }

    return resultado;
  }
}
