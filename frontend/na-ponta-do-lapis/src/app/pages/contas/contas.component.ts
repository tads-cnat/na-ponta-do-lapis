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
import { mapearNomeBanco, BancoResolvido, obterCorOficialBanco } from '../../shared/utils/banco-icon.helper';

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
    IconeBancoComponent
  ],
  template: `

    <!-- TOAST — deve ficar no nível raiz para sobrepor qualquer conteúdo -->
    <p-toast position="top-right" [life]="3000" />

    <div class="w-full flex flex-col gap-6 pb-8">

      <!-- HEADER PRINCIPAL DA PÁGINA (largura total: título e ações sem competir por espaço) -->
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div class="min-w-0">
          <h1 class="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight">
            Contas
          </h1>
          <p class="text-slate-500 mt-1 text-xs sm:text-sm">
            Gestão de contas financeiras
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

          <div class="flex flex-col gap-8 py-4">

            <!-- TIPO DE CONTA -->
            <div class="flex flex-col gap-2">
              <label class="font-semibold text-slate-700">Tipo de Conta</label>
              <div class="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  (click)="formContaFinanceira.get('tipoConta')?.setValue('CONTA_CORRENTE')"
                  [class]="formContaFinanceira.get('tipoConta')?.value === 'CONTA_CORRENTE'
                    ? 'flex items-center gap-3 p-3.5 rounded-xl border-2 border-indigo-600 bg-indigo-50/60 text-indigo-900 font-semibold transition-all'
                    : 'flex items-center gap-3 p-3.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-medium transition-all'">
                  <i class="pi pi-wallet text-xl" [class.text-indigo-600]="formContaFinanceira.get('tipoConta')?.value === 'CONTA_CORRENTE'"></i>
                  <div class="flex flex-col text-left">
                    <span class="text-sm leading-tight">Conta Bancária</span>
                    <span class="text-xs text-slate-500 font-normal">Corrente ou Carteira</span>
                  </div>
                </button>

                <button
                  type="button"
                  (click)="formContaFinanceira.get('tipoConta')?.setValue('CARTAO_CREDITO')"
                  [class]="formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO'
                    ? 'flex items-center gap-3 p-3.5 rounded-xl border-2 border-indigo-600 bg-indigo-50/60 text-indigo-900 font-semibold transition-all'
                    : 'flex items-center gap-3 p-3.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-medium transition-all'">
                  <i class="pi pi-credit-card text-xl" [class.text-indigo-600]="formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO'"></i>
                  <div class="flex flex-col text-left">
                    <span class="text-sm leading-tight">Cartão de Crédito</span>
                    <span class="text-xs text-slate-500 font-normal">Fatura e limites</span>
                  </div>
                </button>
              </div>
            </div>

            <!-- NOME -->
            <div class="flex flex-col gap-2">
              <label class="font-semibold text-slate-700">Nome da Conta</label>
              <input
                pInputText
                formControlName="nome"
                (input)="aoDigitarNomeConta()"
                placeholder="Ex: Nubank, Santander, Inter, Itaú..."
                class="w-full" />

              @if (bancoDetectado?.chave) {
                <div class="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-200/80 text-xs text-slate-700 animate-in fade-in">
                  <div class="flex items-center gap-2 min-w-0">
                    <app-icone-banco [nome]="bancoDetectado?.nomeExibicao" [tamanho]="26" [formato]="'quadrado'"></app-icone-banco>
                    <span class="truncate">Banco detectado: <strong>{{ bancoDetectado?.nomeExibicao }}</strong></span>
                  </div>
                  <button
                    type="button"
                    (click)="aplicarCorOficialBanco()"
                    class="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center gap-1.5 shrink-0 cursor-pointer">
                    <span class="w-3.5 h-3.5 rounded-full border border-slate-300 shadow-2xs" [style.backgroundColor]="'#' + formContaFinanceira.get('cor')?.value"></span>
                    <span>Cor oficial aplicada</span>
                  </button>
                </div>
              }
            </div>

            <!-- SALDO -->
            <div class="flex flex-col gap-2">
              <label class="font-semibold text-slate-700">
                {{ formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO' ? 'Fatura Atual / Saldo Devedor' : 'Saldo Inicial' }}
              </label>
              <p-inputNumber
                  formControlName="saldo"
                  locale="pt-BR"
                  mode="decimal"
                  [minFractionDigits]="2"
                  [maxFractionDigits]="2">
              </p-inputNumber>
              @if (formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO') {
                <span class="text-xs text-slate-500">Valor gasto na fatura atual (use 0 se não houver gastos pendentes).</span>
              }
              @if (modoEdicao && contaSelecionada?.sincronizada) {
                <div class="p-3 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-800 flex items-center gap-2">
                  <span>ℹ️</span>
                  <span>Esta conta é sincronizada via Open Finance. O saldo é atualizado automaticamente pelo banco.</span>
                </div>
              }
            </div>

            <!-- LIMITE TOTAL (Cartão de Crédito) -->
            @if (formContaFinanceira.get('tipoConta')?.value === 'CARTAO_CREDITO') {
              <div class="flex flex-col gap-2">
                <label class="font-semibold text-slate-700">Limite Total do Cartão</label>
                <p-inputNumber
                  formControlName="limiteTotal"
                  locale="pt-BR"
                  mode="decimal"
                  placeholder="Ex: 5000,00"
                  [minFractionDigits]="2"
                  [maxFractionDigits]="2">
                </p-inputNumber>
                <span class="text-xs text-slate-500">Limite concedido para acompanhamento do limite disponível.</span>
              </div>
            }

            <!-- COR -->
            <div class="flex flex-col gap-3">
              <div class="flex items-center justify-between">
                <label class="font-semibold text-slate-700">Cor do cartão</label>
                <div class="flex items-center gap-2">
                  <span class="w-5 h-5 rounded-lg border border-slate-300 shadow-2xs inline-block" [style.backgroundColor]="'#' + formContaFinanceira.get('cor')?.value"></span>
                  <span class="text-xs font-mono text-slate-500">#{{ formContaFinanceira.get('cor')?.value }}</span>
                </div>
              </div>
              <p-colorpicker
                formControlName="cor"
                (onChange)="corCustomizadaManualmente = true">
              </p-colorpicker>
            </div>

            <!-- MOEDA -->
            <div class="bg-slate-50 border border-slate-100 rounded-2xl p-5">

              <span class="text-slate-700 font-bold">
                Moeda
              </span>

              <div class="mt-3 flex flex-col">
                <p-select
                  [options]="moedas"
                  optionLabel="label"
                  optionValue="value"
                  formControlName="moeda"
                  formControlName="moeda"
                  placeholder="Selecione a moeda"
                  [appendTo]="'body'">
                </p-select>
              </div>

            </div>

          </div>

          <ng-template pTemplate="footer">
            <div class="flex justify-end gap-3 w-full">

              <p-button
                label="Cancelar"
                severity="secondary"
                (click)="exibirDialog = false">
              </p-button>

              <p-button
                icon="pi pi-check"
                severity="success"
                label="Salvar Conta"
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
            <div class="flex justify-end gap-3 w-full">

              <p-button
                label="Cancelar"
                severity="secondary"
                (click)="fecharDialogExcluir()">
              </p-button>

              <p-button
                label="Confirmar Exclusão"
                icon="pi pi-trash"
                severity="danger"
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
    private messageService: MessageService
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
    if (!this.contaFinanceiraDados.length) return;
    this.contaSelecionadaIndex =
      this.contaSelecionadaIndex === 0
        ? this.contaFinanceiraDados.length - 1
        : this.contaSelecionadaIndex - 1;
    this.atualizarIndicesCarrossel();
    this.listarUltimasTransacoes();
  }

  selecionarProxima(): void {
    if (!this.contaFinanceiraDados.length) return;
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
  // CRUD — ABERTURA DE DIALOGS
  // =========================================

  abrirDialog(): void {
    this.modoEdicao = false;
    this.idContaEdicao = null;
    this.bancoDetectado = null;
    this.corCustomizadaManualmente = false;
    this.formContaFinanceira.get('saldo')?.enable();
    this.formContaFinanceira.reset({
      nome: '',
      saldo: 0,
      cor: '4F46E5',
      moeda: 'BRL',
      tipoConta: 'CONTA_CORRENTE',
      limiteTotal: null
    });
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
      moeda:       conta.moeda,
      tipoConta:   conta.tipoConta ?? 'CONTA_CORRENTE',
      limiteTotal: conta.limiteTotal ?? null
    });
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
    const payload: IContasRequest = {
      nome:        formValue.nome,
      saldo:       formValue.saldo,
      cor:         `#${String(formValue.cor).replace('#', '')}`,
      moeda:       formValue.moeda,
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
            cor: 'FFDE59',
            moeda: 'BRL',
            tipoConta: 'CONTA_CORRENTE',
            limiteTotal: null
          });
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
          cor: 'FFDE59',
          moeda: 'BRL',
          tipoConta: 'CONTA_CORRENTE',
          limiteTotal: null
        });
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
          borderColor: '#f97316',
          backgroundColor: 'rgba(249,115,22,0.08)',
          fill: true,
          tension: 0.4,
          borderWidth: 2.5,
          pointRadius: 0,
          pointHoverRadius: 6,
          pointHoverBackgroundColor: '#f97316',
          pointHoverBorderColor: '#ffffff',
          pointHoverBorderWidth: 2,
        },
        {
          label: 'Período Anterior',
          data: dadosAnteriores,
          borderColor: 'rgba(249,115,22,0.35)',
          backgroundColor: 'rgba(249,115,22,0.04)',
          fill: true,
          tension: 0.4,
          borderWidth: 2,
          pointRadius: 0,
          pointHoverRadius: 5,
          pointHoverBackgroundColor: 'rgba(249,115,22,0.6)',
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
              const fmt = val.toLocaleString('pt-BR', {
                style: 'currency', currency: 'BRL', maximumFractionDigits: 0
              });
              return ` ${item.dataset.label}: ${fmt}`;
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
