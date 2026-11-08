import { Component, Input } from "@angular/core";
import { CommonModule, CurrencyPipe, DecimalPipe } from "@angular/common";
import { IContas, ehCartaoCredito } from "../../../../model/IContas.models";
import { ITransacoes } from "../../../../model/ITransacoes.model";
import { ICotacao } from "../../../../model/ICotacao.models";

@Component({
  selector: 'app-cards-resumo',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DecimalPipe],
  template: `
    <section class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">

      <!-- Card 1: Dinheiro em Contas -->
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-emerald-100 flex flex-col justify-between min-h-[136px]">
        <div class="flex items-center justify-between mb-3">
          <span class="text-xs font-bold uppercase tracking-wider text-emerald-800">Dinheiro em Contas</span>
          <div class="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </div>
        </div>
        <div>
          <span class="text-2xl lg:text-3xl font-extrabold text-emerald-700 tracking-tight block truncate">
            {{ totalDinheiroContas | currency : 'BRL' }}
          </span>
          <div class="flex items-center justify-between mt-1 text-xs h-5 text-slate-500">
            <span class="truncate">Disponível em contas</span>
            @if (totalReceitasPendentes > 0) {
              <span class="text-emerald-600 font-semibold whitespace-nowrap shrink-0 ml-1">+ {{ totalReceitasPendentes | currency : 'BRL' }} a receber</span>
            }
          </div>
        </div>
      </div>

      <!-- Card 2: Saldo Devedor Pendente -->
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-rose-100 flex flex-col justify-between min-h-[136px]">
        <div class="flex items-center justify-between mb-3">
          <span class="text-xs font-bold uppercase tracking-wider text-rose-800">Saldo Devedor Pendente</span>
          <div class="w-10 h-10 rounded-xl bg-rose-100 text-rose-600 flex items-center justify-center">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
            </svg>
          </div>
        </div>
        <div>
          <span class="text-2xl lg:text-3xl font-extrabold text-rose-600 tracking-tight block truncate">
            - {{ totalSaldoDevedor | currency : 'BRL' }}
          </span>
          <div class="flex items-center justify-between mt-1 text-xs h-5 text-slate-500"
               [title]="'Faturas: ' + (totalFaturasCartoes | currency:'BRL') + ' | Pendentes: ' + (totalDespesasPendentes | currency:'BRL') + ' | Balanço Projetado: ' + (balancoProjetado | currency:'BRL')">
            @if (totalDespesasPendentes > 0) {
              <span class="truncate">Faturas: {{ totalFaturasCartoes | currency : 'BRL' }}</span>
              <span class="text-rose-600 font-semibold whitespace-nowrap shrink-0 ml-1">+ {{ totalDespesasPendentes | currency : 'BRL' }} a pagar</span>
            } @else {
              <span class="truncate">Faturas em aberto</span>
              <span class="text-slate-400 whitespace-nowrap shrink-0 ml-1">Sem pendências</span>
            }
          </div>
        </div>
      </div>

      <!-- Card 3: Receita (Mês Atual) -->
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-100 flex flex-col justify-between min-h-[136px]">
        <div class="flex items-center justify-between mb-3">
          <span class="text-xs font-bold uppercase tracking-wider text-slate-600">Receita ({{ mesAtualNome }})</span>
          <div class="w-10 h-10 rounded-xl bg-green-100 text-green-600 flex items-center justify-center">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          </div>
        </div>
        <div>
          <span class="text-2xl lg:text-3xl font-extrabold text-slate-900 tracking-tight block truncate">
            {{ receitaMesAtualRealizada | currency : 'BRL' }}
          </span>
          <div class="flex items-center justify-between mt-1 text-xs h-5 text-slate-500">
            @if (receitaMesAtualPendente > 0) {
              <span class="truncate">Realizado</span>
              <span class="text-emerald-600 font-semibold whitespace-nowrap shrink-0 ml-1">+ {{ receitaMesAtualPendente | currency : 'BRL' }} a receber</span>
            } @else if (variacaoReceita !== null) {
              <span class="truncate">vs mês anterior</span>
              <span class="whitespace-nowrap shrink-0 ml-1 font-semibold" [ngClass]="variacaoReceita >= 0 ? 'text-emerald-600' : 'text-rose-600'">
                {{ variacaoReceita >= 0 ? '↑' : '↓' }} {{ (variacaoReceita >= 0 ? variacaoReceita : -variacaoReceita) | number:'1.0-1' }}%
              </span>
            } @else {
              <span class="text-slate-400 truncate">Entradas realizadas</span>
            }
          </div>
        </div>
      </div>

      <!-- Card 4: Despesa (Mês Atual) -->
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-slate-100 flex flex-col justify-between min-h-[136px]">
        <div class="flex items-center justify-between mb-3">
          <span class="text-xs font-bold uppercase tracking-wider text-slate-600">Despesa ({{ mesAtualNome }})</span>
          <div class="w-10 h-10 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" />
            </svg>
          </div>
        </div>
        <div>
          <span class="text-2xl lg:text-3xl font-extrabold text-slate-900 tracking-tight block truncate">
            {{ despesaMesAtualRealizada | currency : 'BRL' }}
          </span>
          <div class="flex items-center justify-between mt-1 text-xs h-5 text-slate-500">
            @if (despesaMesAtualPendente > 0) {
              <span class="truncate">Realizado</span>
              <span class="text-amber-600 font-semibold whitespace-nowrap shrink-0 ml-1">+ {{ despesaMesAtualPendente | currency : 'BRL' }} a pagar</span>
            } @else if (variacaoDespesa !== null) {
              <span class="truncate">vs mês anterior</span>
              <span class="whitespace-nowrap shrink-0 ml-1 font-semibold" [ngClass]="variacaoDespesa <= 0 ? 'text-emerald-600' : 'text-rose-600'">
                {{ variacaoDespesa <= 0 ? '↓' : '↑' }} {{ (variacaoDespesa >= 0 ? variacaoDespesa : -variacaoDespesa) | number:'1.0-1' }}%
              </span>
            } @else {
              <span class="text-slate-400 truncate">Saídas realizadas</span>
            }
          </div>
        </div>
      </div>

    </section>
  `
})
export class CardsResumoComponent {
  @Input() contas: IContas[] = [];
  @Input() transacoes: ITransacoes[] = [];
  @Input() cotacaoDolar?: ICotacao;
  @Input() cotacaoEuro?: ICotacao;

