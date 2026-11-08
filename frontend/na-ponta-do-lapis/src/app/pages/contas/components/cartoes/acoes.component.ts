import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';

@Component({
  selector: 'app-acoes-cartoes',
  standalone: true,
  imports: [CommonModule, PrimeNGModuleModule],
  template: `
    <div class="flex flex-wrap gap-3">

      <p-button
        label="Adicionar"
        icon="pi pi-plus"
        class="rounded-xl"
        [style]="{'background-color': '#10b981', 'border-color': '#10b981'}"
        (click)="onAdicionar.emit()">
      </p-button>

      <p-button
        label="Editar"
        icon="pi pi-pencil"
        class="rounded-xl"
        [style]="{'background-color': '#f3e8ff', 'color': '#7843cd', 'border-color': '#7843cd'}"
        (click)="onEditar.emit()">
      </p-button>

      <p-button
        label="Excluir"
        icon="pi pi-trash"
        class="rounded-xl"
        [style]="{'background-color': '#fee2e2', 'color': '#ef4444', 'border-color': '#ef4444'}"
        (click)="onExcluir.emit()">
      </p-button>

      <p-button
        *ngIf="openFinanceConectado"
        label="Sincronizar"
        icon="pi pi-sync"
        class="rounded-xl"
        [loading]="sincronizandoOpenFinance"
        [style]="{'background-color': '#e0e7ff', 'color': '#4338ca', 'border-color': '#4338ca'}"
        (click)="onSincronizarOpenFinance.emit()">
      </p-button>

      <p-button
        *ngIf="!openFinanceConectado"
        label="Conectar Banco"
        icon="pi pi-link"
        class="rounded-xl"
        [style]="{'background-color': '#f8fafc', 'color': '#475569', 'border-color': '#cbd5e1'}"
        (click)="onSincronizarOpenFinance.emit()">
      </p-button>

    </div>
  `
})
export class AcoesCartoesComponent {

  @Input() sincronizandoOpenFinance: boolean = false;
  @Input() openFinanceConectado: boolean = false;

  @Output() onAdicionar = new EventEmitter<void>();
  @Output() onEditar = new EventEmitter<void>();
  @Output() onExcluir = new EventEmitter<void>();
  @Output() onSincronizarOpenFinance = new EventEmitter<void>();

}
