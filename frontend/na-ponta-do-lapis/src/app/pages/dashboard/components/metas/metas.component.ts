import { Component, Output, EventEmitter, Input } from "@angular/core";
import { PrimeNGModuleModule } from './../../../../shared/primeNg.module';
import { MetaResponse } from '../../../../model/IMetas.models';
import { MetaItemDashboardComponent } from './meta-item-dashboard.component';

@Component({
  selector: 'app-metas-dashboard',
  imports: [PrimeNGModuleModule, MetaItemDashboardComponent],
  template: `
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-100 h-full flex flex-col">

        <!-- Cabeçalho -->
        <div class="flex items-center justify-between mb-6">
          <div>
            <h2 class="section-title-npl">Metas</h2>
            <p class="text-xs text-slate-500 mt-0.5">Progresso dos seus objetivos</p>
          </div>
          <p-button
            label="Mostrar todas"
            icon="pi pi-arrow-right"
            iconPos="right"
            size="small"
            styleClass="btn-npl-secondary text-xs px-3.5 py-2"
            (onClick)="onIrParaMetas.emit()">
          </p-button>
        </div>
        <div class="overflow-y-auto max-h-125">
    <ul >
        @for(meta of metas.slice(0, 5); track meta.id) {
            <li class="mb-5">
                <app-meta-item-dashboard
                [meta]="meta"
                >
                </app-meta-item-dashboard>
            </li>
        } @empty {
            <div class="text-center text-gray-500">
                Nenhuma meta cadastrada ainda
            </div>
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
