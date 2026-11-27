import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuItem } from 'primeng/api';
import { MenuModule } from 'primeng/menu';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';

@Component({
  selector: 'app-acoes-cartoes',
  standalone: true,
  imports: [CommonModule, PrimeNGModuleModule, MenuModule],
  template: `
    <div class="flex flex-wrap items-center gap-2 sm:justify-end w-full md:w-auto">

      <p-button
        class="flex-1 sm:flex-none"
        label="Nova Conta"
        icon="pi pi-plus"
        pTooltip="Cadastrar nova conta manual"
        tooltipPosition="bottom"
        styleClass="w-full sm:w-auto justify-center min-h-11 sm:min-h-9 px-3.5 py-2 sm:py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap btn-npl-primary shadow-xs"
        (click)="onAdicionar.emit()">
      </p-button>

      <p-button
        *ngIf="openFinanceConectado"
        class="flex-1 sm:flex-none"
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
        class="flex-1 sm:flex-none"
        label="Conectar Banco"
        icon="pi pi-link"
        pTooltip="Conectar banco via Open Finance"
        tooltipPosition="bottom"
        styleClass="w-full sm:w-auto justify-center min-h-11 sm:min-h-9 px-3.5 py-2 sm:py-1.5 text-xs sm:text-sm font-bold rounded-xl! whitespace-nowrap bg-[#FEF9E7]! text-[#B45309]! border-[#EEC046]! hover:bg-[#FDF3D0]! shadow-xs"
        (click)="onSincronizarOpenFinance.emit()">
      </p-button>

      <p-button
        type="button"
        label="Ações"
        icon="pi pi-ellipsis-v"
        pTooltip="Mais ações para a conta selecionada"
        tooltipPosition="bottom"
        styleClass="justify-center min-h-11 sm:min-h-9 px-3 py-2 sm:py-1.5 text-xs sm:text-sm font-semibold rounded-xl! whitespace-nowrap btn-npl-secondary shadow-2xs"
        (click)="menuAcoes.toggle($event)">
      </p-button>

      <p-menu #menuAcoes [model]="itensMenuAcoes" [popup]="true" appendTo="body" styleClass="rounded-xl shadow-lg text-sm" />

    </div>
  `
})
export class AcoesCartoesComponent {

  @Input() sincronizandoOpenFinance: boolean = false;
  @Input() openFinanceConectado: boolean = false;
  @Input() contaSelecionadaEhCartao: boolean = false;
  @Input() podeTransferir: boolean = false;

  @Output() onAdicionar = new EventEmitter<void>();
  @Output() onEditar = new EventEmitter<void>();
  @Output() onExcluir = new EventEmitter<void>();
  @Output() onSincronizarOpenFinance = new EventEmitter<void>();
  @Output() onTransferir = new EventEmitter<void>();
  @Output() onPagarFatura = new EventEmitter<void>();

  get itensMenuAcoes(): MenuItem[] {
    const itens: MenuItem[] = [];

    if (this.contaSelecionadaEhCartao) {
      itens.push({
        label: 'Pagar fatura',
        icon: 'pi pi-check-circle',
        command: () => this.onPagarFatura.emit(),
      });
    }

    if (this.podeTransferir) {
      itens.push({
        label: 'Transferir entre contas',
        icon: 'pi pi-arrow-right-arrow-left',
        command: () => this.onTransferir.emit(),
      });
    }

    if (itens.length > 0) {
      itens.push({ separator: true });
    }

    itens.push(
      {
        label: 'Editar conta',
        icon: 'pi pi-pencil',
        command: () => this.onEditar.emit(),
      },
      {
        label: 'Excluir conta',
        icon: 'pi pi-trash',
        styleClass: 'text-rose-600',
        command: () => this.onExcluir.emit(),
      }
    );

    return itens;
  }

}
