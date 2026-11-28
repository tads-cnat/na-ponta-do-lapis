import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {
  AdminApiService,
  AdminCategoriaResumo,
  AdminConquistaResumo,
  AdminIaPainel,
  AdminModeloDisponivel,
  AdminProvedorIaConfig,
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
        <h1 class="text-2xl font-bold text-slate-900 m-0">Catálogos &amp; IA</h1>
        <p class="text-sm text-slate-500 mt-1 mb-0">Gerencie categorias padrão, conquistas e modelos de IA da plataforma.</p>
      </div>

      <!-- Abas -->
      <div class="flex flex-wrap gap-2 border-b border-slate-200 pb-2">
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
        <button
          type="button"
          (click)="aba.set('ia')"
          class="px-4 py-2 rounded-xl text-sm font-semibold cursor-pointer inline-flex items-center gap-2"
          [class.bg-[#7843CD]]="aba() === 'ia'"
          [class.text-white]="aba() === 'ia'"
          [class.text-slate-600]="aba() !== 'ia'">
          <span>Modelos de IA</span>
          @if ((painelIa()?.alertasAtivos ?? 0) > 0) {
            <span class="px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-rose-500 text-white leading-none">
              {{ painelIa()!.alertasAtivos }}
            </span>
          }
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

      <!-- ABA MODELOS DE IA -->
      @if (aba() === 'ia') {
        @if (painelIa(); as ia) {
          @if (ia.alertasAtivos > 0) {
            <div class="p-4 rounded-2xl bg-rose-50 border border-rose-300 text-rose-800 text-xs flex items-center gap-3">
              <i class="pi pi-exclamation-triangle text-base text-rose-600 shrink-0"></i>
              <div>
                <div class="font-bold">{{ ia.alertasAtivos }} provedor(es) com alerta de modelo descontinuado / HTTP 404</div>
                <div class="mt-0.5 opacity-90">Clique em <strong>Buscar modelos válidos</strong> no provedor abaixo e salve um modelo ativo (efeito imediato, sem reiniciar o servidor).</div>
              </div>
            </div>
          }

          <!-- Lista de Provedores -->
          <div class="flex flex-col gap-3">
            @for (p of ia.provedores; track p.provedor) {
              <div
                class="bg-white rounded-2xl border p-4 flex flex-col gap-3"
                [class.border-rose-300]="p.alertaModeloDescontinuado"
                [class.border-slate-200]="!p.alertaModeloDescontinuado">
                <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div class="flex items-center gap-2.5 flex-wrap">
                    <h3 class="text-base font-bold text-slate-900 m-0">{{ p.nomeExibicao }}</h3>
                    <span
                      class="text-[11px] font-semibold px-2 py-0.5 rounded-full border"
                      [class.bg-purple-50]="p.customizadoNoBanco"
                      [class.text-[#7843CD]]="p.customizadoNoBanco"
                      [class.border-purple-200]="p.customizadoNoBanco"
                      [class.bg-slate-100]="!p.customizadoNoBanco"
                      [class.text-slate-600]="!p.customizadoNoBanco"
                      [class.border-slate-200]="!p.customizadoNoBanco">
                      {{ p.customizadoNoBanco ? 'Definido no Admin' : 'Fallback (.env)' }}
                    </span>
                    @if (p.totalExecucoes30d > 0) {
                      <span class="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                        {{ p.taxaSucesso30d | number:'1.0-1' }}% sucesso ({{ p.sucessos30d }}/{{ p.totalExecucoes30d }})
                      </span>
                    }
                  </div>
                  <span class="text-[11px] text-slate-400">
                    Padrão env: <code class="font-mono text-slate-600">{{ p.modeloPadraoEnv }}</code>
                  </span>
                </div>

                @if (p.alertaModeloDescontinuado) {
                  <div class="p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-800 flex items-start gap-2">
                    <i class="pi pi-exclamation-triangle text-rose-600 mt-0.5 shrink-0"></i>
                    <div class="min-w-0">
                      <div class="font-bold">Modelo descontinuado / HTTP 404 detectado</div>
                      <div class="mt-0.5 break-words opacity-90">{{ p.ultimoErroModelo }}</div>
                    </div>
                  </div>
                }

                <div class="grid grid-cols-1 lg:grid-cols-12 gap-2.5 items-center">
                  <div class="lg:col-span-5">
                    @if ((modelosDisponiveis()[p.provedor]?.length ?? 0) > 0) {
                      <select
                        [ngModel]="rascunhoModelo[p.provedor]"
                        (ngModelChange)="rascunhoModelo[p.provedor] = $event"
                        class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs font-mono bg-white">
                        @for (m of modelosDisponiveis()[p.provedor]; track m.id) {
                          <option [value]="m.id">{{ m.id }} {{ m.nomeExibicao !== m.id ? '— ' + m.nomeExibicao : '' }}</option>
                        }
                      </select>
                    } @else {
                      <input
                        type="text"
                        [(ngModel)]="rascunhoModelo[p.provedor]"
                        placeholder="ID do modelo (ex: gemini-2.5-flash)"
                        class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs font-mono" />
                    }
                  </div>

                  @if (!p.adminPossuiChaveSalva) {
                    <div class="lg:col-span-3">
                      <input
                        type="password"
                        [(ngModel)]="chaveConsulta[p.provedor]"
                        [placeholder]="'Chave ' + p.nomeExibicao + ' p/ listar'"
                        class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs font-mono" />
                    </div>
                  }

                  <div class="flex flex-wrap gap-2" [class.lg:col-span-7]="p.adminPossuiChaveSalva" [class.lg:col-span-4]="!p.adminPossuiChaveSalva">
                    <button
                      type="button"
                      [disabled]="buscandoModelos()[p.provedor] || (!p.adminPossuiChaveSalva && !chaveConsulta[p.provedor]?.trim())"
                      [title]="!p.adminPossuiChaveSalva && !chaveConsulta[p.provedor]?.trim() ? 'Informe uma chave de ' + p.nomeExibicao + ' ou cadastre em Perfil > IA para consultar os modelos' : 'Consultar modelos direto na API de ' + p.nomeExibicao"
                      (click)="buscarModelosProvedor(p)"
                      class="px-3 py-2 rounded-xl border border-purple-200 bg-purple-50 hover:bg-purple-100 text-[#7843CD] text-xs font-semibold cursor-pointer inline-flex items-center gap-1.5 disabled:opacity-50 disabled:cursor-not-allowed">
                      <i class="pi text-[11px]" [class.pi-spin]="buscandoModelos()[p.provedor]" [class.pi-spinner]="buscandoModelos()[p.provedor]" [class.pi-refresh]="!buscandoModelos()[p.provedor]"></i>
                      {{ buscandoModelos()[p.provedor] ? 'Buscando...' : ((modelosDisponiveis()[p.provedor]?.length ?? 0) > 0 ? 'Atualizar lista' : 'Buscar modelos válidos') }}
                    </button>

                    <button
                      type="button"
                      [disabled]="salvandoModelo()[p.provedor] || !rascunhoModelo[p.provedor]?.trim()"
                      (click)="salvarModeloProvedor(p)"
                      class="px-4 py-2 rounded-xl bg-[#7843CD] hover:bg-[#6635b5] text-white text-xs font-semibold cursor-pointer disabled:opacity-50">
                      {{ salvandoModelo()[p.provedor] ? 'Salvando...' : 'Salvar modelo' }}
                    </button>
                  </div>
                </div>

                @if (p.atualizadoEm || p.modelosConsultadosEm) {
                  <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-[11px] text-slate-400">
                    @if (p.atualizadoEm) {
                      <span>Última alteração por <strong>{{ p.atualizadoPor }}</strong> em {{ p.atualizadoEm | date:'dd/MM/yyyy HH:mm' }}</span>
                    }
                    @if (p.modelosConsultadosEm) {
                      <span>Lista em cache (30 min) consultada às {{ p.modelosConsultadosEm | date:'HH:mm' }} ({{ modelosDisponiveis()[p.provedor]?.length ?? 0 }} modelos filtrados)</span>
                    }
                  </div>
                }
              </div>
            }
          </div>
        } @else {
          <div class="bg-white rounded-2xl border border-slate-200 p-8 text-center text-sm text-slate-500">
            Carregando configurações e métricas de IA...
          </div>
        }
      }
    </div>
  `,
})
export class AdminCatalogosComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);
  private readonly route = inject(ActivatedRoute);

  readonly aba = signal<'categorias' | 'conquistas' | 'ia'>('categorias');
  readonly categorias = signal<AdminCategoriaResumo[]>([]);
  readonly conquistas = signal<AdminConquistaResumo[]>([]);
  readonly painelIa = signal<AdminIaPainel | null>(null);
  readonly modelosDisponiveis = signal<Record<string, AdminModeloDisponivel[]>>({});
  readonly buscandoModelos = signal<Record<string, boolean>>({});
  readonly salvandoModelo = signal<Record<string, boolean>>({});
  readonly feedback = signal<{ tipo: 'ok' | 'erro'; texto: string } | null>(null);

  rascunhoModelo: Record<string, string> = {};
  chaveConsulta: Record<string, string> = {};

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
    const abaParam = this.route.snapshot.queryParamMap.get('aba');
    if (abaParam === 'ia' || abaParam === 'conquistas' || abaParam === 'categorias') {
      this.aba.set(abaParam);
    }
    this.carregarCategorias();
    this.carregarConquistas();
    this.carregarPainelIa();
  }

  carregarPainelIa(): void {
    this.adminApi.obterConfiguracoesIa(30).subscribe({
      next: (painel) => {
        this.painelIa.set(painel);
        const mapaModelos: Record<string, AdminModeloDisponivel[]> = { ...this.modelosDisponiveis() };
        for (const p of painel.provedores) {
          this.rascunhoModelo[p.provedor] = p.modeloAtivo;
          if (p.modelosDisponiveis && p.modelosDisponiveis.length > 0) {
            mapaModelos[p.provedor] = p.modelosDisponiveis;
          }
        }
        this.modelosDisponiveis.set(mapaModelos);
      },
      error: (err) => this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, 'Erro ao carregar configurações de IA.') }),
    });
  }

  buscarModelosProvedor(p: AdminProvedorIaConfig): void {
    this.feedback.set(null);
    this.buscandoModelos.update((m) => ({ ...m, [p.provedor]: true }));
    this.adminApi.listarModelosDisponiveisIa(p.provedor, this.chaveConsulta[p.provedor]).subscribe({
      next: (lista) => {
        this.buscandoModelos.update((m) => ({ ...m, [p.provedor]: false }));
        this.modelosDisponiveis.update((m) => ({ ...m, [p.provedor]: lista }));
        if (lista.length > 0 && !lista.some((x) => x.id === this.rascunhoModelo[p.provedor])) {
          this.rascunhoModelo[p.provedor] = lista[0].id;
        }
        this.feedback.set({
          tipo: 'ok',
          texto: `${lista.length} modelo(s) válido(s) salvo(s) direto da API (${p.nomeExibicao}).`,
        });
        this.carregarPainelIa();
      },
      error: (err) => {
        this.buscandoModelos.update((m) => ({ ...m, [p.provedor]: false }));
        this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, `Erro ao consultar modelos de ${p.nomeExibicao}.`) });
      },
    });
  }

  salvarModeloProvedor(p: AdminProvedorIaConfig): void {
    const novoModelo = (this.rascunhoModelo[p.provedor] || '').trim();
    if (!novoModelo) return;
    this.feedback.set(null);
    this.salvandoModelo.update((m) => ({ ...m, [p.provedor]: true }));
    this.adminApi.atualizarModeloIa(p.provedor, novoModelo).subscribe({
      next: () => {
        this.salvandoModelo.update((m) => ({ ...m, [p.provedor]: false }));
        this.feedback.set({ tipo: 'ok', texto: `Modelo de ${p.nomeExibicao} atualizado para "${novoModelo}" (efeito imediato).` });
        this.carregarPainelIa();
      },
      error: (err) => {
        this.salvandoModelo.update((m) => ({ ...m, [p.provedor]: false }));
        this.feedback.set({ tipo: 'erro', texto: extrairMensagemErroAdmin(err, `Erro ao salvar modelo de ${p.nomeExibicao}.`) });
      },
    });
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
