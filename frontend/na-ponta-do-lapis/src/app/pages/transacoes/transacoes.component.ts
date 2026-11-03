import { ChangeDetectorRef, Component, DestroyRef, inject, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { TransacoesService } from './service/transacoes.service';
import {
  Categoria,
  FaturaAnaliseOpcoes,
  FaturaAnaliseResponse,
  FaturaErro,
  ITransacaoFatura,
  ITransacaoRequest,
  ITransacoes,
  ProvedorIa,
  Tipo,
} from '../../model/ITransacoes.model';
import { IContasRequest } from '../../model/IContas.models';
import { Marcador, MarcadorRequest } from '../../model/IMarcador.models';
import { Popover } from 'primeng/popover';
import { MessageService } from 'primeng/api';
import { CardSaldoComponent } from '../../shared/components/card-saldo/card-saldo.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { MarcadorDlgComponent } from '../marcador-dlg/marcador-dlg.component';

interface GrupoParcelamentoResumo {
  grupoId: string;
  descricao: string;
  numeroParcela: number;
  totalParcelas: number;
  valorTotalCompra: number | null;
}

@Component({
  selector: 'app-transacoes',
  imports: [CardSaldoComponent, MarcadorDlgComponent, PrimeNGModuleModule, CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './transacoes.component.html',
  providers: [MessageService],
  styleUrl: './transacoes.component.css',
})
export class TransacoesComponent {
  exibirDialog: boolean = false;
  exibirDialogMarcador: boolean = false;
  exibirDialogEscolha: boolean = false;
  exibirDialogUpload: boolean = false;
  exibirDialogRevisao: boolean = false;
  exibirDialogConsentimento: boolean = false;
  exibirDialogGrupo: boolean = false;
  exibirDialogConfirmarPagamento: boolean = false;
  transacaoParaConfirmar: ITransacoes | null = null;
  carregandoConfirmacao: boolean = false;
  exibirDialogExcluirParcelamento: boolean = false;
  excluirApenasFuturas: boolean = false;
  carregandoExclusao: boolean = false;
  transacoesMarcadas: ITransacoes[] = [];
  exibirDialogExcluirSelecionadas: boolean = false;
  carregandoExclusaoSelecionadas: boolean = false;
  transacaoParaExcluir: ITransacoes | null = null;
  exibirDialogExcluirIndividual: boolean = false;
  carregandoExclusaoIndividual: boolean = false;
  carregando: boolean = false;
  carregandoConsentimento: boolean = false;
  carregandoGrupo: boolean = false;
  editandoParcelada: boolean = false;
  erroUpload: string | null = null;
  codigoErroUpload: string | null = null;
  validandoArquivo: boolean = false;
  faturaPreviaValida: boolean = false;
  emissorDetectado: string | null = null;
  temChaveSalva: boolean = false;
  provedorSalvo: ProvedorIa | null = null;
  carregandoSessaoIa: boolean = false;
  erroConsentimento: string | null = null;
  arquivoSelecionado: File | null = null;

  get podeAnalisarFatura(): boolean {
    if (!this.arquivoSelecionado || this.carregando || this.validandoArquivo) {
      return false;
    }
    if (this.codigoErroUpload === 'DOCUMENTO_NAO_EH_FATURA' || !this.faturaPreviaValida) {
      return false;
    }
    return this.temChaveSalva || !!this.apiKeyIa?.trim();
  }

  projetarParcelasFuturas: boolean = true;

  get temParcelasNaRevisao(): boolean {
    return this.transacoesExtraidas.some(
      t => t.selecionada && !!t.numeroParcela && !!t.totalParcelas && t.numeroParcela < t.totalParcelas
    );
  }

  get totalParcelasEsperadas(): number {
    return this.parcelasGrupo[0]?.totalParcelas ?? this.parcelasGrupo.length;
  }

  get numerosParcelasCadastradas(): number[] {
    return this.parcelasGrupo
      .map(p => p.numeroParcela || 0)
      .filter(n => n > 0);
  }

  get menorParcelaCadastrada(): number {
    const nums = this.numerosParcelasCadastradas;
    return nums.length > 0 ? Math.min(...nums) : 1;
  }

  get maiorParcelaCadastrada(): number {
    const nums = this.numerosParcelasCadastradas;
    return nums.length > 0 ? Math.max(...nums) : 0;
  }

  get qtdParcelasFaltantes(): number {
    const total = this.totalParcelasEsperadas;
    return Math.max(0, total - this.parcelasGrupo.length);
  }

  get qtdFuturasFaltantes(): number {
    const total = this.totalParcelasEsperadas;
    if (this.maiorParcelaCadastrada >= total) return 0;
    return Math.max(0, total - this.maiorParcelaCadastrada);
  }

  get qtdAnterioresFaltantes(): number {
    if (this.menorParcelaCadastrada <= 1) return 0;
    return this.menorParcelaCadastrada - 1;
  }

  get temParcelasPendentesNoGrupo(): boolean {
    return this.parcelasGrupo.some(p => p.estado === 'PENDENTE');
  }

  get proximaParcelaPendente(): ITransacoes | null {
    const pendentes = this.parcelasGrupo
      .filter(p => p.estado === 'PENDENTE')
      .sort((a, b) => (a.numeroParcela || 0) - (b.numeroParcela || 0));
    return pendentes[0] || null;
  }
  transacoesExtraidas: ITransacaoFatura[] = [];
  contaSelecionadaRevisao: number | null = null;
  faturaAnalise: FaturaAnaliseResponse | null = null;
  etapaAnalise: string = 'Lendo páginas do arquivo...';
  private intervaloEtapas: any = null;
  faturaPendenteConsentimento: FaturaAnaliseResponse | null = null;
  precisaAtivarIa: boolean = false;
  precisaSenhaPdf: boolean = false;
  senhaPdf: string = '';
  qtdDuplicatasDetectadas: number = 0;

  provedorSelecionado: ProvedorIa = 'GEMINI';
  apiKeyIa: string = '';
  apiKeyIaTocada: boolean = false;
  manterNaSessao: boolean = false;
  usarIa: boolean = true;
  consentimentoLgpd: boolean = false;

  gruposParcelamentoCriados: GrupoParcelamentoResumo[] = [];
  gruposGerados = new Set<string>();

  grupoSelecionado: string | null = null;
  parcelasGrupo: ITransacoes[] = [];

  private messageService = inject(MessageService)
  private destroyRef = inject(DestroyRef)
  id: number | null = null;
  @ViewChild(CardSaldoComponent) cardSaldo?: CardSaldoComponent;
  formTransacao: FormGroup;
  constructor(
    private fb: FormBuilder,
    private transacoesService: TransacoesService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.formTransacao = this.fb.group({
      descricao: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      idCategoria: ['', [Validators.required]],
      valor: [0, [Validators.required, Validators.min(0.01)]],
      idContaFinanceira: ['', [Validators.required]],
      dataHora: ['', [Validators.required]],
      estado: ['', [Validators.required]],
      tipo: ['', [Validators.required]],
      marcadorId: [''],
      parcelado: [false],
      totalParcelas: [2],
      numeroParcela: [1],
      valorTotalCompra: [null],
      criarParcelasAnteriores: [true]
    }, {
      validators: [this.validarParcelamento]
    })

    this.formTransacao.get('parcelado')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((parcelado: boolean) => this.ajustarValidacaoParcelas(parcelado));

    this.formTransacao.get('totalParcelas')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((novoTotal: number) => {
        const atual = this.formTransacao.get('numeroParcela')?.value;
        if (novoTotal && atual && Number(atual) > Number(novoTotal)) {
          this.formTransacao.get('numeroParcela')?.setValue(Number(novoTotal));
        }
        this.formTransacao.updateValueAndValidity({ emitEvent: false });
      });

    this.formTransacao.get('numeroParcela')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.formTransacao.updateValueAndValidity({ emitEvent: false });
      });
  }

  validarParcelamento = (control: AbstractControl): ValidationErrors | null => {
    const parcelado = control.get('parcelado')?.value;
    const numeroControl = control.get('numeroParcela');
    if (!parcelado) {
      if (numeroControl?.hasError('numeroMaiorQueTotal')) {
        const errors = { ...numeroControl.errors };
        delete errors['numeroMaiorQueTotal'];
        numeroControl.setErrors(Object.keys(errors).length ? errors : null);
      }
      return null;
    }

    const numero = numeroControl?.value;
    const total = control.get('totalParcelas')?.value;

    if (numero != null && total != null && Number(numero) > Number(total)) {
      numeroControl?.setErrors({ ...(numeroControl.errors || {}), numeroMaiorQueTotal: true });
      return { numeroMaiorQueTotal: true };
    } else {
      if (numeroControl?.hasError('numeroMaiorQueTotal')) {
        const errors = { ...numeroControl.errors };
        delete errors['numeroMaiorQueTotal'];
        numeroControl.setErrors(Object.keys(errors).length ? errors : null);
      }
    }
    return null;
  };

  /** Parcelas só são obrigatórias quando a transação é parcelada. */
  private ajustarValidacaoParcelas(parcelado: boolean) {
    const total = this.formTransacao.get('totalParcelas');
    const atual = this.formTransacao.get('numeroParcela');
    if (!total || !atual) return;
    total.setValidators(parcelado
      ? [Validators.required, Validators.min(2), Validators.max(60)]
      : []);
    atual.setValidators(parcelado ? [Validators.required, Validators.min(1)] : []);
    total.updateValueAndValidity({ emitEvent: false });
    atual.updateValueAndValidity({ emitEvent: false });
    this.formTransacao.updateValueAndValidity({ emitEvent: false });
  }

  ngOnInit(): void {
    this.listarTransacoes()
    this.listarContas()
    this.listarCategorias()
    this.listarMarcadores()

    this.route.queryParamMap
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((params) => {
        const openDialog = params.get('openDialog');
        if (openDialog) {
          void this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {},
            replaceUrl: true,
          });

          if (openDialog === 'escolha') {
            this.abrirDialogEscolha();
          } else {
            this.abrirDialog();
          }
        }
      });
  }
  transacoesTodas: ITransacoes[] = [];
  transacoesDados: ITransacoes[] = [];

  // Filtros e ordenação
  painelFiltrosAberto: boolean = false;
  textoBusca: string = '';
  filtroTipo: 'TODOS' | 'RECEITA' | 'DESPESA' = 'TODOS';
  filtroEstado: 'TODOS' | 'REALIZADA' | 'PENDENTE' = 'TODOS';
  filtroContaId: number | null = null;
  filtroCategoriaId: number | null = null;
  filtroMarcadorId: number | null = null;
  filtroPeriodoRapido: 'TODOS' | 'MES_ATUAL' | 'MES_ANTERIOR' | 'ULTIMOS_30' | 'ANO_ATUAL' | 'CUSTOM' = 'TODOS';
  filtroDataRange: Date[] | null = null;

  opcoesFiltroTipo = [
    { label: 'Todos os tipos', value: 'TODOS' },
    { label: 'Despesas', value: 'DESPESA' },
    { label: 'Receitas', value: 'RECEITA' }
  ];

  opcoesFiltroEstado = [
    { label: 'Todos os estados', value: 'TODOS' },
    { label: 'Pagas / Realizadas', value: 'REALIZADA' },
    { label: 'Pendentes', value: 'PENDENTE' }
  ];

  get opcoesFiltroConta(): { id: number | null; nome: string }[] {
    const contas = (this.opcoesConta as any[]).map(c => ({ id: c.id as number, nome: c.nome as string }));
    return [{ id: null, nome: 'Todas as contas' }, ...contas];
  }

  get opcoesFiltroCategoria(): { id: number | null; nome: string }[] {
    const cats = this.opcoesCategoria.map(c => ({ id: c.id, nome: c.nome }));
    return [{ id: null, nome: 'Todas as categorias' }, ...cats];
  }

  get opcoesFiltroMarcador(): { id: number | null; nome: string; cor?: string }[] {
    const marcs = this.opcoesMarcador.map(m => ({ id: m.id, nome: m.nome, cor: m.cor }));
    return [{ id: null, nome: 'Todos os marcadores' }, ...marcs];
  }

  get marcadorFiltroSelecionado(): { id: number | null; nome: string; cor?: string } {
    return this.opcoesFiltroMarcador.find(m => m.id === this.filtroMarcadorId)
      ?? { id: null, nome: 'Todos os marcadores' };
  }

  get qtdFiltrosAtivos(): number {
    let count = 0;
    if (this.filtroTipo !== 'TODOS') count++;
    if (this.filtroEstado !== 'TODOS') count++;
    if (this.filtroContaId !== null) count++;
    if (this.filtroCategoriaId !== null) count++;
    if (this.filtroMarcadorId !== null) count++;
    if (this.filtroPeriodoRapido !== 'TODOS') count++;
    return count;
  }

  get totalReceitasFiltradas(): number {
    return this.transacoesDados
      .filter(t => t.tipo === 'RECEITA')
      .reduce((acc, t) => acc + (Number(t.valor) || 0), 0);
  }

  get totalDespesasFiltradas(): number {
    return this.transacoesDados
      .filter(t => t.tipo === 'DESPESA')
      .reduce((acc, t) => acc + (Number(t.valor) || 0), 0);
  }

  get saldoFiltrado(): number {
    return this.totalReceitasFiltradas - this.totalDespesasFiltradas;
  }

  get totalValorSelecionadas(): number {
    return this.transacoesMarcadas.reduce((acc, t) => acc + (Number(t.valor) || 0), 0);
  }

  alternarPainelFiltros() {
    this.painelFiltrosAberto = !this.painelFiltrosAberto;
  }

  selecionarPeriodoRapido(periodo: 'TODOS' | 'MES_ATUAL' | 'MES_ANTERIOR' | 'ULTIMOS_30' | 'ANO_ATUAL') {
    this.filtroPeriodoRapido = periodo;
    const hoje = new Date();

    if (periodo === 'TODOS') {
      this.filtroDataRange = null;
    } else if (periodo === 'MES_ATUAL') {
      const inicio = new Date(hoje.getFullYear(), hoje.getMonth(), 1);
      const fim = new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0, 23, 59, 59);
      this.filtroDataRange = [inicio, fim];
    } else if (periodo === 'MES_ANTERIOR') {
      const inicio = new Date(hoje.getFullYear(), hoje.getMonth() - 1, 1);
      const fim = new Date(hoje.getFullYear(), hoje.getMonth(), 0, 23, 59, 59);
      this.filtroDataRange = [inicio, fim];
    } else if (periodo === 'ULTIMOS_30') {
      const inicio = new Date();
      inicio.setDate(inicio.getDate() - 30);
      inicio.setHours(0, 0, 0, 0);
      const fim = new Date();
      fim.setHours(23, 59, 59, 999);
      this.filtroDataRange = [inicio, fim];
    } else if (periodo === 'ANO_ATUAL') {
      const inicio = new Date(hoje.getFullYear(), 0, 1);
      const fim = new Date(hoje.getFullYear(), 11, 31, 23, 59, 59);
      this.filtroDataRange = [inicio, fim];
    }
    this.aplicarFiltros();
  }

  aoMudarDataRange() {
    this.filtroPeriodoRapido = 'CUSTOM';
    this.aplicarFiltros();
  }

  limparFiltros() {
    this.textoBusca = '';
    this.filtroTipo = 'TODOS';
    this.filtroEstado = 'TODOS';
    this.filtroContaId = null;
    this.filtroCategoriaId = null;
    this.filtroMarcadorId = null;
    this.filtroPeriodoRapido = 'TODOS';
    this.filtroDataRange = null;
    this.aplicarFiltros();
  }

  public abrirDialogMarcador() {
    this.exibirDialogMarcador = true
  }

  public aoFecharDialogMarcador() {
    this.exibirDialogMarcador = false;
    this.listarMarcadores();
  }

  public buscarPorDescricao(descricao: string): void {
    this.textoBusca = descricao;
    this.aplicarFiltros();
  }

  public aplicarFiltros(): void {
    let lista = [...this.transacoesTodas];

    // Busca textual inteligente (descrição, categoria, conta, marcador, valor)
    if (this.textoBusca && this.textoBusca.trim()) {
      const termo = this.textoBusca.toLowerCase().trim();
      lista = lista.filter(t => {
        const desc = (t.descricao || '').toLowerCase();
        const cat = (t.categoria?.nome || '').toLowerCase();
        const conta = (t.conta?.nome || '').toLowerCase();
        const marcador = (t.marcador?.nome || '').toLowerCase();
        const valorStr = String(t.valor || '');
        return desc.includes(termo) ||
               cat.includes(termo) ||
               conta.includes(termo) ||
               marcador.includes(termo) ||
               valorStr.includes(termo);
      });
    }

    // Tipo
    if (this.filtroTipo !== 'TODOS') {
      lista = lista.filter(t => t.tipo === this.filtroTipo);
    }

    // Estado
    if (this.filtroEstado !== 'TODOS') {
      lista = lista.filter(t => t.estado === this.filtroEstado);
    }

    // Conta
    if (this.filtroContaId !== null) {
      lista = lista.filter(t => t.conta?.id === this.filtroContaId);
    }

    // Categoria
    if (this.filtroCategoriaId !== null) {
      lista = lista.filter(t => t.categoria?.id === this.filtroCategoriaId);
    }

    // Marcador
    if (this.filtroMarcadorId !== null) {
      lista = lista.filter(t => t.marcador?.id === this.filtroMarcadorId);
    }

    // Período
    if (this.filtroDataRange && this.filtroDataRange.length > 0) {
      const inicio = this.filtroDataRange[0];
      const fim = this.filtroDataRange[1] || inicio;

      if (inicio) {
        const dInicio = new Date(inicio);
        dInicio.setHours(0, 0, 0, 0);
        const dFim = new Date(fim);
        dFim.setHours(23, 59, 59, 999);

        lista = lista.filter(t => {
          if (!t.dataHora) return false;
          const dataTransacao = new Date(t.dataHora);
          return dataTransacao >= dInicio && dataTransacao <= dFim;
        });
      }
    }

    this.transacoesDados = lista;
    this.cdr.detectChanges();
  }

  public listarTransacoes(): any {
    this.transacoesService.listarTransacoes().subscribe({
      next: (res: ITransacoes[]) => {
        this.transacoesTodas = res;
        if (this.transacoesMarcadas.length > 0) {
          const idsExistentes = new Set(res.map(r => r.id));
          this.transacoesMarcadas = this.transacoesMarcadas.filter(m => m.id && idsExistentes.has(m.id));
        }
        this.aplicarFiltros();
      },
      error: (erro: Error) => {
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao carregar transações',
          detail: '',
          life: 2000
        });
      }
    })
  }

  public abrirConfirmacaoExcluirSelecionadas(): void {
    if (this.transacoesMarcadas.length === 0) return;
    this.exibirDialogExcluirSelecionadas = true;
  }

  public fecharConfirmacaoExcluirSelecionadas(): void {
    if (this.carregandoExclusaoSelecionadas) return;
    this.exibirDialogExcluirSelecionadas = false;
  }

  public limparSelecao(): void {
    this.transacoesMarcadas = [];
  }

  public executarExclusaoSelecionadas(): void {
    if (this.transacoesMarcadas.length === 0) return;

    this.carregandoExclusaoSelecionadas = true;
    const idsParaExcluir = this.transacoesMarcadas
      .map(t => t.id)
      .filter((id): id is number => id !== undefined && id !== null);

    if (idsParaExcluir.length === 0) {
      this.carregandoExclusaoSelecionadas = false;
      this.exibirDialogExcluirSelecionadas = false;
      this.transacoesMarcadas = [];
      return;
    }

    this.transacoesService.deletarTransacoesEmLote(idsParaExcluir).subscribe({
      next: () => {
        const qtd = idsParaExcluir.length;
        this.messageService.add({
          severity: 'success',
          summary: 'Exclusão concluída',
          detail: `${qtd} movimentaç${qtd > 1 ? 'ões excluídas' : 'ão excluída'}.`,
          life: 2500
        });
        this.carregandoExclusaoSelecionadas = false;
        this.exibirDialogExcluirSelecionadas = false;
        this.transacoesMarcadas = [];
        this.listarTransacoes();
        this.atualizarSaldo();
      },
      error: (erro) => {
        console.error('Erro ao excluir movimentações em lote', erro);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro na exclusão em lote',
          detail: 'Não foi possível excluir os registros selecionados.',
          life: 3500
        });
        this.carregandoExclusaoSelecionadas = false;
        this.listarTransacoes();
        this.atualizarSaldo();
      }
    });
  }

  public abrirConfirmacaoExcluir(t: ITransacoes): void {
    this.transacaoParaExcluir = t;
    this.exibirDialogExcluirIndividual = true;
  }

  public fecharConfirmacaoExcluir(): void {
    if (this.carregandoExclusaoIndividual) return;
    this.transacaoParaExcluir = null;
    this.exibirDialogExcluirIndividual = false;
  }

  public confirmarExclusaoIndividual(): void {
    if (!this.transacaoParaExcluir?.id) return;
    const id = this.transacaoParaExcluir.id;
    this.carregandoExclusaoIndividual = true;

    this.transacoesService.deletarTransacaoPorId(id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Movimentação excluída',
          life: 2500
        });
        this.carregandoExclusaoIndividual = false;
        this.fecharConfirmacaoExcluir();
        this.listarTransacoes();
        this.atualizarSaldo();
      },
      error: (erro) => {
        console.error('Erro ao deletar transação', erro);
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao excluir movimentação',
          life: 3000
        });
        this.carregandoExclusaoIndividual = false;
      }
    });
  }

  public excluir(id: number): void {
    const t = this.transacoesDados.find(item => item.id === id);
    if (t) {
      this.abrirConfirmacaoExcluir(t);
    } else {
      this.transacaoParaExcluir = { id } as ITransacoes;
      this.confirmarExclusaoIndividual();
    }
  }


  novaTransacao: ITransacoes = this.resetForm();
  opcoesConta: IContasRequest[] = [];

  public listarContas() {
    this.transacoesService.listarContasUsuarioLogado().subscribe({
      next: (res: IContasRequest[]) => {
        this.opcoesConta = res
      },
      error: (error: Error) => {
        console.error("Erro ao listar Contas Financeiras", error)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao carregar as transações',
          detail: ``,
          life: 2000
        });
      }
    })
  }

  opcoesCategoria: Categoria[] = []
  public listarCategorias() {
    this.transacoesService.listarCategorias().subscribe({
      next: (res: Categoria[]) => {
        this.opcoesCategoria = res
      },
      error: (error: Error) => {
        console.error("Erro ao listar Categorias", error)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao carregar as categorias',
          detail: ``,
          life: 2000
        });
      }
    })
  }

  marcadorSelecionado: any = null
  opcoesMarcador: Marcador[] = []
  @ViewChild('op') op!: Popover;
  toggle(event: any) {
    this.op.toggle(event);
  }
  selecionarMarcador(marcador: Marcador) {
    this.marcadorSelecionado = marcador
    this.formTransacao.patchValue({
      marcadorId: marcador.id
    })
    this.op.hide();
    this.formTransacao.get('marcadorId')?.markAsDirty();
  }

  public listarMarcadores() {
    this.transacoesService.listarMarcadores().subscribe({
      next: (res: Marcador[]) => {
        this.opcoesMarcador = res
      },
      error: (error: Error) => {
        console.error("Erro a listar Marcadores", error)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao carregar os marcadores',
          detail: ``,
          life: 2000
        });
      }
    })
  }

  opcoesEstado: any[] = [
    { label: 'Realizada', value: 'REALIZADA' },
    { label: 'Pendente', value: 'PENDENTE' }
  ]
  opcoesTipo: any[] = [
    { label: 'Despesa', value: 'DESPESA' },
    { label: 'Receita', value: 'RECEITA' }
  ];

  opcoesProvedorIa: { label: string; value: ProvedorIa; suportaPdf: boolean }[] = [
    { label: 'Gemini', value: 'GEMINI', suportaPdf: true },
    { label: 'Claude', value: 'CLAUDE', suportaPdf: true },
    { label: 'OpenAI', value: 'OPENAI', suportaPdf: false },
    { label: 'DeepSeek', value: 'DEEPSEEK', suportaPdf: false },
    { label: 'Groq', value: 'GROQ', suportaPdf: false },
  ];

  get opcoesProvedorPdf() {
    return this.opcoesProvedorIa.filter(p => p.suportaPdf);
  }

  get provedorSelecionadoSuportaPdf(): boolean {
    return this.opcoesProvedorIa.find(p => p.value === this.provedorSelecionado)?.suportaPdf ?? false;
  }

  get tutorialApiKey(): {
    provedorNome: string;
    url: string;
    labelLink: string;
    resumoTooltip: string;
    passos: string[];
  } {
    switch (this.provedorSelecionado) {
      case 'GEMINI':
        return {
          provedorNome: 'Google Gemini',
          url: 'https://aistudio.google.com/app/apikey',
          labelLink: 'Obter chave gratuita',
          resumoTooltip: 'Passo a passo rápido:\n1. Acesse o Google AI Studio com sua conta Google\n2. Clique em "Criar chave de API" (ou "Get API key")\n3. Copie o código da chave gerada',
          passos: [
            'Acesse o Google AI Studio com sua conta Google.',
            'Clique no botão "Criar chave de API" (ou "Get API key" / "Create API key").',
            'Selecione ou crie um projeto (ex: Meu Projeto) e confirme.',
            'Copie a chave gerada e cole no campo de Chave de Acesso.'
          ]
        };
      case 'CLAUDE':
        return {
          provedorNome: 'Anthropic Claude',
          url: 'https://console.anthropic.com/settings/keys',
          labelLink: 'Obter chave Claude',
          resumoTooltip: 'Passo a passo rápido (site em inglês):\n1. Acesse o Anthropic Console\n2. Vá no menu Settings > "API Keys"\n3. Clique em "Create Key" e copie a chave',
          passos: [
            'Acesse o Anthropic Console e entre com sua conta (painel em inglês).',
            'No menu superior ou lateral, vá em Settings > "API Keys".',
            'Clique em "Create Key", digite um nome e confirme.',
            'Copie o código gerado (começa com sk-ant-...) e cole aqui.'
          ]
        };
      case 'OPENAI':
        return {
          provedorNome: 'OpenAI (ChatGPT)',
          url: 'https://platform.openai.com/api-keys',
          labelLink: 'Obter chave OpenAI',
          resumoTooltip: 'Passo a passo rápido (site em inglês):\n1. Acesse platform.openai.com\n2. No menu lateral, acesse "API keys"\n3. Clique em "+ Create new secret key" e copie',
          passos: [
            'Acesse a plataforma da OpenAI e faça login na sua conta.',
            'No menu lateral esquerdo, selecione "API keys".',
            'Clique em "+ Create new secret key", escolha um nome e crie.',
            'Copie o código gerado de imediato e cole aqui.'
          ]
        };
      case 'DEEPSEEK':
        return {
          provedorNome: 'DeepSeek',
          url: 'https://platform.deepseek.com/api_keys',
          labelLink: 'Obter chave DeepSeek',
          resumoTooltip: 'Passo a passo rápido (site em inglês):\n1. Acesse platform.deepseek.com\n2. No menu lateral, acesse "API Keys"\n3. Clique em "Create API Key" e copie o código',
          passos: [
            'Acesse platform.deepseek.com e entre com sua conta (painel em inglês).',
            'No menu lateral esquerdo, clique em "API Keys".',
            'Clique em "Create API Key", confirme o nome e gere.',
            'Copie o código da chave e cole no campo abaixo.'
          ]
        };
      case 'GROQ':
        return {
          provedorNome: 'Groq Cloud',
          url: 'https://console.groq.com/keys',
          labelLink: 'Obter chave Groq',
          resumoTooltip: 'Passo a passo rápido (site em inglês):\n1. Acesse console.groq.com\n2. No menu lateral, acesse "API Keys"\n3. Clique em "Create API Key" e copie',
          passos: [
            'Acesse o Groq Console com sua conta (painel em inglês).',
            'No menu lateral, selecione a seção "API Keys".',
            'Clique no botão "Create API Key", dê um nome e confirme.',
            'Copie o código da chave (começa com gsk_...) e cole aqui.'
          ]
        };
      default:
        return {
          provedorNome: 'Provedor de IA',
          url: 'https://aistudio.google.com/app/apikey',
          labelLink: 'Obter chave',
          resumoTooltip: 'Acesse o painel oficial para gerar sua chave.',
          passos: [
            'Acesse o painel do provedor de IA escolhido.',
            'Navegue até a seção de chaves (API Keys).',
            'Crie uma chave e copie o código para cá.'
          ]
        };
    }
  }

  get linkApiKeyProvedor() {
    return {
      url: this.tutorialApiKey.url,
      label: this.tutorialApiKey.labelLink,
      dicaLink: this.tutorialApiKey.resumoTooltip
    };
  }

  resetForm(): ITransacoes {
    return {
      id: 0,
      descricao: '',
      categoria: null,
      conta: null,
      tipo: null,
      estado: 'REALIZADA',
      valor: 0,
      dataHora: new Date().toISOString().slice(0, 19),
      marcador: null,
      grupoParcelamento: null,
      numeroParcela: null,
      totalParcelas: null,
      valorTotalCompra: null
    };
  }

  abrirDialog() {
    this.id = null;
    this.marcadorSelecionado = null;
    this.formTransacao.reset({
      descricao: '',
      idCategoria: '',
      valor: 0,
      idContaFinanceira: '',
      dataHora: null,
      estado: 'REALIZADA',
      tipo: '',
      marcadorId: null,
      parcelado: false,
      totalParcelas: 2,
      numeroParcela: 1,
      valorTotalCompra: null,
      criarParcelasAnteriores: true,
    });
    this.editandoParcelada = false;
    this.ajustarValidacaoParcelas(false);
    this.novaTransacao = this.resetForm();
    this.exibirDialog = true;
  }

  abrirDialogEscolha() {
    this.exibirDialogEscolha = true;
  }

  escolherManual() {
    this.exibirDialogEscolha = false;
    this.abrirDialog();
  }

  escolherUpload() {
    this.exibirDialogEscolha = false;
    this.arquivoSelecionado = null;
    this.erroUpload = null;
    this.codigoErroUpload = null;
    this.validandoArquivo = false;
    this.faturaPreviaValida = false;
    this.emissorDetectado = null;
    this.precisaAtivarIa = false;
    this.precisaSenhaPdf = false;
    this.senhaPdf = '';
    this.apiKeyIaTocada = false;
    this.faturaAnalise = null;
    this.exibirDialogUpload = true;
    this.carregarSessaoIa();
  }

  carregarSessaoIa() {
    this.carregandoSessaoIa = true;
    this.transacoesService.obterSessaoIa().subscribe({
      next: (res) => {
        this.carregandoSessaoIa = false;
        this.temChaveSalva = res.temChaveSalva;
        this.provedorSalvo = res.provedor;
        if (this.temChaveSalva && res.provedor) {
          this.provedorSelecionado = res.provedor;
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.carregandoSessaoIa = false;
      }
    });
  }

  selecionarArquivo(event: any) {
    const file = event.target?.files?.[0] ?? null;
    this.erroUpload = null;
    this.codigoErroUpload = null;
    this.precisaAtivarIa = false;
    this.precisaSenhaPdf = false;
    this.senhaPdf = '';
    this.validandoArquivo = false;
    this.faturaPreviaValida = false;
    this.emissorDetectado = null;

    if (file) {
      const nome = file.name.toLowerCase();
      if (!nome.endsWith('.pdf')) {
        this.arquivoSelecionado = null;
        this.codigoErroUpload = 'FORMATO_INVALIDO';
        this.erroUpload = 'Formato não suportado. O Na Ponta do Lápis suporta apenas faturas em formato PDF.';
        this.messageService.add({
          severity: 'warn',
          summary: 'Formato não suportado',
          detail: 'Por favor, selecione um arquivo em formato PDF.',
          life: 4000
        });
        if (event.target) event.target.value = '';
        return;
      }
      if (file.size > 10 * 1024 * 1024) {
        this.arquivoSelecionado = null;
        this.codigoErroUpload = 'ARQUIVO_MUITO_GRANDE';
        this.erroUpload = 'O arquivo selecionado excede o limite máximo permitido de 10 MB.';
        this.messageService.add({
          severity: 'warn',
          summary: 'Arquivo muito grande',
          detail: 'O limite máximo para upload é de 10 MB.',
          life: 4000
        });
        if (event.target) event.target.value = '';
        return;
      }

      this.arquivoSelecionado = file;
      this.executarValidacaoPrevia(file);
    } else {
      this.arquivoSelecionado = null;
    }
  }

  executarValidacaoPrevia(file: File, senha?: string) {
    this.validandoArquivo = true;
    this.erroUpload = null;
    this.codigoErroUpload = null;
    this.faturaPreviaValida = false;
    this.emissorDetectado = null;

    this.transacoesService.validarPreviaFatura(file, senha).subscribe({
      next: (res) => {
        this.validandoArquivo = false;
        if (res.protegidoPorSenha) {
          this.precisaSenhaPdf = true;
          this.erroUpload = res.mensagem;
          this.codigoErroUpload = res.codigo;
          this.cdr.detectChanges();
          return;
        }

        if (res.valido) {
          this.faturaPreviaValida = true;
          this.emissorDetectado = res.emissor;
          this.precisaSenhaPdf = false;
          this.erroUpload = null;
          this.codigoErroUpload = null;
        } else {
          this.faturaPreviaValida = false;
          this.codigoErroUpload = res.codigo;
          this.erroUpload = res.mensagem || 'O documento enviado não parece ser uma fatura de cartão de crédito.';
          this.messageService.add({
            severity: 'error',
            summary: 'Documento não é fatura',
            detail: this.erroUpload,
            life: 5000
          });
        }
        this.cdr.detectChanges();
      },
      error: () => {
        // Se a validação prévia falhar por conexão/rede, não bloqueia o fluxo normal
        this.validandoArquivo = false;
        this.cdr.detectChanges();
      }
    });
  }

  enviarFatura() {
    if (!this.arquivoSelecionado) return;
    this.analisarFatura({});
  }

  enviarFaturaComSenha() {
    if (!this.arquivoSelecionado || !this.senhaPdf.trim()) return;
    this.analisarFatura({});
  }

  ativarIaETentarNovamente() {
    this.usarIa = true;
    this.precisaAtivarIa = false;
    this.erroUpload = null;
    this.codigoErroUpload = null;
    this.enviarFatura();
  }

  private analisarFatura(extras: FaturaAnaliseOpcoes) {
    if (!this.arquivoSelecionado) return;

    const opcoes: FaturaAnaliseOpcoes = {
      provedor: this.provedorSelecionado,
      apiKey: this.apiKeyIa?.trim() || undefined,
      senha: this.senhaPdf?.trim() || undefined,
      manterNaSessao: this.manterNaSessao,
      usarIa: this.usarIa,
      ...extras,
    };

    this.carregando = true;
    this.erroUpload = null;
    this.codigoErroUpload = null;
    this.precisaAtivarIa = false;
    this.erroConsentimento = null;
    this.iniciarMensagensProgresso();

    this.transacoesService.analisarFatura(this.arquivoSelecionado, opcoes).subscribe({
      next: (res: FaturaAnaliseResponse) => {
        this.carregando = false;
        this.pararMensagensProgresso();

        if (res.precisaHabilitarIa) {
          this.precisaAtivarIa = true;
          this.erroUpload = res.mensagem ?? 'Não conseguimos ler esta fatura sem IA. Ative o uso de IA para continuar.';
          this.cdr.detectChanges();
          return;
        }

        if (res.precisaConsentimentoPdf) {
          this.carregandoConsentimento = false;
          this.faturaPendenteConsentimento = res;
          this.consentimentoLgpd = false;
          this.ajustarProvedorParaPdf();
          this.exibirDialogUpload = false;
          this.exibirDialogConsentimento = true;
          this.cdr.detectChanges();
          return;
        }

        // Fecha o modal de upload primeiro para que a animação não trave na tela
        this.exibirDialogUpload = false;
        this.cdr.detectChanges();

        // Abre a revisão suavemente no próximo ciclo
        setTimeout(() => {
          this.aplicarResultadoAnalise(res);
        }, 50);
      },
      error: (err: HttpErrorResponse) => {
        this.carregando = false;
        this.pararMensagensProgresso();
        const corpo = (typeof err.error === 'object' && err.error !== null) ? err.error as FaturaErro : null;
        this.codigoErroUpload = corpo?.codigo ?? (err.status === 400 ? 'DOCUMENTO_NAO_EH_FATURA' : null);
        const mensagem = this.mensagemErroFatura(err);

        if (this.exibirDialogConsentimento) {
          this.carregandoConsentimento = false;
          this.erroConsentimento = mensagem;
        } else {
          this.erroUpload = mensagem;
        }

        const ehNaoFatura = this.codigoErroUpload === 'DOCUMENTO_NAO_EH_FATURA';
        this.messageService.add({
          severity: ehNaoFatura ? 'error' : (err.status === 428 || err.status === 422 ? 'warn' : 'error'),
          summary: ehNaoFatura ? 'Documento não é fatura' : 'Atenção na leitura da fatura',
          detail: mensagem,
          life: 5000
        });

        this.cdr.detectChanges();
      }
    });
  }

  private aplicarResultadoAnalise(res: FaturaAnaliseResponse) {
    this.carregandoConsentimento = false;
    this.precisaAtivarIa = false;
    this.precisaSenhaPdf = false;
    this.faturaAnalise = res;
    this.transacoesExtraidas = res.transacoes.map(t => {
      const dataStr = typeof t.data === 'string' ? t.data.slice(0, 10) : this.dataParaIso(t.data);
      return {
        ...t,
        tipo: t.tipo ?? 'DESPESA',
        data: dataStr,
        selecionada: true
      };
    });
    this.faturaPendenteConsentimento = null;
    this.contaSelecionadaRevisao = null;
    if (this.opcoesConta.length > 0) {
      this.contaSelecionadaRevisao = (this.opcoesConta[0] as any).id;
    }
    this.detectarDuplicatas();
    this.recalcularTotaisRevisao();
    this.exibirDialogRevisao = true;

    if (res.consentimentoPdfIa) {
      this.messageService.add({
        severity: 'info',
        summary: 'PDF enviado à IA com a sua autorização',
        life: 3500
      });
    } else if (res.ocrUtilizado) {
      this.messageService.add({
        severity: 'info',
        summary: 'Fatura lida a partir da imagem',
        detail: 'Nenhum dado saiu do nosso servidor.',
        life: 3500
      });
    }

    this.cdr.detectChanges();
  }

  enviarFaturaComConsentimento() {
    if (!this.consentimentoLgpd) {
      this.erroConsentimento = 'Marque a autorização para enviar o PDF ao provedor de IA.';
      return;
    }
    if (!this.arquivoSelecionado) {
      this.erroConsentimento = 'Selecione novamente o arquivo PDF.';
      return;
    }

    this.carregandoConsentimento = true;
    this.erroConsentimento = null;
    this.analisarFatura({
      enviarPdfParaIa: true,
      consentimentoLgpd: true,
      usarIa: true,
      provedor: this.provedorSelecionado,
    });
  }

  cancelarConsentimento() {
    this.carregandoConsentimento = false;
    this.exibirDialogConsentimento = false;
    this.faturaPendenteConsentimento = null;
    this.erroConsentimento = null;
    this.consentimentoLgpd = false;
    this.exibirDialogUpload = true;
  }

  private ajustarProvedorParaPdf() {
    if (!this.provedorSelecionadoSuportaPdf) {
      this.provedorSelecionado = 'GEMINI';
    }
  }

  private mensagemErroFatura(err: HttpErrorResponse): string {
    const corpo = err.error as FaturaErro | null;
    const detalhe = corpo?.error;

    switch (err.status) {
      case 428:
        return detalhe || 'Não conseguimos ler esta fatura automaticamente. Informe uma chave de acesso de IA para continuar.';
      case 422:
        if (corpo?.codigo === 'PDF_PROTEGIDO_POR_SENHA') {
          this.precisaSenhaPdf = true;
          return 'Esta fatura está protegida por senha. Informe a senha abaixo (geralmente os primeiros dígitos do CPF) para continuar.';
        }
        if (corpo?.codigo === 'SENHA_INCORRETA') {
          this.precisaSenhaPdf = true;
          return 'A senha informada está incorreta para abrir este PDF. Confira e tente novamente.';
        }
        if (corpo?.codigo === 'CONSENTIMENTO_OBRIGATORIO') {
          return 'Marque a autorização para enviar o PDF à IA.';
        }
        if (corpo?.codigo === 'PROVEDOR_SEM_SUPORTE_PDF') {
          return 'Escolha Gemini ou Claude para enviar o PDF à IA.';
        }
        if (corpo?.codigo === 'CHAVE_INVALIDA') {
          return 'A chave de acesso foi recusada. Confira se copiou corretamente e tente de novo.';
        }
        return detalhe || 'Não foi possível processar a fatura.';
      case 429:
        return 'O serviço de IA atingiu o limite de uso. Aguarde alguns instantes e tente novamente.';
      case 413:
        return 'O arquivo é maior que o limite permitido de 10 MB.';
      case 400:
        if (corpo?.codigo === 'DOCUMENTO_NAO_EH_FATURA') {
          return detalhe || 'O documento enviado não parece ser uma fatura de cartão de crédito. Verifique se selecionou o arquivo correto.';
        }
        return detalhe || 'Arquivo inválido. Envie um PDF de fatura de cartão de crédito.';
      case 502:
        return 'O serviço de IA não respondeu como esperado. Tente novamente.';
      case 503:
        return 'Serviço temporariamente indisponível. Tente novamente em alguns instantes.';
      case 504:
        return 'Tempo limite excedido. O PDF pode ser muito grande ou o serviço está lento.';
      default:
        return detalhe || 'Erro ao analisar fatura. Verifique o arquivo e tente novamente.';
    }
  }

  esquecerChaveIa() {
    this.transacoesService.esquecerChaveIa().subscribe({
      next: () => {
        this.apiKeyIa = '';
        this.temChaveSalva = false;
        this.provedorSalvo = null;
        this.manterNaSessao = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Chave apagada',
          life: 2500
        });
        this.cdr.detectChanges();
      },
      error: () => {
        this.messageService.add({
          severity: 'warn',
          summary: 'Não foi possível apagar a chave salva',
          life: 2500
        });
      }
    });
  }

  somaTransacoesRevisao: number = 0;
  diferencaRevisao: number | null = null;
  alertaSomaRevisao: boolean = false;
  qtdSelecionadas: number = 0;
  todasSelecionadas: boolean = true;

  detectarDuplicatas(desmarcarDuplicatasAutomaticamente: boolean = true) {
    if (!this.contaSelecionadaRevisao || !this.transacoesExtraidas.length) {
      this.qtdDuplicatasDetectadas = 0;
      return;
    }

    const contaId = Number(this.contaSelecionadaRevisao);
    let count = 0;

    this.transacoesExtraidas.forEach(t => {
      const valorNum = Number(t.valor) || 0;
      const dataIso = typeof t.data === 'string' ? t.data.slice(0, 10) : this.dataParaIso(t.data);

      const duplicataExistente = this.transacoesDados.find(existente => {
        const contaMatch = existente.conta?.id === contaId;
        const dataMatch = existente.dataHora && existente.dataHora.slice(0, 10) === dataIso;
        const valorMatch = Math.abs(Number(existente.valor) - valorNum) < 0.01;
        const tipoMatch = !existente.tipo || existente.tipo === t.tipo;
        return contaMatch && dataMatch && valorMatch && tipoMatch;
      });

      if (duplicataExistente) {
        t.duplicata = true;
        const partes = dataIso.split('-');
        const dataBr = partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : dataIso;
        t.duplicataInfo = `Já existe na conta: "${duplicataExistente.descricao}" (${dataBr})`;
        if (desmarcarDuplicatasAutomaticamente) {
          t.selecionada = false;
        }
        count++;
      } else {
        t.duplicata = false;
        t.duplicataInfo = undefined;
      }
    });

    this.qtdDuplicatasDetectadas = count;
  }

  aoMudarContaRevisao() {
    this.detectarDuplicatas(true);
    this.recalcularTotaisRevisao();
  }

  recalcularTotaisRevisao() {
    const selecionadas = this.transacoesExtraidas.filter(t => t.selecionada !== false);
    this.qtdSelecionadas = selecionadas.length;
    this.todasSelecionadas = this.transacoesExtraidas.length > 0 && selecionadas.length === this.transacoesExtraidas.length;

    this.somaTransacoesRevisao = Number(selecionadas.reduce((acc, t) => {
      const valor = Number(t.valor) || 0;
      return t.tipo === 'RECEITA' ? acc - valor : acc + valor;
    }, 0).toFixed(2));

    if (this.faturaAnalise?.valorTotalFatura !== null && this.faturaAnalise?.valorTotalFatura !== undefined) {
      this.diferencaRevisao = Number((this.faturaAnalise.valorTotalFatura - this.somaTransacoesRevisao).toFixed(2));
      this.alertaSomaRevisao = Math.abs(this.diferencaRevisao) > 0.01;
    } else {
      this.diferencaRevisao = null;
      this.alertaSomaRevisao = false;
    }
  }

  removerTransacaoExtraida(index: number) {
    this.transacoesExtraidas.splice(index, 1);
    this.recalcularTotaisRevisao();
  }

  get transacoesSelecionadas(): ITransacaoFatura[] {
    return this.transacoesExtraidas.filter(t => t.selecionada !== false);
  }

  alternarSelecionarTodas() {
    const marcar = !this.todasSelecionadas;
    this.transacoesExtraidas.forEach(t => t.selecionada = marcar);
    this.recalcularTotaisRevisao();
  }

  aoAlternarItem() {
    this.recalcularTotaisRevisao();
  }

  iniciarMensagensProgresso() {
    const mensagens = [
      'Lendo páginas e estrutura do documento...',
      'Identificando emissor e transações...',
      'Conferindo conciliação contábil...',
      'Finalizando leitura e ajustes...'
    ];
    let idx = 0;
    this.etapaAnalise = mensagens[0];
    clearInterval(this.intervaloEtapas);
    this.intervaloEtapas = setInterval(() => {
      idx = (idx + 1) % mensagens.length;
      this.etapaAnalise = mensagens[idx];
      this.cdr.detectChanges();
    }, 2500);
  }

  pararMensagensProgresso() {
    clearInterval(this.intervaloEtapas);
    this.intervaloEtapas = null;
  }

  confirmarTransacoes() {
    if (!this.contaSelecionadaRevisao || this.transacoesSelecionadas.length === 0) return;

    if (this.parcelamentoInvalido()) {
      this.messageService.add({
        severity: 'error',
        summary: 'Parcelamento inválido',
        detail: 'Preencha a parcela atual e o total, com a parcela entre 1 e o total (máximo 60).',
        life: 4500
      });
      return;
    }

    const transacoesParaEnviar: ITransacaoFatura[] = this.transacoesSelecionadas.map(t => ({
      descricao: t.descricao.trim(),
      valor: Number(t.valor),
      data: this.dataParaIso(t.data),
      categoria: t.categoria,
      tipo: t.tipo ?? 'DESPESA',
      numeroParcela: t.numeroParcela ?? null,
      totalParcelas: t.totalParcelas ?? null,
      valorTotalCompra: t.valorTotalCompra ?? null
    }));

    this.carregando = true;
    this.transacoesService.salvarFaturaEmLote(transacoesParaEnviar, this.contaSelecionadaRevisao).subscribe({
      next: (salvas: ITransacoes[]) => {
        this.carregando = false;
        this.exibirDialogRevisao = false;
        this.listarTransacoes();
        this.atualizarSaldo();

        if (this.projetarParcelasFuturas) {
          const gruposParaGerar = [...new Set(
            salvas
              .filter(t => t.grupoParcelamento && t.numeroParcela && t.totalParcelas && t.numeroParcela < t.totalParcelas)
              .map(t => t.grupoParcelamento!)
          )];

          if (gruposParaGerar.length > 0) {
            const chamadas = gruposParaGerar.map(id => this.transacoesService.gerarParcelasFuturas(id));
            forkJoin(chamadas).subscribe({
              next: (resultados) => {
                const totalGeradas = resultados.reduce((acc, curr) => acc + curr.length, 0);
                this.listarTransacoes();
                this.atualizarSaldo();
                this.messageService.add({
                  severity: 'success',
                  summary: 'Fatura importada com sucesso!',
                  detail: `${salvas.length} transação(ões) salva(s) e ${totalGeradas} parcela(s) restante(s) projetada(s) no seu planejamento.`,
                  life: 4500
                });
              },
              error: () => {
                this.messageService.add({
                  severity: 'success',
                  summary: 'Transações salvas com sucesso!',
                  life: 3000
                });
              }
            });
            this.cdr.detectChanges();
            return;
          }
        }

        this.messageService.add({ severity: 'success', summary: 'Transações salvas com sucesso!', life: 3000 });
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.carregando = false;
        let msg = 'Erro ao salvar transações. Tente novamente.';
        if (err.status === 400) msg = 'Dados inválidos. Verifique os campos e tente novamente.';
        else if (err.status === 403) msg = 'Você não tem permissão para salvar transações nesta conta.';
        else if (err.status === 503) msg = 'Serviço temporariamente indisponível. Tente novamente.';
        this.messageService.add({ severity: 'error', summary: msg, life: 5000 });
        this.cdr.detectChanges();
      }
    });
  }

  parcelamentoInvalido(): boolean {
    return this.transacoesSelecionadas.some(t => {
      const numero = t.numeroParcela ?? null;
      const total = t.totalParcelas ?? null;
      if (numero === null && total === null) return false;
      return numero === null || total === null || numero < 1 || total < 2 || numero > total || total > 60;
    });
  }

  abrirGrupoParcelamento(transacao: ITransacoes) {
    if (!transacao.grupoParcelamento) return;

    this.grupoSelecionado = transacao.grupoParcelamento;
    this.parcelasGrupo = [];
    this.exibirDialogGrupo = true;
    this.carregarGrupo();
  }

  private carregarGrupo() {
    if (!this.grupoSelecionado) return;

    this.carregandoGrupo = true;
    this.transacoesService.listarParcelamento(this.grupoSelecionado).subscribe({
      next: (parcelas: ITransacoes[]) => {
        this.parcelasGrupo = parcelas;
        this.carregandoGrupo = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.carregandoGrupo = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao carregar o parcelamento',
          life: 3000
        });
        this.cdr.detectChanges();
      }
    });
  }

  gerarFuturasGrupoAtual() {
    if (!this.grupoSelecionado) return;

    this.transacoesService.gerarParcelasFuturas(this.grupoSelecionado).subscribe({
      next: (novas: ITransacoes[]) => {
        this.messageService.add({
          severity: 'success',
          summary: novas.length > 0
            ? `${novas.length} parcela(s) futura(s) gerada(s)`
            : 'Não há parcelas futuras para gerar',
          life: 3000
        });
        this.carregarGrupo();
        this.listarTransacoes();
        this.atualizarSaldo();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao gerar parcelas futuras',
          life: 3000
        });
      }
    });
  }

  gerarAnterioresGrupoAtual() {
    if (!this.grupoSelecionado) return;

    this.transacoesService.gerarParcelasAnteriores(this.grupoSelecionado).subscribe({
      next: (novas: ITransacoes[]) => {
        this.messageService.add({
          severity: 'success',
          summary: novas.length > 0
            ? `${novas.length} parcela(s) anterior(es) registrada(s) no histórico`
            : 'Não há parcelas anteriores para registrar',
          life: 3000
        });
        this.carregarGrupo();
        this.listarTransacoes();
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao registrar parcelas anteriores',
          life: 3000
        });
      }
    });
  }

  confirmarPagamento(transacao: ITransacoes) {
    if (!transacao.id || transacao.estado === 'REALIZADA') return;
    this.transacaoParaConfirmar = transacao;
    this.exibirDialogConfirmarPagamento = true;
  }

  fecharDialogConfirmarPagamento() {
    this.transacaoParaConfirmar = null;
    this.exibirDialogConfirmarPagamento = false;
    this.carregandoConfirmacao = false;
  }

  executarConfirmacaoPagamento() {
    if (!this.transacaoParaConfirmar?.id) return;
    const id = this.transacaoParaConfirmar.id;

    this.carregandoConfirmacao = true;
    this.transacoesService.confirmarPagamento(id).subscribe({
      next: () => {
        this.carregandoConfirmacao = false;
        this.exibirDialogConfirmarPagamento = false;
        this.transacaoParaConfirmar = null;

        this.messageService.add({
          severity: 'success',
          summary: 'Pagamento confirmado!',
          detail: 'A transação foi realizada e o saldo da conta foi atualizado.',
          life: 3000
        });

        const idx = this.parcelasGrupo.findIndex(p => p.id === id);
        if (idx !== -1) {
          this.parcelasGrupo[idx] = { ...this.parcelasGrupo[idx], estado: 'REALIZADA' };
        }

        this.listarTransacoes();
        this.atualizarSaldo();
        this.cdr.detectChanges();
      },
      error: () => {
        this.carregandoConfirmacao = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao confirmar pagamento',
          detail: 'Não foi possível confirmar o pagamento. Tente novamente.',
          life: 3000
        });
      }
    });
  }

  excluirGrupoParcelamento(apenasFuturas: boolean) {
    if (!this.grupoSelecionado) return;
    this.excluirApenasFuturas = apenasFuturas;
    this.exibirDialogExcluirParcelamento = true;
  }

  executarExclusaoParcelamento() {
    if (!this.grupoSelecionado) return;

    this.carregandoExclusao = true;
    this.transacoesService.excluirParcelamento(this.grupoSelecionado, this.excluirApenasFuturas).subscribe({
      next: () => {
        this.carregandoExclusao = false;
        this.exibirDialogExcluirParcelamento = false;
        this.messageService.add({
          severity: 'success',
          summary: this.excluirApenasFuturas ? 'Parcelas futuras excluídas' : 'Parcelamento excluído',
          life: 3000
        });
        this.exibirDialogGrupo = false;
        this.grupoSelecionado = null;
        this.parcelasGrupo = [];
        this.listarTransacoes();
        this.atualizarSaldo();
        this.cdr.detectChanges();
      },
      error: () => {
        this.carregandoExclusao = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao excluir o parcelamento',
          life: 3000
        });
      }
    });
  }

  get opcoesCategoriaNome() {
    return this.opcoesCategoria.map(c => c.nome);
  }

  public prepararEdicao(transacao: ITransacoes) {
    this.id = transacao.id;
    this.exibirDialog = true;

    // Preenche o formulário com os dados da linha selecionada
    this.formTransacao.patchValue({
      descricao: transacao.descricao,
      valor: transacao.valor,
      idCategoria: transacao.categoria?.id,
      idContaFinanceira: transacao.conta?.id,
      dataHora: new Date(transacao.dataHora),
      estado: transacao.estado,
      tipo: transacao.tipo,
      marcadorId: transacao.marcador?.id,
      parcelado: false,
      totalParcelas: transacao.totalParcelas ?? 2,
      numeroParcela: transacao.numeroParcela ?? 1,
      valorTotalCompra: null,
      criarParcelasAnteriores: true
    });
    this.editandoParcelada = !!transacao.grupoParcelamento;
    this.ajustarValidacaoParcelas(false);

    this.marcadorSelecionado = transacao.marcador;
  }

  salvar() {
    if (this.formTransacao.valid) {
      const dadosParaEnviar: any = { ...this.formTransacao.value }
      this.exibirDialog = false

      if (this.id) {
        // Transação já parcelada: edição altera só esta parcela. Avulsa: pode virar parcelada.
        if (this.editandoParcelada || !dadosParaEnviar.parcelado) {
          delete dadosParaEnviar.parcelado
          delete dadosParaEnviar.totalParcelas
          delete dadosParaEnviar.numeroParcela
          delete dadosParaEnviar.valorTotalCompra
          delete dadosParaEnviar.criarParcelasAnteriores
        } else {
          dadosParaEnviar.totalParcelas = Number(dadosParaEnviar.totalParcelas)
          dadosParaEnviar.numeroParcela = Number(dadosParaEnviar.numeroParcela)
          dadosParaEnviar.valorTotalCompra = dadosParaEnviar.valorTotalCompra
            ? Number(dadosParaEnviar.valorTotalCompra)
            : null
          dadosParaEnviar.criarParcelasAnteriores =
            !!dadosParaEnviar.criarParcelasAnteriores && dadosParaEnviar.numeroParcela > 1
        }
        this.transacoesService.editarTransacao(this.id, dadosParaEnviar).subscribe({
          next: () => this.sucessoAoSalvar('Transação atualizada') ,
          error: (err: Error) => this.erroAoSalvar('Erro ao editar', err)
        })
      } else {
        const parcelado = !!dadosParaEnviar.parcelado
        dadosParaEnviar.parcelado = parcelado
        delete dadosParaEnviar.valorTotalCompra
        if (parcelado) {
          dadosParaEnviar.numeroParcela = Number(dadosParaEnviar.numeroParcela)
          dadosParaEnviar.criarParcelasAnteriores =
            !!dadosParaEnviar.criarParcelasAnteriores && dadosParaEnviar.numeroParcela > 1
        } else {
          delete dadosParaEnviar.numeroParcela
          delete dadosParaEnviar.criarParcelasAnteriores
        }
        dadosParaEnviar.totalParcelas = parcelado ? Number(dadosParaEnviar.totalParcelas) : null

        this.transacoesService.adicionarTransacao(dadosParaEnviar).subscribe({
          next: (res: ITransacoes[]) => {
            this.sucessoAoSalvar(parcelado
              ? `Compra parcelada em ${res.length} vez(es)`
              : 'Transação criada!')
          },
          error: (error: HttpErrorResponse) => {
            const corpo = error.error as FaturaErro | null
            this.erroAoSalvar(corpo?.error || 'Error ao salvar', error)
          }
        })
      }
    }
  }

  private sucessoAoSalvar(mensagem: string) {
    this.messageService.add({ severity: 'success', summary: mensagem, life: 2000 });
    this.listarTransacoes();
    this.atualizarSaldo();
    this.id = null;
  }

  private atualizarSaldo(): void {
    this.cardSaldo?.listarContas();
  }

  private erroAoSalvar(mensagem: string, erro: any) {
    console.error(erro);
    this.messageService.add({ severity: 'error', summary: mensagem, life: 2000 });
  }

  private paraDataLocal(data: string | Date): Date {
    if (data instanceof Date) return data;

    const somenteData = /^(\d{4})-(\d{2})-(\d{2})/.exec(data);
    if (somenteData) {
      return new Date(Number(somenteData[1]), Number(somenteData[2]) - 1, Number(somenteData[3]));
    }
    return new Date(data);
  }

  private dataParaIso(data: string | Date): string {
    if (typeof data === 'string' && /^\d{4}-\d{2}-\d{2}/.test(data)) {
      return data.slice(0, 10);
    }
    const d = data instanceof Date ? data : this.paraDataLocal(data);
    const ano = d.getFullYear();
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  getSeverity(status: string) {
    if (status == 'RECEITA') {
      return 'success'
    }
    if (status == 'DESPESA') {
      return 'danger'
    }
    if (status == 'PENDENTE') {
      return 'danger'
    }
    if (status == 'REALIZADA') {
      return 'success'
    }
    return 'info'
  }

}
