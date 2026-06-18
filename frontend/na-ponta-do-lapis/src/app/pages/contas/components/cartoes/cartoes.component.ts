import { Component, Input, Output, EventEmitter } from '@angular/core';
import { IContas } from '../../../../model/IContas.models';
import { AcoesCartoesComponent } from './acoes.component';
import { CarrosselCartoesComponent } from './carrosselCartoes.component';

@Component({
  selector: 'app-cartoes-contas',
  standalone: true,
  imports: [AcoesCartoesComponent, CarrosselCartoesComponent],
  template: `
    <div class="flex flex-col gap-5">

      <!-- HEADER -->
      <div class="flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">

        <div class="min-w-0">
          <h1 class="text-4xl font-bold text-slate-900">
            Contas
          </h1>
          <p class="text-slate-500 mt-2">
            Gestão de contas financeiras
          </p>
        </div>

        <!-- AÇÕES -->
        <app-acoes-cartoes
          (onAdicionar)="onAdicionar.emit()"
          (onEditar)="onEditar.emit()"
          (onExcluir)="onExcluir.emit()">
        </app-acoes-cartoes>

      </div>

      <!-- CARROSSEL -->
      <app-carrossel-cartoes
        [contas]="contas"
        [contaSelecionadaIndex]="contaSelecionadaIndex"
        [contaAnterior]="contaAnterior"
        [contaProxima]="contaProxima"
        [carregando]="carregando"
        (onContaSelecionada)="onContaSelecionada.emit($event)"
        (onAnterior)="onAnterior.emit()"
        (onProxima)="onProxima.emit()">
      </app-carrossel-cartoes>

    </div>
  `
})
export class CartaoContaComponent {

  // =========================================
  // INPUTS
  // =========================================

  @Input() contas: IContas[] = [];
  @Input() contaSelecionadaIndex: number = 0;
  @Input() contaAnterior: number = 0;
  @Input() contaProxima: number = 0;
  @Input() carregando: boolean = true;

  // =========================================
  // OUTPUTS
  // =========================================

  @Output() onAdicionar = new EventEmitter<void>();
  @Output() onEditar = new EventEmitter<void>();
  @Output() onExcluir = new EventEmitter<void>();
  @Output() onContaSelecionada = new EventEmitter<number>();
  @Output() onAnterior = new EventEmitter<void>();
  @Output() onProxima = new EventEmitter<void>();

}
