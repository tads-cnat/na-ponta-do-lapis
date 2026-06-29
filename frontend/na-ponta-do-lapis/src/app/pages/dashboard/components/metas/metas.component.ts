import { MetaItemComponent } from './../../../metas/components/meta-item/meta-item.component';
import { Component, Output, EventEmitter, Input } from "@angular/core";
import { PrimeNGModuleModule } from './../../../../shared/primeNg.module';
import { MetaResponse } from '../../../../model/IMetas.models';

@Component({
  selector: 'app-metas-dashboard',
  imports: [PrimeNGModuleModule, MetaItemComponent],
  template: `
    <div class="bg-white rounded-2xl p-6 shadow-sm">

        <!-- Cabeçalho -->
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-bold text-gray-800">Metas</h2>
          <p-button
          label="Mostrar todas"
          severity="secondary"
          styleClass="rounded-xl!"
          (click)="onIrParaMetas.emit()">
        </p-button>
        </div>
        <div class="overflow-y-auto max-h-125">
    <ul >
        @for(meta of metas.slice(0, 5); track meta.id) {
            <li class="mb-5">
                <app-meta-item
                [meta]="meta"
                >
                </app-meta-item>
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
