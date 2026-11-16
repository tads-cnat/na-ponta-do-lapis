import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PrimeNGModuleModule } from '../../../../shared/primeNg.module';
import { ITransacoes } from '../../../../model/ITransacoes.model';
import { MetaResponse } from '../../../../model/IMetas.models';
import { PreferenciaMoedaService } from '../../../../shared/service/preferencia-moeda.service';

export type PeriodoRelatorio = 'mes_atual' | 'mes_anterior' | 'ultimos_3_meses' | 'ano_atual' | 'personalizado';

interface CategoriaResumoItem {
  nome: string;
  valor: number;
  percentual: number;
  quantidade: number;
}

@Component({
  selector: 'app-exportar-relatorio-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, PrimeNGModuleModule],
  template: `
    <p-dialog
      [visible]="visible"
      (visibleChange)="fecharModal($event)"
      [modal]="true"
      [draggable]="false"
      [resizable]="false"
      header="Exportar Relatório Financeiro"
      [style]="{ width: '95vw', maxWidth: '540px' }">

      <div class="flex flex-col gap-5 pt-1">
        <!-- Seleção de Período -->
        <div class="flex flex-col gap-2">
          <label class="text-xs font-bold uppercase tracking-wider text-slate-500">Período do relatório</label>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
            @for (op of opcoesPeriodo; track op.valor) {
              <button
                type="button"
                (click)="selecionarPeriodo(op.valor)"
                class="px-3 py-2 rounded-xl text-xs font-semibold border transition-all cursor-pointer text-left flex items-center justify-between"
                [class]="periodoSelecionado === op.valor
                  ? 'bg-[#F3E8FF] border-[#7843CD] text-[#4A2585]'
                  : 'bg-white border-slate-200 text-slate-600 hover:border-slate-300'">
                <span>{{ op.label }}</span>
                @if (periodoSelecionado === op.valor) {
                  <i class="pi pi-check text-[10px] text-[#7843CD]"></i>
                }
              </button>
            }
          </div>

          @if (periodoSelecionado === 'personalizado') {
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-2">
              <div class="flex flex-col gap-1">
                <label class="text-xs font-semibold text-slate-600">Data inicial</label>
                <input
                  type="date"
                  [(ngModel)]="dataInicioCustom"
                  class="px-3 py-2 text-sm border border-slate-200 rounded-xl focus:outline-none focus:border-[#7843CD]" />
              </div>
              <div class="flex flex-col gap-1">
                <label class="text-xs font-semibold text-slate-600">Data final</label>
                <input
                  type="date"
                  [(ngModel)]="dataFimCustom"
                  class="px-3 py-2 text-sm border border-slate-200 rounded-xl focus:outline-none focus:border-[#7843CD]" />
              </div>
            </div>
          }
        </div>

        <!-- Prévia rápida do período -->
        <div class="rounded-xl bg-slate-50 border border-slate-100 p-3.5 flex items-center justify-between gap-3">
          <div>
            <p class="text-xs font-semibold text-slate-700 m-0">{{ descricaoPeriodoAtual }}</p>
            <p class="text-[11px] text-slate-500 m-0 mt-0.5">
              {{ transacoesFiltradas.length }} movimentações encontradas no período
            </p>
          </div>
          <div class="text-right text-xs">
            <div class="text-emerald-600 font-semibold">+ {{ formatarMoeda(totalReceitas) }}</div>
            <div class="text-rose-600 font-semibold">- {{ formatarMoeda(totalDespesas) }}</div>
          </div>
        </div>

        <!-- Seções para incluir -->
        <div class="flex flex-col gap-2.5">
          <label class="text-xs font-bold uppercase tracking-wider text-slate-500">Seções a incluir</label>

          <label class="flex items-center gap-2.5 text-sm text-slate-700 cursor-pointer">
            <p-checkbox [(ngModel)]="incluirResumo" [binary]="true"></p-checkbox>
            <span>Resumo Geral (Saldo atual, Receitas, Despesas e Balanço)</span>
          </label>

          <label class="flex items-center gap-2.5 text-sm text-slate-700 cursor-pointer">
            <p-checkbox [(ngModel)]="incluirCategorias" [binary]="true"></p-checkbox>
            <span>Gastos por Categoria (ranking e percentual)</span>
          </label>

          <label class="flex items-center gap-2.5 text-sm text-slate-700 cursor-pointer">
            <p-checkbox [(ngModel)]="incluirMetas" [binary]="true"></p-checkbox>
            <span>Progresso das Metas Financeiras</span>
          </label>

          <label class="flex items-center gap-2.5 text-sm text-slate-700 cursor-pointer">
            <p-checkbox [(ngModel)]="incluirTransacoes" [binary]="true"></p-checkbox>
            <span>Listagem detalhada das Transações</span>
          </label>
        </div>

        <!-- Botões de Ação -->
        <div class="flex flex-col sm:flex-row items-stretch sm:items-center justify-end gap-2.5 pt-2 border-t border-slate-100">
          <p-button
            type="button"
            label="Baixar CSV (Excel)"
            icon="pi pi-download"
            styleClass="w-full sm:w-auto btn-npl-secondary text-xs sm:text-sm px-4 py-2.5 rounded-xl!"
            [disabled]="!algumaSecaoMarcada"
            (onClick)="exportarCSV()">
          </p-button>

          <p-button
            type="button"
            label="Gerar PDF"
            icon="pi pi-file-pdf"
            styleClass="w-full sm:w-auto btn-npl-primary text-xs sm:text-sm px-4 py-2.5 rounded-xl!"
            [disabled]="!algumaSecaoMarcada"
            (onClick)="exportarPDF()">
          </p-button>
        </div>
      </div>
    </p-dialog>
  `,
})
export class ExportarRelatorioModalComponent {
  private readonly moedaService = inject(PreferenciaMoedaService);

