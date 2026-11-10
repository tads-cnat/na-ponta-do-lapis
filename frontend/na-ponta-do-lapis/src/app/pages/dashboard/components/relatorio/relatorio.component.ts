import { Component, Input, OnChanges, inject } from "@angular/core";
import { IContas } from "../../../../model/IContas.models";
import { ITransacoes } from "../../../../model/ITransacoes.model";
import { ChartModule } from "primeng/chart";
import { PreferenciaMoedaService } from "../../../../shared/service/preferencia-moeda.service";

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
    <section class="bg-white rounded-2xl p-6 shadow-sm border border-slate-100">

      <div class="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 mb-5">
        <div>
          <h2 class="section-title-npl">
            Relatório mensal
          </h2>
          <p class="text-xs text-slate-500 mt-0.5">
            Evolução acumulada do saldo ao longo dos meses
          </p>
        </div>

        <span class="badge-npl-amarelo self-start sm:self-auto">
          <span class="w-1.5 h-1.5 rounded-full bg-[#EEC046]"></span>
          Ano atual
        </span>
      </div>

      @if(chartData){
        <p-chart
            type="line"
            [data]="chartData"
            [options]="chartOptions"
            [style]="{
                height:'260px'
            }"
            class="w-full">
        </p-chart>
      }

    </section>
  `,
})
export class RelatorioComponent implements OnChanges {

    private preferenciaMoedaService = inject(PreferenciaMoedaService);

    @Input() transacoes:any[] = [];

    @Input() saldoAtual:number = 0;

    chartData: any;

    chartOptions: any;

    ngOnChanges(){
        this.gerarGrafico();
    }

    gerarGrafico(){
        const valores = this.calcularSaldoMensal();

        this.chartData = {
            labels: [
                'Jan','Fev','Mar',
                'Abr','Mai','Jun',
                'Jul', 'Ago', 'Set',
                'Out', 'Nov', 'Dez'
            ],
            datasets:[
              {
                    label:'Saldo',
                    data: valores,
                    fill:true,
                    tension:0.4,
                    borderColor:'#7843CD',
                    backgroundColor: 'rgba(120, 67, 205, 0.10)',
                    borderWidth: 2.5,
                    pointRadius: 4,
                    pointBackgroundColor: '#EEC046',
                    pointBorderColor: '#7843CD',
                    pointBorderWidth: 2,
                    pointHoverRadius: 6,
                    pointHoverBackgroundColor: '#EEC046',
                    pointHoverBorderColor: '#7843CD'
                }
            ]
        };
        this.chartOptions = {
            maintainAspectRatio: false,
            plugins:{
                legend:{ display:false }
            },
            scales:{
                y:{
                    ticks:{
                        callback:(value:any)=>{
                          const simbolo = this.preferenciaMoedaService.obterSimboloMoeda();
                          return `${simbolo} ` + value.toLocaleString('pt-BR');
                        }
                    }
                }
            }
        };
    }
    private calcularSaldoMensal(): number[] {
      const movimentacoes = new Array(12).fill(0);

      for(const t of this.transacoes){
        const data = new Date(t.dataHora);
        const mes = data.getMonth();

        if(t.tipo === 'RECEITA'){
          movimentacoes[mes] += t.valor;
        }

        if(t.tipo === 'DESPESA'){
          movimentacoes[mes] -= t.valor;
        }

      }

      const resultado = new Array(12);

      let saldo = 0;

      for(let i = 0; i < 12; i++){
        saldo += movimentacoes[i];
        resultado[i] = saldo;
      }

      return resultado;
    }
}
