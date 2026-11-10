import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TransacoesService } from '../transacoes/service/transacoes.service';
import { ITransacoes } from '../../model/ITransacoes.model';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { ColorPickerModule } from 'primeng/colorpicker';
import { ChartModule } from 'primeng/chart';

import { ContaFinanceiraService } from './service/contas.service';
import { IntegracoesService } from '../../service/integracoes.service';
import { IContas, IContasRequest, Moeda, TipoConta } from '../../model/IContas.models';

import { AcoesCartoesComponent } from './components/cartoes/acoes.component';
import { CartaoContaComponent }  from './components/cartoes/cartoes.component';
import { InformacoesComponent }  from './components/informacoes/informacoes.component';
import { GraficoComponent }      from './components/grafico/grafico.component';
import { TransacoesComponent }   from './components/transacoes/transacoes.component';
import { IconeBancoComponent }    from '../../shared/components/icone-banco/icone-banco.component';
import { InputMoedaComponent }    from '../../shared/components/input-moeda/input-moeda.component';
import { AppMoedaPipe }          from '../../shared/pipes/app-moeda.pipe';
import { PreferenciaMoedaService } from '../../shared/service/preferencia-moeda.service';
import { mapearNomeBanco, BancoResolvido, obterCorOficialBanco, temBancoDetectado } from '../../shared/utils/banco-icon.helper';

