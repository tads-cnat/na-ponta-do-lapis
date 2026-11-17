import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';

@Component({
  selector: 'app-acoes-cartoes',
  standalone: true,
  imports: [CommonModule, PrimeNGModuleModule],
  template: `
    <div class="grid grid-cols-2 sm:flex sm:flex-wrap items-center gap-2 sm:justify-end w-full md:w-auto">

      <p-button
        class="w-full sm:w-auto"
        label="Nova Conta"
        icon="pi pi-plus"
        pTooltip="Cadastrar nova conta manual"
        tooltipPosition="bottom"
        styleClass="w-full sm:w-auto justify-center min-h-11 sm:min-h-9 px-3.5 py-2 sm:py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap btn-npl-primary shadow-xs"
        (click)="onAdicionar.emit()">
      </p-button>

      <p-button
        *ngIf="openFinanceConectado"
        class="w-full sm:w-auto"
        label="Sincronizar"
        icon="pi pi-sync"
        pTooltip="Sincronizar dados das contas via Open Finance"
        tooltipPosition="bottom"
        [loading]="sincronizandoOpenFinance"
        styleClass="w-full sm:w-auto justify-center min-h-11 sm:min-h-9 px-3 py-2 sm:py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap btn-soft-edit-npl shadow-2xs"
        (click)="onSincronizarOpenFinance.emit()">
      </p-button>

      <p-button
        *ngIf="!openFinanceConectado"
        class="w-full sm:w-auto"
        label="Conectar Banco"
        icon="pi pi-link"
        pTooltip="Conectar banco via Open Finance"
        tooltipPosition="bottom"
        styleClass="w-full sm:w-auto justify-center min-h-11 sm:min-h-9 px-3.5 py-2 sm:py-1.5 text-xs sm:text-sm font-bold rounded-xl! whitespace-nowrap bg-[#FEF9E7]! text-[#B45309]! border-[#EEC046]! hover:bg-[#FDF3D0]! shadow-xs"
        (click)="onSincronizarOpenFinance.emit()">
      </p-button>

      <p-button
        class="w-full sm:w-auto"
        label="Editar"
        icon="pi pi-pencil"
        pTooltip="Editar conta selecionada"
        tooltipPosition="bottom"
        styleClass="w-full sm:w-auto justify-center min-h-11 sm:min-h-9 px-3 py-2 sm:py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap btn-soft-edit-npl shadow-2xs"
        (click)="onEditar.emit()">
      </p-button>

      <p-button
        class="w-full sm:w-auto"
        label="Excluir"
        icon="pi pi-trash"
        pTooltip="Excluir conta selecionada"
        tooltipPosition="bottom"
        styleClass="w-full sm:w-auto justify-center min-h-11 sm:min-h-9 px-3 py-2 sm:py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap btn-soft-delete-npl shadow-2xs"
        (click)="onExcluir.emit()">
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
