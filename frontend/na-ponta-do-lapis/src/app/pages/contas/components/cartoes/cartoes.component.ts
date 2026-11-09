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
      <!-- CARROSSEL DE CONTAS -->
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
  @Input() sincronizandoOpenFinance: boolean = false;
<<<<<<< HEAD
<<<<<<< HEAD
  @Input() openFinanceConectado: boolean = false;
=======
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
  @Input() openFinanceConectado: boolean = false;
>>>>>>> c2d9529 (feat(openfinance): suporte a receitas/despesas pendentes e atalho inteligente para conexao bancaria)

  // =========================================
  // OUTPUTS
  // =========================================

  @Output() onAdicionar = new EventEmitter<void>();
  @Output() onEditar = new EventEmitter<void>();
  @Output() onExcluir = new EventEmitter<void>();
  @Output() onSincronizarOpenFinance = new EventEmitter<void>();
  @Output() onContaSelecionada = new EventEmitter<number>();
  @Output() onAnterior = new EventEmitter<void>();
  @Output() onProxima = new EventEmitter<void>();

}