  @Input() visible = false;
  @Output() visibleChange = new EventEmitter<boolean>();

  @Input() transacoes: ITransacoes[] = [];
  @Input() metas: MetaResponse[] = [];
  @Input() saldoAtual = 0;

  periodoSelecionado: PeriodoRelatorio = 'mes_atual';
  dataInicioCustom = '';
  dataFimCustom = '';

  incluirResumo = true;
  incluirCategorias = true;
  incluirMetas = true;
  incluirTransacoes = true;

  readonly opcoesPeriodo: { valor: PeriodoRelatorio; label: string }[] = [
    { valor: 'mes_atual', label: 'Mês atual' },
    { valor: 'mes_anterior', label: 'Mês anterior' },
    { valor: 'ultimos_3_meses', label: 'Últimos 3 meses' },
    { valor: 'ano_atual', label: 'Ano atual' },
    { valor: 'personalizado', label: 'Personalizado' },
  ];

  get algumaSecaoMarcada(): boolean {
    return this.incluirResumo || this.incluirCategorias || this.incluirMetas || this.incluirTransacoes;
  }

  selecionarPeriodo(periodo: PeriodoRelatorio): void {
    this.periodoSelecionado = periodo;
    if (periodo === 'personalizado' && (!this.dataInicioCustom || !this.dataFimCustom)) {
      const hoje = new Date();
      const inicioMes = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
      this.dataInicioCustom = this.paraIsoDate(inicioMes);
      this.dataFimCustom = this.paraIsoDate(hoje);
    }
  }

  fecharModal(novoValor: boolean): void {
    this.visible = novoValor;
    this.visibleChange.emit(novoValor);
  }

  get intervaloDatas(): { inicio: Date; fim: Date; label: string } {
    const agora = new Date();
    switch (this.periodoSelecionado) {
      case 'mes_anterior': {
        const inicio = new Date(agora.getFullYear(), agora.getMonth() - 1, 1, 0, 0, 0);
        const fim = new Date(agora.getFullYear(), agora.getMonth(), 0, 23, 59, 59);
        return {
          inicio,
          fim,
          label: inicio.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' }),
        };
      }
      case 'ultimos_3_meses': {
        const inicio = new Date(agora.getFullYear(), agora.getMonth() - 2, 1, 0, 0, 0);
        const fim = new Date(agora.getFullYear(), agora.getMonth() + 1, 0, 23, 59, 59);
        return {
          inicio,
          fim,
          label: `Últimos 3 meses (${inicio.toLocaleDateString('pt-BR')} a ${fim.toLocaleDateString('pt-BR')})`,
        };
      }
      case 'ano_atual': {
        const inicio = new Date(agora.getFullYear(), 0, 1, 0, 0, 0);
        const fim = new Date(agora.getFullYear(), 11, 31, 23, 59, 59);
        return {
          inicio,
          fim,
          label: `Ano de ${agora.getFullYear()}`,
        };
      }
      case 'personalizado': {
        const inicio = this.dataInicioCustom ? new Date(`${this.dataInicioCustom}T00:00:00`) : new Date(agora.getFullYear(), agora.getMonth(), 1);
        const fim = this.dataFimCustom ? new Date(`${this.dataFimCustom}T23:59:59`) : new Date();
        return {
          inicio,
          fim,
          label: `${inicio.toLocaleDateString('pt-BR')} a ${fim.toLocaleDateString('pt-BR')}`,
        };
      }
      case 'mes_atual':
      default: {
        const inicio = new Date(agora.getFullYear(), agora.getMonth(), 1, 0, 0, 0);
        const fim = new Date(agora.getFullYear(), agora.getMonth() + 1, 0, 23, 59, 59);
        return {
          inicio,
          fim,
          label: inicio.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' }),
        };
      }
    }
  }

