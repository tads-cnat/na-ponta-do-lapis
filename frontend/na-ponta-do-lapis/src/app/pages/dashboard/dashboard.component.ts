import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CardsResumoComponent } from './components/cards/cards-resumo.component';
import { RelatorioComponent } from './components/relatorio/relatorio.component';
import { GastosCategoriaComponent } from './components/gastos-categoria/gastos.component';
import { MetasDashboardComponent } from './components/metas/metas.component';
import { IContas } from '../../model/IContas.models';
import { ITransacoes } from '../../model/ITransacoes.model';
import { MetaRequest, MetaResponse } from '../../model/IMetas.models';
import { ContaFinanceiraService } from '../contas/service/contas.service';
import { MessageService } from 'primeng/api';
import { MetasService } from '../metas/services/metas.service';
import { TransacoesService } from '../transacoes/service/transacoes.service';
import { DashboardService } from './service/dashboard.service';
import { ICotacao } from '../../model/ICotacao.models';
import { Categoria } from '../../model/ITransacoes.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    PrimeNGModuleModule,
    CardsResumoComponent,
    RelatorioComponent,
    GastosCategoriaComponent,
    MetasDashboardComponent
  ],
  /*
   * FIX 1: <main> substituído por <div class="flex flex-col gap-6">.
   *
   * Por quê importa:
   * - A regra global `main > *:not(p-toast):not(router-outlet)` em styles.css aplica
   *   `display:flex; flex-direction:column; flex:1 1 0%` em qualquer filho direto de
   *   qualquer elemento <main>. Com o <main> aninhado aqui, essa regra atingia
   *   <app-cards-resumo>, <app-relatorio> e — criticamente — a <section class="grid
   *   grid-cols-2"> do bloco inferior, sobrescrevendo `display:grid` por `display:flex`
   *   e quebrando o layout de 2 colunas.
   * - Além disso, `flex` sem `flex-col` colocava todas as seções lado a lado na horizontal.
   * - <main> aninhado dentro de outro <main> também é HTML inválido semanticamente.
   *
   * Solução: usar <div> com flex-col + gap-6. O host <app-dashboard> já recebe
   * `flex:1 1 0%; display:flex; flex-direction:column` pela regra global (ele é filho
   * direto do <main> do main-layout), então o wrapper <div> se comporta corretamente.
   */
  template: `
    <div class="flex flex-col gap-6 overflow-y-auto" >

      <!-- Seção 1: Cards de resumo (4 colunas) -->
      <!-- FIX 2: class="block w-full" garante que o host element do componente
           seja block-level e ocupe a largura total dentro do flex-col pai.
           Sem isso, componentes Angular são display:inline por padrão. -->
      <app-cards-resumo
        class="block w-full"
        [contas]="contas"
        [transacoes]="transacoes"
        [cotacaoDolar]="cotacaoDolar"
        [cotacaoEuro]="cotacaoEuro"
        >
        </app-cards-resumo>

      <!-- Seção 2: Relatório mensal (largura total) -->
      <app-relatorio
      class="block w-full"
      [transacoes]="transacoes"
      [saldoAtual]="saldoAtual"
      >
      </app-relatorio>

      <!-- Seção 3: Gastos por categoria + Metas (grid 2 colunas)
           FIX 3: Removido h-[320px] — a altura fixa era insuficiente para 5 metas
           com gap-5 (~440px necessários vs 320px disponíveis). Conteúdo auto-dimensiona. -->
      <section class="grid grid-cols-1 lg:grid-cols-2 gap-4">

        <app-gastos-categoria
        class="block"
        [transacoes]="transacoes"
        [categorias_disponiveis]="categorias"
        >
        </app-gastos-categoria>

        <app-metas-dashboard
          class="block"
          [metas]="metas"
          (onIrParaMetas)="irParaMetas()">
        </app-metas-dashboard>

      </section>

    </div>
  `,
})
export class DashboardComponent implements OnInit {

  constructor(private router: Router) {

  }

  contaService = inject(ContaFinanceiraService);

  transacaoService = inject(TransacoesService);

  dashboardService = inject(DashboardService);

  private messageService = inject(MessageService);

  private cdr = inject(ChangeDetectorRef);

  private metaService = inject(MetasService);

  contas: IContas[] = [];

  transacoes: ITransacoes[] = [];

  saldoAtual: number = 0;

  cotacaoDolar?: ICotacao;

  cotacaoEuro?: ICotacao;

  metas: MetaResponse[] = [];

  categorias: Categoria[] = [];

  ngOnInit(): void {
    this.buscarContas();
    this.buscarTransacoes();
    this.buscarCotacoes();
    this.buscarMetas();
    this.buscarCategorias();
  }

  buscarContas(): void {
    this.contaService.listarContasUsuario().subscribe({
      next: (res: IContas[]) => {
        this.contas = res;
        console.log(this.contas);
        this.saldoAtual = this.contas.reduce( (total, conta) =>
                            total + conta.saldo, 0);
        this.cdr.detectChanges();
      },
      error: (err:Error) => {
        console.log(err)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro as carregar Dados da conta',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
      }
    })
  }

  public buscarTransacoes(): void {
    this.transacaoService.listarTransacoes().subscribe({
      next: (res: ITransacoes[]) => {
        this.transacoes = res;
        console.log(this.transacoes);
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
          console.log(err)
          this.messageService.add({
            severity: 'warn',
            summary: 'Erro as carregar Dados das transações',
            detail: '',
            life: 2000
          });
          this.cdr.detectChanges();
      }
    });
  }

  buscarCotacoes(): void {
    this.dashboardService.listarCotacoes('USD-BRL').subscribe({
      next: (res) => {
        this.cotacaoDolar = res;
      },
      error: (err) => {
        console.error("Erro ao buscar dólar", err);

      }
    });

    this.dashboardService.listarCotacoes('EUR-BRL').subscribe({
      next: (res) =>  {
        this.cotacaoEuro = res;
      },
      error: (err) => {
        console.error("Erro ao buscar euro", err);

      }
    });
  }

  buscarCategorias(): void {
    this.transacaoService.listarCategorias().subscribe({
      next: (res: Categoria[]) => {
        this.categorias = res;
        this.cdr.detectChanges();
      },
      error: (err: Error) => {
        console.error('Erro ao buscar categorias', err);
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao carregar categorias',
          detail: '',
          life: 2000
        });
      }
    });
  }

  irParaMetas(): void {
    this.router.navigate(['/app/metas']);
  }
  buscarMetas(): void {

    this.metaService.listarMetas().subscribe({
      next: (res: MetaResponse[]) => {
        this.metas = res;
        console.log("QTd de metas:", res.length);
      },
      error: (err: Error) => {
        console.error(err);
        this.messageService.add({
          severity:'warn',
          summary:'Erro ao carregar metas',
          detail:'',
          life:2000
        });
      }
    });
  }
}