@Component({
  selector: 'app-Contas',
  standalone: true,
  providers: [MessageService],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ToastModule,
    PrimeNGModuleModule,
    ColorPickerModule,
    ChartModule,
    AcoesCartoesComponent,
    CartaoContaComponent,
    InformacoesComponent,
    GraficoComponent,
    TransacoesComponent,
    IconeBancoComponent,
    InputMoedaComponent,
    AppMoedaPipe
  ],
  template: `

    <!-- TOAST — deve ficar no nível raiz para sobrepor qualquer conteúdo -->
    <p-toast position="top-right" [life]="3000" />

    <div class="w-full flex flex-col gap-6 pb-8">

      <!-- HEADER PRINCIPAL DA PÁGINA (largura total: título e ações sem competir por espaço) -->
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div class="min-w-0">
          <h1 class="page-title-npl m-0">
            Contas
          </h1>
          <p class="page-subtitle-npl">
            Gestão de contas financeiras e cartões
          </p>
        </div>

        <!-- AÇÕES -->
        <app-acoes-cartoes
          class="shrink-0"
          [sincronizandoOpenFinance]="sincronizandoOpenFinance"
          [openFinanceConectado]="openFinanceConectado"
          (onAdicionar)="abrirDialog()"
          (onEditar)="editarConta(contaSelecionada)"
          (onExcluir)="abrirDialogExcluir(contaSelecionada)"
          (onSincronizarOpenFinance)="sincronizarOpenFinance()">
        </app-acoes-cartoes>
      </div>

      <!-- GRID PRINCIPAL (em telas xl e superiores fica em 2 colunas: 5 e 7; abaixo de xl fica em coluna única) -->
      <div class="grid grid-cols-1 xl:grid-cols-12 gap-6 w-full items-start">

        <!-- COLUNA ESQUERDA -->
        <div class="xl:col-span-5 flex flex-col gap-6 min-w-0">

          <!-- CARTÕES (carrossel) -->
          <app-cartoes-contas
            [contas]="contaFinanceiraDados"
            [contaSelecionadaIndex]="contaSelecionadaIndex"
            [contaAnterior]="contaAnterior"
            [contaProxima]="contaProxima"
            [carregando]="carregandoContas"
            (onContaSelecionada)="selecionarConta($event)"
            (onAnterior)="selecionarAnterior()"
            (onProxima)="selecionarProxima()">
          </app-cartoes-contas>

          <!-- INFORMAÇÕES DA CONTA SELECIONADA -->
          <app-informacoes-conta
            [conta]="contaSelecionada">
          </app-informacoes-conta>

        </div>

        <!-- COLUNA DIREITA -->
        <div class="xl:col-span-7 flex flex-col gap-6 min-w-0">

          <!-- GRÁFICO -->
          <app-grafico-conta
            [chartData]="chartData"
            [chartOptions]="chartOptions"
            (onPeriodoMudou)="atualizarGraficoSaldo($event)">
          </app-grafico-conta>

          <!-- TRANSAÇÕES -->
          <app-transacoes-conta
            [transacoesPaginadas]="transacoesPaginadas"
            [ultimasTransacoes]="ultimasTransacoes"
            [transacoesPorPagina]="transacoesPorPagina"
            [paginaAtualTransacoes]="paginaAtualTransacoes"
            (onProximaPagina)="proximaPaginaTransacoes()"
            (onPaginaAnterior)="paginaAnteriorTransacoes()"
            (onIrParaTransacoes)="irParaTransacoes()">
          </app-transacoes-conta>

        </div>

      </div>

      <!--
        DIALOGS — ficam fora do grid, no nível raiz do componente.

        Motivo: p-dialog é um overlay renderizado como portal no body, não como
        seção de página. O estado que os controla (exibirDialog, excluirDialog,
        formContaFinanceira) pertence ao ContasComponent, portanto aqui é o lugar
        natural. Extrair para um filho exigiria passar o FormGroup por @Input ou
        duplicar a lógica de CRUD, sem nenhum ganho arquitetural.

        O <form [formGroup]> precisa ser ancestral dos formControlName no template;
        como p-dialog projeta seu conteúdo no slot do componente (não fora da
        árvore de componentes Angular), o binding reativo funciona corretamente.
      -->
      <form [formGroup]="formContaFinanceira">

        <!-- DIALOG — NOVA CONTA / EDITAR CONTA -->
        <p-dialog
          [header]="modoEdicao ? 'Editar Conta' : 'Nova Conta'"
          [(visible)]="exibirDialog"
          [modal]="true"
          [draggable]="false"
          [resizable]="false"
          styleClass="rounded-3xl!"
          [style]="{ width: '42rem', maxWidth: '95vw' }"
          [breakpoints]="{ '960px': '85vw', '640px': '95vw' }">

          <div class="flex flex-col gap-6 py-2">

            <!-- PREVIEW EM TEMPO REAL DO CARTÃO / CONTA (FIEL AO CARROSSEL) -->
            <div class="flex justify-center w-full py-1">
              <div
                class="relative w-[400px] max-w-[85vw] h-[220px] rounded-[2rem] overflow-hidden shadow-2xl transition-transform hover:scale-[1.01]"
                [style.background]="obterCorPreview()">
                <!-- EFEITOS DO CARROSSEL -->
                <div class="absolute top-[-30px] right-[-20px] w-52 h-52 rounded-full border border-white/20 pointer-events-none"></div>
                <div class="absolute top-[-70px] right-[20px] w-72 h-72 rounded-full border border-white/10 pointer-events-none"></div>
                <div class="absolute inset-0 bg-black/10 pointer-events-none"></div>

                <!-- CONTEÚDO -->
                <div class="relative z-10 h-full p-7 flex flex-col justify-between text-white">
                  <div class="flex items-start justify-between gap-4">
                    <div class="min-w-0 flex-1">
                      @if (modoEdicao && contaSelecionada?.sincronizada) {
                        <span class="inline-flex items-center gap-1.5 text-[11px] bg-white/20 backdrop-blur-sm px-2.5 py-0.5 rounded-full font-semibold tracking-wide">
                          <svg class="w-3.5 h-3.5 inline-block shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="2">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M3 21h18M3 10h18M5 10v11M9 10v11M15 10v11M19 10v11M12 3l9 5H3l9-5z"/>
                          </svg>
                          <span>Open Finance</span>
                        </span>
                      }
                      <h3 class="text-2xl font-bold mt-2 leading-snug line-clamp-2 break-words">
                        {{ formContaFinanceira.get('nome')?.value || 'Nome da Conta' }}
                      </h3>
                    </div>

                    <div class="shrink-0 flex items-center justify-center select-none filter drop-shadow-[0_4px_8px_rgba(0,0,0,0.35)] drop-shadow-[0_1px_2px_rgba(0,0,0,0.25)]">
                      @if (temBanco(formContaFinanceira.get('nome')?.value)) {
                        <app-icone-banco [nome]="formContaFinanceira.get('nome')?.value" [tamanho]="46" [formato]="'quadrado'"></app-icone-banco>
                      } @else {
                        <div class="w-11 h-11 flex items-center justify-center text-white">
                          <i [class]="(ehCartaoPreview() ? 'pi pi-credit-card' : 'pi pi-wallet') + ' text-3xl text-white'"></i>
                        </div>
                      }
                    </div>
                  </div>

                  <!-- RODAPÉ DO CARD: SALDO / FATURA -->
                  <div class="flex items-end justify-between pt-2 border-t border-white/20">
                    <div>
                      <p class="text-[11px] font-medium text-white/80 uppercase tracking-wider">
                        {{ ehCartaoPreview() ? 'Fatura Atual' : 'Saldo Disponível' }}
                      </p>
                      <p class="text-xl font-bold tracking-tight">
                        {{ ehCartaoPreview() ? '-' : '' }} {{ (formContaFinanceira.get('saldo')?.value || 0) | appMoeda }}
                      </p>
                    </div>
                    @if (ehCartaoPreview() && formContaFinanceira.get('limiteTotal')?.value) {
                      <div class="text-right text-[11px] text-white/90">
                        <span class="opacity-80">Disp.: </span>
                        <span class="font-bold">{{ limiteTotalOuDisponivelPreview() | appMoeda }}</span>
                      </div>
                    }
                  </div>
                </div>
              </div>
            </div>

            <!-- TIPO DE CONTA -->
            <div class="flex flex-col gap-2">
              <label class="font-semibold text-slate-700 text-sm">Tipo de Conta</label>
              <div class="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  (click)="formContaFinanceira.get('tipoConta')?.setValue('CONTA_CORRENTE')"
                  [class]="formContaFinanceira.get('tipoConta')?.value === 'CONTA_CORRENTE'
                    ? 'flex items-center gap-3 p-3 rounded-2xl border-2 border-[#7843CD] bg-[#7843CD]/5 text-slate-900 font-bold transition-colors shadow-xs cursor-pointer'
                    : 'flex items-center gap-3 p-3 rounded-2xl border-2 border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-medium transition-colors cursor-pointer'">
                  <div class="w-9 h-9 rounded-xl flex items-center justify-center shrink-0"
                       [class]="formContaFinanceira.get('tipoConta')?.value === 'CONTA_CORRENTE' ? 'bg-[#7843CD] text-white' : 'bg-slate-100 text-slate-500'">
                    <i class="pi pi-wallet text-base"></i>
                  </div>
                  <span class="text-sm font-bold leading-tight text-left flex-1">Conta Bancária</span>
                  @if (formContaFinanceira.get('tipoConta')?.value === 'CONTA_CORRENTE') {
                    <span class="w-5 h-5 rounded-full bg-[#EEC046] text-slate-900 flex items-center justify-center text-[10px] font-extrabold shrink-0">
                      <i class="pi pi-check"></i>
                    </span>
                  }
                </button>

                <button
                  type="button"
                  (click)="formContaFinanceira.get('tipoConta')?.setValue('CARTAO_CREDITO')"
                  [class]="formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO'
                    ? 'flex items-center gap-3 p-3 rounded-2xl border-2 border-[#7843CD] bg-[#7843CD]/5 text-slate-900 font-bold transition-colors shadow-xs cursor-pointer'
                    : 'flex items-center gap-3 p-3 rounded-2xl border-2 border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-medium transition-colors cursor-pointer'">
                  <div class="w-9 h-9 rounded-xl flex items-center justify-center shrink-0"
                       [class]="formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO' ? 'bg-[#7843CD] text-white' : 'bg-slate-100 text-slate-500'">
                    <i class="pi pi-credit-card text-base"></i>
                  </div>
                  <span class="text-sm font-bold leading-tight text-left flex-1">Cartão de Crédito</span>
                  @if (formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO') {
                    <span class="w-5 h-5 rounded-full bg-[#EEC046] text-slate-900 flex items-center justify-center text-[10px] font-extrabold shrink-0">
                      <i class="pi pi-check"></i>
                    </span>
                  }
                </button>
              </div>
            </div>

            <!-- NOME DA CONTA -->
            <div class="flex flex-col gap-1.5">
              <label class="font-semibold text-slate-700 text-sm">Nome da Conta</label>
              <input
                pInputText
                formControlName="nome"
                (input)="aoDigitarNomeConta()"
                placeholder="Ex: Nubank, Santander, Inter, Itaú..."
                class="w-full text-base h-[42px]" />

              @if (bancoDetectado?.chave) {
                <div class="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-200/80 text-xs text-slate-700 animate-in fade-in">
                  <div class="flex items-center gap-2 min-w-0">
                    <app-icone-banco [nome]="bancoDetectado?.nomeExibicao" [tamanho]="26" [formato]="'quadrado'"></app-icone-banco>
                    <span class="truncate">Banco detectado: <strong>{{ bancoDetectado?.nomeExibicao }}</strong></span>
                  </div>
                  <button
                    type="button"
                    (click)="aplicarCorOficialBanco()"
                    class="text-xs font-semibold text-[#7843CD] hover:text-[#6214CC] flex items-center gap-1.5 shrink-0 cursor-pointer">
                    <span class="w-3.5 h-3.5 rounded-full border border-slate-300 shadow-2xs" [style.backgroundColor]="'#' + formContaFinanceira.get('cor')?.value"></span>
                    <span>Cor oficial aplicada</span>
                  </button>
                </div>
              }
            </div>

            <!-- VALORES: SALDO E LIMITE (PADRÃO ESTÉTICO INTEGRADO) -->
            @if (formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO') {
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <!-- FATURA ATUAL -->
                <div class="flex flex-col gap-1.5">
                  <label class="font-semibold text-slate-700 text-sm">
                    Fatura Atual
                  </label>
                  <app-input-moeda
                    formControlName="saldo"
                    [moeda]="formContaFinanceira.get('moeda')?.value"
                    [disabled]="!!(modoEdicao && contaSelecionada?.sincronizada)"
                    tamanho="md"
                    placeholder="0,00">
                  </app-input-moeda>
                </div>

                <!-- LIMITE TOTAL -->
                <div class="flex flex-col gap-1.5">
                  <label class="font-semibold text-slate-700 text-sm">
                    Limite Total
                  </label>
                  <app-input-moeda
                    formControlName="limiteTotal"
                    [moeda]="formContaFinanceira.get('moeda')?.value"
                    tamanho="md"
                    [permitirVazio]="true"
                    placeholder="0,00">
                  </app-input-moeda>
                </div>
              </div>
            } @else {
              <!-- CONTA BANCÁRIA: SALDO INICIAL -->
              <div class="flex flex-col gap-1.5">
                <label class="font-semibold text-slate-700 text-sm">Saldo Inicial</label>
                <app-input-moeda
                  formControlName="saldo"
                  [moeda]="formContaFinanceira.get('moeda')?.value"
                  [disabled]="!!(modoEdicao && contaSelecionada?.sincronizada)"
                  tamanho="md"
                  placeholder="0,00">
                </app-input-moeda>
                @if (modoEdicao && contaSelecionada?.sincronizada) {
                  <div class="p-2.5 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-800 flex items-center gap-2 mt-1">
                    <i class="pi pi-info-circle text-amber-600"></i>
                    <span>Conta sincronizada via Open Finance. Saldo atualizado pelo banco.</span>
                  </div>
                }
              </div>
            }

            <!-- PALETA DE CORES RÁPIDAS + SELETOR DE COR DESTACADO -->
            <div class="flex flex-col gap-2">
              <div class="flex items-center justify-between">
                <label class="font-semibold text-slate-700 text-sm">Cor do Cartão</label>
                <div class="flex items-center gap-1.5 text-xs text-slate-500 font-mono">
                  <span>#{{ formContaFinanceira.get('cor')?.value }}</span>
                </div>
              </div>

              <div class="flex flex-wrap items-center gap-2">
                @for (cor of coresPredefinidas; track cor.hex) {
                  <button
                    type="button"
                    (click)="selecionarCorRapida(cor.hex)"
                    [title]="cor.nome"
                    [style.backgroundColor]="'#' + cor.hex"
                    class="w-8 h-8 rounded-xl transition-all duration-150 transform hover:scale-110 flex items-center justify-center shadow-xs cursor-pointer border-2"
                    [class.border-[#7843CD]]="formContaFinanceira.get('cor')?.value?.toLowerCase() === cor.hex.toLowerCase()"
                    [class.ring-2]="formContaFinanceira.get('cor')?.value?.toLowerCase() === cor.hex.toLowerCase()"
                    [class.ring-[#D1C4FF]]="formContaFinanceira.get('cor')?.value?.toLowerCase() === cor.hex.toLowerCase()"
                    [class.border-white]="formContaFinanceira.get('cor')?.value?.toLowerCase() !== cor.hex.toLowerCase()">
                    @if (formContaFinanceira.get('cor')?.value?.toLowerCase() === cor.hex.toLowerCase()) {
                      <i class="pi pi-check text-xs text-white drop-shadow-xs"></i>
                    }
                  </button>
                }

                <!-- SELETOR DE COR PERSONALIZADA BEM DESTACADO (NATIVO, SEM QUEBRAR O MODAL) -->
                <div class="relative flex items-center ml-1 pl-2 border-l border-slate-200">
                  <label
                    class="relative flex items-center gap-2 px-3 py-1.5 rounded-xl border-2 border-dashed border-[#D1C4FF] hover:border-[#7843CD] bg-[#F3E8FF]/60 hover:bg-[#F3E8FF] text-[#7843CD] cursor-pointer transition-colors shadow-2xs group"
                    title="Clique para escolher qualquer outra cor">
                    <i class="pi pi-palette text-sm text-[#7843CD] group-hover:scale-110 transition-transform"></i>
                    <span class="text-xs font-bold text-[#7843CD]">Personalizar</span>
                    <input
                      type="color"
                      class="absolute inset-0 opacity-0 w-full h-full cursor-pointer"
                      [value]="'#' + (formContaFinanceira.get('cor')?.value || '7843CD')"
                      (input)="aoSelecionarCorNativa($event)" />
                  </label>
                </div>
              </div>
            </div>

          </div>

          <ng-template pTemplate="footer">
            <div class="flex justify-end gap-2 w-full pt-2">
              <p-button
                label="Cancelar"
                severity="secondary"
                [outlined]="true"
                size="small"
                styleClass="rounded-xl! text-xs px-4"
                (click)="exibirDialog = false">
              </p-button>

              <p-button
                icon="pi pi-check"
                size="small"
                styleClass="rounded-xl! text-xs px-5 btn-npl-primary"
                [label]="modoEdicao ? 'Salvar Alterações' : 'Criar Conta'"
                (click)="salvarConta()">
              </p-button>
            </div>
          </ng-template>

        </p-dialog>

        <!-- DIALOG — CONFIRMAR EXCLUSÃO -->
        <p-dialog
          header="Excluir Conta"
          [(visible)]="excluirDialog"
          [modal]="true"
          [draggable]="false"
          [resizable]="false"
          styleClass="rounded-3xl!"
          [style]="{ width: '32rem', maxWidth: '95vw' }"
          [breakpoints]="{ '640px': '95vw' }">

          <div class="flex flex-col gap-6 py-4">

            <div class="flex items-center gap-4">

              <div class="w-14 h-14 rounded-full bg-red-100 flex items-center justify-center">
                <i class="pi pi-trash text-red-500 text-2xl"></i>
              </div>

              <div class="flex flex-col">
                <span class="text-xl font-semibold text-slate-900">
                  Confirmar exclusão
                </span>
                <span class="text-slate-500">
                  Esta ação não poderá ser desfeita.
                </span>
              </div>

            </div>

            <div class="bg-slate-50 border border-slate-200 rounded-2xl p-5">
              <span class="text-slate-400 text-sm block mb-2">
                Conta selecionada
              </span>
              <span class="text-2xl font-bold text-slate-900">
                {{ contaExcluir?.nome }}
              </span>
            </div>

          </div>

          <ng-template pTemplate="footer">
            <div class="flex justify-end gap-2 w-full pt-2">

              <p-button
                label="Cancelar"
                severity="secondary"
                [outlined]="true"
                size="small"
                styleClass="rounded-xl! text-xs px-4"
                (click)="fecharDialogExcluir()">
              </p-button>

              <p-button
                label="Confirmar Exclusão"
                icon="pi pi-trash"
                severity="danger"
                size="small"
                styleClass="rounded-xl! text-xs px-4 font-semibold"
                (click)="confirmarExcluirConta()">
              </p-button>

            </div>
          </ng-template>

        </p-dialog>

      </form>

    </div>

  `,
})
export class ContasComponent implements OnInit {

