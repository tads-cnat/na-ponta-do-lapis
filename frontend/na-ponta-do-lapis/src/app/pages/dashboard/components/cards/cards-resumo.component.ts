import { Component, inject, Input, ChangeDetectorRef } from "@angular/core";
import { CurrencyPipe } from "@angular/common";
import { IContas } from "../../../../model/IContas.models";
import { ContaFinanceiraService } from "../../../contas/service/contas.service";
import { MessageService } from "primeng/api";

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
        <span class="text-3xl font-bold text-gray-800">R$89,000</span>
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
        <span class="text-3xl font-bold text-gray-800">$89,000</span>
      </div>

      <!-- Card: Gastos -->
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
          <span class="text-sm text-gray-500 font-medium">Gastos</span>
          <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
            <span class="text-red-400 text-lg">🕐</span>
          </div>
        </div>
        <span class="text-3xl font-bold text-gray-800">2,040</span>
      </div>

    </section>
  `
})

export class CardsResumoComponent {
  @Input() contas: IContas[] = [];
  contaService = inject(ContaFinanceiraService);
  saldoTotal: number = 0;
  private cdr = inject(ChangeDetectorRef);
  private messageService = inject(MessageService)

  ngOnInit(): void {
    this.listarContas();
  }

  public listarContas(): void {
  this.contaService.listarContasUsuarioLogado().subscribe({
    next: (res:IContas[]) => {
      this.contas = res;
      console.log(this.contas)
      this.saldoTotal = 0;
      res.forEach(atual =>  this.saldoTotal += atual.saldo);
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

}
