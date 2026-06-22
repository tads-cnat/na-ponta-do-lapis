import { Component, Output, EventEmitter } from '@angular/core';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';

@Component({
  selector: 'app-acoes-cartoes',
  standalone: true,
  imports: [PrimeNGModuleModule],
  template: `
    <div class="flex flex-wrap gap-3">

      <p-button
        label="Adicionar"
        icon="pi pi-plus"
        severity="success"
        class="rounded-xl"
        (click)="onAdicionar.emit()">
      </p-button>

      <p-button
        label="Editar"
        icon="pi pi-pencil"
        severity="contrast"
        class="rounded-xl"
        (click)="onEditar.emit()">
      </p-button>

      <p-button
        label="Excluir"
        icon="pi pi-trash"
        severity="danger"
        class="rounded-xl"
        (click)="onExcluir.emit()">
      </p-button>

    </div>
  `
})
export class AcoesCartoesComponent {

  @Output() onAdicionar = new EventEmitter<void>();

  @Output() onEditar = new EventEmitter<void>();

  @Output() onExcluir = new EventEmitter<void>();

}
