import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IContas, Moeda, ehCartaoCredito } from '../../../../model/IContas.models';

@Component({
  selector: 'app-informacoes-conta',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (conta) {
      <div class="bg-white rounded-[2rem] p-6 shadow-sm border border-slate-100 flex flex-col gap-5">

        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-bold text-slate-900">
              Informações da Conta
            </h2>
            <p class="text-slate-500 text-sm">
              {{ ehCartao ? 'Resumo da fatura e limite do cartão' : 'Resumo e saldo disponível' }}
            </p>
          </div>

          <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold"
                [ngClass]="ehCartao ? 'bg-purple-100 text-[#7843cd]' : 'bg-emerald-100 text-emerald-700'">
            <i [class]="ehCartao ? 'pi pi-credit-card' : 'pi pi-wallet'"></i>
            {{ ehCartao ? 'Cartão de Crédito' : 'Conta Corrente / Bancária' }}
          </span>
        </div>

        <!-- GRID DE DADOS -->
        <div class="grid grid-cols-1 gap-4">

          <!-- SE FOR CARTÃO DE CRÉDITO: SEPARAÇÃO DE FATURAS -->
          @if (ehCartao) {
            <!-- DISCRIMINAÇÃO: FATURA FECHADA vs FATURA EM ABERTO -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              
              <!-- Card: Fatura Fechada (A Pagar Agora) -->
              <div class="bg-rose-50/60 border border-rose-200/80 rounded-2xl p-4 flex flex-col justify-between">
                <div>
                  <div class="flex items-center justify-between mb-1.5">
                    <span class="text-xs font-bold text-rose-700 uppercase tracking-wider flex items-center gap-1">
                      <i class="pi pi-exclamation-circle text-xs"></i>
                      Fatura Fechada (A Pagar)
                    </span>
                    <span class="text-[10px] font-bold bg-rose-200 text-rose-800 px-2 py-0.5 rounded-full">
                      Pagar Agora
                    </span>
                  </div>
                  <span class="text-2xl font-extrabold text-rose-700 tracking-tight">
                    - {{ (conta.valorFaturaFechada ?? conta.saldo) | currency: conta.moeda }}
                  </span>
                </div>
                <div class="mt-3 pt-2 border-t border-rose-200/60 text-xs text-rose-600 flex items-center justify-between">
                  <span>{{ conta.dataVencimentoFatura ? ('Vencimento: ' + (conta.dataVencimentoFatura | date:'dd/MM/yyyy')) : 'Aguardando vencimento' }}</span>
                  <i class="pi pi-calendar text-xs"></i>
                </div>
              </div>

              <!-- Card: Fatura em Aberto (Próximo Mês) -->
              <div class="bg-amber-50/60 border border-amber-200/80 rounded-2xl p-4 flex flex-col justify-between">
                <div>
                  <div class="flex items-center justify-between mb-1.5">
                    <span class="text-xs font-bold text-amber-800 uppercase tracking-wider flex items-center gap-1">
                      <i class="pi pi-clock text-xs"></i>
                      Fatura em Aberto (Próximo Mês)
                    </span>
                    <span class="text-[10px] font-bold bg-amber-200 text-amber-900 px-2 py-0.5 rounded-full">
                      Em Aberto
                    </span>
                  </div>
                  <span class="text-2xl font-extrabold text-amber-900 tracking-tight">
                    - {{ (conta.valorFaturaAberta ?? 0) | currency: conta.moeda }}
                  </span>
                </div>
                <div class="mt-3 pt-2 border-t border-amber-200/60 text-xs text-amber-700 flex items-center justify-between">
                  <span>Compras do ciclo atual</span>
                  <i class="pi pi-shopping-bag text-xs"></i>
                </div>
              </div>

            </div>

            <!-- Total Acumulado no Cartão (Ambas as Faturas) -->
            <div class="bg-slate-50 border border-slate-200/80 rounded-xl p-3 flex items-center justify-between text-xs">
              <span class="text-slate-600 font-medium flex items-center gap-1.5">
                <i class="pi pi-info-circle text-slate-400"></i>
                Total Devedor Acumulado (Fechada + Aberta):
              </span>
              <span class="font-bold text-slate-900 text-sm">
                - {{ conta.saldo | currency: conta.moeda }}
              </span>
            </div>
          } @else {
            <!-- SE FOR CONTA CORRENTE: SALDO DISPONÍVEL -->
            <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5 min-w-0">
              <div class="flex items-center justify-between mb-2">
                <p class="text-xs font-bold uppercase tracking-wider text-slate-500">
                  Saldo Disponível (Dinheiro em Conta)
                </p>
                <span class="text-[11px] font-semibold text-emerald-700 bg-emerald-100/70 px-2 py-0.5 rounded-md">
                  Disponível
                </span>
              </div>

              <span class="text-3xl font-extrabold text-slate-900 break-words leading-tight">
                {{
                  conta.saldo.toLocaleString(
                    localeDaMoeda(conta.moeda),
                    { style: 'currency', currency: conta.moeda }
                  )
                }}
              </span>
            </div>
          }

          <!-- BLOCO DE LIMITES DO CARTÃO (SE FOR CARTÃO DE CRÉDITO) -->
          @if (ehCartao) {
            <div class="bg-slate-50 border border-slate-200/80 rounded-2xl p-5 flex flex-col gap-3">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center gap-1.5">
                  <i class="pi pi-chart-pie text-[#7843cd]"></i>
                  Uso do Limite de Crédito
                </span>
                @if (percentualLimiteConsumido !== null) {
                  <span class="text-xs font-bold text-[#7843cd]">
                    {{ percentualLimiteConsumido | number:'1.0-0' }}% consumido
                  </span>
                }
              </div>

              <!-- Barra de Progresso do Limite -->
              <div class="w-full h-3 bg-slate-200 rounded-full overflow-hidden">
                <div class="h-full rounded-full transition-all duration-500"
                     [style.width.%]="percentualLimiteConsumido ?? 50"
                     [ngClass]="(percentualLimiteConsumido ?? 0) > 85 ? 'bg-rose-500' : ((percentualLimiteConsumido ?? 0) > 60 ? 'bg-amber-500' : 'bg-[#7843cd]')">
                </div>
              </div>

              <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 pt-2 text-xs">
                <div class="flex flex-col">
                  <span class="text-slate-400 font-medium">Limite Consumido</span>
                  <span class="font-bold text-rose-600 text-sm">
                    {{ (limiteConsumidoCalculado) | currency: conta.moeda }}
                  </span>
                </div>

                <div class="flex flex-col">
                  <span class="text-slate-400 font-medium">Limite Disponível</span>
                  <span class="font-bold text-emerald-600 text-sm">
                    {{ (limiteDisponivelCalculado) | currency: conta.moeda }}
                  </span>
                </div>

                @if (limiteTotalCalculado) {
                  <div class="flex flex-col col-span-2 sm:col-span-1">
                    <span class="text-slate-400 font-medium">Limite Total</span>
                    <span class="font-bold text-slate-700 text-sm">
                      {{ limiteTotalCalculado | currency: conta.moeda }}
                    </span>
                  </div>
                }
              </div>
            </div>
          }

          <!-- MOEDA -->
          <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5 min-w-0">
            <p class="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-3">
              Moeda
            </p>

            <div class="flex items-center gap-3">
              <span class="inline-flex items-center justify-center w-11 h-11 rounded-xl bg-orange-100 text-orange-600 text-base font-bold shrink-0">
                {{ simboloMoeda(conta.moeda) }}
              </span>

              <div class="flex flex-col min-w-0">
                <span class="text-lg font-bold text-slate-900 truncate">
                  {{ conta.moeda }}
                </span>
                <span class="text-sm text-slate-500 truncate">
                  {{ nomeMoeda(conta.moeda) }}
                </span>
              </div>
            </div>
          </div>

          @if (conta.sincronizada) {
            <div class="bg-indigo-50/70 border border-indigo-100 rounded-2xl p-4 flex items-center gap-3">
              <div class="w-8 h-8 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center shrink-0">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M3 21h18M3 10h18M5 10v11M9 10v11M15 10v11M19 10v11M12 3l9 5H3l9-5z"/>
                </svg>
              </div>
              <div>
                <p class="text-xs font-bold text-indigo-900">Conta Sincronizada via Open Finance</p>
                <p class="text-[11px] text-indigo-700">O saldo e as faturas são atualizados automaticamente pela integração bancária.</p>
              </div>
            </div>
          }

        </div>

      </div>
    }
  `
})
export class InformacoesComponent {

  @Input() conta: IContas | null = null;

  get ehCartao(): boolean {
    return ehCartaoCredito(this.conta);
  }

  get limiteConsumidoCalculado(): number {
    if (!this.conta) return 0;
    return Number(this.conta.saldo) || 0;
  }

  get limiteDisponivelCalculado(): number {
    if (!this.conta) return 0;
    if (this.conta.limiteDisponivel !== undefined && this.conta.limiteDisponivel !== null) {
      return Number(this.conta.limiteDisponivel);
    }
    if (this.conta.limiteTotal) {
      return Math.max(0, Number(this.conta.limiteTotal) - this.limiteConsumidoCalculado);
    }
    return 0;
  }

  get limiteTotalCalculado(): number | null {
    if (!this.conta) return null;
    if (this.conta.limiteTotal) {
      return Number(this.conta.limiteTotal);
    }
    if (this.conta.limiteDisponivel !== undefined && this.conta.limiteDisponivel !== null) {
      return this.limiteConsumidoCalculado + Number(this.conta.limiteDisponivel);
    }
    return null;
  }

  get percentualLimiteConsumido(): number | null {
    const total = this.limiteTotalCalculado;
    if (!total || total <= 0) return null;
    const pct = (this.limiteConsumidoCalculado / total) * 100;
    return Math.min(100, Math.max(0, pct));
  }

  localeDaMoeda(moeda: Moeda | null): string {
    switch (moeda) {
      case 'USD': return 'en-US';
      case 'EUR': return 'de-DE';
      default:    return 'pt-BR';
    }
  }

  simboloMoeda(moeda: Moeda | null): string {
    const simbolos: Record<string, string> = { BRL: 'R$', USD: 'US$', EUR: '€' };
    return simbolos[moeda ?? 'BRL'] ?? moeda ?? '';
  }

  nomeMoeda(moeda: Moeda | null): string {
    const nomes: Record<string, string> = {
      BRL: 'Real brasileiro',
      USD: 'Dólar americano',
      EUR: 'Euro'
    };
    return nomes[moeda ?? 'BRL'] ?? 'Moeda desconhecida';
  }

}
