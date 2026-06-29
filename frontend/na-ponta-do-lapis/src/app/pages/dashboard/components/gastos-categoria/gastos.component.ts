import { Component, Input, OnChanges } from "@angular/core";
import { ChartModule } from "primeng/chart";
import { ITransacoes } from "../../../../model/ITransacoes.model";

// ─── Paleta fixa: 20 categorias com cores únicas permanentes ─────────────────
// A ORDEM aqui é a mesma que será usada no chart e na legenda.
// Categorias sem transações aparecem na legenda com 0% mas NÃO geram fatia visual
// no doughnut (Chart.js ignora segmentos com valor 0 — comportamento correto).
const CATEGORIAS: { nome: string; cor: string }[] = [
  { nome: 'Supermercado',           cor: '#E74C3C' },
  { nome: 'Restaurantes',           cor: '#E67E22' },
  { nome: 'Academia',               cor: '#F1C40F' },
  { nome: 'Assinaturas Streaming',  cor: '#2ECC71' },
  { nome: 'Combustível',            cor: '#3498DB' },
  { nome: 'Manutenção Carro',       cor: '#9B59B6' },
  { nome: 'Estacionamento',         cor: '#1ABC9C' },
  { nome: 'Pedágio',                cor: '#95A5A6' },
  { nome: 'Cinema',                 cor: '#E91E63' },
  { nome: 'Jogos Online',           cor: '#FF5722' },
  { nome: 'Viagens',                cor: '#00BCD4' },
  { nome: 'Hospedagem',             cor: '#8BC34A' },
  { nome: 'Farmácia',               cor: '#673AB7' },
  { nome: 'Consultas Médicas',      cor: '#FF9800' },
  { nome: 'Exames',                 cor: '#009688' },
  { nome: 'Suplementos',            cor: '#F06292' },
  { nome: 'Cursos Online',          cor: '#26C6DA' },
  { nome: 'Livros',                 cor: '#FFA726' },
  { nome: 'Material Escolar',       cor: '#AB47BC' },
  { nome: 'Mensalidade Faculdade',  cor: '#5C6BC0' },
];

interface CategoriaVM {
  nome:       string;
  cor:        string;
  valor:      number;
  percentual: string;
}

@Component({
  selector: 'app-gastos-categoria',
  standalone: true,
  imports: [ChartModule],
  template: `
    <div class="bg-white rounded-2xl p-5 shadow-sm h-full flex flex-col">

      <h2 class="text-base font-bold text-gray-800 text-center mb-4">
        Gastos por categoria
      </h2>

      <div class="flex items-start gap-4 flex-1 min-h-0">

        <!-- Doughnut chart -->
        <div class="flex-shrink-0 flex items-center justify-center"
             style="width:185px; height:185px;">
          @if (temDados) {
            <p-chart
              type="doughnut"
              [data]="chartData"
              [options]="chartOptions"
              [style]="{ height: '185px', width: '185px' }">
            </p-chart>
          } @else {
            <!--
              Quando não há despesas: mantém o visual do donut vazio
              mas exibe todas as categorias na legenda com 0%.
            -->
            <div class="w-full h-full rounded-full flex items-center justify-center"
                 style="border: 26px solid #f1f5f9;">
              <span class="text-[11px] text-gray-400 text-center leading-snug">
                Sem<br>despesas
              </span>
            </div>
          }
        </div>

        <!--
          Legenda scrollável — exibe TODAS as 20 categorias SEMPRE,
          independentemente de haver transações nelas.
          Categorias sem dados mostram "0%" para preservar a cor/rótulo.
        -->
        <div class="flex-1 overflow-y-auto min-w-0 pr-0.5" style="max-height: 185px;">
          @for (cat of categorias; track cat.nome) {
            <div class="flex items-center gap-1.5 py-[3px]">
              <span class="w-2 h-2 rounded-full flex-shrink-0"
                    [style.background-color]="cat.cor">
              </span>
              <span class="text-[11px] text-gray-600 flex-1 min-w-0 truncate">
                {{ cat.nome }}
              </span>
              <span class="text-[11px] font-semibold text-gray-800 tabular-nums ml-1 flex-shrink-0">
                {{ cat.percentual }}
              </span>
            </div>
          }
        </div>

      </div>
    </div>
  `,
})
export class GastosCategoriaComponent implements OnChanges {

  @Input() transacoes: ITransacoes[] = [];

  chartData:    any;
  chartOptions: any;
  categorias:   CategoriaVM[] = CATEGORIAS.map(c => ({ ...c, valor: 0, percentual: '0%' }));
  temDados:     boolean = false;

  ngOnChanges(): void {
    this.calcularGastos();
    this.configurarOpcoes();
  }

  // ─── Cálculo ─────────────────────────────────────────────────────────────────

  private calcularGastos(): void {
    // Soma despesas REALIZADAS por nome de categoria
    const totalPorNome: Record<string, number> = {};

    this.transacoes
      .filter(t => t.tipo === 'DESPESA' && t.estado !== 'PENDENTE' && t.categoria != null)
      .forEach(t => {
        const nome = t.categoria!.nome;
        totalPorNome[nome] = (totalPorNome[nome] ?? 0) + t.valor;
      });

    const totalGeral = Object.values(totalPorNome).reduce((a, b) => a + b, 0);
    this.temDados    = totalGeral > 0;

    // Reconstrói lista sempre com TODAS as 20 categorias (valor 0 se sem dados)
    this.categorias = CATEGORIAS.map(cat => {
      const valor = totalPorNome[cat.nome] ?? 0;
      return {
        ...cat,
        valor,
        percentual: totalGeral > 0
          ? (valor / totalGeral * 100).toFixed(1) + '%'
          : '0%',
      };
    });

    if (this.temDados) {
      this.chartData = {
        labels:   this.categorias.map(c => c.nome),
        datasets: [{
          // Categorias com valor 0: sem fatia visual no chart (correto).
          // A legenda customizada mostra todas independentemente.
          data:            this.categorias.map(c => c.valor),
          backgroundColor: this.categorias.map(c => c.cor),
          borderColor:     '#ffffff',
          borderWidth:     2,
        }]
      };
    } else {
      this.chartData = null;
    }
  }

  // ─── Opções Chart.js ─────────────────────────────────────────────────────────

  private configurarOpcoes(): void {
    this.chartOptions = {
      responsive:          true,
      maintainAspectRatio: false,
      cutout:              '62%',
      plugins: {
        legend: { display: false },   // legenda customizada na template
        tooltip: {
          callbacks: {
            label: (ctx: any) => {
              const val = (ctx.parsed as number)
                .toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
              return ` ${ctx.label}: R$ ${val}`;
            }
          }
        }
      }
    };
  }
}
