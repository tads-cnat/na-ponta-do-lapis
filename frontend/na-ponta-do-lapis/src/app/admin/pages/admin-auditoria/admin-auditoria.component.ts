import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApiService, AdminAuditLog, AdminPageResponse, extrairMensagemErroAdmin } from '../../services/admin-api.service';

@Component({
  selector: 'app-admin-auditoria',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex flex-col gap-5">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 m-0">Auditoria de Ações Admin</h1>
        <p class="text-sm text-slate-500 mt-1 mb-0">Registro imutável de todas as alterações feitas pelo painel de administração.</p>
      </div>

      <div class="bg-white rounded-2xl border border-slate-200 p-4 grid grid-cols-1 md:grid-cols-4 gap-3">
        <input
          type="text"
          [(ngModel)]="filtroAdmin"
          (keyup.enter)="carregar(0)"
          placeholder="Filtrar por e-mail do admin..."
          class="md:col-span-2 px-3.5 py-2 rounded-xl border border-slate-200 text-sm" />

        <select [(ngModel)]="filtroAcao" (change)="carregar(0)" class="px-3 py-2 rounded-xl border border-slate-200 text-sm bg-white">
          <option value="">Todas as ações</option>
          <option value="SUSPENDER_USUARIO">SUSPENDER_USUARIO</option>
          <option value="REATIVAR_USUARIO">REATIVAR_USUARIO</option>
          <option value="VERIFICAR_EMAIL_USUARIO">VERIFICAR_EMAIL_USUARIO</option>
          <option value="ALTERAR_PAPEL_USUARIO">ALTERAR_PAPEL_USUARIO</option>
          <option value="RESETAR_ONBOARDING">RESETAR_ONBOARDING</option>
          <option value="EXCLUIR_USUARIO">EXCLUIR_USUARIO</option>
          <option value="CRIAR_CATEGORIA">CRIAR_CATEGORIA</option>
          <option value="EDITAR_CATEGORIA">EDITAR_CATEGORIA</option>
          <option value="EXCLUIR_CATEGORIA">EXCLUIR_CATEGORIA</option>
          <option value="CRIAR_CONQUISTA">CRIAR_CONQUISTA</option>
          <option value="EDITAR_CONQUISTA">EDITAR_CONQUISTA</option>
          <option value="ENVIAR_NOTIFICACAO_GLOBAL">ENVIAR_NOTIFICACAO_GLOBAL</option>
          <option value="ENVIAR_NOTIFICACAO_INDIVIDUAL">ENVIAR_NOTIFICACAO_INDIVIDUAL</option>
        </select>

        <div class="flex gap-2">
          <button type="button" (click)="carregar(0)" class="flex-1 px-4 py-2 rounded-xl bg-[#7843CD] hover:bg-[#6635b5] text-white text-sm font-semibold cursor-pointer">
            Filtrar
          </button>
          @if (filtroAcao || filtroAdmin.trim()) {
            <button
              type="button"
              (click)="limparFiltros()"
              title="Limpar filtros"
              class="px-3 py-2 rounded-xl border border-slate-200 hover:bg-slate-100 text-slate-600 text-xs font-semibold cursor-pointer">
              <i class="pi pi-filter-slash"></i>
            </button>
          }
        </div>
      </div>

      @if (erro()) {
        <div class="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-sm">
          {{ erro() }}
        </div>
      }

      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse text-xs">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 uppercase text-slate-500">
                <th class="py-3 px-4">Data</th>
                <th class="py-3 px-4">Admin</th>
                <th class="py-3 px-4">Ação</th>
                <th class="py-3 px-4">Alvo</th>
                <th class="py-3 px-4">Motivo / Detalhes</th>
                <th class="py-3 px-4">IP</th>
              </tr>
            </thead>
            <tbody>
              @for (l of pagina()?.content ?? []; track l.id) {
                <tr class="border-b border-slate-100 hover:bg-slate-50/70">
                  <td class="py-3 px-4 whitespace-nowrap text-slate-500">{{ l.criadoEm | date:'dd/MM/yyyy HH:mm:ss' }}</td>
                  <td class="py-3 px-4 font-medium text-slate-800">{{ l.adminEmail }}</td>
                  <td class="py-3 px-4">
                    <span class="px-2.5 py-0.5 rounded-full text-[11px] font-semibold whitespace-nowrap" [ngClass]="classeAcao(l.acao)">
                      {{ rotuloAcao(l.acao) }}
                    </span>
                  </td>
                  <td class="py-3 px-4">
                    <div class="font-semibold text-slate-800">{{ l.alvoDescricao ?? l.alvoId ?? '—' }}</div>
                    <div class="text-[10px] text-slate-400">{{ l.entidadeAlvo }} #{{ l.alvoId }}</div>
                  </td>
                  <td class="py-3 px-4 max-w-xs">
                    @if (l.motivo) {
                      <div class="text-slate-800 font-medium">{{ l.motivo }}</div>
                    }
                    @if (formatarDetalhes(l); as resumoDetalhe) {
                      <div class="text-[11px] text-slate-500 mt-0.5">{{ resumoDetalhe }}</div>
                    }
                  </td>
                  <td class="py-3 px-4 font-mono text-slate-400">{{ l.ipOrigem ?? '—' }}</td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="6" class="py-10 text-center text-slate-400">
                    {{ carregando() ? 'Carregando logs...' : 'Nenhum registro de auditoria encontrado.' }}
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        @if (pagina(); as p) {
          <div class="px-4 py-3 bg-slate-50 border-t border-slate-200 flex items-center justify-between text-xs text-slate-600">
            <span>Total: <strong>{{ p.totalElements }}</strong> registro(s)</span>
            <div class="flex gap-2">
              <button type="button" [disabled]="p.page <= 0" (click)="carregar(p.page - 1)" class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white disabled:opacity-40 cursor-pointer">Anterior</button>
              <button type="button" [disabled]="p.page + 1 >= p.totalPages" (click)="carregar(p.page + 1)" class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white disabled:opacity-40 cursor-pointer">Próxima</button>
            </div>
          </div>
        }
      </div>
    </div>
  `,
})
export class AdminAuditoriaComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);

  filtroAcao = '';
  filtroAdmin = '';

  readonly pagina = signal<AdminPageResponse<AdminAuditLog> | null>(null);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar(0);
  }

  limparFiltros(): void {
    this.filtroAcao = '';
    this.filtroAdmin = '';
    this.carregar(0);
  }

  carregar(page: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.adminApi
      .listarAuditoria({
        acao: this.filtroAcao || undefined,
        adminEmail: this.filtroAdmin || undefined,
        page,
        size: 25,
      })
      .subscribe({
        next: (res) => {
          this.pagina.set(res);
          this.carregando.set(false);
        },
        error: (err) => {
          this.erro.set(extrairMensagemErroAdmin(err, 'Erro ao carregar registros de auditoria.'));
          this.carregando.set(false);
        },
      });
  }

  rotuloAcao(acao: string): string {
    const mapa: Record<string, string> = {
      SUSPENDER_USUARIO: 'Suspensão de conta',
      REATIVAR_USUARIO: 'Reativação de conta',
      VERIFICAR_EMAIL_USUARIO: 'Verificação de e-mail',
      ALTERAR_PAPEL_USUARIO: 'Alteração de papel',
      RESETAR_ONBOARDING: 'Reset de onboarding',
      EXCLUIR_USUARIO: 'Exclusão de usuário',
      CRIAR_CATEGORIA: 'Nova categoria',
      EDITAR_CATEGORIA: 'Edição de categoria',
      EXCLUIR_CATEGORIA: 'Exclusão de categoria',
      CRIAR_CONQUISTA: 'Nova conquista',
      EDITAR_CONQUISTA: 'Edição de conquista',
      ENVIAR_NOTIFICACAO_GLOBAL: 'Aviso global',
      ENVIAR_NOTIFICACAO_INDIVIDUAL: 'Aviso individual',
    };
    return mapa[acao] ?? acao;
  }

  classeAcao(acao: string): string {
    if (acao.includes('EXCLUIR') || acao.includes('SUSPENDER')) {
      return 'bg-rose-50 text-rose-700 border border-rose-200';
    }
    if (acao.includes('REATIVAR') || acao.includes('CRIAR') || acao.includes('VERIFICAR')) {
      return 'bg-emerald-50 text-emerald-700 border border-emerald-200';
    }
    if (acao.includes('PAPEL')) {
      return 'bg-purple-50 text-[#7843CD] border border-purple-200';
    }
    if (acao.includes('NOTIFICACAO')) {
      return 'bg-sky-50 text-sky-700 border border-sky-200';
    }
    return 'bg-amber-50 text-amber-800 border border-amber-200';
  }

  formatarDetalhes(log: AdminAuditLog): string {
    if (!log.detalhesJson) return '';
    try {
      const obj = JSON.parse(log.detalhesJson) as Record<string, unknown>;
      if (obj['antes'] !== undefined && obj['depois'] !== undefined) {
        return `${obj['antes']} → ${obj['depois']}`;
      }
      const rotulos: Record<string, string> = {
        nome: 'Nome',
        titulo: 'Título',
        destinatarios: 'Destinatários',
        pontosXp: 'XP',
        categoria: 'Categoria',
        ativo: 'Status',
        emailVerificado: 'E-mail',
        onboardingConcluido: 'Onboarding',
      };
      const partes: string[] = [];
      for (const [k, v] of Object.entries(obj)) {
        if (k === 'email' && log.alvoDescricao && String(v).toLowerCase() === log.alvoDescricao.toLowerCase()) {
          continue;
        }
        if (k === 'ativo') {
          partes.push(v ? 'Conta reativada' : 'Conta suspensa');
          continue;
        }
        if (k === 'emailVerificado') {
          partes.push('E-mail verificado manualmente');
          continue;
        }
        if (k === 'onboardingConcluido') {
          partes.push('Onboarding resetado');
          continue;
        }
        const label = rotulos[k] ?? k;
        partes.push(`${label}: ${String(v)}`);
      }
      return partes.join(' • ');
    } catch {
      return '';
    }
  }
}
