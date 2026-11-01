import { ChangeDetectorRef, Component, DestroyRef, inject, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransacoesService } from './service/transacoes.service';
import {
  Categoria,
  CompraParceladaRequest,
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
  exibirDialogParcelada: boolean = false;
  exibirDialogParcelasFuturas: boolean = false;
  exibirDialogGrupo: boolean = false;
  carregando: boolean = false;
  carregandoConsentimento: boolean = false;
  carregandoGrupo: boolean = false;
  erroUpload: string | null = null;
  erroConsentimento: string | null = null;
  arquivoSelecionado: File | null = null;
  transacoesExtraidas: ITransacaoFatura[] = [];
  contaSelecionadaRevisao: number | null = null;
  faturaAnalise: FaturaAnaliseResponse | null = null;
  faturaPendenteConsentimento: FaturaAnaliseResponse | null = null;

  provedorSelecionado: ProvedorIa = 'GEMINI';
  apiKeyIa: string = '';
  manterNaSessao: boolean = false;
  forceAiAnalysis: boolean = false;
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
  formParcelada: FormGroup;
  constructor(
    private fb: FormBuilder,
    private transacoesService: TransacoesService,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.formTransacao = this.fb.group({
      descricao: ['', [Validators.required, Validators.minLength(3)]],
      idCategoria: ['', [Validators.required]],
      valor: [0, [Validators.required, Validators.min(0.01)]],
      idContaFinanceira: ['', [Validators.required]],
      dataHora: ['', [Validators.required]],
      estado: ['', [Validators.required]],
      tipo: ['', [Validators.required]],
      marcadorId: ['']
    })

    this.formParcelada = this.fb.group({
      descricao: ['', [Validators.required, Validators.minLength(3)]],
      valorTotal: [0, [Validators.required, Validators.min(0.01)]],
      totalParcelas: [2, [Validators.required, Validators.min(2), Validators.max(60)]],
      dataPrimeiraParcela: [new Date(), [Validators.required]],
      idContaFinanceira: ['', [Validators.required]],
      idCategoria: ['', [Validators.required]],
      marcadorId: [null]
    })
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
  transacoesDados: ITransacoes[] = [];

  public abrirDialogMarcador() {
    this.exibirDialogMarcador = true
  }

  public aoFecharDialogMarcador() {
    this.exibirDialogMarcador = false;
    this.listarMarcadores();
  }

  public buscarPorDescricao(descricao: string): void {
    this.transacoesService.listarTransacoesPorDescricao(descricao).subscribe({
      next: (res: ITransacoes[]) => {
        this.transacoesDados = res
        this.cdr.detectChanges()
      },
      error: (erro: HttpErrorResponse) => {
        console.error(erro)
        this.erroAoSalvar("Erro ao carregar transação", erro);
      }
    })
  }

  public listarTransacoes(): any {
    this.transacoesService.listarTransacoes().subscribe({
      next: (res: ITransacoes[]) => {
        this.transacoesDados = res
        this.cdr.detectChanges()
      },
      error: (erro: Error) => {
        console.log(erro)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao carregar transação',
          detail: '',
          life: 2000
        });
      }
    })
  }
  public excluir(id: number): void {
    this.transacoesService.deletarTransacaoPorId(id).subscribe({
      next: (res: any) => {
        this.listarTransacoes()
        this.atualizarSaldo()
        this.messageService.add({
          severity: 'success',
          summary: 'Transação excluida com sucesso',
          detail: '',
          life: 2000
        });
      },
      error: (res: Error) => {
        console.error("Erro ao deletar Transação", res)
        this.listarTransacoes()
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao excluir com transação',
          detail: '',
          life: 2000
        });
      }
    })
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
    });
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

  escolherParcelada() {
    this.exibirDialogEscolha = false;
    this.formParcelada.reset({
      descricao: '',
      valorTotal: 0,
      totalParcelas: 2,
      dataPrimeiraParcela: new Date(),
      idContaFinanceira: '',
      idCategoria: '',
      marcadorId: null,
    });
    this.exibirDialogParcelada = true;
  }

  escolherUpload() {
    this.exibirDialogEscolha = false;
    this.arquivoSelecionado = null;
    this.erroUpload = null;
    this.faturaAnalise = null;
    this.exibirDialogUpload = true;
  }

  selecionarArquivo(event: any) {
    this.arquivoSelecionado = event.target.files[0] ?? null;
    this.erroUpload = null;
  }

  enviarFatura() {
    if (!this.arquivoSelecionado) return;
    this.analisarFatura({});
  }

  private analisarFatura(extras: FaturaAnaliseOpcoes) {
    if (!this.arquivoSelecionado) return;

    const opcoes: FaturaAnaliseOpcoes = {
      provedor: this.provedorSelecionado,
      apiKey: this.apiKeyIa?.trim() || undefined,
      manterNaSessao: this.manterNaSessao,
      forceAiAnalysis: this.forceAiAnalysis,
      ...extras,
    };

    this.carregando = true;
    this.erroUpload = null;
    this.erroConsentimento = null;

    this.transacoesService.analisarFatura(this.arquivoSelecionado, opcoes).subscribe({
      next: (res: FaturaAnaliseResponse) => {
        this.carregando = false;

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

        this.aplicarResultadoAnalise(res);
      },
      error: (err: HttpErrorResponse) => {
        this.carregando = false;
        const mensagem = this.mensagemErroFatura(err);
        if (this.exibirDialogConsentimento) {
          this.carregandoConsentimento = false;
          this.erroConsentimento = mensagem;
        } else {
          this.erroUpload = mensagem;
        }
        this.cdr.detectChanges();
      }
    });
  }

  private aplicarResultadoAnalise(res: FaturaAnaliseResponse) {
    this.carregandoConsentimento = false;
    this.faturaAnalise = res;
    this.transacoesExtraidas = res.transacoes.map(t => ({
      ...t,
      data: this.paraDataLocal(t.data)
    }));
    this.exibirDialogUpload = false;
    this.exibirDialogConsentimento = false;
    this.faturaPendenteConsentimento = null;
    this.contaSelecionadaRevisao = null;
    this.exibirDialogRevisao = true;

    if (res.consentimentoPdfIa) {
      this.messageService.add({
        severity: 'info',
        summary: 'PDF enviado à IA com seu consentimento',
        life: 3500
      });
    } else if (res.ocrUtilizado) {
      this.messageService.add({
        severity: 'info',
        summary: 'PDF lido por OCR local',
        detail: 'Nenhum dado foi enviado para fora do servidor.',
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
        return detalhe || 'O regex não conseguiu ler esta fatura. Informe uma chave de IA para continuar.';
      case 422:
        if (corpo?.codigo === 'CONSENTIMENTO_OBRIGATORIO') {
          return 'É necessário autorizar o envio do PDF para a IA.';
        }
        if (corpo?.codigo === 'PROVEDOR_SEM_SUPORTE_PDF') {
          return 'Escolha Gemini ou Claude para enviar o PDF à IA.';
        }
        if (corpo?.codigo === 'CHAVE_INVALIDA') {
          return 'A chave de IA foi recusada. Confira a chave e tente novamente.';
        }
        return detalhe || 'Não foi possível processar a fatura.';
      case 429:
        return 'O provedor de IA atingiu o limite de uso. Aguarde alguns instantes e tente novamente.';
      case 413:
        return 'O arquivo é maior que o limite permitido de 10 MB.';
      case 400:
        return detalhe || 'Arquivo inválido. Envie um PDF de fatura de cartão de crédito.';
      case 502:
        return 'O provedor de IA retornou uma resposta inválida. Tente novamente.';
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
        this.manterNaSessao = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Chave de IA removida da sessão',
          life: 2500
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'warn',
          summary: 'Não foi possível remover a chave da sessão',
          life: 2500
        });
      }
    });
  }

  removerTransacaoExtraida(index: number) {
    this.transacoesExtraidas.splice(index, 1);
  }

  confirmarTransacoes() {
    if (!this.contaSelecionadaRevisao || this.transacoesExtraidas.length === 0) return;

    if (this.parcelamentoInvalido()) {
      this.messageService.add({
        severity: 'error',
        summary: 'Parcelamento inválido',
        detail: 'Preencha a parcela atual e o total, com a parcela entre 1 e o total (máximo 60).',
        life: 4500
      });
      return;
    }

    const transacoesParaEnviar: ITransacaoFatura[] = this.transacoesExtraidas.map(t => ({
      descricao: t.descricao.trim(),
      valor: Number(t.valor),
      data: this.dataParaIso(t.data),
      categoria: t.categoria,
      numeroParcela: t.numeroParcela ?? null,
      totalParcelas: t.totalParcelas ?? null,
      valorTotalCompra: t.valorTotalCompra ?? null
    }));

    this.carregando = true;
    this.transacoesService.salvarFaturaEmLote(transacoesParaEnviar, this.contaSelecionadaRevisao).subscribe({
      next: (salvas: ITransacoes[]) => {
        this.carregando = false;
        this.exibirDialogRevisao = false;
        this.messageService.add({ severity: 'success', summary: 'Transações salvas com sucesso!', life: 3000 });
        this.listarTransacoes();
        this.atualizarSaldo();
        this.prepararParcelasFuturas(salvas);
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

  private parcelamentoInvalido(): boolean {
    return this.transacoesExtraidas.some(t => {
      const numero = t.numeroParcela ?? null;
      const total = t.totalParcelas ?? null;
      if (numero === null && total === null) return false;
      return numero === null || total === null || numero < 1 || total < 2 || numero > total || total > 60;
    });
  }

  private prepararParcelasFuturas(salvas: ITransacoes[]) {
    const grupos = new Map<string, GrupoParcelamentoResumo>();

    salvas.forEach(t => {
      if (!t.grupoParcelamento || !t.numeroParcela || !t.totalParcelas) return;
      if (t.numeroParcela >= t.totalParcelas) return;

      const atual = grupos.get(t.grupoParcelamento);
      if (!atual || t.numeroParcela > atual.numeroParcela) {
        grupos.set(t.grupoParcelamento, {
          grupoId: t.grupoParcelamento,
          descricao: t.descricao,
          numeroParcela: t.numeroParcela,
          totalParcelas: t.totalParcelas,
          valorTotalCompra: t.valorTotalCompra
        });
      }
    });

    this.gruposParcelamentoCriados = [...grupos.values()];
    this.gruposGerados.clear();

    if (this.gruposParcelamentoCriados.length > 0) {
      this.exibirDialogParcelasFuturas = true;
    }
  }

  gerarFuturasDoGrupo(grupoId: string) {
    this.transacoesService.gerarParcelasFuturas(grupoId).subscribe({
      next: (novas: ITransacoes[]) => {
        this.gruposGerados.add(grupoId);
        this.messageService.add({
          severity: 'success',
          summary: novas.length > 0
            ? `${novas.length} parcela(s) futura(s) gerada(s)`
            : 'Não há parcelas futuras para gerar',
          life: 3000
        });
        this.listarTransacoes();
        this.atualizarSaldo();
        this.cdr.detectChanges();
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

  fecharDialogParcelasFuturas() {
    this.exibirDialogParcelasFuturas = false;
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

  excluirGrupoParcelamento(apenasFuturas: boolean) {
    if (!this.grupoSelecionado) return;

    const mensagem = apenasFuturas
      ? 'Excluir somente as parcelas pendentes deste parcelamento?'
      : 'Excluir TODAS as parcelas deste parcelamento?';
    if (!window.confirm(mensagem)) return;

    this.transacoesService.excluirParcelamento(this.grupoSelecionado, apenasFuturas).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: apenasFuturas ? 'Parcelas futuras excluídas' : 'Parcelamento excluído',
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
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao excluir o parcelamento',
          life: 3000
        });
      }
    });
  }

  salvarParcelada() {
    if (this.formParcelada.invalid) return;

    const valor = this.formParcelada.value;
    const dto: CompraParceladaRequest = {
      descricao: valor.descricao.trim(),
      valorTotal: Number(valor.valorTotal),
      totalParcelas: Number(valor.totalParcelas),
      dataPrimeiraParcela: this.dataParaIso(valor.dataPrimeiraParcela),
      idContaFinanceira: Number(valor.idContaFinanceira),
      idCategoria: Number(valor.idCategoria),
      marcadorId: valor.marcadorId ? Number(valor.marcadorId) : null
    };

    this.carregando = true;
    this.transacoesService.criarCompraParcelada(dto).subscribe({
      next: (parcelas: ITransacoes[]) => {
        this.carregando = false;
        this.exibirDialogParcelada = false;
        this.messageService.add({
          severity: 'success',
          summary: `Compra parcelada em ${parcelas.length} vez(es)`,
          life: 3000
        });
        this.listarTransacoes();
        this.atualizarSaldo();
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.carregando = false;
        const corpo = err.error as FaturaErro | null;
        this.messageService.add({
          severity: 'error',
          summary: corpo?.error || 'Erro ao criar a compra parcelada',
          life: 4000
        });
        this.cdr.detectChanges();
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
      marcadorId: transacao.marcador?.id 
    });
    console.log(this.formTransacao.value);

    this.marcadorSelecionado = transacao.marcador;
  }

  salvar() {
    if (this.formTransacao.valid) {
      const dadosParaEnviar: any = this.formTransacao.value
      console.log("Dados para enviar:", dadosParaEnviar)
      this.exibirDialog = false

      if (this.id) {
        console.log("Editando transação com ID:", this.id, "Dados:", dadosParaEnviar)
        this.transacoesService.editarTransacao(this.id, dadosParaEnviar).subscribe({
          next: () => this.sucessoAoSalvar('Transação atualizada') ,
          error: (err: Error) => this.erroAoSalvar('Erro ao editar', err)
        })
      } else {
        this.transacoesService.adicionarTransacao(dadosParaEnviar).subscribe({
          next: (res: ITransacoes) => {
            this.sucessoAoSalvar('Transação criada!')
          },
          error: (error: Error) => {
            this.erroAoSalvar('Error ao salvar', error)
            console.error(error)
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
