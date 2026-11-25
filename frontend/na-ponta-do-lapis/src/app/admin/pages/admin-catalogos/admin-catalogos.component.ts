import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  AdminApiService,
  AdminCategoriaResumo,
  AdminConquistaResumo,
  CategoriaConquista,
  extrairMensagemErroAdmin,
} from '../../services/admin-api.service';

@Component({
  selector: 'app-admin-catalogos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex flex-col gap-5">
      <div>
        <h1 class="text-2xl font-bold text-slate-900 m-0">Catálogos Globais</h1>
        <p class="text-sm text-slate-500 mt-1 mb-0">Gerencie categorias padrão e conquistas de gamificação da plataforma.</p>
      </div>

      <!-- Abas -->
      <div class="flex gap-2 border-b border-slate-200 pb-2">
        <button
          type="button"
          (click)="aba.set('categorias')"
          class="px-4 py-2 rounded-xl text-sm font-semibold cursor-pointer"
          [class.bg-[#7843CD]]="aba() === 'categorias'"
          [class.text-white]="aba() === 'categorias'"
          [class.text-slate-600]="aba() !== 'categorias'">
          Categorias Padrão ({{ categorias().length }})
        </button>
        <button
          type="button"
          (click)="aba.set('conquistas')"
          class="px-4 py-2 rounded-xl text-sm font-semibold cursor-pointer"
          [class.bg-[#7843CD]]="aba() === 'conquistas'"
          [class.text-white]="aba() === 'conquistas'"
          [class.text-slate-600]="aba() !== 'conquistas'">
          Conquistas ({{ conquistas().length }})
        </button>
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

      <!-- ABA CATEGORIAS -->
      @if (aba() === 'categorias') {
        <div class="bg-white rounded-2xl border border-slate-200 p-4 flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            [(ngModel)]="novaCategoriaNome"
            (keyup.enter)="criarCategoria()"
            placeholder="Nome da nova categoria global..."
            class="flex-1 px-3.5 py-2 rounded-xl border border-slate-200 text-sm" />
          <button
            type="button"
            (click)="criarCategoria()"
            class="px-4 py-2 rounded-xl bg-[#7843CD] hover:bg-[#6635b5] text-white text-sm font-semibold cursor-pointer">
            Adicionar Categoria
          </button>
        </div>

        <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <div class="overflow-x-auto">
          <table class="w-full text-left border-collapse text-sm">
            <thead>
              <tr class="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500">
                <th class="py-3 px-4">ID</th>
                <th class="py-3 px-4">Nome</th>
                <th class="py-3 px-4 text-center">Transações Vinculadas</th>
                <th class="py-3 px-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              @for (c of categorias(); track c.id) {
                <tr class="border-b border-slate-100">
                  <td class="py-3 px-4 text-xs text-slate-400">#{{ c.id }}</td>
                  <td class="py-3 px-4">
                    @if (editandoCatId() === c.id) {
                      <input type="text" [(ngModel)]="editandoCatNome" class="px-2.5 py-1 rounded-lg border border-slate-300 text-sm w-full max-w-xs" />
                    } @else {
                      <span class="font-medium text-slate-900">{{ c.nome }}</span>
                    }
                  </td>
                  <td class="py-3 px-4 text-center text-xs font-semibold text-slate-600">{{ c.totalTransacoes }}</td>
                  <td class="py-3 px-4 text-right">
                    <div class="inline-flex gap-2">
                      @if (editandoCatId() === c.id) {
                        <button type="button" (click)="salvarEdicaoCategoria(c.id)" class="px-2.5 py-1 rounded-lg bg-emerald-600 text-white text-xs font-semibold cursor-pointer">Salvar</button>
                        <button type="button" (click)="editandoCatId.set(null)" class="px-2.5 py-1 rounded-lg border border-slate-200 text-xs cursor-pointer">Cancelar</button>
                      } @else if (confirmandoExclusaoCatId() === c.id) {
                        <button type="button" (click)="excluirCategoria(c)" class="px-2.5 py-1 rounded-lg bg-rose-600 text-white text-xs font-semibold cursor-pointer">Confirmar exclusão</button>
                        <button type="button" (click)="confirmandoExclusaoCatId.set(null)" class="px-2.5 py-1 rounded-lg border border-slate-200 text-xs cursor-pointer">Cancelar</button>
                      } @else {
                        <button type="button" (click)="iniciarEdicaoCategoria(c)" class="px-2.5 py-1 rounded-lg border border-slate-200 hover:bg-slate-100 text-xs font-semibold cursor-pointer">Renomear</button>
                        <button
                          type="button"
                          [disabled]="c.totalTransacoes > 0"
                          [title]="c.totalTransacoes > 0 ? 'Em uso por transações' : 'Excluir categoria'"
                          (click)="confirmandoExclusaoCatId.set(c.id)"
                          class="px-2.5 py-1 rounded-lg bg-rose-50 text-rose-700 hover:bg-rose-100 text-xs font-semibold disabled:opacity-40 cursor-pointer">
                          Excluir
                        </button>
                      }
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
          </div>
        </div>
      }

      <!-- ABA CONQUISTAS -->
      @if (aba() === 'conquistas') {
        <div
          class="bg-white rounded-2xl border p-4 grid grid-cols-1 md:grid-cols-6 gap-3 transition-colors"
          [class.border-[#7843CD]]="editandoConquista()"
          [class.bg-purple-50/30]="editandoConquista()"
          [class.border-slate-200]="!editandoConquista()">
          @if (editandoConquista()) {
            <div class="md:col-span-6 text-xs font-bold text-[#7843CD] flex items-center gap-1.5">
              <i class="pi pi-pencil text-[11px]"></i>
              Editando conquista {{ formConquista.id }}
            </div>
          }
          <input type="text" [(ngModel)]="formConquista.id" [disabled]="editandoConquista()" placeholder="ID (ex: META_OURO)" class="px-3 py-2 rounded-xl border border-slate-200 text-xs disabled:bg-slate-100" />
          <input type="text" [(ngModel)]="formConquista.titulo" placeholder="Título" class="px-3 py-2 rounded-xl border border-slate-200 text-xs" />
          <input type="text" [(ngModel)]="formConquista.descricao" placeholder="Descrição" class="md:col-span-2 px-3 py-2 rounded-xl border border-slate-200 text-xs" />
          <div class="flex items-center gap-2">
            <span class="w-9 h-9 rounded-xl bg-purple-50 text-[#7843CD] flex items-center justify-center shrink-0 border border-purple-100" title="Preview do ícone">
              <i [class]="iconeClasse(formConquista.icone)"></i>
            </span>
            <input type="text" [(ngModel)]="formConquista.icone" placeholder="Ícone (ex: pi-trophy)" class="flex-1 min-w-0 px-3 py-2 rounded-xl border border-slate-200 text-xs" />
          </div>
          <div class="flex gap-2">
            <input type="number" [(ngModel)]="formConquista.pontosXp" min="0" placeholder="XP" class="w-20 px-2.5 py-2 rounded-xl border border-slate-200 text-xs" />
            <select [(ngModel)]="formConquista.categoria" class="flex-1 px-2 py-2 rounded-xl border border-slate-200 text-xs bg-white">
              <option value="METAS">METAS</option>
              <option value="ECONOMIA">ECONOMIA</option>
              <option value="DISCIPLINA">DISCIPLINA</option>
              <option value="FAMILIA">FAMILIA</option>
            </select>
          </div>
          <div class="md:col-span-6 flex justify-end gap-2">
            @if (editandoConquista()) {
              <button type="button" (click)="limparFormConquista()" class="px-3 py-1.5 rounded-xl border border-slate-200 text-xs cursor-pointer">Cancelar edição</button>
            }
            <button type="button" (click)="salvarConquista()" class="px-4 py-2 rounded-xl bg-[#7843CD] hover:bg-[#6635b5] text-white text-xs font-semibold cursor-pointer">
              {{ editandoConquista() ? 'Atualizar Conquista' : 'Criar Conquista' }}
            </button>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          @for (c of conquistas(); track c.id) {
            <div class="bg-white rounded-2xl border border-slate-200 p-4 flex items-start justify-between gap-3">
              <div class="flex items-start gap-3">
                <div class="w-10 h-10 rounded-xl bg-purple-50 text-[#7843CD] flex items-center justify-center shrink-0">
                  <i [class]="iconeClasse(c.icone)"></i>
                </div>
                <div class="flex flex-col gap-1">
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="text-xs font-mono px-2 py-0.5 rounded bg-slate-100 text-slate-600">{{ c.id }}</span>
                    <span class="text-[11px] font-bold px-2 py-0.5 rounded-full bg-purple-50 text-[#7843CD]">{{ c.categoria }} • {{ c.pontosXp }} XP</span>
                  </div>
                  <div class="font-bold text-slate-900 text-sm">{{ c.titulo }}</div>
                  <div class="text-xs text-slate-500">{{ c.descricao }}</div>
                  <div class="text-[11px] text-slate-400">Desbloqueada por <strong>{{ c.totalDesbloqueios }}</strong> usuário(s)</div>
                </div>
              </div>
              <button type="button" (click)="selecionarConquista(c)" class="px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-100 text-xs font-semibold shrink-0 cursor-pointer">
                Editar
              </button>
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class AdminCatalogosComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);

  readonly aba = signal<'categorias' | 'conquistas'>('categorias');
  readonly categorias = signal<AdminCategoriaResumo[]>([]);
  readonly conquistas = signal<AdminConquistaResumo[]>([]);
  readonly feedback = signal<{ tipo: 'ok' | 'erro'; texto: string } | null>(null);

  novaCategoriaNome = '';
  readonly editandoCatId = signal<number | null>(null);
  readonly confirmandoExclusaoCatId = signal<number | null>(null);
  editandoCatNome = '';

  readonly editandoConquista = signal(false);
  formConquista: {
    id: string;
    titulo: string;
    descricao: string;
    icone: string;
    pontosXp: number;
    categoria: CategoriaConquista;
  } = { id: '', titulo: '', descricao: '', icone: 'pi-trophy', pontosXp: 50, categoria: 'DISCIPLINA' };

  ngOnInit(): void {
    this.carregarCategorias();
    this.carregarConquistas();
  }

  carregarCategorias(): void {
    this.adminApi.listarCategorias().subscribe({
      next: (list) => this.categorias.set(list),
      error: (err) => this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, 'Erro ao carregar categorias.') }),
    });
  }

  carregarConquistas(): void {
    this.adminApi.listarConquistas().subscribe({
      next: (list) => this.conquistas.set(list),
      error: (err) => this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, 'Erro ao carregar conquistas.') }),
    });
  }

  iconeClasse(icone: string | null | undefined): string {
    const limpo = (icone ?? '').trim();
    if (!limpo) return 'pi pi-trophy';
    return limpo.startsWith('pi ') ? limpo : `pi ${limpo}`;
  }

  criarCategoria(): void {
    if (!this.novaCategoriaNome.trim()) return;
    this.adminApi.criarCategoria(this.novaCategoriaNome.trim()).subscribe({
      next: () => {
        this.novaCategoriaNome = '';
        this.feedback.set({ tipo: 'ok', texto: 'Categoria criada com sucesso.' });
        this.carregarCategorias();
      },
      error: (err) => this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, 'Erro ao criar categoria.') }),
    });
  }

  iniciarEdicaoCategoria(c: AdminCategoriaResumo): void {
    this.editandoCatId.set(c.id);
    this.editandoCatNome = c.nome;
  }

  salvarEdicaoCategoria(id: number): void {
    if (!this.editandoCatNome.trim()) return;
    this.adminApi.atualizarCategoria(id, this.editandoCatNome.trim()).subscribe({
      next: () => {
        this.editandoCatId.set(null);
        this.feedback.set({ tipo: 'ok', texto: 'Categoria atualizada.' });
        this.carregarCategorias();
      },
      error: (err) => this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, 'Erro ao atualizar categoria.') }),
    });
  }

  excluirCategoria(c: AdminCategoriaResumo): void {
    this.confirmandoExclusaoCatId.set(null);
    this.adminApi.excluirCategoria(c.id).subscribe({
      next: () => {
        this.feedback.set({ tipo: 'ok', texto: `Categoria "${c.nome}" excluída.` });
        this.carregarCategorias();
      },
      error: (err) => this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, 'Erro ao excluir categoria.') }),
    });
  }

  selecionarConquista(c: AdminConquistaResumo): void {
    this.editandoConquista.set(true);
    this.formConquista = {
      id: c.id,
      titulo: c.titulo,
      descricao: c.descricao,
      icone: c.icone,
      pontosXp: c.pontosXp,
      categoria: c.categoria,
    };
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  limparFormConquista(): void {
    this.editandoConquista.set(false);
    this.formConquista = { id: '', titulo: '', descricao: '', icone: 'pi-trophy', pontosXp: 50, categoria: 'DISCIPLINA' };
  }

  salvarConquista(): void {
    const req$ = this.editandoConquista()
      ? this.adminApi.atualizarConquista(this.formConquista.id, this.formConquista)
      : this.adminApi.criarConquista(this.formConquista);

    req$.subscribe({
      next: () => {
        this.feedback.set({ tipo: 'ok', texto: this.editandoConquista() ? 'Conquista atualizada.' : 'Conquista criada.' });
        this.limparFormConquista();
        this.carregarConquistas();
      },
      error: (err) => this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, 'Erro ao salvar conquista.') }),
    });
  }
}
