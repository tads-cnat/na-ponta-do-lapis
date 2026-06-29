import { Component, inject, Input, ChangeDetectorRef } from "@angular/core";
import { CurrencyPipe } from "@angular/common";
import { IContas } from "../../../../model/IContas.models";
import { ContaFinanceiraService } from "../../../contas/service/contas.service";
import { MessageService } from "primeng/api";
import { TransacoesService } from "../../../transacoes/service/transacoes.service";
import { ITransacoes } from "../../../../model/ITransacoes.model";
import { MetasService } from "../../../metas/services/metas.service";
import { MetaResponse } from './../../../../model/IMetas.models';

@Component({
  selector: 'app-cards-resumo',
  imports: [CurrencyPipe],
  template: `
    <section class="grid grid-cols-4 gap-4">

      <!-- Card: Saldo atual -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Saldo atual</span>
          <div class="w-10 h-10 rounded-full bg-purple-100 flex items-center justify-center">
            <!-- ícone placeholder -->
            <span class="text-purple-400 text-lg">👤</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">{{ saldoTotal | currency : 'BRL' }}</span>
      </div>

      <!-- Card: Moeda -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Moeda</span>
          <div class="w-10 h-10 rounded-full bg-yellow-100 flex items-center justify-center">
            <span class="text-yellow-500 text-lg">🪙</span>
          </div>
        </div>
        <div class="flex items-center gap-3 text-xs text-gray-600">
          <span>🇺🇸 USD $6.12 <span class="text-green-500">↑</span></span>
          <span>🇨🇦 CAD $3.82 <span class="text-green-500">↑</span></span>
          <span>🇵🇹 EUR $3.82 <span class="text-red-500">↓</span></span>
        </div>
      </div>

      <!-- Card: Receita -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Receita</span>
          <div class="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center">
            <span class="text-green-500 text-lg">📈</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">{{ transacoesReceita | currency: 'BRL' }}</span>
      </div>

      <!-- Card: Despesa -->
      <div class="
    bg-white
    rounded-2xl
    px-5
    py-4
    shadow-sm
    flex
    flex-col
    justify-between
">
        <div class="flex items-center justify-between">
          <span class="text-sm text-gray-500 font-medium">Despesa</span>
          <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
            <span class="text-red-400 text-lg">🕐</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">{{transacoesDespesa | currency: 'BRL' }}</span>
      </div>

    </section>
  `
})

export class CardsResumoComponent {
  @Input() contas: IContas[] = [];

  @Input() transacoes: ITransacoes[] = [];

  @Input() metas: MetaResponse[] = [];

  contaService = inject(ContaFinanceiraService);

  transacaoService = inject(TransacoesService);

  metaService = inject(MetasService);

  saldoTotal: number = 0;

  transacoesReceita: number = 0;

  transacoesDespesa: number = 0;

  private cdr = inject(ChangeDetectorRef);

  private messageService = inject(MessageService)

  ngOnInit(): void {
    this.listarContas();
    this.listarTransacoes();
    this.listarMetas();
  }

  public listarContas(): void {
    this.contaService.listarContasUsuarioLogado().subscribe({
      next: (res: IContas[]) => {
        this.contas = res;
        console.log(this.contas)
        this.saldoTotal = 0;
        res.forEach(atual => this.saldoTotal += atual.saldo);
        this.cdr.detectChanges();
      },
      error: (err:Error) => {
        console.log(err)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro as carregar Dados da conta',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
      }
    })
  }

public listarTransacoes(): void {
  this.transacaoService.listarTransacoes().subscribe({
    next: (res: ITransacoes[]) => {
      this.transacoes = res;
      console.log(this.transacoes);
      this.transacoesReceita = 0;
      this.transacoesDespesa = 0;
      res.forEach(atual => {
        if(atual.estado !== 'PENDENTE'){
          if(atual.tipo == 'RECEITA'){
            this.transacoesReceita += atual.valor;
          }
          else {
            this.transacoesDespesa += atual.valor;
          }
        }
        else {
          return;
        }
      });
    },
    error: (err: Error) => {
        console.log(err)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro as carregar Dados das transações',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
    }
  })
}

public listarMetas(): void {
  this.metaService.listarMetas().subscribe({
    next: (res: MetaResponse[]) => {
      this.metas = res;
      console.log(this.metas);
      },
    error: (err: Error) => {
        console.log(err)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro as carregar Dados das metas',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
    }
  })
}


}
