import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CardsResumoComponent } from './components/cards/cards-resumo.component';
import { RelatorioComponent } from './components/relatorio/relatorio.component';
import { GastosCategoriaComponent } from './components/gastos-categoria/gastos.component';
import { MetasDashboardComponent } from './components/metas/metas.component';
import { IContas } from '../../model/IContas.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    PrimeNGModuleModule,
    CardsResumoComponent,
    RelatorioComponent,
    GastosCategoriaComponent,
    MetasDashboardComponent,
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
        >
        </app-cards-resumo>

      <!-- Seção 2: Relatório mensal (largura total) -->
      <app-relatorio class="block w-full"></app-relatorio>

      <!-- Seção 3: Gastos por categoria + Metas (grid 2 colunas)
           FIX 3: Removido h-[320px] — a altura fixa era insuficiente para 5 metas
           com gap-5 (~440px necessários vs 320px disponíveis). Conteúdo auto-dimensiona. -->
      <section class="grid grid-cols-2 gap-4">
        <app-gastos-categoria class="block"></app-gastos-categoria>
        <app-metas-dashboard
          class="block"
          (onIrParaMetas)="irParaMetas()">
        </app-metas-dashboard>
      </section>

    </div>
  `,
})
export class DashboardComponent {

  constructor(private router: Router) {

  }

  contas: IContas[] = []

  irParaMetas(): void {
    this.router.navigate(['/app/metas']);
  }
}