  // =========================================
  // DIALOG
  // =========================================

  exibirDialog: boolean = false;
  modoEdicao: boolean = false;
  idContaEdicao: number | null = null;
  excluirDialog: boolean = false;
  contaExcluir: IContas | null = null;
  bancoDetectado: BancoResolvido | null = null;
  corCustomizadaManualmente: boolean = false;

  // =========================================
  // VALORES MONETÁRIOS FLUIDOS (DIGITAÇÃO ESTILO FINTECH)
  // =========================================

  saldoFormatadoExibicao: string = '0,00';
  limiteFormatadoExibicao: string = '';

  readonly coresPredefinidas: { nome: string; hex: string }[] = [
    { nome: 'Nubank', hex: '820AD1' },
    { nome: 'Inter', hex: 'FF7A00' },
    { nome: 'Itaú', hex: 'EC7000' },
    { nome: 'Bradesco', hex: 'CC092F' },
    { nome: 'Santander', hex: 'EA1D2C' },
    { nome: 'Banco do Brasil', hex: '003882' },
    { nome: 'Caixa', hex: '0066B3' },
    { nome: 'C6 Bank', hex: '242424' },
    { nome: 'Mercado Pago', hex: '009EE3' },
    { nome: 'Índigo', hex: '4F46E5' },
    { nome: 'Esmeralda', hex: '059669' },
    { nome: 'Escuro', hex: '1E293B' },
  ];

