import { Component, Input, OnChanges, inject } from "@angular/core";
import { ChartModule } from "primeng/chart";
import { Categoria, ITransacoes } from "../../../../model/ITransacoes.model";
import { PreferenciaMoedaService } from "../../../../shared/service/preferencia-moeda.service";

// ─── Paleta de cores ──────────────────────────────────────────────────────────
// Não há mais nomes fixos: as categorias são as que vêm de GET /categorias
// (CRUD do usuário, ex.: Alimentação, Transporte, Saúde...). A cor de cada
// categoria é atribuída pela posição (id ou índice) dentro desta paleta,
// repetindo em ciclo caso existam mais categorias do que cores aqui.
// ─── Paleta de cores de alto contraste ────────────────────────────────────────
// Ordenada para máxima distinção visual entre fatias adjacentes no gráfico:
// inicia com o Roxo (#7843CD) e Amarelo Ouro (#EEC046) da marca, seguidos por
// matizes distantes no círculo cromático (Esmeralda, Carmim, Azul Oceano,
// Laranja, Turquesa, Rosa Magenta, Verde Lima, Azul Royal, Terracota, Ciano...).
const PALETA_CORES: string[] = [
  '#7843CD', // 1. Roxo NPL
  '#EEC046', // 2. Amarelo Ouro NPL
  '#10B981', // 3. Verde Esmeralda
  '#E11D48', // 4. Vermelho Carmim
  '#0284C7', // 5. Azul Oceano
  '#EA580C', // 6. Laranja Vibrante
  '#0D9488', // 7. Turquesa Escuro
  '#DB2777', // 8. Rosa Magenta
  '#65A30D', // 9. Verde Lima
  '#2563EB', // 10. Azul Royal
  '#92400E', // 11. Terracota / Âmbar Escuro
  '#06B6D4', // 12. Ciano Brilhante
  '#C026D3', // 13. Fúcsia
  '#475569', // 14. Grafite Azulado
  '#DC2626', // 15. Vermelho Vivo
  '#059669', // 16. Verde Floresta
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
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-100 h-full flex flex-col">

      <div class="mb-5">
        <h2 class="section-title-npl">
          Gastos por categoria
        </h2>
        <p class="text-xs text-slate-500 mt-0.5">
          Distribuição das despesas realizadas
        </p>
      </div>

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
        <div class="flex-1 overflow-y-auto min-w-0 pr-1" style="max-height: 185px;">
          @for (cat of categorias; track cat.nome) {
            <div class="flex items-center gap-2 py-1">
              <span class="w-3 h-3 rounded-md flex-shrink-0 shadow-2xs"
                    [style.background-color]="cat.cor">
              </span>
              <span class="text-xs text-slate-700 font-medium flex-1 min-w-0 truncate">
                {{ cat.nome }}
              </span>
              <span class="text-xs font-bold text-slate-900 tabular-nums ml-1 flex-shrink-0">
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

  private preferenciaMoedaService = inject(PreferenciaMoedaService);

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

    // Ordena primeiro as categorias com maior gasto no período para que as
    // fatias ativas recebam sempre as primeiras cores de maior contraste da paleta
    // e apareçam no topo da legenda.
    const categoriasOrdenadas = [...baseCategorias].sort((a, b) => {
      const valA = totalPorId[a.id] ?? 0;
      const valB = totalPorId[b.id] ?? 0;
      if (valB !== valA) return valB - valA;
      return a.nome.localeCompare(b.nome);
    });

    this.categorias = categoriasOrdenadas.map((cat, index) => {
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
              const simbolo = this.preferenciaMoedaService.obterSimboloMoeda();
              return ` ${ctx.label}: ${simbolo} ${val}`;
            }
          }
        }
      }
    };
  }
}
