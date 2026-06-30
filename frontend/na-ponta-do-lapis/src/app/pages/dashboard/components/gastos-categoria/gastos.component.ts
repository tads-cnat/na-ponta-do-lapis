import { Component, Input, OnChanges } from "@angular/core";
import { ChartModule } from "primeng/chart";
import { Categoria, ITransacoes } from "../../../../model/ITransacoes.model";

// ─── Paleta de cores ──────────────────────────────────────────────────────────
// Não há mais nomes fixos: as categorias são as que vêm de GET /categorias
// (CRUD do usuário, ex.: Alimentação, Transporte, Saúde...). A cor de cada
// categoria é atribuída pela posição (id ou índice) dentro desta paleta,
// repetindo em ciclo caso existam mais categorias do que cores aqui.
const PALETA_CORES: string[] = [
  '#E74C3C', '#E67E22', '#F1C40F', '#2ECC71', '#3498DB',
  '#9B59B6', '#1ABC9C', '#95A5A6', '#E91E63', '#FF5722',
  '#00BCD4', '#8BC34A', '#673AB7', '#FF9800', '#009688',
  '#F06292', '#26C6DA', '#FFA726', '#AB47BC', '#5C6BC0',
];

interface CategoriaVM {
  id:         number | null;
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
          Legenda scrollável — exibe TODAS as categorias do usuário (vindas de
          GET /categorias) SEMPRE, independentemente de haver transações nelas.
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
  @Input() categorias_disponiveis: Categoria[] = [];

  chartData:    any;
  chartOptions: any;
  categorias:   CategoriaVM[] = [];
  temDados:     boolean = false;

  ngOnChanges(): void {
    this.calcularGastos();
    this.configurarOpcoes();
  }

  // ─── Cálculo ─────────────────────────────────────────────────────────────────

  private calcularGastos(): void {
    // Soma despesas REALIZADAS por id de categoria (id é a identidade estável;
    // nome é só exibição, então nunca deve ser usado como chave de agregação).
    const totalPorId: Record<number, number> = {};

    this.transacoes
      .filter(t => t.tipo === 'DESPESA' && t.estado !== 'PENDENTE' && t.categoria != null)
      .forEach(t => {
        const id = t.categoria!.id;
        totalPorId[id] = (totalPorId[id] ?? 0) + t.valor;
      });

    const totalGeral = Object.values(totalPorId).reduce((a, b) => a + b, 0);
    this.temDados    = totalGeral > 0;

    // Base de categorias: usa a lista real vinda de GET /categorias quando
    // disponível; se ainda não tiver chegado, cai de volta para as categorias
    // que aparecem nas próprias transações (evita tela vazia por timing).
    const baseCategorias: Categoria[] = this.categorias_disponiveis.length
      ? this.categorias_disponiveis
      : this.extrairCategoriasDasTransacoes();

    this.categorias = baseCategorias.map((cat, index) => {
      const valor = totalPorId[cat.id] ?? 0;
      return {
        id:   cat.id,
        nome: cat.nome,
        cor:  PALETA_CORES[index % PALETA_CORES.length],
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

  /** Fallback: se a lista de categorias do backend ainda não chegou via
   *  @Input(), reconstrói a partir das próprias transações para não deixar
   *  a legenda vazia enquanto isso. */
  private extrairCategoriasDasTransacoes(): Categoria[] {
    const vistos = new Map<number, Categoria>();
    this.transacoes
      .filter(t => t.categoria != null)
      .forEach(t => vistos.set(t.categoria!.id, t.categoria!));
    return [...vistos.values()];
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