  // =========================================
  // DADOS
  // =========================================

  carregandoContas: boolean = true;
  sincronizandoOpenFinance: boolean = false;
  openFinanceConectado: boolean = false;
  contaFinanceiraDados: IContas[] = [];
  contaSelecionadaIndex: number = 0;
  contaAnterior: number = 0;
  contaProxima: number = 0;

  // =========================================
  // FORM
  // =========================================

  formContaFinanceira: FormGroup;

  // =========================================
  // GRÁFICO
  // =========================================

  /** Mantido em sync com GraficoComponent via (onPeriodoMudou). */
  periodoSelecionado: string = 'mensal';

  chartData: any;
  chartOptions: any;

  // =========================================
  // ÚLTIMAS TRANSAÇÕES
  // =========================================

  ultimasTransacoes: ITransacoes[] = [];
  paginaAtualTransacoes: number = 0;
  readonly transacoesPorPagina: number = 5;

  // =========================================
  // CONSTRUCTOR
  // =========================================

  constructor(
    private fb: FormBuilder,
    private contaFinanceiraService: ContaFinanceiraService,
    private integracoesService: IntegracoesService,
    private cdr: ChangeDetectorRef,
    private transacoesService: TransacoesService,
    private router: Router,
    private messageService: MessageService,
    private preferenciaMoedaService: PreferenciaMoedaService
  ) {
    this.formContaFinanceira = this.fb.group({
      nome:        ['', [Validators.required, Validators.minLength(3)]],
      saldo:       [0,  [Validators.required]],
      cor:         ['', [Validators.required, Validators.maxLength(7)]],
      moeda:       ['', [Validators.required]],
      tipoConta:   ['CONTA_CORRENTE', [Validators.required]],
      limiteTotal: [null]
    });
  }

  // =========================================
  // INIT
  // =========================================

  ngOnInit(): void {
    this.listarContas();
    this.verificarStatusOpenFinance();
  }

  verificarStatusOpenFinance(): void {
    this.integracoesService.obterStatusOpenFinance().subscribe({
      next: (status) => {
        this.openFinanceConectado = !!status.conectado;
        this.cdr.detectChanges();
      },
      error: () => {
        this.openFinanceConectado = this.contaFinanceiraDados.some(c => c.sincronizada);
        this.cdr.detectChanges();
      }
    });
  }

