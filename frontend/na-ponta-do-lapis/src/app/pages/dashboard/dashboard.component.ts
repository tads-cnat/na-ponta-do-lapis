import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { ChartModule } from 'primeng/chart';

import { CardsResumoComponent } from './components/cards/cards-resumo.component';
import { RelatorioComponent } from "./components/relatorio/relatorio.component";
import { GastosCategoriaComponent } from "./components/gastos-categoria/gastos.component";
import { MetasDashboardComponent } from './components/metas/metas.component';


@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [PrimeNGModuleModule, CardsResumoComponent, RelatorioComponent, MetasDashboardComponent, GastosCategoriaComponent],
  template: `
      <!-- ============================================================
       ÁREA PRINCIPAL DO DASHBOARD
       Ignora sidebar e header — apenas o conteúdo central
  ============================================================ -->
  <main class="flex gap-6">

    <!-- ============================================================
         SEÇÃO 1 – CARDS DE RESUMO (4 colunas)
    ============================================================ -->
    <app-cards-resumo></app-cards-resumo>

    <!-- ============================================================
         SEÇÃO 2 – RELATÓRIO MENSAL (gráfico de linha, full width)
    ============================================================ -->
    <app-relatorio></app-relatorio>


    <!-- ============================================================
         SEÇÃO 3 – LINHA INFERIOR (2 colunas: Gastos por categoria + Metas)
    ============================================================ -->
    <section class="grid grid-cols-2 gap-4 h-[320px]">

      <!-- ──────────────────────────────────────────────
      3A – Gastos por categoria (gráfico donut)
      ────────────────────────────────────────────── -->
      <app-gastos-categoria></app-gastos-categoria>

      <!-- ──────────────────────────────────────────────
      3B – Metas (lista de progresso)
      ────────────────────────────────────────────── -->
      <app-metas-dashboard
      (onIrParaMetas)="irParaMetas()"
      >
      </app-metas-dashboard>


    </section>

  </main>


  `
})
export class DashboardComponent {

  constructor(private router: Router){

  }

  irParaMetas(): void {
    this.router.navigate(['/app/metas']);
  }

}
