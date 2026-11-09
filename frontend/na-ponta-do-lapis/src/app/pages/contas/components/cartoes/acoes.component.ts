import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';

@Component({
  selector: 'app-acoes-cartoes',
  standalone: true,
  imports: [CommonModule, PrimeNGModuleModule],
  template: `
    <div class="flex flex-wrap items-center gap-2 sm:justify-end">

      <p-button
        label="Adicionar"
        icon="pi pi-plus"
        pTooltip="Cadastrar nova conta manual"
        tooltipPosition="bottom"
        styleClass="h-9 px-3 py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap bg-[#10b981]! border-[#10b981]! text-white! hover:bg-[#059669]! shadow-xs"
        (click)="onAdicionar.emit()">
      </p-button>

      <p-button
        label="Editar"
        icon="pi pi-pencil"
        pTooltip="Editar conta selecionada"
        tooltipPosition="bottom"
        styleClass="h-9 px-3 py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap bg-[#f3e8ff]! text-[#7843cd]! border-[#7843cd]! hover:bg-[#e9d5ff]! shadow-xs"
        (click)="onEditar.emit()">
      </p-button>

      <p-button
        label="Excluir"
        icon="pi pi-trash"
        pTooltip="Excluir conta selecionada"
        tooltipPosition="bottom"
        styleClass="h-9 px-3 py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap bg-[#fee2e2]! text-[#ef4444]! border-[#ef4444]! hover:bg-[#fecaca]! shadow-xs"
        (click)="onExcluir.emit()">
      </p-button>

      <p-button
        *ngIf="openFinanceConectado"
        label="Sincronizar"
        icon="pi pi-sync"
        pTooltip="Sincronizar dados das contas via Open Finance"
        tooltipPosition="bottom"
        [loading]="sincronizandoOpenFinance"
        styleClass="h-9 px-3 py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap bg-indigo-50! text-[#4338ca]! border-[#4338ca]! hover:bg-indigo-100! shadow-xs"
        (click)="onSincronizarOpenFinance.emit()">
      </p-button>

      <p-button
        *ngIf="!openFinanceConectado"
        label="Conectar Banco"
        icon="pi pi-link"
        pTooltip="Conectar banco via Open Finance"
        tooltipPosition="bottom"
        styleClass="h-9 px-3 py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap bg-white! text-slate-700! border-slate-300! hover:bg-slate-50! shadow-xs"
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