  // =========================================
  // GETTERS
  // =========================================

  get contaSelecionada(): IContas | null {
    if (!this.contaFinanceiraDados?.length) return null;
    return this.contaFinanceiraDados[this.contaSelecionadaIndex] ?? null;
  }

  get transacoesPaginadas(): ITransacoes[] {
    const inicio = this.paginaAtualTransacoes * this.transacoesPorPagina;
    return this.ultimasTransacoes.slice(inicio, inicio + this.transacoesPorPagina);
  }

  // =========================================
  // CARROSSEL
  // =========================================

  selecionarConta(index: number): void {
    if (!this.contaFinanceiraDados?.length) return;
    this.contaSelecionadaIndex = index;
    this.atualizarIndicesCarrossel();
    this.listarUltimasTransacoes();
  }

  selecionarAnterior(): void {
    if (this.contaFinanceiraDados.length <= 1) return;
    this.contaSelecionadaIndex =
      this.contaSelecionadaIndex === 0
        ? this.contaFinanceiraDados.length - 1
        : this.contaSelecionadaIndex - 1;
    this.atualizarIndicesCarrossel();
    this.listarUltimasTransacoes();
  }

  selecionarProxima(): void {
    if (this.contaFinanceiraDados.length <= 1) return;
    this.contaSelecionadaIndex =
      this.contaSelecionadaIndex === this.contaFinanceiraDados.length - 1
        ? 0
        : this.contaSelecionadaIndex + 1;
    this.atualizarIndicesCarrossel();
    this.listarUltimasTransacoes();
  }

  private atualizarIndicesCarrossel(): void {
    const total = this.contaFinanceiraDados.length;
    if (total <= 1) { this.contaAnterior = 0; this.contaProxima = 0; return; }
    this.contaAnterior =
      this.contaSelecionadaIndex === 0 ? total - 1 : this.contaSelecionadaIndex - 1;
    this.contaProxima =
      this.contaSelecionadaIndex === total - 1 ? 0 : this.contaSelecionadaIndex + 1;
  }

  // =========================================
  // PAGINAÇÃO DE TRANSAÇÕES
  // =========================================

  proximaPaginaTransacoes(): void {
    const totalPaginas = Math.ceil(this.ultimasTransacoes.length / this.transacoesPorPagina);
    if (this.paginaAtualTransacoes < totalPaginas - 1) this.paginaAtualTransacoes++;
  }

  paginaAnteriorTransacoes(): void {
    if (this.paginaAtualTransacoes > 0) this.paginaAtualTransacoes--;
  }

  irParaTransacoes(): void {
    this.router.navigate(['/app/transacoes']);
  }

  // =========================================
  // CRUD — OPÇÕES DE MOEDA
  // =========================================

  moedas: { label: string; value: string }[] = [
    { label: 'R$ — Real brasileiro',  value: 'BRL' },
    { label: 'US$ — Dólar americano', value: 'USD' },
    { label: '€ — Euro',             value: 'EUR' },
  ];

  localeDaMoeda(moeda: Moeda): string {
    switch (moeda) {
      case 'USD': return 'en-US';
      case 'EUR': return 'de-DE';
      default:    return 'pt-BR';
    }
  }

  simboloMoeda(moeda: Moeda): string {
    const simbolos: Record<Moeda, string> = { BRL: 'R$', USD: 'US$', EUR: '€' };
    return simbolos[moeda ?? 'BRL'] ?? moeda ?? '';
  }

  nomeMoeda(moeda: Moeda): string {
    const nomes: Record<Moeda, string> = {
      BRL: 'Real brasileiro',
      USD: 'Dólar americano',
      EUR: 'Euro'
    };
    return nomes[moeda ?? 'BRL'] ?? 'Moeda desconhecida';
  }

  // =========================================
  // DETECÇÃO AUTOMÁTICA DE BANCO E COR
  // =========================================

  aoDigitarNomeConta(): void {
    const nome = this.formContaFinanceira.get('nome')?.value;
    if (!nome || !nome.trim()) {
      this.bancoDetectado = null;
      return;
    }
    const banco = mapearNomeBanco(nome);
    if (banco.chave) {
      this.bancoDetectado = banco;
      if (!this.corCustomizadaManualmente && banco.corFundo) {
        this.formContaFinanceira.get('cor')?.setValue(banco.corFundo.replace('#', ''));
      }
    } else {
      this.bancoDetectado = null;
    }
  }

  aplicarCorOficialBanco(): void {
    if (this.bancoDetectado?.corFundo) {
      this.formContaFinanceira.get('cor')?.setValue(this.bancoDetectado.corFundo.replace('#', ''));
      this.corCustomizadaManualmente = false;
    }
  }

  // =========================================
  // VALORES MONETÁRIOS FLUIDOS (DIGITAÇÃO ESTILO FINTECH)
  // =========================================

  obterSimboloMoeda(moeda?: string): string {
    return this.preferenciaMoedaService.obterSimboloMoeda(moeda);
  }

  selecionarCorRapida(hex: string): void {
    this.formContaFinanceira.get('cor')?.setValue(hex.replace('#', ''));
    this.corCustomizadaManualmente = true;
  }

