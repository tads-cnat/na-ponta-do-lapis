import { Component, Output, EventEmitter, Input } from "@angular/core";
import { PrimeNGModuleModule } from './../../../../shared/primeNg.module';
import { MetaResponse } from '../../../../model/IMetas.models';
import { MetaItemDashboardComponent } from './meta-item-dashboard.component';

@Component({
  selector: 'app-metas-dashboard',
  imports: [PrimeNGModuleModule, MetaItemDashboardComponent],
  template: `
    <div class="bg-white rounded-2xl p-4 sm:p-6 shadow-sm border border-slate-100 h-full flex flex-col">

        <!-- Cabeçalho -->
        <div class="flex flex-wrap items-center justify-between gap-3 mb-4 sm:mb-6">
          <div>
            <h2 class="section-title-npl m-0">Metas</h2>
            <p class="text-xs text-slate-500 mt-0.5 mb-0">Progresso dos seus objetivos</p>
          </div>
          <p-button
            label="Mostrar todas"
            icon="pi pi-arrow-right"
            iconPos="right"
            size="small"
            styleClass="btn-npl-secondary text-xs px-3.5 py-2 rounded-xl!"
            (onClick)="onIrParaMetas.emit()">
          </p-button>
        </div>
        <div class="overflow-y-auto max-h-125">
          <ul class="list-none p-0 m-0 flex flex-col gap-3">
              @for(meta of metas.slice(0, 5); track meta.id) {
                  <li>
                      <app-meta-item-dashboard [meta]="meta"></app-meta-item-dashboard>
                  </li>
              } @empty {
                  <li class="py-8 text-center text-sm text-slate-400">
                      Nenhuma meta cadastrada ainda
                  </li>
              }
          </ul>
        </div>
  </div>
  `
})

export class MetasDashboardComponent {
  @Input() metas: MetaResponse[] = [];
  @Output() onIrParaMetas = new EventEmitter<void>();

}
