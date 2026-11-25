import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  AdminApiService,
  AdminPageResponse,
  AdminUsuarioDetalhe,
  AdminUsuarioResumo,
  extrairMensagemErroAdmin,
} from '../../services/admin-api.service';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex flex-col gap-5">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 m-0">Gestão de Usuários</h1>
        <p class="text-sm text-slate-500 mt-1 mb-0">Consulte contas, gerencie acessos e execute ações administrativas com auditoria.</p>
      </div>

      <!-- Filtros -->
      <div class="bg-white rounded-2xl border border-slate-200 p-4 grid grid-cols-1 md:grid-cols-6 gap-3">
        <input
          type="text"
          [(ngModel)]="filtroQ"
          (keyup.enter)="buscar(0)"
          placeholder="Buscar por nome, e-mail ou username..."
          class="md:col-span-2 px-3.5 py-2 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-[#7843CD]" />

        <select [(ngModel)]="filtroPapel" (change)="buscar(0)" class="px-3 py-2 rounded-xl border border-slate-200 text-sm bg-white">
          <option value="">Todos os papéis</option>
          <option value="USUARIO">USUARIO</option>
          <option value="ADMIN_FAMILIA">ADMIN_FAMILIA</option>
          <option value="ADMIN_SITE">ADMIN_SITE</option>
        </select>

        <select [(ngModel)]="filtroAtivo" (change)="buscar(0)" class="px-3 py-2 rounded-xl border border-slate-200 text-sm bg-white">
          <option value="">Status: Todos</option>
          <option value="true">Ativos</option>
          <option value="false">Suspensos</option>
        </select>

        <select [(ngModel)]="filtroEmail" (change)="buscar(0)" class="px-3 py-2 rounded-xl border border-slate-200 text-sm bg-white">
          <option value="">E-mail: Todos</option>
          <option value="true">Verificado</option>
          <option value="false">Não verificado</option>
        </select>

        <div class="flex gap-2">
          <button
            type="button"
            (click)="buscar(0)"
            class="flex-1 px-4 py-2 rounded-xl bg-[#7843CD] hover:bg-[#6635b5] text-white text-sm font-semibold cursor-pointer">
            Filtrar
          </button>
          @if (temFiltrosAtivos()) {
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

      @if (feedback()) {
        <div
          class="p-3.5 rounded-xl text-sm border flex items-center justify-between gap-3"
          [class.bg-emerald-50]="feedback()!.tipo === 'ok'"
          [class.border-emerald-200]="feedback()!.tipo === 'ok'"
          [class.text-emerald-800]="feedback()!.tipo === 'ok'"
          [class.bg-rose-50]="feedback()!.tipo === 'erro'"
          [class.border-rose-200]="feedback()!.tipo === 'erro'"
          [class.text-rose-800]="feedback()!.tipo === 'erro'">
          <span>{{ feedback()!.texto }}</span>
          <button type="button" (click)="feedback.set(null)" class="opacity-60 hover:opacity-100 cursor-pointer" aria-label="Fechar aviso">
            <i class="pi pi-times text-xs"></i>
          </button>
        </div>
      }

      <!-- Tabela -->
      <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-xs">
        <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse text-sm">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500">
                <th class="py-3 px-4">Usuário</th>
                <th class="py-3 px-4">Papel</th>
                <th class="py-3 px-4">Status</th>
                <th class="py-3 px-4">E-mail / Tour</th>
                <th class="py-3 px-4">Família</th>
                <th class="py-3 px-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              @for (u of pagina()?.content ?? []; track u.id) {
                <tr class="border-b border-slate-100 hover:bg-slate-50/70">
                  <td class="py-3 px-4">
                    <div class="font-semibold text-slate-900">{{ u.nome }}</div>
                    <div class="text-xs text-slate-500">{{ u.email }} • &#64;{{ u.username }}</div>
                  </td>
                  <td class="py-3 px-4">
                    <span
                      class="px-2 py-0.5 rounded-full text-[11px] font-bold"
                      [class.bg-purple-100]="u.papel === 'ADMIN_SITE'"
                      [class.text-[#7843CD]]="u.papel === 'ADMIN_SITE'"
                      [class.bg-sky-100]="u.papel === 'ADMIN_FAMILIA'"
                      [class.text-sky-700]="u.papel === 'ADMIN_FAMILIA'"
                      [class.bg-slate-100]="u.papel === 'USUARIO'"
                      [class.text-slate-600]="u.papel === 'USUARIO'">
                      {{ u.papel }}
                    </span>
                  </td>
                  <td class="py-3 px-4">
                    @if (u.ativo) {
                      <span class="inline-flex items-center gap-1 text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full">
                        <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Ativo
                      </span>
                    } @else {
                      <span class="inline-flex items-center gap-1 text-xs font-semibold text-rose-700 bg-rose-50 px-2 py-0.5 rounded-full">
                        <span class="w-1.5 h-1.5 rounded-full bg-rose-500"></span> Suspenso
                      </span>
                    }
                  </td>
                  <td class="py-3 px-4 text-xs">
                    <div [class.text-emerald-600]="u.emailVerificado" [class.text-amber-600]="!u.emailVerificado">
                      {{ u.emailVerificado ? 'E-mail verificado' : 'Pendente verificação' }}
                    </div>
                    <div class="text-slate-400">Tour: {{ u.onboardingConcluido ? 'Concluído' : 'Pendente' }}</div>
                  </td>
                  <td class="py-3 px-4 text-xs text-slate-600">{{ u.familiaNome ?? '—' }}</td>
                  <td class="py-3 px-4 text-right">
                    <button
                      type="button"
                      (click)="abrirDetalhes(u.id)"
                      class="px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-100 text-xs font-semibold text-slate-700 cursor-pointer">
                      Gerenciar
                    </button>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="6" class="py-10 text-center text-slate-400">
                    {{ carregando() ? 'Carregando usuários...' : 'Nenhum usuário encontrado com os filtros atuais.' }}
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        @if (pagina(); as p) {
          <div class="px-4 py-3 bg-slate-50 border-t border-slate-200 flex items-center justify-between text-xs text-slate-600">
            <span>Total: <strong>{{ p.totalElements }}</strong> usuário(s) • Página {{ p.page + 1 }} de {{ p.totalPages || 1 }}</span>
            <div class="flex gap-2">
              <button
                type="button"
                [disabled]="p.page <= 0"
                (click)="buscar(p.page - 1)"
                class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white disabled:opacity-40 cursor-pointer">
                Anterior
              </button>
              <button
                type="button"
                [disabled]="p.page + 1 >= p.totalPages"
                (click)="buscar(p.page + 1)"
                class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white disabled:opacity-40 cursor-pointer">
                Próxima
              </button>
            </div>
          </div>
        }
      </div>
    </div>

    <!-- Modal Detalhe e Ações do Usuário -->
    @if (selecionado(); as u) {
      <div class="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4" (click)="fecharModal()">
        <div class="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl p-6 flex flex-col gap-5" (click)="$event.stopPropagation()">
          <div class="flex items-start justify-between gap-4 border-b border-slate-100 pb-4">
            <div>
              <h2 class="text-lg font-bold text-slate-900 m-0">{{ u.nome }}</h2>
              <p class="text-xs text-slate-500 m-0">{{ u.email }} • &#64;{{ u.username }} • ID #{{ u.id }}</p>
            </div>
            <button type="button" (click)="fecharModal()" class="text-slate-400 hover:text-slate-700 cursor-pointer">
              <i class="pi pi-times"></i>
            </button>
          </div>

          <!-- Metadados LGPD -->
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 text-center">
            <div class="p-3 rounded-xl bg-slate-50 border border-slate-100">
              <div class="text-xl font-bold text-slate-900">{{ u.totalContas }}</div>
              <div class="text-[11px] text-slate-500">Contas</div>
            </div>
            <div class="p-3 rounded-xl bg-slate-50 border border-slate-100">
              <div class="text-xl font-bold text-slate-900">{{ u.totalTransacoes }}</div>
              <div class="text-[11px] text-slate-500">Transações</div>
            </div>
            <div class="p-3 rounded-xl bg-slate-50 border border-slate-100">
              <div class="text-xl font-bold text-slate-900">{{ u.totalMetas }}</div>
              <div class="text-[11px] text-slate-500">Metas</div>
            </div>
            <div class="p-3 rounded-xl bg-slate-50 border border-slate-100">
              <div class="text-xl font-bold text-slate-900">{{ u.totalConquistas }}</div>
              <div class="text-[11px] text-slate-500">Conquistas</div>
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs text-slate-600 bg-slate-50/70 p-3.5 rounded-xl">
            <div><strong>Papel:</strong> {{ u.papel }}</div>
            <div><strong>Status:</strong> {{ u.ativo ? 'Ativo' : 'Suspenso' }}</div>
            <div><strong>E-mail verificado:</strong> {{ u.emailVerificado ? 'Sim' : 'Não' }}</div>
            <div><strong>Onboarding:</strong> {{ u.onboardingConcluido ? 'Concluído' : 'Pendente' }}</div>
            <div><strong>IA configurada:</strong> {{ u.possuiChaveIa ? 'Sim' : 'Não' }}</div>
            <div><strong>Open Finance:</strong> {{ u.possuiOpenFinance ? 'Sim' : 'Não' }}</div>
            <div><strong>Família:</strong> {{ u.familiaNome ?? 'Nenhuma' }}</div>
            <div><strong>Termos:</strong> {{ u.versaoTermos ?? '—' }}</div>
            <div><strong>Cadastro:</strong> {{ u.criadoEm ? (u.criadoEm | date:'dd/MM/yyyy HH:mm') : '—' }}</div>
            <div><strong>Último acesso:</strong> {{ u.ultimoAcessoEm ? (u.ultimoAcessoEm | date:'dd/MM/yyyy HH:mm') : '—' }}</div>
          </div>

          @if (!u.ativo && u.motivoSuspensao) {
            <div class="p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-800">
              <strong>Motivo da suspensão:</strong> {{ u.motivoSuspensao }}
            </div>
          }

          <!-- Ações Rápidas -->
          <div class="flex flex-wrap gap-2">
            @if (!u.emailVerificado) {
              <button type="button" (click)="verificarEmail(u)" class="px-3 py-2 rounded-xl bg-sky-50 hover:bg-sky-100 text-sky-700 text-xs font-semibold cursor-pointer">
                Forçar verificação de e-mail
              </button>
            }
            <button type="button" (click)="resetarOnboarding(u)" class="px-3 py-2 rounded-xl bg-amber-50 hover:bg-amber-100 text-amber-800 text-xs font-semibold cursor-pointer">
              Resetar onboarding
            </button>
            <button type="button" (click)="irParaNotificacao(u)" class="px-3 py-2 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-semibold cursor-pointer">
              <i class="pi pi-bell mr-1 text-[11px]"></i>Enviar notificação
            </button>
            <button type="button" (click)="acaoConfirmacao.set('papel')" class="px-3 py-2 rounded-xl bg-purple-50 hover:bg-purple-100 text-[#7843CD] text-xs font-semibold cursor-pointer">
              {{ u.papel === 'ADMIN_SITE' ? 'Rebaixar para USUARIO' : 'Promover para ADMIN_SITE' }}
            </button>
            <button
              type="button"
              (click)="acaoConfirmacao.set('suspensao')"
              class="px-3 py-2 rounded-xl text-xs font-semibold cursor-pointer"
              [class.bg-rose-50]="u.ativo"
              [class.text-rose-700]="u.ativo"
              [class.bg-emerald-50]="!u.ativo"
              [class.text-emerald-700]="!u.ativo">
              {{ u.ativo ? 'Suspender conta' : 'Reativar conta' }}
            </button>
            <button type="button" (click)="acaoConfirmacao.set('excluir')" class="px-3 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold cursor-pointer ml-auto">
              Excluir conta
            </button>
          </div>

          <!-- Painel de Confirmação Dupla -->
          @if (acaoConfirmacao()) {
            <div class="p-4 rounded-xl border-2 border-rose-200 bg-rose-50/40 flex flex-col gap-3">
              <div class="text-xs font-bold text-rose-800 uppercase">
                @switch (acaoConfirmacao()) {
                  @case ('suspensao') { Confirmar {{ u.ativo ? 'suspensão' : 'reativação' }} da conta }
                  @case ('papel') { Confirmar alteração de papel para {{ u.papel === 'ADMIN_SITE' ? 'USUARIO' : 'ADMIN_SITE' }} }
                  @case ('excluir') { Exclusão definitiva — ação irreversível }
                }
              </div>

              @if (acaoConfirmacao() === 'excluir') {
                <input
                  type="text"
                  [(ngModel)]="confirmacaoEmail"
                  [placeholder]="'Digite exatamente: ' + u.email"
                  class="px-3 py-2 rounded-lg border border-rose-300 bg-white text-xs" />
              }

              <input
                type="text"
                [(ngModel)]="motivoAcao"
                placeholder="Motivo (registrado na auditoria)..."
                class="px-3 py-2 rounded-lg border border-slate-300 bg-white text-xs" />

              <div class="flex justify-end gap-2">
                <button type="button" (click)="cancelarConfirmacao()" class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white text-xs cursor-pointer">
                  Cancelar
                </button>
                <button
                  type="button"
                  (click)="executarAcaoConfirmada(u)"
                  class="px-4 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold cursor-pointer">
                  Confirmar ação
                </button>
              </div>
            </div>
          }
        </div>
      </div>
    }
  `,
})
export class AdminUsuariosComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  filtroQ = '';
  filtroPapel = '';
  filtroAtivo = '';
  filtroEmail = '';

  readonly pagina = signal<AdminPageResponse<AdminUsuarioResumo> | null>(null);
  readonly carregando = signal(false);
  readonly selecionado = signal<AdminUsuarioDetalhe | null>(null);
  readonly feedback = signal<{ tipo: 'ok' | 'erro'; texto: string } | null>(null);

  readonly acaoConfirmacao = signal<'suspensao' | 'papel' | 'excluir' | null>(null);
  motivoAcao = '';
  confirmacaoEmail = '';

  ngOnInit(): void {
    const qp = this.route.snapshot.queryParamMap;
    this.filtroQ = qp.get('q') ?? '';
    this.filtroPapel = qp.get('papel') ?? '';
    this.filtroAtivo = qp.get('ativo') ?? '';
    this.filtroEmail = qp.get('emailVerificado') ?? '';
    this.buscar(0);
  }

  temFiltrosAtivos(): boolean {
    return !!(this.filtroQ.trim() || this.filtroPapel || this.filtroAtivo || this.filtroEmail);
  }

  limparFiltros(): void {
    this.filtroQ = '';
    this.filtroPapel = '';
    this.filtroAtivo = '';
    this.filtroEmail = '';
    this.router.navigate([], { relativeTo: this.route, queryParams: {} });
    this.buscar(0);
  }

  irParaNotificacao(u: AdminUsuarioDetalhe): void {
    this.fecharModal();
    this.router.navigate(['/admin/comunicacao'], {
      queryParams: { usuarioId: u.id, nome: u.nome, email: u.email },
    });
  }

  buscar(page: number): void {
    this.carregando.set(true);
    this.adminApi
      .listarUsuarios({
        q: this.filtroQ,
        papel: this.filtroPapel || undefined,
        ativo: this.filtroAtivo || undefined,
        emailVerificado: this.filtroEmail || undefined,
        page,
        size: 20,
      })
      .subscribe({
        next: (res) => {
          this.pagina.set(res);
          this.carregando.set(false);
        },
        error: (err) => {
          this.mostrarFeedback('erro', extrairMensagemErroAdmin(err, 'Erro ao carregar usuários.'));
          this.carregando.set(false);
        },
      });
  }

  abrirDetalhes(id: number): void {
    this.cancelarConfirmacao();
    this.adminApi.obterUsuario(id).subscribe({
      next: (u) => this.selecionado.set(u),
      error: (err) => this.mostrarFeedback('erro', extrairMensagemErroAdmin(err, 'Erro ao carregar detalhes.')),
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.selecionado()) {
      this.fecharModal();
    }
  }

  fecharModal(): void {
    this.selecionado.set(null);
    this.cancelarConfirmacao();
  }

  cancelarConfirmacao(): void {
    this.acaoConfirmacao.set(null);
    this.motivoAcao = '';
    this.confirmacaoEmail = '';
  }

  verificarEmail(u: AdminUsuarioDetalhe): void {
    this.adminApi.verificarEmail(u.id).subscribe({
      next: (atualizado) => {
        this.selecionado.set(atualizado);
        this.mostrarFeedback('ok', `E-mail de ${u.email} verificado manualmente.`);
        this.buscar(this.pagina()?.page ?? 0);
      },
      error: (err) => this.mostrarFeedback('erro', extrairMensagemErroAdmin(err, 'Erro ao verificar e-mail.')),
    });
  }

  resetarOnboarding(u: AdminUsuarioDetalhe): void {
    this.adminApi.resetarOnboarding(u.id).subscribe({
      next: (atualizado) => {
        this.selecionado.set(atualizado);
        this.mostrarFeedback('ok', `Onboarding de ${u.email} resetado.`);
        this.buscar(this.pagina()?.page ?? 0);
      },
      error: (err) => this.mostrarFeedback('erro', extrairMensagemErroAdmin(err, 'Erro ao resetar onboarding.')),
    });
  }

  executarAcaoConfirmada(u: AdminUsuarioDetalhe): void {
    const acao = this.acaoConfirmacao();
    if (!acao) return;

    if (acao === 'suspensao') {
      const novoAtivo = !u.ativo;
      if (!novoAtivo && !this.motivoAcao.trim()) {
        this.mostrarFeedback('erro', 'Informe o motivo da suspensão.');
        return;
      }
      this.adminApi.alterarSuspensao(u.id, novoAtivo, this.motivoAcao.trim()).subscribe({
        next: (atualizado) => {
          this.selecionado.set(atualizado);
          this.cancelarConfirmacao();
          this.mostrarFeedback('ok', novoAtivo ? 'Conta reativada.' : 'Conta suspensa e sessão invalidada.');
          this.buscar(this.pagina()?.page ?? 0);
        },
        error: (err) => this.mostrarFeedback('erro', extrairMensagemErroAdmin(err, 'Falha ao alterar suspensão.')),
      });
    } else if (acao === 'papel') {
      const novoPapel = u.papel === 'ADMIN_SITE' ? 'USUARIO' : 'ADMIN_SITE';
      this.adminApi.alterarPapel(u.id, novoPapel, this.motivoAcao.trim()).subscribe({
        next: (atualizado) => {
          this.selecionado.set(atualizado);
          this.cancelarConfirmacao();
          this.mostrarFeedback('ok', `Papel alterado para ${novoPapel}.`);
          this.buscar(this.pagina()?.page ?? 0);
        },
        error: (err) => this.mostrarFeedback('erro', extrairMensagemErroAdmin(err, 'Falha ao alterar papel.')),
      });
    } else if (acao === 'excluir') {
      if (!this.confirmacaoEmail.trim() || !this.motivoAcao.trim()) {
        this.mostrarFeedback('erro', 'Preencha o e-mail de confirmação e o motivo.');
        return;
      }
      this.adminApi.excluirUsuario(u.id, this.confirmacaoEmail.trim(), this.motivoAcao.trim()).subscribe({
        next: () => {
          this.fecharModal();
          this.mostrarFeedback('ok', `Conta ${u.email} excluída definitivamente.`);
          this.buscar(this.pagina()?.page ?? 0);
        },
        error: (err) => this.mostrarFeedback('erro', extrairMensagemErroAdmin(err, 'Falha ao excluir conta.')),
      });
    }
  }

  private mostrarFeedback(tipo: 'ok' | 'erro', texto: string): void {
    this.feedback.set({ tipo, texto });
  }
}