  readonly mesesNomes = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
  ];

  get mesAtualNome(): string {
    return this.mesesNomes[new Date().getMonth()];
  }

  get totalDinheiroContas(): number {
    return this.contas
      .filter(c => !ehCartaoCredito(c))
      .reduce((total, conta) => total + (Number(conta.saldo) || 0), 0);
  }

  get totalFaturasCartoes(): number {
    return this.contas
      .filter(c => ehCartaoCredito(c))
      .reduce((total, conta) => total + (Number(conta.saldo) || 0), 0);
  }

  get totalDespesasPendentes(): number {
    return this.transacoes
      .filter(t => t.tipo !== 'RECEITA' && t.estado === 'PENDENTE')
      .reduce((sum, t) => sum + (Number(t.valor) || 0), 0);
  }

  get totalReceitasPendentes(): number {
    return this.transacoes
      .filter(t => t.tipo === 'RECEITA' && t.estado === 'PENDENTE')
      .reduce((sum, t) => sum + (Number(t.valor) || 0), 0);
  }

  get totalSaldoDevedor(): number {
    return this.totalFaturasCartoes + this.totalDespesasPendentes;
  }

  get balancoLiquido(): number {
    return this.totalDinheiroContas - this.totalSaldoDevedor;
  }

  get balancoProjetado(): number {
    return this.totalDinheiroContas + this.totalReceitasPendentes - this.totalSaldoDevedor;
  }

  private ehDoMes(dataStr: string | undefined | null, ano: number, mesBase1: number): boolean {
    if (!dataStr) return false;
    const mesFormatado = String(mesBase1).padStart(2, '0');
    const prefixo = `${ano}-${mesFormatado}`;
    if (dataStr.startsWith(prefixo)) return true;
    try {
      const d = new Date(dataStr);
      return !isNaN(d.getTime()) && d.getFullYear() === ano && (d.getMonth() + 1) === mesBase1;
    } catch {
      return false;
    }
  }

  get receitaMesAtualRealizada(): number {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth() + 1;
    return this.transacoes
      .filter(t => t.tipo === 'RECEITA' && t.estado !== 'PENDENTE' && this.ehDoMes(t.dataHora, ano, mes))
      .reduce((sum, t) => sum + (Number(t.valor) || 0), 0);
  }

  get receitaMesAtualPendente(): number {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth() + 1;
    return this.transacoes
      .filter(t => t.tipo === 'RECEITA' && t.estado === 'PENDENTE' && this.ehDoMes(t.dataHora, ano, mes))
      .reduce((sum, t) => sum + (Number(t.valor) || 0), 0);
  }

  get receitaMesAtual(): number {
    return this.receitaMesAtualRealizada + this.receitaMesAtualPendente;
  }

  get despesaMesAtualRealizada(): number {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth() + 1;
    return this.transacoes
      .filter(t => t.tipo !== 'RECEITA' && t.estado !== 'PENDENTE' && this.ehDoMes(t.dataHora, ano, mes))
      .reduce((sum, t) => sum + (Number(t.valor) || 0), 0);
  }

  get despesaMesAtualPendente(): number {
    const hoje = new Date();
    const ano = hoje.getFullYear();
    const mes = hoje.getMonth() + 1;
    return this.transacoes
      .filter(t => t.tipo !== 'RECEITA' && t.estado === 'PENDENTE' && this.ehDoMes(t.dataHora, ano, mes))
      .reduce((sum, t) => sum + (Number(t.valor) || 0), 0);
  }

  get despesaMesAtual(): number {
    return this.despesaMesAtualRealizada + this.despesaMesAtualPendente;
  }

  get receitaMesAnterior(): number {
    const hoje = new Date();
    const d = new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1);
    const ano = d.getFullYear();
    const mes = d.getMonth() + 1;
    return this.transacoes
      .filter(t => t.tipo === 'RECEITA' && t.estado !== 'PENDENTE' && this.ehDoMes(t.dataHora, ano, mes))
      .reduce((sum, t) => sum + (Number(t.valor) || 0), 0);
  }

  get despesaMesAnterior(): number {
    const hoje = new Date();
    const d = new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1);
    const ano = d.getFullYear();
    const mes = d.getMonth() + 1;
    return this.transacoes
      .filter(t => t.tipo !== 'RECEITA' && t.estado !== 'PENDENTE' && this.ehDoMes(t.dataHora, ano, mes))
      .reduce((sum, t) => sum + (Number(t.valor) || 0), 0);
  }

  get variacaoReceita(): number | null {
    const anterior = this.receitaMesAnterior;
    if (anterior <= 0) return null;
    return ((this.receitaMesAtualRealizada - anterior) / anterior) * 100;
  }

  get variacaoDespesa(): number | null {
    const anterior = this.despesaMesAnterior;
    if (anterior <= 0) return null;
    return ((this.despesaMesAtualRealizada - anterior) / anterior) * 100;
  }
}
