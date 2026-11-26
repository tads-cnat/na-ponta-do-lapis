import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AppMoedaPipe } from '../../../../shared/pipes/app-moeda.pipe';
import {
  AssinaturasResumo,
  AssinaturasService,
  ParcelamentoResumo,
} from '../../../assinaturas/service/assinaturas.service';

interface ProximaCobrancaItem {
  tipo: 'ASSINATURA' | 'PARCELA';
  descricao: string;
  subtitulo: string;
  valor: number;
  data: string;
  badgeTempo: string;
  urgente: boolean;
}

@Component({
  selector: 'app-fixos-parcelas-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, AppMoedaPipe],
  template: `
    <div class="bg-white rounded-2xl border border-slate-200/80 shadow-xs p-5 flex flex-col gap-4">
      <div class="flex items-start justify-between gap-3">
        <div>
          <h2 class="text-base font-bold text-slate-900 m-0">Fixos e Parcelas do Mês</h2>
          <p class="text-xs text-slate-500 mt-0.5 mb-0">
            Comprometimento mensal com assinaturas ativas e compras parceladas
          </p>
        </div>
        <div class="flex items-center gap-2 shrink-0">
          <a
            [routerLink]="['/app/transacoes']"
            [queryParams]="{ aba: 'assinaturas' }"
            class="text-xs font-semibold text-[#7843CD] hover:underline no-underline">
            Assinaturas
          </a>
          <span class="text-slate-300">•</span>
          <a
            [routerLink]="['/app/transacoes']"
            [queryParams]="{ aba: 'parcelamentos' }"
            class="text-xs font-semibold text-[#7843CD] hover:underline no-underline">
            Parcelas
          </a>
        </div>
      </div>

      <!-- Resumo numérico -->
      <div class="grid grid-cols-3 gap-2.5">
        <div class="rounded-xl bg-purple-50/70 border border-purple-200/60 p-3">
          <span class="text-[11px] font-semibold text-purple-900 block">Comprometido/mês</span>
          <strong class="text-sm sm:text-base font-extrabold text-[#7843CD] block mt-0.5">
            {{ totalComprometido() | appMoeda }}
          </strong>
        </div>
        <div class="rounded-xl bg-slate-50 border border-slate-200/70 p-3">
          <span class="text-[11px] font-semibold text-slate-500 block">Assinaturas</span>
          <strong class="text-sm sm:text-base font-extrabold text-slate-800 block mt-0.5">
            {{ totalAssinaturas() | appMoeda }}
          </strong>
        </div>
        <div class="rounded-xl bg-slate-50 border border-slate-200/70 p-3">
          <span class="text-[11px] font-semibold text-slate-500 block">Parcelas</span>
          <strong class="text-sm sm:text-base font-extrabold text-slate-800 block mt-0.5">
            {{ totalParcelas() | appMoeda }}
          </strong>
        </div>
      </div>

      <!-- Próximas cobranças -->
      @if (carregando()) {
        <div class="flex flex-col gap-2">
          @for (i of [1, 2, 3]; track i) {
            <div class="h-12 rounded-xl bg-slate-100 animate-pulse"></div>
          }
        </div>
      } @else if (proximas().length === 0) {
        <div class="rounded-xl bg-slate-50 border border-dashed border-slate-200 p-4 text-center">
          <p class="text-xs text-slate-500 m-0">
            Nenhuma assinatura ativa ou parcelamento em aberto no momento.
          </p>
        </div>
      } @else {
        <div class="flex flex-col gap-2">
          <span class="text-[11px] font-bold uppercase tracking-wider text-slate-400">
            Próximas cobranças
          </span>
          @for (item of proximas(); track $index) {
            <div class="flex items-center justify-between gap-3 p-2.5 rounded-xl bg-slate-50/80 hover:bg-slate-100/70 border border-slate-100 transition-colors">
              <div class="flex items-center gap-2.5 min-w-0">
                <span
                  class="w-8 h-8 rounded-lg flex items-center justify-center shrink-0"
                  [class.bg-purple-100]="item.tipo === 'ASSINATURA'"
                  [class.text-[#7843CD]]="item.tipo === 'ASSINATURA'"
                  [class.bg-amber-100]="item.tipo === 'PARCELA'"
                  [class.text-amber-700]="item.tipo === 'PARCELA'">
                  <i class="pi text-xs" [class.pi-sync]="item.tipo === 'ASSINATURA'" [class.pi-credit-card]="item.tipo === 'PARCELA'"></i>
                </span>
                <div class="min-w-0">
                  <div class="text-xs sm:text-sm font-bold text-slate-800 truncate">{{ item.descricao }}</div>
                  <div class="text-[11px] text-slate-500 truncate">{{ item.subtitulo }}</div>
                </div>
              </div>

              <div class="text-right shrink-0">
                <div class="text-xs sm:text-sm font-extrabold text-rose-600">
                  -{{ item.valor | appMoeda }}
                </div>
                <span
                  class="text-[10px] font-bold px-1.5 py-0.5 rounded-full inline-block"
                  [class.bg-amber-100]="item.urgente"
                  [class.text-amber-800]="item.urgente"
                  [class.bg-slate-200/80]="!item.urgente"
                  [class.text-slate-600]="!item.urgente">
                  {{ item.badgeTempo }}
                </span>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class FixosParcelasDashboardComponent implements OnInit {
  private assinaturasService = inject(AssinaturasService);

  carregando = signal(true);
  totalAssinaturas = signal(0);
  totalParcelas = signal(0);
  totalComprometido = signal(0);
  proximas = signal<ProximaCobrancaItem[]>([]);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    forkJoin({
      assinaturas: this.assinaturasService.listar(),
      parcelamentos: this.assinaturasService.listarParcelamentos(),
    }).subscribe({
      next: ({ assinaturas, parcelamentos }) => {
        this.processar(assinaturas, parcelamentos || []);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
      },
    });
  }

  private processar(resumo: AssinaturasResumo, parcelamentos: ParcelamentoResumo[]): void {
    const mensalAssinaturas = resumo?.totalMensalDespesas ?? 0;
    const mensalParcelas = parcelamentos.reduce((acc, p) => acc + (p.valorParcela || 0), 0);

    this.totalAssinaturas.set(mensalAssinaturas);
    this.totalParcelas.set(mensalParcelas);
    this.totalComprometido.set(mensalAssinaturas + mensalParcelas);

    const itens: ProximaCobrancaItem[] = [];

    for (const a of resumo?.assinaturas ?? []) {
      if (a.status === 'ATIVA' && a.tipo === 'DESPESA' && a.proximaCobranca) {
        const infoTempo = this.calcularBadgeTempo(a.proximaCobranca);
        itens.push({
          tipo: 'ASSINATURA',
          descricao: a.descricao,
          subtitulo: `Assinatura • ${a.contaNome}`,
          valor: a.valor,
          data: a.proximaCobranca,
          badgeTempo: infoTempo.label,
          urgente: infoTempo.urgente,
        });
      }
    }

    for (const p of parcelamentos) {
      if (p.proximaParcelaData) {
        const infoTempo = this.calcularBadgeTempo(p.proximaParcelaData);
        const parcelaAtual = Math.min(p.parcelasPagas + 1, p.totalParcelas);
        itens.push({
          tipo: 'PARCELA',
          descricao: p.descricao,
          subtitulo: `Parcela ${parcelaAtual}/${p.totalParcelas} • ${p.contaNome || 'Conta'}`,
          valor: p.valorParcela,
          data: p.proximaParcelaData,
          badgeTempo: infoTempo.label,
          urgente: infoTempo.urgente,
        });
      }
    }

    itens.sort((a, b) => a.data.localeCompare(b.data));
    this.proximas.set(itens.slice(0, 4));
  }

  private calcularBadgeTempo(dataIso: string): { label: string; urgente: boolean } {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const [ano, mes, dia] = dataIso.split('-').map(Number);
    const alvo = new Date(ano, (mes || 1) - 1, dia || 1);
    const diffDias = Math.round((alvo.getTime() - hoje.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDias < 0) return { label: 'Vencida', urgente: true };
    if (diffDias === 0) return { label: 'Hoje', urgente: true };
    if (diffDias === 1) return { label: 'Amanhã', urgente: true };
    if (diffDias <= 5) return { label: `Em ${diffDias} dias`, urgente: true };

    const dd = String(dia).padStart(2, '0');
    const mm = String(mes).padStart(2, '0');
    return { label: `${dd}/${mm}`, urgente: false };
  }
}
