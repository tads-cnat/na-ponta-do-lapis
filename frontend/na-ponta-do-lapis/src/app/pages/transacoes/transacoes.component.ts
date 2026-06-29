import { ChangeDetectorRef, Component, DestroyRef, inject, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransacoesService } from './service/transacoes.service';
import { Categoria, ITransacaoFatura, ITransacaoRequest, ITransacoes, Tipo } from '../../model/ITransacoes.model';
import { IContasRequest } from '../../model/IContas.models';
import { Marcador, MarcadorRequest } from '../../model/IMarcador.models';
import { Popover } from 'primeng/popover';
import { MessageService } from 'primeng/api';
import { CardSaldoComponent } from '../../shared/components/card-saldo/card-saldo.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { MarcadorDlgComponent } from '../marcador-dlg/marcador-dlg.component';

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
  carregando: boolean = false;
  erroUpload: string | null = null;
  arquivoSelecionado: File | null = null;
  transacoesExtraidas: ITransacaoFatura[] = [];
  contaSelecionadaRevisao: number | null = null;
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
      descricao: ['', [Validators.required, Validators.minLength(3)]],
      idCategoria: ['', [Validators.required]],
      valor: [0, [Validators.required, Validators.min(0.01)]],
      idContaFinanceira: ['', [Validators.required]],
      dataHora: [[null, Validators.required]],
      estado: ['', [Validators.required]],
      tipo: ['', [Validators.required]],
      marcadorId: [null, [Validators.required]]
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
    console.log("Marcador selecionado:", marcador)
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
    { label: 'Pendente', value: 'PENDENTE' },
    { label: 'Realizada', value: 'REALIZADA' }
  ]
  opcoesTipo: any[] = [
    { label: 'Receita', value: 'RECEITA' },
    { label: 'Despesa', value: 'DESPESA' }
  ];


  resetForm(): ITransacoes {
    return {
      id: 0,
      descricao: '',
      categoria: null,
      conta: null,
      tipo: null,
      estado: 'PENDENTE',
      valor: 0,
      dataHora: new Date().toISOString().slice(0, 19),
      marcador: null
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
      estado: 'PENDENTE',
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

  escolherUpload() {
    this.exibirDialogEscolha = false;
    this.arquivoSelecionado = null;
    this.erroUpload = null;
    this.exibirDialogUpload = true;
  }

  selecionarArquivo(event: any) {
    this.arquivoSelecionado = event.target.files[0] ?? null;
  }

  enviarFatura() {
    if (!this.arquivoSelecionado) return;
    this.carregando = true;
    this.erroUpload = null;
    this.transacoesService.analisarFatura(this.arquivoSelecionado).subscribe({
      next: (res: ITransacaoFatura[]) => {
        this.transacoesExtraidas = res.map(t => ({
          ...t,
          data: t.data ? new Date(t.data) : new Date()
        }));
        this.carregando = false;
        this.exibirDialogUpload = false;
        this.contaSelecionadaRevisao = null;
        this.exibirDialogRevisao = true;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.carregando = false;
        if (err.status === 503) {
          this.erroUpload = 'Serviço temporariamente indisponível. Tente novamente em alguns instantes.';
        } else if (err.status === 504) {
          this.erroUpload = 'Tempo limite excedido. O PDF pode ser muito grande ou o serviço está lento.';
        } else if (err.status === 502) {
          this.erroUpload = 'Erro ao processar a fatura. O PDF pode estar corrompido ou em formato não esperado.';
        } else if (err.status === 400) {
          this.erroUpload = 'Arquivo inválido. Envie um PDF de fatura de cartão de crédito.';
        } else {
          this.erroUpload = 'Erro ao analisar fatura. Verifique o arquivo e tente novamente.';
        }
        this.cdr.detectChanges();
      }
    });
  }

  removerTransacaoExtraida(index: number) {
    this.transacoesExtraidas.splice(index, 1);
  }

  confirmarTransacoes() {
    if (!this.contaSelecionadaRevisao || this.transacoesExtraidas.length === 0) return;
    const transacoesParaEnviar: ITransacaoFatura[] = this.transacoesExtraidas.map(t => ({
      descricao: t.descricao,
      valor: t.valor,
      data: t.data instanceof Date ? t.data.toISOString() : t.data,
      categoria: t.categoria
    }));
    this.carregando = true;
    this.transacoesService.salvarFaturaEmLote(transacoesParaEnviar, this.contaSelecionadaRevisao).subscribe({
      next: () => {
        this.carregando = false;
        this.exibirDialogRevisao = false;
        this.messageService.add({ severity: 'success', summary: 'Transações salvas com sucesso!', life: 3000 });
        this.listarTransacoes();
        this.atualizarSaldo();
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
      dataHora: new Date(transacao.dataHora).toISOString().slice(0, 19),
      estado: transacao.estado,
      tipo: transacao.tipo,
      marcadorId: transacao.marcador ? transacao.marcador.id : null
    });

    this.marcadorSelecionado = transacao.marcador;
  }

  salvar() {
    if (this.formTransacao.valid) {
      const dadosParaEnviar: ITransacaoRequest = this.formTransacao.value
      console.log("Dados para enviar:", dadosParaEnviar)
      this.exibirDialog = false

      if (this.id) {
        this.transacoesService.editarTransacao(this.id, dadosParaEnviar).subscribe({
          next: () => this.sucessoAoSalvar('Transação atualizada'),
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