  aoSelecionarCorNativa(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input?.value) {
      this.selecionarCorRapida(input.value);
    }
  }

  // =========================================
  // PREVIEW DO CARTÃO
  // =========================================

  temBanco(nome?: string | null): boolean {
    return temBancoDetectado(nome);
  }

  ehCartaoPreview(): boolean {
    return this.formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO';
  }

  obterCorPreview(): string {
    const cor = this.formContaFinanceira.get('cor')?.value;
    if (cor && cor !== 'FFDE59') {
      return cor.startsWith('#') ? cor : `#${cor}`;
    }
    const nome = this.formContaFinanceira.get('nome')?.value;
    const corOficial = obterCorOficialBanco(nome);
    if (corOficial) {
      return corOficial;
    }
    return cor ? (cor.startsWith('#') ? cor : `#${cor}`) : '#4F46E5';
  }

  limiteTotalOuDisponivelPreview(): number {
    const limite = Number(this.formContaFinanceira.get('limiteTotal')?.value || 0);
    const saldo = Number(this.formContaFinanceira.get('saldo')?.value || 0);
    return Math.max(0, limite - saldo);
  }

  atualizarStringsExibicaoMonetaria(): void {
    const saldo = this.formContaFinanceira.get('saldo')?.value;
    const limite = this.formContaFinanceira.get('limiteTotal')?.value;
    this.saldoFormatadoExibicao = (typeof saldo === 'number')
      ? saldo.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      : '0,00';
    this.limiteFormatadoExibicao = (typeof limite === 'number' && limite > 0)
      ? limite.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
      : '';
  }

  aoDigitarValorMonetario(event: Event, campo: 'saldo' | 'limiteTotal'): void {
    const input = event.target as HTMLInputElement;
    const digitos = input.value.replace(/\D/g, '');

    if (!digitos) {
      if (campo === 'saldo') {
        this.formContaFinanceira.get('saldo')?.setValue(0);
        this.saldoFormatadoExibicao = '0,00';
        input.value = '0,00';
      } else {
        this.formContaFinanceira.get('limiteTotal')?.setValue(null);
        this.limiteFormatadoExibicao = '';
        input.value = '';
      }
      return;
    }

    const valorNumerico = Number(digitos) / 100;
    this.formContaFinanceira.get(campo)?.setValue(valorNumerico);
    const formatado = valorNumerico.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    input.value = formatado;
    if (campo === 'saldo') {
      this.saldoFormatadoExibicao = formatado;
    } else {
      this.limiteFormatadoExibicao = formatado;
    }
  }

  aoSairDoCampoMonetario(campo: 'saldo' | 'limiteTotal'): void {
    this.atualizarStringsExibicaoMonetaria();
  }

  // =========================================
  // CRUD — ABERTURA DE DIALOGS
  // =========================================

  abrirDialog(): void {
    this.modoEdicao = false;
    this.idContaEdicao = null;
    this.bancoDetectado = null;
    this.corCustomizadaManualmente = false;
    this.formContaFinanceira.get('saldo')?.enable();
    const moedaPadrao = this.preferenciaMoedaService.obterMoedaAtiva();
    this.formContaFinanceira.reset({
      nome: '',
      saldo: 0,
      cor: '4F46E5',
      moeda: moedaPadrao,
      tipoConta: 'CONTA_CORRENTE',
      limiteTotal: null
    });
    this.atualizarStringsExibicaoMonetaria();
    this.exibirDialog = true;
  }

  editarConta(conta: IContas | null): void {
    if (!conta) return;
    this.modoEdicao    = true;
    this.idContaEdicao = conta.id ?? null;
    this.corCustomizadaManualmente = true;
    this.bancoDetectado = conta.nome ? mapearNomeBanco(conta.nome) : null;
    if (conta.sincronizada) {
      this.formContaFinanceira.get('saldo')?.disable();
    } else {
      this.formContaFinanceira.get('saldo')?.enable();
    }
    this.formContaFinanceira.patchValue({
      nome:        conta.nome,
      saldo:       conta.saldo,
      cor:         conta.cor.replace('#', ''),
      moeda:       conta.moeda || this.preferenciaMoedaService.obterMoedaAtiva(),
      tipoConta:   conta.tipoConta ?? 'CONTA_CORRENTE',
      limiteTotal: conta.limiteTotal ?? null
    });
    this.atualizarStringsExibicaoMonetaria();
    this.exibirDialog = true;
  }

  sincronizarOpenFinance(): void {
    if (!this.openFinanceConectado) {
      this.messageService.add({
        severity: 'info',
        summary: 'Conectar Open Finance',
        detail: 'Redirecionando para Integrações para conectar seu banco...',
        life: 2500
      });
      this.router.navigate(['/app/perfil'], { queryParams: { aba: 'integracoes', secao: 'bancos' } });
      return;
    }

    if (this.sincronizandoOpenFinance) return;

    this.sincronizandoOpenFinance = true;
    this.integracoesService.sincronizarOpenFinanceManual().subscribe({
      next: (status) => {
        this.sincronizandoOpenFinance = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Sincronização concluída',
          detail: 'Contas e transações bancárias atualizadas com sucesso via Open Finance!',
          life: 3500
        });
        this.listarContas();
      },
      error: (err) => {
        this.sincronizandoOpenFinance = false;
        if (err.status === 400 || (err.error?.message && err.error.message.includes('Nenhuma conta')) || (err.error?.error && err.error.error.includes('Nenhuma conta'))) {
          this.openFinanceConectado = false;
          this.messageService.add({
            severity: 'info',
            summary: 'Conectar Open Finance',
            detail: 'Nenhuma conta bancária conectada. Redirecionando para Integrações...',
            life: 3000
          });
          setTimeout(() => {
            this.router.navigate(['/app/perfil'], { queryParams: { aba: 'integracoes', secao: 'bancos' } });
          }, 1000);
          return;
        }

        const msg = err.error?.message || err.error?.error || err.error || 'Não foi possível sincronizar agora. Verifique suas credenciais ou limite diário.';
        this.messageService.add({
          severity: 'error',
          summary: 'Erro na sincronização',
          detail: typeof msg === 'string' ? msg : 'Erro ao sincronizar contas.',
          life: 5000
        });
      }
    });
  }

  // =========================================
  // CRUD — SALVAR (CRIAR / EDITAR)
  // =========================================

  salvarConta(): void {
    if (this.formContaFinanceira.invalid) return;

    const formValue = this.formContaFinanceira.getRawValue();
    const moedaFinal = (formValue.moeda || (this.modoEdicao && this.contaSelecionada?.moeda ? this.contaSelecionada.moeda : this.preferenciaMoedaService.obterMoedaAtiva())) as Moeda;
    const payload: IContasRequest = {
      nome:        formValue.nome,
      saldo:       formValue.saldo,
      cor:         `#${String(formValue.cor).replace('#', '')}`,
      moeda:       moedaFinal,
      tipoConta:   formValue.tipoConta,
      limiteTotal: formValue.tipoConta === 'CARTAO_CREDITO' ? formValue.limiteTotal : null,
    };

    this.exibirDialog = false;

    if (this.modoEdicao && this.idContaEdicao) {
      this.contaFinanceiraService.atualizarConta(payload, this.idContaEdicao).subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary:  'Conta atualizada!',
            detail:   `"${payload.nome}" foi salva com sucesso.`,
            life:     3000
          });
          this.modoEdicao    = false;
          this.idContaEdicao = null;
          this.formContaFinanceira.reset({
            nome: '',
            saldo: 0,
            cor: '4F46E5',
            moeda: this.preferenciaMoedaService.obterMoedaAtiva(),
            tipoConta: 'CONTA_CORRENTE',
            limiteTotal: null
          });
          this.atualizarStringsExibicaoMonetaria();
          this.listarContas();
        },
        error: (e: Error) => {
          console.error('Erro ao atualizar conta', e);
          this.messageService.add({
            severity: 'error',
            summary:  'Erro ao atualizar conta',
            detail:   'Verifique os dados e tente novamente.',
            life:     4000
          });
        }
      });
      return;
    }

    this.contaFinanceiraService.adicionarConta(payload).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary:  'Conta criada!',
          detail:   `"${payload.nome}" foi adicionada com sucesso.`,
          life:     3000
        });
        this.formContaFinanceira.reset({
          nome: '',
          saldo: 0,
          cor: '4F46E5',
          moeda: this.preferenciaMoedaService.obterMoedaAtiva(),
          tipoConta: 'CONTA_CORRENTE',
          limiteTotal: null
        });
        this.atualizarStringsExibicaoMonetaria();
        this.listarContas();
      },
      error: (e: Error) => {
        console.error('Erro ao criar conta', e);
        this.messageService.add({
          severity: 'error',
          summary:  'Erro ao criar conta',
          detail:   'Verifique os dados e tente novamente.',
          life:     4000
        });
      }
    });
  }

  // =========================================
  // EXCLUSÃO
  // =========================================

  abrirDialogExcluir(conta: IContas | null): void {
    if (!conta) return;
    this.contaExcluir  = conta;
    this.excluirDialog = true;
  }

  fecharDialogExcluir(): void {
    this.excluirDialog = false;
    this.contaExcluir  = null;
  }

  confirmarExcluirConta(): void {
    if (!this.contaExcluir?.id) return;

    // Capturar id e nome ANTES de fecharDialogExcluir(),
    // pois ela seta this.contaExcluir = null imediatamente.
    const idExcluir    = this.contaExcluir.id;
    const nomeExcluida = this.contaExcluir.nome;
    this.fecharDialogExcluir();

    this.contaFinanceiraService.deletarContaPorId(idExcluir).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary:  'Conta excluída!',
          detail:   `"${nomeExcluida}" foi removida com sucesso.`,
          life:     3000
        });
        this.listarContas();
      },
      error: (e: Error) => {
        console.error('Erro ao excluir conta', e);
        this.messageService.add({
          severity: 'error',
          summary:  'Erro ao excluir conta',
          detail:   'Não foi possível remover a conta. Tente novamente.',
          life:     4000
        });
      }
    });
  }

  // =========================================
  // LISTAGEM
  // =========================================

  listarContas(): void {
    this.carregandoContas = true;
    this.contaFinanceiraService.listarContasUsuarioLogado().subscribe({
      next: (res: IContas[]) => {
        if (!res?.length) {
          this.contaFinanceiraDados  = [];
          this.contaSelecionadaIndex = 0;
          this.contaAnterior         = 0;
          this.contaProxima          = 0;
          this.carregandoContas      = false;
          this.ultimasTransacoes     = [];
          this.atualizarGraficoSaldo();
          this.cdr.detectChanges();
          return;
        }
        this.contaFinanceiraDados  = [...res].sort((a, b) => (a.id ?? 0) - (b.id ?? 0));
        if (this.contaFinanceiraDados.some(c => c.sincronizada)) {
          this.openFinanceConectado = true;
        }
        this.contaSelecionadaIndex = 0;
        this.atualizarIndicesCarrossel();
        this.carregandoContas = false;
        this.listarUltimasTransacoes();
        this.cdr.detectChanges();
      },
      error: (e: Error) => {
        console.error('Erro ao listar contas', e);
        this.carregandoContas = false;
        this.messageService.add({
          severity: 'warn',
          summary:  'Erro ao carregar contas',
          detail:   'Não foi possível carregar suas contas. Recarregue a página.',
          life:     4000
        });
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Carrega todas as transações do usuário, filtra pela conta selecionada
   * e ao final chama atualizarGraficoSaldo() com os dados reais prontos.
   *
   * IMPORTANTE: atualizarGraficoSaldo() NÃO deve ser chamado antes deste
   * método completar, pois depende de ultimasTransacoes estar populado.
   */
  listarUltimasTransacoes(): void {
    const contaSelecionada = this.contaSelecionada;

    if (!contaSelecionada?.id) {
      this.ultimasTransacoes = [];
      this.atualizarGraficoSaldo();
      this.cdr.detectChanges();
      return;
    }

    this.transacoesService.listarTransacoes().subscribe({
      next: (res: ITransacoes[]) => {
        if (!res?.length) {
          this.ultimasTransacoes     = [];
          this.paginaAtualTransacoes = 0;
          this.atualizarGraficoSaldo();
          this.cdr.detectChanges();
          return;
        }

        this.ultimasTransacoes =
          res
            .filter((t: ITransacoes) => t.conta?.id === contaSelecionada.id)
            .sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime());

        this.paginaAtualTransacoes = 0;
        this.atualizarGraficoSaldo();
        this.cdr.detectChanges();
      },
      error: (e: Error) => {
        console.error('Erro ao listar transações', e);
        this.ultimasTransacoes = [];
        this.atualizarGraficoSaldo();
        this.messageService.add({
          severity: 'warn',
          summary:  'Erro ao carregar transações',
          detail:   'Não foi possível carregar o histórico desta conta.',
          life:     3000
        });
        this.cdr.detectChanges();
      }
    });
  }

  // =========================================
  // GRÁFICO — COMPUTAÇÃO DE DADOS
  // =========================================

  /**
   * Ponto central de atualização do gráfico.
   * Chamado sempre APÓS ultimasTransacoes estar populado.
   * Também chamado via (onPeriodoMudou) do GraficoComponent quando o filtro muda.
   */
  atualizarGraficoSaldo(periodo?: string): void {
    if (periodo) this.periodoSelecionado = periodo;

    const saldoAtual = this.contaSelecionada?.saldo ?? 0;

    const { labels, dadosAtuais, dadosAnteriores } =
      this.periodoSelecionado === 'semanal'
        ? this.computarSemanal(this.ultimasTransacoes, saldoAtual)
        : this.computarMensal(this.ultimasTransacoes, saldoAtual);

    this.chartData    = this.montarChartData(labels, dadosAtuais, dadosAnteriores);
    this.chartOptions = this.montarChartOptions();
  }

  private computarSemanal(
    transacoes: ITransacoes[],
    saldoAtual: number
  ): { labels: string[]; dadosAtuais: number[]; dadosAnteriores: number[] } {

    const labels = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];
    const agora  = new Date();

    const diaJs      = agora.getDay();
    const diasAteSeg = diaJs === 0 ? 6 : diaJs - 1;
    const inicioSemAtual = new Date(agora);
    inicioSemAtual.setDate(agora.getDate() - diasAteSeg);
    inicioSemAtual.setHours(0, 0, 0, 0);

    const inicioSemAnterior = new Date(inicioSemAtual);
    inicioSemAnterior.setDate(inicioSemAtual.getDate() - 7);

    const netAtual:    number[] = new Array(7).fill(0);
    const netAnterior: number[] = new Array(7).fill(0);

    for (const t of transacoes) {
      const data = new Date(t.dataHora);
      const net  = t.tipo === 'RECEITA' ? t.valor : -t.valor;

      if (data >= inicioSemAtual) {
        const idx = Math.floor((data.getTime() - inicioSemAtual.getTime()) / 86_400_000);
        if (idx >= 0 && idx < 7) netAtual[idx] += net;
      } else if (data >= inicioSemAnterior) {
        const idx = Math.floor((data.getTime() - inicioSemAnterior.getTime()) / 86_400_000);
        if (idx >= 0 && idx < 7) netAnterior[idx] += net;
      }
    }

    const dadosAtuais     = this.regressivo(netAtual,    saldoAtual);
    const dadosAnteriores = this.regressivo(netAnterior, dadosAtuais[0]);

    return { labels, dadosAtuais, dadosAnteriores };
  }

  private computarMensal(
    transacoes: ITransacoes[],
    saldoAtual: number
  ): { labels: string[]; dadosAtuais: number[]; dadosAnteriores: number[] } {

    const labels      = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez'];
    const anoAtual    = new Date().getFullYear();
    const anoAnterior = anoAtual - 1;

    const netAtual:    number[] = new Array(12).fill(0);
    const netAnterior: number[] = new Array(12).fill(0);

    for (const t of transacoes) {
      const data = new Date(t.dataHora);
      const net  = t.tipo === 'RECEITA' ? t.valor : -t.valor;
      const mes  = data.getMonth();
      const ano  = data.getFullYear();

      if (ano === anoAtual)    netAtual[mes]    += net;
      if (ano === anoAnterior) netAnterior[mes] += net;
    }

    const dadosAtuais     = this.regressivo(netAtual,    saldoAtual);
    const dadosAnteriores = this.regressivo(netAnterior, dadosAtuais[0]);

    return { labels, dadosAtuais, dadosAnteriores };
  }

  private regressivo(net: number[], saldoFinal: number): number[] {
    const out = new Array(net.length).fill(0);
    let s = saldoFinal;
    for (let i = net.length - 1; i >= 0; i--) {
      s = s - net[i];
      out[i] = Math.round(s * 100) / 100;
    }
    return out;
  }

  // =========================================
  // GRÁFICO — MONTAGEM DO OBJETO CHART.JS
  // =========================================

  private montarChartData(
    labels: string[],
    dadosAtuais: number[],
    dadosAnteriores: number[]
  ): any {
    return {
      labels,
      datasets: [
        {
          label: 'Período Atual',
          data: dadosAtuais,
          borderColor: '#7843CD',
          backgroundColor: 'rgba(120,67,205,0.08)',
          fill: true,
          tension: 0.4,
          borderWidth: 2.5,
          pointRadius: 0,
          pointHoverRadius: 6,
          pointHoverBackgroundColor: '#7843CD',
          pointHoverBorderColor: '#ffffff',
          pointHoverBorderWidth: 2,
        },
        {
          label: 'Período Anterior',
          data: dadosAnteriores,
          borderColor: '#EEC046',
          backgroundColor: 'rgba(238,192,70,0.08)',
          fill: true,
          tension: 0.4,
          borderWidth: 2,
          pointRadius: 0,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: '#EEC046',
          pointHoverBorderColor: '#ffffff',
          pointHoverBorderWidth: 2,
        }
      ]
    };
  }

  private montarChartOptions(): any {
    return {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#ffffff',
          titleColor:       '#0f172a',
          bodyColor:        '#64748b',
          borderColor:      '#e2e8f0',
          borderWidth:      1,
          padding:          14,
          cornerRadius:     12,
          boxPadding:       6,
          callbacks: {
            title: (items: any[]) => items[0]?.label ?? '',
            label: (item: any) => {
              const val: number = item.parsed.y;
              const codMoeda = this.preferenciaMoedaService.obterMoedaAtiva();
              const simbolo = this.preferenciaMoedaService.obterSimboloMoeda(codMoeda);
              const fmt = val.toLocaleString('pt-BR', {
                minimumFractionDigits: 0,
                maximumFractionDigits: 2
              });
              return ` ${item.dataset.label}: ${simbolo} ${fmt}`;
            }
          }
        }
      },
      scales: {
        x: {
          grid:   { display: false },
          border: { display: false },
          ticks:  { color: '#94a3b8', font: { size: 12 } }
        },
        y: {
          border: { display: false },
          grid:   { color: 'rgba(226,232,240,0.7)', drawBorder: false },
          ticks: {
            color: '#94a3b8',
            font:  { size: 12 },
            callback: function(val: any): string {
              return Number(val).toLocaleString('pt-BR', { maximumFractionDigits: 0 });
            }
          }
        }
      }
    };
  }

}
