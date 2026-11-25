import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AdminApiService, AdminUsuarioResumo, extrairMensagemErroAdmin } from '../../services/admin-api.service';

@Component({
  selector: 'app-admin-comunicacao',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="flex flex-col gap-5 max-w-3xl">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 m-0">Comunicação & Avisos</h1>
        <p class="text-sm text-slate-500 mt-1 mb-0">Dispare notificações para o sininho de todos os usuários ativos ou para um usuário específico.</p>
      </div>

      @if (feedback()) {
        <div
          class="p-3.5 rounded-xl text-sm border"
          [class.bg-emerald-50]="feedback()!.tipo === 'ok'"
          [class.border-emerald-200]="feedback()!.tipo === 'ok'"
          [class.text-emerald-800]="feedback()!.tipo === 'ok'"
          [class.bg-rose-50]="feedback()!.tipo === 'erro'"
          [class.border-rose-200]="feedback()!.tipo === 'erro'"
          [class.text-rose-800]="feedback()!.tipo === 'erro'">
          {{ feedback()!.texto }}
        </div>
      }

      <div class="bg-white rounded-2xl border border-slate-200 p-6 flex flex-col gap-4 shadow-xs">
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
          <button
            type="button"
            (click)="modo = 'global'"
            class="py-2.5 px-3 rounded-xl border text-xs font-semibold cursor-pointer"
            [class.border-[#7843CD]]="modo === 'global'"
            [class.bg-purple-50]="modo === 'global'"
            [class.text-[#7843CD]]="modo === 'global'"
            [class.border-slate-200]="modo !== 'global'">
            <i class="pi pi-globe mr-1.5"></i> Todos os ativos
          </button>
          <button
            type="button"
            (click)="modo = 'churn'"
            class="py-2.5 px-3 rounded-xl border text-xs font-semibold cursor-pointer"
            [class.border-[#7843CD]]="modo === 'churn'"
            [class.bg-purple-50]="modo === 'churn'"
            [class.text-[#7843CD]]="modo === 'churn'"
            [class.border-slate-200]="modo !== 'churn'">
            <i class="pi pi-exclamation-triangle mr-1.5"></i> Em risco de churn (14+ dias)
          </button>
          <button
            type="button"
            (click)="modo = 'individual'"
            class="py-2.5 px-3 rounded-xl border text-xs font-semibold cursor-pointer"
            [class.border-[#7843CD]]="modo === 'individual'"
            [class.bg-purple-50]="modo === 'individual'"
            [class.text-[#7843CD]]="modo === 'individual'"
            [class.border-slate-200]="modo !== 'individual'">
            <i class="pi pi-user mr-1.5"></i> Usuário específico
          </button>
        </div>

        @if (modo === 'churn') {
          <div class="p-3 rounded-xl bg-purple-50/70 border border-purple-200 text-xs text-slate-700 flex flex-wrap items-center justify-between gap-2">
            <span>
              <strong>Público-alvo:</strong> apenas usuários já ativados que não acessam nem lançam transações/aportes há <strong>14 dias ou mais</strong>.
            </span>
            <a
              [routerLink]="['/admin/usuarios']"
              [queryParams]="{ riscoChurn: 'true' }"
              class="font-semibold text-[#7843CD] hover:underline whitespace-nowrap">
              Ver quem são &rarr;
            </a>
          </div>
        }

        <!-- Templates Rápidos -->
        <div class="flex flex-wrap items-center gap-2">
          <span class="text-[11px] font-semibold text-slate-400 uppercase tracking-wider mr-1">Templates:</span>
          @for (tpl of templates; track tpl.rotulo) {
            <button
              type="button"
              (click)="aplicarTemplate(tpl)"
              class="px-2.5 py-1 rounded-lg border border-slate-200 hover:border-[#7843CD]/50 hover:bg-purple-50/50 text-xs font-medium text-slate-700 cursor-pointer transition-colors">
              {{ tpl.rotulo }}
            </button>
          }
        </div>

        @if (modo === 'individual') {
          <div class="flex flex-col gap-2">
            <label class="block text-xs font-semibold text-slate-600">Destinatário</label>
            @if (destinatarioSelecionado(); as dest) {
              <div class="flex items-center justify-between gap-2 px-3.5 py-2.5 rounded-xl bg-purple-50 border border-purple-200 text-xs">
                <div>
                  <span class="font-bold text-slate-900">{{ dest.nome }}</span>
                  <span class="text-slate-600 ml-1.5">{{ dest.email }} (#{{ dest.id }})</span>
                </div>
                <button type="button" (click)="limparDestinatario()" class="text-slate-500 hover:text-rose-600 cursor-pointer" aria-label="Remover destinatário">
                  <i class="pi pi-times"></i>
                </button>
              </div>
            } @else {
              <div class="flex gap-2">
                <input
                  type="text"
                  [(ngModel)]="termoBuscaUsuario"
                  (input)="buscarUsuarios()"
                  placeholder="Digite nome, e-mail ou username para buscar..."
                  class="flex-1 px-3.5 py-2 rounded-xl border border-slate-200 text-sm" />
              </div>
              @if (sugestoesUsuarios().length > 0) {
                <div class="border border-slate-200 rounded-xl overflow-hidden divide-y divide-slate-100 bg-white shadow-xs">
                  @for (u of sugestoesUsuarios(); track u.id) {
                    <button
                      type="button"
                      (click)="selecionarDestinatario(u)"
                      class="w-full px-3.5 py-2.5 text-left hover:bg-slate-50 flex items-center justify-between text-xs cursor-pointer">
                      <div>
                        <div class="font-semibold text-slate-900">{{ u.nome }}</div>
                        <div class="text-slate-500">{{ u.email }} • &#64;{{ u.username }}</div>
                      </div>
                      <span class="text-[11px] text-slate-400">#{{ u.id }}</span>
                    </button>
                  }
                </div>
              }
            }
          </div>
        }

        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-1">Título (máx. 120 caracteres)</label>
          <input type="text" [(ngModel)]="titulo" maxlength="120" placeholder="Ex.: Novidade no Na Ponta do Lápis!" class="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-sm" />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-1">Mensagem (máx. 500 caracteres)</label>
          <textarea [(ngModel)]="mensagem" maxlength="500" rows="4" placeholder="Escreva o comunicado para os usuários..." class="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-sm"></textarea>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-600 mb-1">Abrir tela ao clicar (opcional)</label>
          <select [(ngModel)]="linkRedirecionamento" class="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-sm bg-white">
            <option value="">Nenhuma (apenas marcar como lida)</option>
            <option value="/app/dashboard">Dashboard (/app/dashboard)</option>
            <option value="/app/transacoes">Transações (/app/transacoes)</option>
            <option value="/app/contas">Contas e Cartões (/app/contas)</option>
            <option value="/app/metas">Metas (/app/metas)</option>
            <option value="/app/familia">Família (/app/familia)</option>
            <option value="/app/perfil">Meu Perfil (/app/perfil)</option>
          </select>
        </div>

        <!-- Preview -->
        @if (titulo || mensagem) {
          <div class="p-4 rounded-xl bg-slate-50 border border-slate-200">
            <div class="text-[11px] font-bold uppercase text-slate-400 mb-1.5">Pré-visualização no sininho</div>
            <div class="font-bold text-sm text-slate-900">{{ titulo || 'Sem título' }}</div>
            <div class="text-xs text-slate-600 mt-0.5 whitespace-pre-line">{{ mensagem }}</div>
          </div>
        }

        <div class="flex justify-end">
          <button
            type="button"
            [disabled]="enviando()"
            (click)="enviar()"
            class="px-5 py-2.5 rounded-xl bg-[#7843CD] hover:bg-[#6635b5] text-white text-sm font-semibold disabled:opacity-50 cursor-pointer">
            {{ enviando() ? 'Enviando...' : 'Disparar Notificação' }}
          </button>
        </div>
      </div>
    </div>
  `,
})
export class AdminComunicacaoComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);
  private readonly route = inject(ActivatedRoute);

  modo: 'global' | 'churn' | 'individual' = 'global';
  termoBuscaUsuario = '';
  titulo = '';
  mensagem = '';
  linkRedirecionamento = '';

  readonly destinatarioSelecionado = signal<{ id: number; nome: string; email: string } | null>(null);
  readonly sugestoesUsuarios = signal<AdminUsuarioResumo[]>([]);
  readonly enviando = signal(false);
  readonly feedback = signal<{ tipo: 'ok' | 'erro'; texto: string } | null>(null);

  readonly templates = [
    {
      rotulo: 'Reengajar inativos',
      titulo: 'Sentimos sua falta no Na Ponta do Lápis!',
      mensagem: 'Faz alguns dias que você não registra seus lançamentos. Importe sua fatura PDF ou atualize suas metas em 1 minuto!',
      link: '/app/transacoes',
    },
    {
      rotulo: 'Lembrete de fatura PDF',
      titulo: 'Sua fatura do cartão já fechou?',
      mensagem: 'Importe o PDF da sua fatura e deixe o Na Ponta do Lápis categorizar seus gastos automaticamente.',
      link: '/app/transacoes',
    },
    {
      rotulo: 'Novidade no app',
      titulo: 'Temos novidades para você!',
      mensagem: 'Confira as últimas melhorias no app para acompanhar suas contas, metas e finanças da família com mais facilidade.',
      link: '/app/dashboard',
    },
  ];

  private buscaTimer: ReturnType<typeof setTimeout> | null = null;

  aplicarTemplate(tpl: { titulo: string; mensagem: string; link: string }): void {
    this.titulo = tpl.titulo;
    this.mensagem = tpl.mensagem;
    this.linkRedirecionamento = tpl.link;
  }

  ngOnInit(): void {
    const qp = this.route.snapshot.queryParamMap;
    const segmento = qp.get('segmento');
    const uid = Number(qp.get('usuarioId'));
    if (uid > 0) {
      this.modo = 'individual';
      this.destinatarioSelecionado.set({
        id: uid,
        nome: qp.get('nome') ?? `Usuário #${uid}`,
        email: qp.get('email') ?? '',
      });
    } else if (segmento === 'RISCO_CHURN') {
      this.modo = 'churn';
    }
  }

  buscarUsuarios(): void {
    if (this.buscaTimer) clearTimeout(this.buscaTimer);
    const termo = this.termoBuscaUsuario.trim();
    if (termo.length < 2) {
      this.sugestoesUsuarios.set([]);
      return;
    }
    this.buscaTimer = setTimeout(() => {
      this.adminApi.listarUsuarios({ q: termo, page: 0, size: 6 }).subscribe({
        next: (res) => this.sugestoesUsuarios.set(res.content),
        error: () => this.sugestoesUsuarios.set([]),
      });
    }, 250);
  }

  selecionarDestinatario(u: AdminUsuarioResumo): void {
    this.destinatarioSelecionado.set({ id: u.id, nome: u.nome, email: u.email });
    this.sugestoesUsuarios.set([]);
    this.termoBuscaUsuario = '';
  }

  limparDestinatario(): void {
    this.destinatarioSelecionado.set(null);
  }

  enviar(): void {
    if (!this.titulo.trim() || !this.mensagem.trim()) {
      this.feedback.set({ tipo: 'erro', texto: 'Preencha título e mensagem.' });
      return;
    }
    const dest = this.destinatarioSelecionado();
    if (this.modo === 'individual' && !dest) {
      this.feedback.set({ tipo: 'erro', texto: 'Busque e selecione um usuário destinatário.' });
      return;
    }

    this.enviando.set(true);
    this.feedback.set(null);
    this.adminApi
      .enviarNotificacao({
        titulo: this.titulo.trim(),
        mensagem: this.mensagem.trim(),
        linkRedirecionamento: this.linkRedirecionamento.trim() || undefined,
        usuarioId: this.modo === 'individual' ? dest!.id : null,
        segmento: this.modo === 'churn' ? 'RISCO_CHURN' : this.modo === 'individual' ? 'INDIVIDUAL' : 'TODOS',
      })
      .subscribe({
        next: (res) => {
          this.enviando.set(false);
          this.titulo = '';
          this.mensagem = '';
          this.linkRedirecionamento = '';
          const tipoLabel =
            this.modo === 'churn' ? 'em risco de churn' : res.global ? 'envio global' : 'envio individual';
          this.feedback.set({
            tipo: 'ok',
            texto: `Notificação entregue para ${res.destinatarios} usuário(s) (${tipoLabel}).`,
          });
        },
        error: (err) => {
          this.enviando.set(false);
          this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, 'Erro ao disparar notificação.') });
        },
      });
  }
}
