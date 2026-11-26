import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ChartModule } from 'primeng/chart';
import { AdminApiService, AdminDashboardResumo, extrairMensagemErroAdmin } from '../../services/admin-api.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, ChartModule],
  template: `
    <div class="flex flex-col gap-5">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 class="text-2xl font-bold text-slate-900 m-0">Dashboard Executivo</h1>
          <p class="text-sm text-slate-500 mt-1 mb-0">Métricas-Norte de produto, crescimento e engajamento da plataforma</p>
        </div>
        <div class="flex items-center gap-3 self-start">
          @if (atualizadoEm(); as dt) {
            <span class="text-xs text-slate-400">Atualizado às {{ dt | date:'HH:mm' }}</span>
          }
          <button
            type="button"
            (click)="carregar()"
            [disabled]="carregando()"
            class="px-4 py-2 rounded-xl bg-white border border-slate-200 hover:bg-slate-50 text-xs font-semibold text-slate-700 flex items-center gap-2 cursor-pointer">
            <i class="pi pi-refresh text-xs" [class.pi-spin]="carregando()"></i>
            <span>Atualizar</span>
          </button>
        </div>
      </div>

      @if (erro()) {
        <div class="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm">{{ erro() }}</div>
      }

      @if (resumo(); as r) {
        <!-- Alertas Operacionais -->
        @if (r.ativosNaoVerificados > 0 || r.usuariosSuspensos > 0 || r.usuariosRiscoChurn > 0) {
          <div class="flex flex-wrap items-center gap-2.5">
            @if (r.usuariosRiscoChurn > 0) {
              <a
                [routerLink]="['/admin/comunicacao']"
                [queryParams]="{ segmento: 'RISCO_CHURN' }"
                class="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-purple-50 hover:bg-purple-100 border border-purple-200 text-[#7843CD] text-xs font-semibold transition-colors no-underline"
                title="Usuários já ativados sem acesso, transação ou aporte nos últimos 14 dias">
                <i class="pi pi-exclamation-triangle text-xs"></i>
                <span>{{ r.usuariosRiscoChurn }} usuário(s) em risco de churn (14+ dias inativos)</span>
                <i class="pi pi-arrow-right text-[10px]"></i>
              </a>
            }
            @if (r.ativosNaoVerificados > 0) {
              <a
                [routerLink]="['/admin/usuarios']"
                [queryParams]="{ emailVerificado: 'false' }"
                class="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-amber-50 hover:bg-amber-100 border border-amber-200 text-amber-800 text-xs font-semibold transition-colors no-underline">
                <i class="pi pi-envelope text-xs"></i>
                <span>{{ r.ativosNaoVerificados }} conta(s) com e-mail pendente</span>
                <i class="pi pi-arrow-right text-[10px]"></i>
              </a>
            }
            @if (r.usuariosSuspensos > 0) {
              <a
                [routerLink]="['/admin/usuarios']"
                [queryParams]="{ ativo: 'false' }"
                class="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-rose-50 hover:bg-rose-100 border border-rose-200 text-rose-800 text-xs font-semibold transition-colors no-underline">
                <i class="pi pi-ban text-xs"></i>
                <span>{{ r.usuariosSuspensos }} conta(s) suspensa(s)</span>
                <i class="pi pi-arrow-right text-[10px]"></i>
              </a>
            }
          </div>
        }

        <!-- SEÇÃO 1: 4 MÉTRICAS-NORTE -->
        <section class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
          <!-- 1. Taxa de Ativação (24h) -->
          <div class="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between gap-2">
                <span class="text-xs font-semibold uppercase tracking-wider text-slate-500 flex items-center gap-1">
                  Ativação (24h)
                  <i
                    class="pi pi-question-circle text-[11px] text-slate-400 cursor-help"
                    title="% de novos cadastrados que importam 1 fatura PDF, criam 1 transação ou 1 meta nas primeiras 24h após o cadastro."></i>
                </span>
                <span
                  class="text-[11px] font-bold px-2 py-0.5 rounded-full shrink-0"
                  [class.bg-emerald-50]="r.taxaAtivacao24h >= 60"
                  [class.text-emerald-700]="r.taxaAtivacao24h >= 60"
                  [class.bg-amber-50]="r.taxaAtivacao24h < 60"
                  [class.text-amber-700]="r.taxaAtivacao24h < 60">
                  Meta &gt; 60%
                </span>
              </div>
              <p class="text-3xl font-bold text-slate-900 mt-2.5 mb-1">{{ r.taxaAtivacao24h }}%</p>
              <p class="text-xs text-slate-500 m-0">
                {{ r.usuariosAtivados24h }} de {{ r.usuariosBaseAtivacao24h }} ativaram em 24h
              </p>
            </div>
            <div class="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden mt-3">
              <div
                class="h-full rounded-full transition-all"
                [class.bg-emerald-500]="r.taxaAtivacao24h >= 60"
                [class.bg-[#7843CD]]="r.taxaAtivacao24h < 60"
                [style.width.%]="r.taxaAtivacao24h > 100 ? 100 : r.taxaAtivacao24h"></div>
            </div>
          </div>

          <!-- 2. Índice de Automação -->
          <div class="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between gap-2">
                <span class="text-xs font-semibold uppercase tracking-wider text-slate-500 flex items-center gap-1">
                  Índice de Automação
                  <i
                    class="pi pi-question-circle text-[11px] text-slate-400 cursor-help"
                    title="% de transações criadas via Fatura PDF ou Open Finance em relação ao total de transações da plataforma."></i>
                </span>
                <span class="w-7 h-7 rounded-lg bg-purple-50 text-[#7843CD] flex items-center justify-center shrink-0"><i class="pi pi-bolt text-xs"></i></span>
              </div>
              <p class="text-3xl font-bold text-slate-900 mt-2.5 mb-1">{{ r.indiceAutomacao }}%</p>
              <p class="text-xs text-slate-500 m-0">
                PDF ({{ r.transacoesFaturaPdf }}) + Open Finance ({{ r.transacoesOpenFinance }}) vs. Manual ({{ r.transacoesManuais }})
              </p>
            </div>
            <div class="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden mt-3">
              <div class="h-full bg-[#7843CD] rounded-full transition-all" [style.width.%]="r.indiceAutomacao > 100 ? 100 : r.indiceAutomacao"></div>
            </div>
          </div>

          <!-- 3. Fator Viral Familiar (K) -->
          <div class="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between gap-2">
                <span class="text-xs font-semibold uppercase tracking-wider text-slate-500 flex items-center gap-1">
                  Fator Viral (K)
                  <i
                    class="pi pi-question-circle text-[11px] text-slate-400 cursor-help"
                    title="Média de membros ativos por grupo familiar criado (membros ativos em famílias / total de famílias)."></i>
                </span>
                <span class="w-7 h-7 rounded-lg bg-sky-50 text-sky-600 flex items-center justify-center shrink-0"><i class="pi pi-home text-xs"></i></span>
              </div>
              <p class="text-3xl font-bold text-slate-900 mt-2.5 mb-1">{{ r.fatorViralFamiliar | number:'1.2-2' }}</p>
              <p class="text-xs text-slate-500 m-0">
                {{ r.usuariosEmFamilia }} membro(s) em {{ r.totalFamilias }} família(s)
              </p>
            </div>
            <div class="text-[11px] text-slate-400 mt-3">{{ r.convitesPendentes }} convite(s) pendente(s)</div>
          </div>

          <!-- 4. Retenção D7 & 2º Mês -->
          <div class="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs flex flex-col justify-between">
            <div>
              <div class="flex items-center justify-between gap-2">
                <span class="text-xs font-semibold uppercase tracking-wider text-slate-500 flex items-center gap-1">
                  Retenção (D7 / M2)
                  <i
                    class="pi pi-question-circle text-[11px] text-slate-400 cursor-help"
                    title="D7: % de contas com > 7d que voltaram após o 7º dia. M2: % de contas com > 30d que voltaram no 2º mês."></i>
                </span>
                <span class="w-7 h-7 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0"><i class="pi pi-replay text-xs"></i></span>
              </div>
              <div class="flex items-baseline gap-3 mt-2.5 mb-1">
                <div>
                  <span class="text-2xl font-bold text-slate-900">{{ r.usuariosElegiveisD7 > 0 ? (r.retencaoD7 + '%') : '—' }}</span>
                  <span class="text-[11px] font-semibold text-slate-400 ml-1">D7</span>
                </div>
                <span class="text-slate-300">/</span>
                <div>
                  <span class="text-2xl font-bold text-emerald-700">{{ r.usuariosElegiveisM2 > 0 ? (r.retencaoSegundoMes + '%') : '—' }}</span>
                  <span class="text-[11px] font-semibold text-slate-400 ml-1">M2</span>
                </div>
              </div>
              <p class="text-xs text-slate-500 m-0">
                @if (r.usuariosElegiveisD7 > 0 || r.usuariosElegiveisM2 > 0) {
                  D7: {{ r.usuariosRetidosD7 }}/{{ r.usuariosElegiveisD7 }} • M2: {{ r.usuariosRetidosM2 }}/{{ r.usuariosElegiveisM2 }}
                } @else {
                  Aguardando contas com &gt; 7 dias
                }
              </p>
            </div>
            <div class="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden mt-3">
              <div
                class="h-full bg-emerald-500 rounded-full transition-all"
                [style.width.%]="(r.usuariosElegiveisM2 > 0 ? r.retencaoSegundoMes : r.retencaoD7) > 100 ? 100 : (r.usuariosElegiveisM2 > 0 ? r.retencaoSegundoMes : r.retencaoD7)"></div>
            </div>
          </div>
        </section>

        <!-- SEÇÃO 2: FAIXA COMPACTA DE ENGAJAMENTO & VOLUME -->
        <section class="bg-white rounded-2xl border border-slate-200 p-4 shadow-xs grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-4 divide-y sm:divide-y-0 lg:divide-x divide-slate-100">
          <a routerLink="/admin/usuarios" class="no-underline block group">
            <span class="text-[11px] text-slate-500 group-hover:text-[#7843CD]">Usuários</span>
            <p class="text-lg font-bold text-slate-900 m-0">{{ r.totalUsuarios }}</p>
            <span class="text-[11px] text-emerald-600">{{ r.usuariosAtivos }} ativos</span>
          </a>
          <div class="lg:pl-4">
            <span class="text-[11px] text-slate-500">Novos (30d)</span>
            <p class="text-lg font-bold text-slate-900 m-0">+{{ r.novosUltimos30Dias }}</p>
            <span class="text-[11px] text-slate-400">+{{ r.novosUltimos7Dias }} em 7d</span>
          </div>
          <div class="lg:pl-4">
            <span class="text-[11px] text-slate-500 flex items-center gap-1">
              WAU / MAU
              <i class="pi pi-question-circle text-[10px] text-slate-400 cursor-help" title="Ativos na semana (7d) / Ativos no mês (30d)."></i>
            </span>
            <p class="text-lg font-bold text-slate-900 m-0">{{ r.stickinessWauMau }}%</p>
            <span class="text-[11px] text-slate-400">{{ r.wau }} / {{ r.mau }} usuários</span>
          </div>
          <div class="lg:pl-4">
            <span class="text-[11px] text-slate-500">Metas Concluídas</span>
            <p class="text-lg font-bold text-slate-900 m-0">{{ r.taxaConclusaoMetas }}%</p>
            <span class="text-[11px] text-slate-400">{{ r.metasConcluidas }} de {{ r.totalMetas }}</span>
          </div>
          <div class="lg:pl-4">
            <span class="text-[11px] text-slate-500">Transações</span>
            <p class="text-lg font-bold text-slate-900 m-0">{{ r.totalTransacoes }}</p>
            <span class="text-[11px] text-slate-400">{{ r.usuariosAtivados }} ativados</span>
          </div>
          <div class="lg:pl-4">
            <span class="text-[11px] text-slate-500">Usuários c/ IA</span>
            <p class="text-lg font-bold text-slate-900 m-0">{{ r.usuariosComIa }}</p>
            <span class="text-[11px] text-slate-400">Chave BYOK</span>
          </div>
          <div class="lg:pl-4">
            <span class="text-[11px] text-slate-500">Open Finance</span>
            <p class="text-lg font-bold text-slate-900 m-0">{{ r.usuariosComOpenFinance }}</p>
            <span class="text-[11px] text-slate-400">Conexões Pluggy</span>
          </div>
        </section>

        <!-- SEÇÃO 3: GRÁFICOS PRINCIPAIS (2/3 Cadastros + 1/3 Origem das Transações) -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
          <div class="lg:col-span-2 bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
              <div>
                <h2 class="text-base font-bold text-slate-900 m-0">Evolução de Cadastros ({{ periodoDias() }} dias)</h2>
                <p class="text-xs text-slate-500 m-0 flex items-center gap-2 mt-0.5 flex-wrap">
                  <span>Total: <strong class="text-slate-800">{{ r.novosNoPeriodo }}</strong></span>
                  @if (variacaoPeriodo(); as v) {
                    <span
                      class="px-2 py-0.5 rounded-full text-[11px] font-bold"
                      [class.bg-emerald-50]="v.positiva"
                      [class.text-emerald-700]="v.positiva"
                      [class.bg-rose-50]="v.negativa"
                      [class.text-rose-700]="v.negativa"
                      [class.bg-slate-100]="!v.positiva && !v.negativa"
                      [class.text-slate-600]="!v.positiva && !v.negativa">
                      {{ v.texto }}
                    </span>
                  }
                </p>
              </div>
              <div class="inline-flex bg-slate-100 p-1 rounded-xl self-start">
                @for (d of [7, 30, 90]; track d) {
                  <button
                    type="button"
                    (click)="alterarPeriodo(d)"
                    class="px-3 py-1 rounded-lg text-xs font-semibold transition-colors cursor-pointer"
                    [class.bg-white]="periodoDias() === d"
                    [class.text-[#7843CD]]="periodoDias() === d"
                    [class.shadow-xs]="periodoDias() === d"
                    [class.text-slate-600]="periodoDias() !== d">
                    {{ d }}d
                  </button>
                }
              </div>
            </div>
            <div class="h-60">
              <p-chart type="line" [data]="cadastrosChartData()" [options]="lineOptions" height="100%"></p-chart>
            </div>
          </div>

          <div class="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs flex flex-col">
            <h2 class="text-base font-bold text-slate-900 m-0">Origem das Transações</h2>
            <p class="text-xs text-slate-500 mt-0.5 mb-4">Automação (PDF / Open Finance) vs. Manual</p>
            @if (r.totalTransacoes > 0) {
              <div class="h-60 flex items-center justify-center">
                <p-chart type="doughnut" [data]="automacaoChartData()" [options]="doughnutOptions" height="100%"></p-chart>
              </div>
            } @else {
              <div class="h-60 flex flex-col items-center justify-center text-center text-slate-400 gap-2">
                <i class="pi pi-chart-pie text-2xl"></i>
                <p class="text-xs m-0">Nenhuma transação registrada ainda.</p>
              </div>
            }
          </div>
        </div>

        <!-- SEÇÃO 4: FUNIL DE ATIVAÇÃO (5 ETAPAS) -->
        <section class="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
            <div>
              <h2 class="text-base font-bold text-slate-900 m-0">Funil de Ativação</h2>
              <p class="text-xs text-slate-500 m-0">Identifique em qual etapa da jornada os usuários estão abandonando</p>
            </div>
            <span class="text-xs font-semibold text-slate-500">
              Risco de churn (14+ dias): <strong class="text-rose-600">{{ r.usuariosRiscoChurn }}</strong>
            </span>
          </div>

          <div class="flex flex-col gap-3">
            @for (etapa of funilEtapas(); track etapa.label; let idx = $index) {
              <div class="flex flex-col gap-1">
                <div class="flex items-center justify-between text-xs">
                  <div class="flex items-center gap-2">
                    <span class="w-5 h-5 rounded-full bg-purple-100 text-[#7843CD] font-bold text-[10px] flex items-center justify-center">
                      {{ idx + 1 }}
                    </span>
                    <span class="font-semibold text-slate-800">{{ etapa.label }}</span>
                    @if (etapa.taxaEtapaAnterior !== null) {
                      <span class="text-[11px] text-slate-400">({{ etapa.taxaEtapaAnterior }}% da etapa anterior)</span>
                    }
                  </div>
                  <div class="flex items-center gap-2">
                    <strong class="text-slate-900">{{ etapa.valor }}</strong>
                    <span class="text-[11px] font-semibold text-[#7843CD] w-11 text-right">{{ etapa.percentualTotal }}%</span>
                  </div>
                </div>
                <div class="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                  <div
                    class="h-full rounded-full transition-all duration-300"
                    [style.width.%]="etapa.percentualTotal"
                    [style.background-color]="etapa.cor"></div>
                </div>
              </div>
            }
          </div>
        </section>

        <!-- SEÇÃO 5: SAÚDE DA BASE + ADOÇÃO POR USUÁRIOS -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <div class="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
            <h2 class="text-base font-bold text-slate-900 m-0">Saúde da Base</h2>
            <p class="text-xs text-slate-500 mt-0.5 mb-4">Distribuição exclusiva ({{ r.totalUsuarios }} contas)</p>
            @if (r.totalUsuarios > 0) {
              <div class="h-56 flex items-center justify-center">
                <p-chart type="doughnut" [data]="statusChartData()" [options]="doughnutOptions" height="100%"></p-chart>
              </div>
            } @else {
              <div class="h-56 flex flex-col items-center justify-center text-center text-slate-400 gap-2">
                <i class="pi pi-users text-2xl"></i>
                <p class="text-xs m-0">Nenhum usuário cadastrado ainda.</p>
              </div>
            }
          </div>

          <div class="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
            <h2 class="text-base font-bold text-slate-900 m-0">Adoção por Usuários</h2>
            <p class="text-xs text-slate-500 mt-0.5 mb-4">Usuários engajados por recurso</p>
            <div class="h-56">
              <p-chart type="bar" [data]="adocaoChartData()" [options]="barOptions" height="100%"></p-chart>
            </div>
          </div>
        </div>

        <!-- SEÇÃO 6: SATISFAÇÃO DO CONSULTOR GRAFITH IA -->
        <section class="bg-white rounded-2xl border border-slate-200 p-5 shadow-xs">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
            <div>
              <h2 class="text-base font-bold text-slate-900 m-0 flex items-center gap-2">
                <i class="pi pi-sparkles text-[#7843CD]"></i>
                <span>Satisfação do Consultor Grafith IA</span>
              </h2>
              <p class="text-xs text-slate-500 mt-0.5 mb-0">
                Avaliações (Like / Dislike) enviadas pelos usuários nas respostas do chat financeiro
              </p>
            </div>
            <span class="text-xs font-bold px-3 py-1 rounded-full bg-purple-50 text-[#7843CD] self-start sm:self-auto">
              {{ (r.consultorLikes || 0) + (r.consultorDislikes || 0) }} avaliação(ões)
            </span>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-4">
            <div class="rounded-xl bg-emerald-50/70 border border-emerald-200/70 p-3.5 flex items-center justify-between">
              <div>
                <span class="text-xs font-semibold text-emerald-800 block">Respostas úteis (Likes)</span>
                <strong class="text-2xl font-extrabold text-emerald-700">{{ r.consultorLikes || 0 }}</strong>
              </div>
              <span class="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
                <i class="pi pi-thumbs-up-fill text-base"></i>
              </span>
            </div>

            <div class="rounded-xl bg-rose-50/70 border border-rose-200/70 p-3.5 flex items-center justify-between">
              <div>
                <span class="text-xs font-semibold text-rose-800 block">Não ajudaram (Dislikes)</span>
                <strong class="text-2xl font-extrabold text-rose-700">{{ r.consultorDislikes || 0 }}</strong>
              </div>
              <span class="w-10 h-10 rounded-xl bg-rose-100 text-rose-700 flex items-center justify-center">
                <i class="pi pi-thumbs-down-fill text-base"></i>
              </span>
            </div>

            <div class="rounded-xl bg-purple-50/70 border border-purple-200/70 p-3.5 flex items-center justify-between">
              <div>
                <span class="text-xs font-semibold text-purple-900 block">Taxa de Aprovação</span>
                <strong class="text-2xl font-extrabold text-[#7843CD]">{{ r.consultorTaxaAprovacao || 0 }}%</strong>
              </div>
              <span class="w-10 h-10 rounded-xl bg-purple-100 text-[#7843CD] flex items-center justify-center">
                <i class="pi pi-chart-line text-base"></i>
              </span>
            </div>
          </div>

          @if (((r.consultorLikes || 0) + (r.consultorDislikes || 0)) > 0) {
            <div class="w-full h-2.5 bg-rose-200 rounded-full overflow-hidden">
              <div
                class="h-full bg-emerald-500 transition-all duration-300"
                [style.width.%]="r.consultorTaxaAprovacao"></div>
            </div>
          } @else {
            <p class="text-xs text-slate-400 m-0">Nenhuma resposta do Grafith foi avaliada ainda.</p>
          }

          @if (r.ultimosDislikesConsultor && r.ultimosDislikesConsultor.length > 0) {
            <div class="mt-5 pt-4 border-t border-slate-100">
              <h3 class="text-xs font-bold uppercase tracking-wider text-rose-700 mb-3 flex items-center gap-1.5">
                <i class="pi pi-thumbs-down-fill text-[11px]"></i>
                <span>Últimas respostas com Dislike ({{ r.ultimosDislikesConsultor.length }})</span>
              </h3>
              <div class="flex flex-col gap-2.5 max-h-72 overflow-y-auto pr-1">
                @for (item of r.ultimosDislikesConsultor; track item.id) {
                  <div class="rounded-xl bg-rose-50/40 border border-rose-200/60 p-3 text-xs flex flex-col gap-1.5">
                    <div class="flex flex-wrap items-center justify-between gap-2 text-[11px] text-slate-500">
                      <span class="font-bold text-slate-700">
                        {{ item.usuarioNome }}
                        @if (item.usuarioEmail) {
                          <span class="font-normal text-slate-400">({{ item.usuarioEmail }})</span>
                        }
                      </span>
                      <div class="flex items-center gap-2">
                        @if (item.provedor) {
                          <span class="px-2 py-0.5 rounded-full bg-white border border-slate-200 text-slate-600 font-semibold">
                            {{ item.provedor }}
                          </span>
                        }
                        @if (item.criadoEm) {
                          <span>{{ item.criadoEm | date:'dd/MM/yyyy HH:mm' }}</span>
                        }
                      </div>
                    </div>
                    @if (item.perguntaResumo) {
                      <div class="text-slate-700">
                        <strong class="text-slate-900">Pergunta:</strong> {{ item.perguntaResumo }}
                      </div>
                    }
                    @if (item.respostaResumo) {
                      <div class="text-slate-600 line-clamp-3">
                        <strong class="text-slate-800">Resposta:</strong> {{ item.respostaResumo }}
                      </div>
                    }
                  </div>
                }
              </div>
            </div>
          }
        </section>
      } @else if (carregando()) {
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
          @for (i of [1,2,3,4]; track i) {
            <div class="h-32 rounded-2xl bg-slate-200/70 animate-pulse"></div>
          }
        </div>
      }
    </div>
  `,
})
export class AdminDashboardComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);

  readonly resumo = signal<AdminDashboardResumo | null>(null);
  readonly periodoDias = signal<number>(30);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly atualizadoEm = signal<Date | null>(null);

  readonly variacaoPeriodo = computed(() => {
    const r = this.resumo();
    if (!r) return null;
    const atual = r.novosNoPeriodo;
    const anterior = r.novosPeriodoAnterior;
    if (anterior === 0 && atual === 0) {
      return { texto: `0% vs. ${r.periodoDias}d anteriores`, positiva: false, negativa: false };
    }
    if (anterior === 0) {
      return { texto: `+${atual} novo(s) vs. 0 anteriores`, positiva: true, negativa: false };
    }
    const pct = Math.round(((atual - anterior) / anterior) * 100);
    if (pct > 0) return { texto: `+${pct}% vs. ${r.periodoDias}d anteriores`, positiva: true, negativa: false };
    if (pct < 0) return { texto: `${pct}% vs. ${r.periodoDias}d anteriores`, positiva: false, negativa: true };
    return { texto: `Estável vs. ${r.periodoDias}d anteriores`, positiva: false, negativa: false };
  });

  readonly cadastrosChartData = computed(() => {
    const r = this.resumo();
    if (!r) return {};
    return {
      labels: r.serieCadastros.map((p) => {
        const [, mes, dia] = p.data.split('-');
        return `${dia}/${mes}`;
      }),
      datasets: [
        {
          label: 'Novos cadastros',
          data: r.serieCadastros.map((p) => p.total),
          fill: true,
          borderColor: '#7843CD',
          backgroundColor: 'rgba(120, 67, 205, 0.14)',
          tension: 0.35,
          pointRadius: r.periodoDias > 30 ? 1.5 : 3,
          pointHoverRadius: 5,
          pointBackgroundColor: '#7843CD',
        },
      ],
    };
  });

  readonly automacaoChartData = computed(() => {
    const r = this.resumo();
    if (!r) return {};
    return {
      labels: ['Manual', 'Fatura PDF', 'Open Finance'],
      datasets: [
        {
          data: [r.transacoesManuais, r.transacoesFaturaPdf, r.transacoesOpenFinance],
          backgroundColor: ['#94A3B8', '#7843CD', '#10B981'],
          hoverBackgroundColor: ['#64748B', '#6635B5', '#059669'],
          borderWidth: 0,
        },
      ],
    };
  });

  readonly statusChartData = computed(() => {
    const r = this.resumo();
    if (!r) return {};
    return {
      labels: ['Ativos verificados', 'Ativos (e-mail pendente)', 'Suspensos'],
      datasets: [
        {
          data: [r.ativosVerificados, r.ativosNaoVerificados, r.usuariosSuspensos],
          backgroundColor: ['#10B981', '#F59E0B', '#F43F5E'],
          hoverBackgroundColor: ['#059669', '#D97706', '#E11D48'],
          borderWidth: 0,
        },
      ],
    };
  });

  readonly adocaoChartData = computed(() => {
    const r = this.resumo();
    if (!r) return {};
    return {
      labels: ['Ativados', 'Em Família', 'Assinaturas', 'Grafith IA', 'Chave IA', 'Open Finance'],
      datasets: [
        {
          label: 'Usuários',
          data: [
            r.usuariosAtivados,
            r.usuariosEmFamilia,
            r.usuariosComAssinaturas || 0,
            r.usuariosUsaramConsultor || 0,
            r.usuariosComIa,
            r.usuariosComOpenFinance,
          ],
          backgroundColor: ['#F59E0B', '#7843CD', '#0EA5E9', '#EC4899', '#8B5CF6', '#10B981'],
          borderRadius: 8,
        },
      ],
    };
  });

  readonly funilEtapas = computed(() => {
    const r = this.resumo();
    if (!r) return [];
    const base = r.totalUsuarios;
    const etapasBrutas = [
      { label: 'Cadastrados', valor: r.totalUsuarios, cor: '#7843CD' },
      { label: 'E-mail verificado', valor: r.emailsVerificados, cor: '#8B5CF6' },
      { label: 'Criou 1ª conta financeira', valor: r.funilComConta, cor: '#0EA5E9' },
      { label: '1ª transação ou meta (Ativado)', valor: r.usuariosAtivados, cor: '#F59E0B' },
      { label: 'Ativo na última semana (WAU)', valor: r.wau, cor: '#10B981' },
    ];
    return etapasBrutas.map((item, idx) => {
      const percentualTotal = base > 0 ? Math.min(100, Math.round((item.valor / base) * 100)) : 0;
      const anterior = idx > 0 ? etapasBrutas[idx - 1].valor : null;
      const taxaEtapaAnterior =
        anterior === null ? null : anterior > 0 ? Math.min(100, Math.round((item.valor / anterior) * 100)) : 0;
      return { ...item, percentualTotal, taxaEtapaAnterior };
    });
  });

  readonly lineOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      y: { beginAtZero: true, ticks: { precision: 0, color: '#64748b' }, grid: { color: '#f1f5f9' } },
      x: { ticks: { maxTicksLimit: 10, color: '#64748b' }, grid: { display: false } },
    },
  };

  readonly doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '64%',
    plugins: {
      legend: {
        position: 'bottom',
        labels: { usePointStyle: true, padding: 12, color: '#475569', font: { size: 11 } },
      },
    },
  };

  readonly barOptions = {
    indexAxis: 'y',
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { beginAtZero: true, ticks: { precision: 0, color: '#64748b' }, grid: { color: '#f1f5f9' } },
      y: { ticks: { color: '#334155', font: { weight: 600, size: 11 } }, grid: { display: false } },
    },
  };

  ngOnInit(): void {
    this.carregar();
  }

  alterarPeriodo(dias: number): void {
    if (this.periodoDias() === dias) return;
    this.periodoDias.set(dias);
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.adminApi.obterResumoDashboard(this.periodoDias()).subscribe({
      next: (data) => {
        this.resumo.set(data);
        this.atualizadoEm.set(new Date());
        this.carregando.set(false);
      },
      error: (err) => {
        this.erro.set(extrairMensagemErroAdmin(err, 'Não foi possível carregar o resumo executivo.'));
        this.carregando.set(false);
      },
    });
  }
}
