import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { AppMoedaPipe } from '../../shared/pipes/app-moeda.pipe';
import { InputMoedaComponent } from '../../shared/components/input-moeda/input-moeda.component';
import { PreferenciaMoedaService } from '../../shared/service/preferencia-moeda.service';
import { TransacoesService } from '../transacoes/service/transacoes.service';
import {
  AssinaturaRecorrente,
  AssinaturaRecorrentePayload,
  AssinaturasResumo,
  AssinaturasService,
  FrequenciaRecorrencia,
  ParcelamentoResumo,
  StatusRecorrencia,
} from './service/assinaturas.service';

@Component({
  selector: 'app-assinaturas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PrimeNGModuleModule,
    AppMoedaPipe,
    InputMoedaComponent,
  ],
  template: `
    <div class="flex flex-col gap-5" [class.pb-20]="!embutido" [class.md:pb-6]="!embutido">
      @if (!embutido) {
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 class="text-2xl font-extrabold text-slate-900 m-0">Assinaturas e Parcelamentos</h1>
          <p class="text-sm text-slate-500 mt-1 mb-0">
            Acompanhe seus gastos fixos recorrentes e o andamento das suas compras parceladas em um só lugar.
          </p>
        </div>
        <button
          type="button"
          (click)="abrirNova()"
          class="btn-npl-primary text-white font-semibold text-sm px-4 py-2.5 rounded-xl flex items-center justify-center gap-2 border-0 cursor-pointer shrink-0">
          <i class="pi pi-plus"></i>
          <span>Nova Assinatura</span>
        </button>
      </div>

      <!-- Abas internas: Assinaturas vs Parcelamentos -->
      <div class="flex items-center gap-2 border-b border-slate-200 pb-2">
        <button
          type="button"
          (click)="abaAtiva.set('assinaturas')"
          [class]="abaAtiva() === 'assinaturas'
            ? 'px-4 py-2 rounded-xl text-sm font-bold bg-[#7843CD] text-white border-0 cursor-pointer transition-colors'
            : 'px-4 py-2 rounded-xl text-sm font-semibold bg-white text-slate-600 border border-slate-200 cursor-pointer hover:bg-slate-50'">
          Assinaturas / Recorrentes ({{ resumo()?.ativas ?? 0 }})
        </button>
        <button
          type="button"
          (click)="abaAtiva.set('parcelamentos')"
          [class]="abaAtiva() === 'parcelamentos'
            ? 'px-4 py-2 rounded-xl text-sm font-bold bg-[#7843CD] text-white border-0 cursor-pointer transition-colors'
            : 'px-4 py-2 rounded-xl text-sm font-semibold bg-white text-slate-600 border border-slate-200 cursor-pointer hover:bg-slate-50'">
          Parcelamentos em aberto ({{ parcelamentos().length }})
        </button>
      </div>
      }

      @if (abaAtiva() === 'assinaturas') {

      <!-- Cards de resumo -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Custo Mensal Fixo</span>
          <div class="text-2xl font-extrabold text-rose-600 mt-1">
            {{ (resumo()?.totalMensalDespesas ?? 0) | appMoeda }}
          </div>
          <span class="text-xs text-slate-500">Equivalente mensal das despesas ativas</span>
        </div>

        <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Projeção Anual</span>
          <div class="text-2xl font-extrabold text-slate-900 mt-1">
            {{ (resumo()?.totalAnualDespesas ?? 0) | appMoeda }}
          </div>
          <span class="text-xs text-slate-500">Estimativa em 12 meses</span>
        </div>

        <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Receitas Recorrentes</span>
          <div class="text-2xl font-extrabold text-emerald-600 mt-1">
            {{ (resumo()?.totalMensalReceitas ?? 0) | appMoeda }}
          </div>
          <span class="text-xs text-slate-500">Entradas fixas mensais</span>
        </div>

        <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs">
          <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Status</span>
          <div class="text-2xl font-extrabold text-[#7843CD] mt-1">
            {{ resumo()?.ativas ?? 0 }} ativa(s)
          </div>
          <span class="text-xs text-slate-500">{{ resumo()?.pausadas ?? 0 }} pausada(s)</span>
        </div>
      </div>

      <!-- Filtro por status -->
      <div class="flex flex-wrap items-center justify-between gap-2">
        <div class="flex flex-wrap items-center gap-2">
          @for (opt of filtrosStatus; track opt.value) {
            <button
              type="button"
              (click)="filtroStatus.set(opt.value)"
              [class]="filtroStatus() === opt.value
                ? 'px-3.5 py-1.5 rounded-full text-xs font-bold bg-[#7843CD] text-white border-0 cursor-pointer'
                : 'px-3.5 py-1.5 rounded-full text-xs font-semibold bg-white text-slate-600 border border-slate-200 cursor-pointer hover:bg-slate-50'">
              {{ opt.label }}
            </button>
          }
        </div>
        @if (embutido) {
          <button
            type="button"
            (click)="abrirNova()"
            class="btn-npl-primary text-white font-semibold text-xs sm:text-sm px-3.5 py-2 rounded-xl flex items-center gap-1.5 border-0 cursor-pointer shrink-0">
            <i class="pi pi-plus text-xs"></i>
            <span>Nova Assinatura</span>
          </button>
        }
      </div>

      <!-- Lista -->
      @if (carregando()) {
        <div class="bg-white rounded-2xl border border-slate-200/80 p-10 flex justify-center">
          <p-progressSpinner styleClass="w-10 h-10" />
        </div>
      } @else if (assinaturasFiltradas().length === 0) {
        <div class="bg-white rounded-2xl border border-slate-200/80 p-10 text-center">
          <i class="pi pi-sync text-3xl text-slate-300 mb-3 block"></i>
          <h3 class="text-base font-bold text-slate-800 m-0">Nenhuma assinatura encontrada</h3>
          <p class="text-sm text-slate-500 mt-1 mb-4">
            Cadastre serviços como streaming, academia, internet ou aluguel para acompanhar suas renovações.
          </p>
          <button
            type="button"
            (click)="abrirNova()"
            class="btn-npl-primary text-white font-semibold text-sm px-4 py-2 rounded-xl border-0 cursor-pointer">
            Cadastrar primeira assinatura
          </button>
        </div>
      } @else {
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          @for (item of assinaturasFiltradas(); track item.id) {
            <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs flex flex-col justify-between gap-3">
              <div class="flex items-start justify-between gap-2">
                <div class="min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <h3 class="text-base font-bold text-slate-900 m-0 truncate">{{ item.descricao }}</h3>
                    <span [class]="badgeStatusClass(item.status)" class="text-[11px] font-bold px-2 py-0.5 rounded-full">
                      {{ item.status }}
                    </span>
                  </div>
                  <p class="text-xs text-slate-500 mt-1 mb-0">
                    {{ item.categoriaNome }} • {{ item.contaNome }}
                  </p>
                </div>
                <div class="text-right shrink-0">
                  <div [class]="item.tipo === 'DESPESA' ? 'text-rose-600' : 'text-emerald-600'" class="text-lg font-extrabold">
                    {{ item.tipo === 'DESPESA' ? '-' : '+' }}{{ item.valor | appMoeda }}
                  </div>
                  <span class="text-[11px] text-slate-400 font-medium">{{ labelFrequencia(item.frequencia) }}</span>
                </div>
              </div>

              <div class="rounded-xl bg-slate-50 border border-slate-100 p-3 text-xs text-slate-600 flex flex-col gap-1">
                <div class="flex justify-between">
                  <span>Próxima cobrança:</span>
                  <strong class="text-slate-800">{{ item.proximaCobranca | date:'dd/MM/yyyy' }}</strong>
                </div>
                <div class="flex justify-between">
                  <span>Ao renovar lança como:</span>
                  <strong class="text-slate-800">{{ item.estadoGeracao === 'REALIZADA' ? 'Realizada' : 'Pendente' }}</strong>
                </div>
                @if (item.frequencia !== 'MENSAL') {
                  <div class="flex justify-between">
                    <span>Equivalente mensal:</span>
                    <strong class="text-slate-800">{{ item.valorMensalEquivalente | appMoeda }}/mês</strong>
                  </div>
                }
              </div>

              <div class="flex items-center justify-between gap-2 pt-1 border-t border-slate-100">
                <div class="flex items-center gap-1.5">
                  @if (item.status === 'ATIVA') {
                    <button
                      type="button"
                      pTooltip="Lançar transação deste ciclo agora"
                      (click)="lancarAgora(item)"
                      class="px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-purple-50 text-[#7843CD] hover:bg-purple-100 border-0 cursor-pointer">
                      <i class="pi pi-bolt mr-1"></i>Lançar
                    </button>
                    <button
                      type="button"
                      pTooltip="Pausar recorrência"
                      (click)="alterarStatus(item, 'PAUSADA')"
                      class="px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-amber-50 text-amber-700 hover:bg-amber-100 border-0 cursor-pointer">
                      Pausar
                    </button>
                  } @else if (item.status === 'PAUSADA') {
                    <button
                      type="button"
                      (click)="alterarStatus(item, 'ATIVA')"
                      class="px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border-0 cursor-pointer">
                      Reativar
                    </button>
                    <button
                      type="button"
                      (click)="alterarStatus(item, 'CANCELADA')"
                      class="px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-slate-100 text-slate-600 hover:bg-slate-200 border-0 cursor-pointer">
                      Cancelar
                    </button>
                  } @else {
                    <button
                      type="button"
                      (click)="alterarStatus(item, 'ATIVA')"
                      class="px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border-0 cursor-pointer">
                      Reativar
                    </button>
                  }
                </div>

                <div class="flex items-center gap-1">
                  <button
                    type="button"
                    aria-label="Editar assinatura"
                    (click)="abrirEdicao(item)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-slate-100 border-0 bg-transparent cursor-pointer">
                    <i class="pi pi-pencil text-xs"></i>
                  </button>
                  <button
                    type="button"
                    aria-label="Excluir assinatura"
                    (click)="excluir(item)"
                    class="w-8 h-8 rounded-lg flex items-center justify-center text-rose-500 hover:bg-rose-50 border-0 bg-transparent cursor-pointer">
                    <i class="pi pi-trash text-xs"></i>
                  </button>
                </div>
              </div>
            </div>
          }
        </div>
      }
      } @else {
        <!-- Aba de Parcelamentos em aberto -->
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Comprometido / Mês</span>
            <div class="text-2xl font-extrabold text-rose-600 mt-1">
              {{ totalMensalParcelas() | appMoeda }}
            </div>
            <span class="text-xs text-slate-500">Soma das parcelas mensais ativas</span>
          </div>
          <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Saldo Restante Total</span>
            <div class="text-2xl font-extrabold text-slate-900 mt-1">
              {{ totalRestanteParcelas() | appMoeda }}
            </div>
            <span class="text-xs text-slate-500">Total ainda a pagar</span>
          </div>
          <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs">
            <span class="text-xs font-semibold uppercase tracking-wide text-slate-400">Compras Parceladas</span>
            <div class="text-2xl font-extrabold text-[#7843CD] mt-1">
              {{ parcelamentos().length }} em aberto
            </div>
            <span class="text-xs text-slate-500">Agrupadas automaticamente</span>
          </div>
        </div>

        @if (carregandoParcelamentos()) {
          <div class="bg-white rounded-2xl border border-slate-200/80 p-10 flex justify-center">
            <p-progressSpinner styleClass="w-10 h-10" />
          </div>
        } @else if (parcelamentos().length === 0) {
          <div class="bg-white rounded-2xl border border-slate-200/80 p-10 text-center">
            <i class="pi pi-credit-card text-3xl text-slate-300 mb-3 block"></i>
            <h3 class="text-base font-bold text-slate-800 m-0">Nenhum parcelamento em aberto</h3>
            <p class="text-sm text-slate-500 mt-1 mb-4">
              Quando você registrar uma compra parcelada em Transações, ela aparecerá aqui com o progresso de pagamento.
            </p>
            <button
              type="button"
              (click)="clicarIrParaLancamentos()"
              class="btn-npl-primary text-white font-semibold text-xs sm:text-sm px-4 py-2 rounded-xl inline-flex items-center gap-2 border-0 cursor-pointer">
              <i class="pi pi-list text-xs"></i>
              Ir para Lançamentos
            </button>
          </div>
        } @else {
          <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            @for (p of parcelamentos(); track p.grupoId) {
              <div class="bg-white rounded-2xl border border-slate-200/80 p-4 shadow-xs flex flex-col gap-3">
                <div class="flex items-start justify-between gap-2">
                  <div class="min-w-0">
                    <h3 class="text-base font-bold text-slate-900 m-0 truncate">{{ p.descricao }}</h3>
                    <p class="text-xs text-slate-500 mt-1 mb-0">
                      {{ p.categoriaNome || 'Sem categoria' }} • {{ p.contaNome || 'Conta' }}
                    </p>
                  </div>
                  <div class="text-right shrink-0">
                    <div class="text-lg font-extrabold text-rose-600">{{ p.valorParcela | appMoeda }}</div>
                    <span class="text-[11px] text-slate-400 font-medium">/mês</span>
                  </div>
                </div>

                <div>
                  <div class="flex justify-between text-xs font-semibold text-slate-600 mb-1">
                    <span>{{ p.parcelasPagas }} de {{ p.totalParcelas }} pagas</span>
                    <span class="text-[#7843CD]">Faltam {{ p.parcelasRestantes }}</span>
                  </div>
                  <div class="w-full h-2 rounded-full bg-slate-100 overflow-hidden">
                    <div
                      class="h-full rounded-full bg-[#7843CD] transition-all"
                      [style.width.%]="p.totalParcelas > 0 ? (p.parcelasPagas / p.totalParcelas) * 100 : 0">
                    </div>
                  </div>
                </div>

                <div class="rounded-xl bg-slate-50 border border-slate-100 p-3 text-xs text-slate-600 flex flex-col gap-1">
                  <div class="flex justify-between">
                    <span>Restante a pagar:</span>
                    <strong class="text-slate-800">{{ p.valorTotalRestante | appMoeda }}</strong>
                  </div>
                  @if (p.proximaParcelaData) {
                    <div class="flex justify-between">
                      <span>Próxima parcela:</span>
                      <strong class="text-slate-800">{{ p.proximaParcelaData | date:'dd/MM/yyyy' }}</strong>
                    </div>
                  }
                  @if (p.ultimaParcelaData) {
                    <div class="flex justify-between">
                      <span>Previsão de término:</span>
                      <strong class="text-slate-800">{{ p.ultimaParcelaData | date:'MM/yyyy' }}</strong>
                    </div>
                  }
                </div>

                <div class="flex items-center justify-between gap-2 pt-2 mt-auto border-t border-slate-100">
                  <button
                    type="button"
                    (click)="clicarVerParcelas(p)"
                    class="px-3 py-1.5 rounded-lg text-xs font-semibold bg-purple-50 text-[#7843CD] hover:bg-purple-100 border-0 cursor-pointer inline-flex items-center gap-1.5 transition-colors">
                    <i class="pi pi-list text-[11px]"></i>
                    Ver parcelas
                  </button>
                  <button
                    type="button"
                    (click)="clicarVerLancamentos(p)"
                    pTooltip="Filtrar esta compra na lista de lançamentos"
                    tooltipPosition="top"
                    class="px-2.5 py-1.5 rounded-lg text-xs font-semibold text-slate-600 hover:bg-slate-100 border-0 bg-transparent cursor-pointer inline-flex items-center gap-1.5 transition-colors">
                    <i class="pi pi-external-link text-[11px]"></i>
                    Ver lançamentos
                  </button>
                </div>
              </div>
            }
          </div>
        }
      }
    </div>

    <p-dialog
      [(visible)]="dialogVisivel"
      [modal]="true"
      [draggable]="false"
      [resizable]="false"
      [style]="{ width: 'min(94vw, 540px)' }"
      [header]="editandoId ? 'Editar Assinatura' : 'Nova Assinatura / Recorrência'">
      <form [formGroup]="form" (ngSubmit)="salvar()" class="flex flex-col gap-4 pt-2">
        <div class="flex flex-col gap-1">
          <label class="font-bold text-sm text-slate-700">Descrição</label>
          <input pInputText formControlName="descricao" placeholder="Ex: Netflix, Academia, Internet..." class="w-full" />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm text-slate-700">Valor</label>
            <app-input-moeda formControlName="valor" [moeda]="codigoMoeda" tamanho="md" placeholder="0,00" />
          </div>
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm text-slate-700">Tipo</label>
            <p-select
              formControlName="tipo"
              [options]="tiposOpcoes"
              optionLabel="label"
              optionValue="value"
              styleClass="w-full" />
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm text-slate-700">Frequência</label>
            <p-select
              formControlName="frequencia"
              [options]="frequenciasOpcoes"
              optionLabel="label"
              optionValue="value"
              styleClass="w-full" />
          </div>
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm text-slate-700">Próxima cobrança</label>
            <input type="date" formControlName="proximaCobranca" class="p-inputtext p-component w-full" />
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm text-slate-700">Conta financeira</label>
            <p-select
              formControlName="contaId"
              [options]="contas()"
              optionLabel="nome"
              optionValue="id"
              placeholder="Selecione a conta"
              styleClass="w-full" />
          </div>
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm text-slate-700">Categoria</label>
            <p-select
              formControlName="categoriaId"
              [options]="categorias()"
              optionLabel="nome"
              optionValue="id"
              placeholder="Selecione a categoria"
              styleClass="w-full" />
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm text-slate-700">Estado ao gerar transação</label>
            <p-select
              formControlName="estadoGeracao"
              [options]="estadosOpcoes"
              optionLabel="label"
              optionValue="value"
              styleClass="w-full" />
          </div>
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm text-slate-700">Marcador (opcional)</label>
            <p-select
              formControlName="marcadorId"
              [options]="marcadores()"
              optionLabel="nome"
              optionValue="id"
              [showClear]="true"
              placeholder="Nenhum"
              styleClass="w-full" />
          </div>
        </div>

        @if (!editandoId) {
          <div class="flex items-center gap-2 pt-1">
            <p-checkbox formControlName="lancarPrimeiraAgora" [binary]="true" inputId="lancarPrimeiraAgora" />
            <label for="lancarPrimeiraAgora" class="text-sm text-slate-600 cursor-pointer">
              Lançar a primeira transação agora e agendar a próxima renovação
            </label>
          </div>
        }

        <div class="flex justify-end gap-2 pt-2">
          <button
            type="button"
            (click)="dialogVisivel = false"
            class="px-4 py-2 rounded-xl text-sm font-semibold bg-slate-100 text-slate-700 border-0 cursor-pointer">
            Cancelar
          </button>
          <button
            type="submit"
            [disabled]="form.invalid || salvando()"
            class="btn-npl-primary text-white px-5 py-2 rounded-xl text-sm font-semibold border-0 cursor-pointer disabled:opacity-50">
            {{ salvando() ? 'Salvando...' : 'Salvar' }}
          </button>
        </div>
      </form>
    </p-dialog>
  `,
})
export class AssinaturasComponent implements OnInit {
  private fb = inject(FormBuilder);
  private assinaturasService = inject(AssinaturasService);
  private transacoesService = inject(TransacoesService);
  private preferenciaMoedaService = inject(PreferenciaMoedaService);
  private messageService = inject(MessageService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  @Input() embutido = false;
  @Input() set abaInicial(valor: 'assinaturas' | 'parcelamentos') {
    if (valor) {
      this.abaAtiva.set(valor);
    }
  }

  @Output() verParcelas = new EventEmitter<ParcelamentoResumo>();
  @Output() verTransacoesParcelamento = new EventEmitter<ParcelamentoResumo>();
  @Output() irParaLancamentos = new EventEmitter<void>();

  abaAtiva = signal<'assinaturas' | 'parcelamentos'>('assinaturas');
  resumo = signal<AssinaturasResumo | null>(null);
  parcelamentos = signal<ParcelamentoResumo[]>([]);
  carregando = signal(true);
  carregandoParcelamentos = signal(true);
  salvando = signal(false);
  filtroStatus = signal<'TODAS' | StatusRecorrencia>('TODAS');
  contas = signal<any[]>([]);
  categorias = signal<any[]>([]);
  marcadores = signal<any[]>([]);

  dialogVisivel = false;
  editandoId: number | null = null;
  form: FormGroup;

  readonly filtrosStatus: Array<{ label: string; value: 'TODAS' | StatusRecorrencia }> = [
    { label: 'Todas', value: 'TODAS' },
    { label: 'Ativas', value: 'ATIVA' },
    { label: 'Pausadas', value: 'PAUSADA' },
    { label: 'Canceladas', value: 'CANCELADA' },
  ];

  readonly tiposOpcoes = [
    { label: 'Despesa', value: 'DESPESA' },
    { label: 'Receita', value: 'RECEITA' },
  ];

  readonly frequenciasOpcoes: Array<{ label: string; value: FrequenciaRecorrencia }> = [
    { label: 'Semanal', value: 'SEMANAL' },
    { label: 'Mensal', value: 'MENSAL' },
    { label: 'Trimestral', value: 'TRIMESTRAL' },
    { label: 'Anual', value: 'ANUAL' },
  ];

  readonly estadosOpcoes = [
    { label: 'Pendente (confirmar pagamento)', value: 'PENDENTE' },
    { label: 'Realizada (já descontar do saldo)', value: 'REALIZADA' },
  ];

  get codigoMoeda(): string {
    return this.preferenciaMoedaService.obterMoedaAtiva();
  }

  constructor() {
    this.form = this.fb.group({
      descricao: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(150)]],
      valor: [null, [Validators.required, Validators.min(0.01)]],
      tipo: ['DESPESA', [Validators.required]],
      frequencia: ['MENSAL', [Validators.required]],
      estadoGeracao: ['PENDENTE', [Validators.required]],
      proximaCobranca: [this.hojeIso(), [Validators.required]],
      contaId: [null, [Validators.required]],
      categoriaId: [null, [Validators.required]],
      marcadorId: [null],
      lancarPrimeiraAgora: [false],
    });
  }

  ngOnInit(): void {
    if (!this.embutido) {
      const aba = this.route.snapshot.queryParamMap.get('aba');
      if (aba === 'parcelamentos' || aba === 'assinaturas') {
        this.abaAtiva.set(aba);
      }
    }
    this.carregarDados();
    this.carregarParcelamentos();
    this.transacoesService.listarContasUsuarioLogado().subscribe({ next: (c: any[]) => this.contas.set(c || []) });
    this.transacoesService.listarCategorias().subscribe({ next: (c: any[]) => this.categorias.set(c || []) });
    this.transacoesService.listarMarcadores().subscribe({ next: (m: any[]) => this.marcadores.set(m || []) });
  }

  clicarVerParcelas(parcelamento: ParcelamentoResumo): void {
    if (this.embutido) {
      this.verParcelas.emit(parcelamento);
    } else {
      void this.router.navigate(['/app/transacoes'], {
        queryParams: { aba: 'lancamentos', grupoParcelamento: parcelamento.grupoId },
      });
    }
  }

  clicarVerLancamentos(parcelamento: ParcelamentoResumo): void {
    if (this.embutido) {
      this.verTransacoesParcelamento.emit(parcelamento);
    } else {
      void this.router.navigate(['/app/transacoes'], {
        queryParams: { aba: 'lancamentos', busca: parcelamento.descricao, parcelado: 'true' },
      });
    }
  }

  clicarIrParaLancamentos(): void {
    if (this.embutido) {
      this.irParaLancamentos.emit();
    } else {
      void this.router.navigate(['/app/transacoes']);
    }
  }

  carregarParcelamentos(): void {
    this.carregandoParcelamentos.set(true);
    this.assinaturasService.listarParcelamentos().subscribe({
      next: (lista) => {
        this.parcelamentos.set(lista || []);
        this.carregandoParcelamentos.set(false);
      },
      error: () => {
        this.carregandoParcelamentos.set(false);
      },
    });
  }

  totalMensalParcelas(): number {
    return this.parcelamentos().reduce((acc, p) => acc + (p.valorParcela || 0), 0);
  }

  totalRestanteParcelas(): number {
    return this.parcelamentos().reduce((acc, p) => acc + (p.valorTotalRestante || 0), 0);
  }

  assinaturasFiltradas(): AssinaturaRecorrente[] {
    const todas = this.resumo()?.assinaturas ?? [];
    const filtro = this.filtroStatus();
    if (filtro === 'TODAS') return todas;
    return todas.filter((a) => a.status === filtro);
  }

  carregarDados(): void {
    this.carregando.set(true);
    this.assinaturasService.listar().subscribe({
      next: (res) => {
        this.resumo.set(res);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.messageService.add({ severity: 'error', summary: 'Erro ao carregar assinaturas' });
      },
    });
  }

  abrirNova(): void {
    this.editandoId = null;
    this.form.reset({
      descricao: '',
      valor: null,
      tipo: 'DESPESA',
      frequencia: 'MENSAL',
      estadoGeracao: 'PENDENTE',
      proximaCobranca: this.hojeIso(),
      contaId: this.contas()[0]?.id ?? null,
      categoriaId: this.categorias()[0]?.id ?? null,
      marcadorId: null,
      lancarPrimeiraAgora: false,
    });
    this.dialogVisivel = true;
  }

  abrirEdicao(item: AssinaturaRecorrente): void {
    this.editandoId = item.id;
    this.form.reset({
      descricao: item.descricao,
      valor: item.valor,
      tipo: item.tipo,
      frequencia: item.frequencia,
      estadoGeracao: item.estadoGeracao,
      proximaCobranca: item.proximaCobranca,
      contaId: item.contaId,
      categoriaId: item.categoriaId,
      marcadorId: item.marcadorId ?? null,
      lancarPrimeiraAgora: false,
    });
    this.dialogVisivel = true;
  }

  salvar(): void {
    if (this.form.invalid) return;
    this.salvando.set(true);
    const payload: AssinaturaRecorrentePayload = this.form.value;

    const req$ = this.editandoId
      ? this.assinaturasService.atualizar(this.editandoId, payload)
      : this.assinaturasService.criar(payload);

    req$.subscribe({
      next: () => {
        this.salvando.set(false);
        this.dialogVisivel = false;
        this.messageService.add({
          severity: 'success',
          summary: this.editandoId ? 'Assinatura atualizada' : 'Assinatura criada',
        });
        this.carregarDados();
      },
      error: (err) => {
        this.salvando.set(false);
        this.messageService.add({
          severity: 'error',
          summary: err?.error?.error || 'Erro ao salvar assinatura',
        });
      },
    });
  }

  alterarStatus(item: AssinaturaRecorrente, status: StatusRecorrencia): void {
    this.assinaturasService.alterarStatus(item.id, status).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: `Assinatura ${status.toLowerCase()}` });
        this.carregarDados();
      },
      error: () => this.messageService.add({ severity: 'error', summary: 'Erro ao atualizar status' }),
    });
  }

  lancarAgora(item: AssinaturaRecorrente): void {
    this.assinaturasService.lancarAgora(item.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Transação lançada!',
          detail: `${item.descricao} foi lançada e a próxima cobrança avançou.`,
        });
        this.carregarDados();
      },
      error: (err) =>
        this.messageService.add({ severity: 'error', summary: err?.error?.error || 'Erro ao lançar ciclo' }),
    });
  }

  excluir(item: AssinaturaRecorrente): void {
    this.assinaturasService.excluir(item.id).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Assinatura removida' });
        this.carregarDados();
      },
      error: () => this.messageService.add({ severity: 'error', summary: 'Erro ao excluir assinatura' }),
    });
  }

  labelFrequencia(freq: FrequenciaRecorrencia): string {
    return { SEMANAL: 'Semanal', MENSAL: 'Mensal', TRIMESTRAL: 'Trimestral', ANUAL: 'Anual' }[freq];
  }

  badgeStatusClass(status: StatusRecorrencia): string {
    return {
      ATIVA: 'bg-emerald-100 text-emerald-700',
      PAUSADA: 'bg-amber-100 text-amber-700',
      CANCELADA: 'bg-slate-200 text-slate-600',
    }[status];
  }

  private hojeIso(): string {
    const d = new Date();
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  }
}