  get descricaoPeriodoAtual(): string {
    const label = this.intervaloDatas.label;
    return label.charAt(0).toUpperCase() + label.slice(1);
  }

  get transacoesFiltradas(): ITransacoes[] {
    if (!this.transacoes?.length) return [];
    const { inicio, fim } = this.intervaloDatas;
    return this.transacoes
      .filter((t) => {
        if (!t.dataHora) return false;
        const data = new Date(t.dataHora);
        return data >= inicio && data <= fim;
      })
      .sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime());
  }

  get totalReceitas(): number {
    return this.transacoesFiltradas
      .filter((t) => t.tipo === 'RECEITA')
      .reduce((acc, t) => acc + (Number(t.valor) || 0), 0);
  }

  get totalDespesas(): number {
    return this.transacoesFiltradas
      .filter((t) => t.tipo === 'DESPESA')
      .reduce((acc, t) => acc + (Number(t.valor) || 0), 0);
  }

  get balancoPeriodo(): number {
    return this.totalReceitas - this.totalDespesas;
  }

  get resumoPorCategoria(): CategoriaResumoItem[] {
    const despesas = this.transacoesFiltradas.filter((t) => t.tipo === 'DESPESA');
    const total = this.totalDespesas;
    if (!despesas.length || total <= 0) return [];

    const mapa = new Map<string, { valor: number; quantidade: number }>();
    for (const d of despesas) {
      const nome = d.categoria?.nome || 'Sem categoria';
      const atual = mapa.get(nome) || { valor: 0, quantidade: 0 };
      atual.valor += Number(d.valor) || 0;
      atual.quantidade += 1;
      mapa.set(nome, atual);
    }

    return Array.from(mapa.entries())
      .map(([nome, info]) => ({
        nome,
        valor: info.valor,
        quantidade: info.quantidade,
        percentual: (info.valor / total) * 100,
      }))
      .sort((a, b) => b.valor - a.valor);
  }

  formatarMoeda(valor: number): string {
    const moeda = this.moedaService.obterMoedaAtiva();
    const locale = this.moedaService.obterLocaleMoeda(moeda);
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: moeda,
    }).format(valor || 0);
  }

  exportarCSV(): void {
    const linhas: string[] = [];
    linhas.push(`Relatório Financeiro - Na Ponta do Lápis`);
    linhas.push(`Período;${this.descricaoPeriodoAtual}`);
    linhas.push(`Gerado em;${new Date().toLocaleString('pt-BR')}`);
    linhas.push('');

    if (this.incluirResumo) {
      linhas.push('RESUMO GERAL');
      linhas.push(`Saldo Atual Total;${this.formatarMoeda(this.saldoAtual)}`);
      linhas.push(`Total de Receitas no Período;${this.formatarMoeda(this.totalReceitas)}`);
      linhas.push(`Total de Despesas no Período;${this.formatarMoeda(this.totalDespesas)}`);
      linhas.push(`Balanço do Período;${this.formatarMoeda(this.balancoPeriodo)}`);
      linhas.push('');
    }

    if (this.incluirCategorias) {
      linhas.push('GASTOS POR CATEGORIA');
      linhas.push('Categoria;Quantidade;Valor;Percentual');
      for (const cat of this.resumoPorCategoria) {
        linhas.push(`${this.escaparCSV(cat.nome)};${cat.quantidade};${this.formatarMoeda(cat.valor)};${cat.percentual.toFixed(1)}%`);
      }
      linhas.push('');
    }

    if (this.incluirMetas && this.metas?.length) {
      linhas.push('METAS FINANCEIRAS');
      linhas.push('Meta;Tipo;Status;Valor Atual;Valor Objetivo;Progresso');
      for (const m of this.metas) {
        const pct = m.valorTotal > 0 ? Math.min(100, (m.valorAtual / m.valorTotal) * 100) : 0;
        linhas.push(
          `${this.escaparCSV(m.nome)};${m.tipoMeta};${m.status};${this.formatarMoeda(m.valorAtual)};${this.formatarMoeda(m.valorTotal)};${pct.toFixed(1)}%`
        );
      }
      linhas.push('');
    }

    if (this.incluirTransacoes) {
      linhas.push('MOVIMENTAÇÕES DO PERÍODO');
      linhas.push('Data;Descrição;Categoria;Conta;Tipo;Estado;Valor');
      for (const t of this.transacoesFiltradas) {
        const dataFmt = t.dataHora ? new Date(t.dataHora).toLocaleDateString('pt-BR') : '';
        const sinal = t.tipo === 'RECEITA' ? '+' : '-';
        linhas.push(
          [
            dataFmt,
            this.escaparCSV(t.descricao || 'Sem descrição'),
            this.escaparCSV(t.categoria?.nome || 'Sem categoria'),
            this.escaparCSV(t.conta?.nome || '-'),
            t.tipo || '-',
            t.estado || '-',
            `${sinal} ${this.formatarMoeda(t.valor || 0)}`,
          ].join(';')
        );
      }
    }

    const conteudo = '\uFEFF' + linhas.join('\r\n');
    const blob = new Blob([conteudo], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `relatorio-npl-${this.paraIsoDate(new Date())}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  exportarPDF(): void {
    const janela = window.open('', '_blank', 'width=960,height=800');
    if (!janela) return;

    const resumoHtml = this.incluirResumo
      ? `
        <section class="section">
          <h2>Resumo Geral</h2>
          <div class="kpi-grid">
            <div class="kpi-card">
              <span class="kpi-label">Saldo Atual nas Contas</span>
              <strong class="kpi-value">${this.escaparHtml(this.formatarMoeda(this.saldoAtual))}</strong>
            </div>
            <div class="kpi-card">
              <span class="kpi-label">Receitas no Período</span>
              <strong class="kpi-value text-green">+ ${this.escaparHtml(this.formatarMoeda(this.totalReceitas))}</strong>
            </div>
            <div class="kpi-card">
              <span class="kpi-label">Despesas no Período</span>
              <strong class="kpi-value text-red">- ${this.escaparHtml(this.formatarMoeda(this.totalDespesas))}</strong>
            </div>
            <div class="kpi-card">
              <span class="kpi-label">Balanço do Período</span>
              <strong class="kpi-value ${this.balancoPeriodo >= 0 ? 'text-green' : 'text-red'}">
                ${this.escaparHtml(this.formatarMoeda(this.balancoPeriodo))}
              </strong>
            </div>
          </div>
        </section>
      `
      : '';

    const categoriasHtml = this.incluirCategorias
      ? `
        <section class="section">
          <h2>Gastos por Categoria</h2>
          ${
            this.resumoPorCategoria.length === 0
              ? '<p class="empty">Nenhuma despesa registrada no período selecionado.</p>'
              : `
                <table>
                  <thead>
                    <tr>
                      <th>Categoria</th>
                      <th class="right">Lançamentos</th>
                      <th class="right">Valor</th>
                      <th class="right">% do Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${this.resumoPorCategoria
                      .map(
                        (c) => `
                        <tr>
                          <td><strong>${this.escaparHtml(c.nome)}</strong></td>
                          <td class="right">${c.quantidade}</td>
                          <td class="right">${this.escaparHtml(this.formatarMoeda(c.valor))}</td>
                          <td class="right">${c.percentual.toFixed(1)}%</td>
                        </tr>
                      `
                      )
                      .join('')}
                  </tbody>
                </table>
              `
          }
        </section>
      `
      : '';

    const metasHtml = this.incluirMetas
      ? `
        <section class="section">
          <h2>Metas Financeiras</h2>
          ${
            !this.metas?.length
              ? '<p class="empty">Nenhuma meta cadastrada.</p>'
              : `
                <table>
                  <thead>
                    <tr>
                      <th>Meta</th>
                      <th>Status</th>
                      <th class="right">Acumulado</th>
                      <th class="right">Objetivo</th>
                      <th class="right">Progresso</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${this.metas
                      .map((m) => {
                        const pct = m.valorTotal > 0 ? Math.min(100, (m.valorAtual / m.valorTotal) * 100) : 0;
                        return `
                          <tr>
                            <td><strong>${this.escaparHtml(m.nome)}</strong></td>
                            <td>${this.escaparHtml(m.status)}</td>
                            <td class="right">${this.escaparHtml(this.formatarMoeda(m.valorAtual))}</td>
                            <td class="right">${this.escaparHtml(this.formatarMoeda(m.valorTotal))}</td>
                            <td class="right">${pct.toFixed(1)}%</td>
                          </tr>
                        `;
                      })
                      .join('')}
                  </tbody>
                </table>
              `
          }
        </section>
      `
      : '';

    const transacoesHtml = this.incluirTransacoes
      ? `
        <section class="section">
          <h2>Movimentações do Período (${this.transacoesFiltradas.length})</h2>
          ${
            this.transacoesFiltradas.length === 0
              ? '<p class="empty">Nenhuma movimentação encontrada neste período.</p>'
              : `
                <table>
                  <thead>
                    <tr>
                      <th>Data</th>
                      <th>Descrição</th>
                      <th>Categoria</th>
                      <th>Conta</th>
                      <th class="right">Valor</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${this.transacoesFiltradas
                      .map((t) => {
                        const dataFmt = t.dataHora ? new Date(t.dataHora).toLocaleDateString('pt-BR') : '-';
                        const ehReceita = t.tipo === 'RECEITA';
                        return `
                          <tr>
                            <td>${dataFmt}</td>
                            <td><strong>${this.escaparHtml(t.descricao || 'Sem descrição')}</strong></td>
                            <td>${this.escaparHtml(t.categoria?.nome || 'Sem categoria')}</td>
                            <td>${this.escaparHtml(t.conta?.nome || '-')}</td>
                            <td class="right ${ehReceita ? 'text-green' : 'text-red'}">
                              ${ehReceita ? '+' : '-'} ${this.escaparHtml(this.formatarMoeda(t.valor || 0))}
                            </td>
                          </tr>
                        `;
                      })
                      .join('')}
                  </tbody>
                </table>
              `
          }
        </section>
      `
      : '';

    const html = `
      <!DOCTYPE html>
      <html lang="pt-BR">
      <head>
        <meta charset="UTF-8" />
        <title>Relatório Financeiro - Na Ponta do Lápis</title>
        <style>
          * { box-sizing: border-box; font-family: 'Segoe UI', Roboto, Arial, sans-serif; }
          body { margin: 0; padding: 32px; color: #1e293b; background: #ffffff; }
          .header {
            display: flex; align-items: center; justify-content: space-between;
            padding: 20px 24px; border-radius: 16px;
            background: linear-gradient(135deg, #4A2585 0%, #7843CD 100%);
            color: #ffffff; margin-bottom: 24px;
          }
          .header h1 { margin: 0; font-size: 20px; font-weight: 800; }
          .header p { margin: 4px 0 0; font-size: 12px; opacity: 0.9; }
          .badge {
            background: #EEC046; color: #4A2585; font-weight: 700;
            padding: 6px 12px; border-radius: 999px; font-size: 12px;
          }
          .section { margin-bottom: 24px; page-break-inside: avoid; }
          .section h2 {
            font-size: 15px; font-weight: 700; color: #4A2585;
            border-bottom: 2px solid #F3E8FF; padding-bottom: 6px; margin: 0 0 12px;
          }
          .kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
          .kpi-card {
            border: 1px solid #e2e8f0; border-radius: 12px; padding: 12px 14px; background: #f8fafc;
          }
          .kpi-label { display: block; font-size: 11px; color: #64748b; margin-bottom: 4px; }
          .kpi-value { font-size: 15px; font-weight: 800; color: #0f172a; }
          table { width: 100%; border-collapse: collapse; font-size: 12px; }
          th, td { padding: 8px 10px; border-bottom: 1px solid #f1f5f9; text-align: left; }
          th { background: #f8fafc; color: #475569; font-weight: 700; font-size: 11px; text-transform: uppercase; }
          .right { text-align: right; }
          .text-green { color: #059669; font-weight: 700; }
          .text-red { color: #e11d48; font-weight: 700; }
          .empty { font-size: 12px; color: #94a3b8; font-style: italic; }
          @media print {
            body { padding: 12px; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <div>
            <h1>Na Ponta do Lápis — Relatório Financeiro</h1>
            <p>Gerado em ${new Date().toLocaleString('pt-BR')}</p>
          </div>
          <span class="badge">${this.escaparHtml(this.descricaoPeriodoAtual)}</span>
        </div>
        ${resumoHtml}
        ${categoriasHtml}
        ${metasHtml}
        ${transacoesHtml}
        <script>
          window.onload = function() { setTimeout(function() { window.print(); }, 250); };
        </script>
      </body>
      </html>
    `;

    janela.document.open();
    janela.document.write(html);
    janela.document.close();
  }

  private paraIsoDate(data: Date): string {
    return data.toISOString().split('T')[0];
  }

  private escaparCSV(texto: string): string {
    const limpo = (texto || '').replace(/"/g, '""');
    return limpo.includes(';') || limpo.includes('"') ? `"${limpo}"` : limpo;
  }

  private escaparHtml(texto: string): string {
    return (texto || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }
}

