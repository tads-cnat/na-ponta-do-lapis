// contas.component.ts

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

import { PrimeNGModuleModule } from '../../shared/primeNg.module';

import { ColorPickerModule } from 'primeng/colorpicker';
import { ChartModule } from 'primeng/chart';
import { SelectButtonModule } from 'primeng/selectbutton';

import { ContaFinanceiraService } from './service/contas.service';

import {
  IContas,
  IContasRequest
} from '../../model/IContas.models';
import { GraficoComponent } from "./components/grafico/grafico.component";
import { InformacoesComponent } from "./components/informacoes/informacoes.component";
import { TransacoesComponent } from "./components/transacoes/transacoes.component";
import { CartaoContaComponent } from "./components/cartoes/cartoes.component";

@Component({
  selector: 'app-Contas',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PrimeNGModuleModule,
    ColorPickerModule,
    ChartModule,
    SelectButtonModule,
    GraficoComponent,
    InformacoesComponent,
    TransacoesComponent,
    CartaoContaComponent
],

  templateUrl: './contas.component.html',
})

export class ContasComponent implements OnInit {

  // =========================================
  // DIALOG
  // =========================================

  exibirDialog: boolean = false;

  idContaEdicao: number | null = null;

  modoEdicao: boolean = false;

  excluirDialog: boolean = false;

  contaExcluir: IContas | null = null;

// =========================================
// DADOS
// =========================================

carregandoContas: boolean = true;

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

  periodosGrafico = [
    {
      label: 'Semanal',
      value: 'semanal'
    },

    {
      label: 'Mensal',
      value: 'mensal'
    }
  ];

  periodoSelecionado: string = 'semanal';

  chartData: any;

  chartOptions: any;

  // =========================================
  // ÚLTIMAS TRANSAÇÕES
  // =========================================

ultimasTransacoes: ITransacoes[] = [];

paginaAtualTransacoes: number = 0;

readonly transacoesPorPagina: number = 5;

  public listarUltimasTransacoes(): void {

    const contaSelecionada = this.contaSelecionada;

    if (!contaSelecionada?.id) {

      this.ultimasTransacoes = [];

      this.cdr.detectChanges();

      return;
    }

    this.transacoesService
      .listarTransacoes()
      .subscribe({

        next: (res: ITransacoes[]) => {

          if (!res || res.length === 0) {

            this.ultimasTransacoes = [];

            this.paginaAtualTransacoes = 0;

            this.cdr.detectChanges();

            return;
          }

          // =====================================
          // FILTRA SOMENTE A CONTA SELECIONADA
          // =====================================

          const transacoesFiltradas = res.filter(

            (transacao: ITransacoes) => {

              return (
                transacao.conta?.id ===
                contaSelecionada.id
              );
            }
          );

          // =====================================
          // ORDENA MAIS RECENTE -> MAIS ANTIGA
          // =====================================

          this.ultimasTransacoes =
            transacoesFiltradas.sort((a, b) => {

              const dataA =
                new Date(a.dataHora).getTime();

              const dataB =
                new Date(b.dataHora).getTime();

              return dataB - dataA;
            });

          // =====================================
          // RESET PAGINAÇÃO
          // =====================================

          this.paginaAtualTransacoes = 0;

          this.cdr.detectChanges();
        },

        error: (error: Error) => {

          console.error(error);

          this.ultimasTransacoes = [];

          this.cdr.detectChanges();
        }
      });
  }

  public obterIconeCategoria(categoria: string | undefined): string {

    const mapa: Record<string, string> = {

      'Supermercado': 'pi-shopping-cart',
      'Restaurantes': 'pi-shop',
      'Academia': 'pi-heart',
      'Assinaturas Streaming': 'pi-play-circle',
      'Combustível': 'pi-car',
      'Manutenção Carro': 'pi-cog',
      'Estacionamento': 'pi-map-marker',
      'Pedágio': 'pi-directions',
      'Cinema': 'pi-video',
      'Jogos Online': 'pi-desktop',
      'Viagens': 'pi-send',
      'Hospedagem': 'pi-home',
      'Farmácia': 'pi-plus-circle',
      'Consultas Médicas': 'pi-user-plus',
      'Exames': 'pi-search',
      'Suplementos': 'pi-bolt',
      'Cursos Online': 'pi-book',
      'Livros': 'pi-bookmark',
      'Material Escolar': 'pi-pencil',
      'Mensalidade Faculdade': 'pi-building-columns'
    };

    return mapa[categoria || ''] || 'pi-wallet';
  }

  get transacoesPaginadas(): ITransacoes[] {

    const inicio =
      this.paginaAtualTransacoes * this.transacoesPorPagina;

    const fim = inicio + this.transacoesPorPagina;

    return this.ultimasTransacoes.slice(inicio, fim);
  }

  public proximaPaginaTransacoes(): void {

    const totalPaginas = Math.ceil(
      this.ultimasTransacoes.length /
      this.transacoesPorPagina
    );

    if (this.paginaAtualTransacoes < totalPaginas - 1) {
      this.paginaAtualTransacoes++;
    }
  }

  public paginaAnteriorTransacoes(): void {

    if (this.paginaAtualTransacoes > 0) {
      this.paginaAtualTransacoes--;
    }
  }

  public irParaTransacoes(): void {
    this.router.navigate(['/app/transacoes']);
  }

  // =========================================
  // CONSTRUCTOR
  // =========================================

  constructor(
    private fb: FormBuilder,
    private contaFinanceiraService: ContaFinanceiraService,
    private cdr: ChangeDetectorRef,
    private transacoesService: TransacoesService,
    private router: Router
  ) {

    this.formContaFinanceira = this.fb.group({

      nome: [
        '',
        [
          Validators.required,
          Validators.minLength(3)
        ]
      ],

      saldo: [
        0,
        [
          Validators.required
        ]
      ],

      cor: [
        '',
        [
          Validators.required,
          Validators.maxLength(7)
        ]
      ]
    });
  }

  // =========================================
  // INIT
  // =========================================

  ngOnInit(): void {

    this.listarContas();

  }

  // =========================================
  // GETTERS
  // =========================================

  get contaSelecionada(): IContas | null {

    if (
      !this.contaFinanceiraDados ||
      this.contaFinanceiraDados.length === 0
    ) {

      return null;
    }

    return this.contaFinanceiraDados[this.contaSelecionadaIndex] ?? null;
  }

  // =========================================
  // CARROSSEL
  // =========================================

  selecionarConta(index: number): void {

    if (
      !this.contaFinanceiraDados ||
      this.contaFinanceiraDados.length === 0
    ) {
      return;
    }

    this.contaSelecionadaIndex = index;

    this.atualizarIndicesCarrossel();

    this.atualizarGraficoSaldo();

    this.listarUltimasTransacoes();
  }

  selecionarAnterior(): void {

    if (!this.contaFinanceiraDados.length) {
      return;
    }

    this.contaSelecionadaIndex =
      this.contaSelecionadaIndex === 0
        ? this.contaFinanceiraDados.length - 1
        : this.contaSelecionadaIndex - 1;

    this.atualizarIndicesCarrossel();

    this.atualizarGraficoSaldo();

    this.listarUltimasTransacoes();
  }

  selecionarProxima(): void {

    if (!this.contaFinanceiraDados.length) {
      return;
    }

    this.contaSelecionadaIndex =
      this.contaSelecionadaIndex === this.contaFinanceiraDados.length - 1
        ? 0
        : this.contaSelecionadaIndex + 1;

    this.atualizarIndicesCarrossel();

    this.atualizarGraficoSaldo();

    this.listarUltimasTransacoes();
  }

  private atualizarIndicesCarrossel(): void {

    const total = this.contaFinanceiraDados.length;

    if (total <= 1) {

      this.contaAnterior = 0;
      this.contaProxima = 0;

      return;
    }

    this.contaAnterior =
      this.contaSelecionadaIndex === 0
        ? total - 1
        : this.contaSelecionadaIndex - 1;

    this.contaProxima =
      this.contaSelecionadaIndex === total - 1
        ? 0
        : this.contaSelecionadaIndex + 1;
  }

  // =========================================
  // CRUD
  // =========================================

  public abrirDialog(): void {

    this.modoEdicao = false;

    this.idContaEdicao = null;

    this.formContaFinanceira.reset({

      nome: '',
      saldo: 0,
      cor: 'FFDE59'
    });

    this.exibirDialog = true;
  }

  public editarConta(conta: IContas | null): void {

    if (!conta) {
      return;
    }

    this.modoEdicao = true;

    this.idContaEdicao = conta.id ?? null;

    this.formContaFinanceira.patchValue({

      nome: conta.nome,

      saldo: conta.saldo,

      cor: conta.cor.replace('#', '')
    });

    this.exibirDialog = true;
  }

  public salvarConta(): void {

    if (this.formContaFinanceira.invalid) {
      return;
    }

    const formValue = this.formContaFinanceira.getRawValue();

    const payload: IContasRequest = {

      nome: formValue.nome,

      saldo: formValue.saldo,

      cor: `#${String(formValue.cor).replace('#', '')}`,
    };

    // =====================================
    // UPDATE
    // =====================================

    if (this.modoEdicao && this.idContaEdicao) {

      this.contaFinanceiraService
        .atualizarConta(payload, this.idContaEdicao)
        .subscribe({

          next: () => {

            this.exibirDialog = false;

            this.modoEdicao = false;

            this.idContaEdicao = null;

            this.formContaFinanceira.reset();

            this.listarContas();
          },

          error: (error) => {

            console.error(error);
          }
        });

      return;
    }

    // =====================================
    // CREATE
    // =====================================

    this.contaFinanceiraService
      .adicionarConta(payload)
      .subscribe({

        next: () => {

          this.exibirDialog = false;

          this.formContaFinanceira.reset({

            nome: '',
            saldo: 0,
            cor: ''
          });

          this.listarContas();
        },

        error: (error) => {

          console.error(error);
        }
      });
  }

// =========================================
// EXCLUSÃO COM CONFIRMAÇÃO
// =========================================

  public abrirDialogExcluir(conta: IContas | null): void {

    if (!conta) {
      return;
    }

    this.contaExcluir = conta;

    this.excluirDialog = true;
  }

  public fecharDialogExcluir(): void {

    this.excluirDialog = false;

    this.contaExcluir = null;
  }

  public confirmarExcluirConta(): void {

    if (!this.contaExcluir?.id) {
      return;
    }

    this.contaFinanceiraService
      .deletarContaPorId(this.contaExcluir.id)
      .subscribe({

        next: () => {

          this.fecharDialogExcluir();

          this.listarContas();
        },

        error: (res: Error) => {

          console.error('Erro ao deletar Conta', res);

          alert('Erro ao excluir conta');
        }
      });
  }

  // =========================================
  // LISTAGEM
  // =========================================

  public listarContas(): void {

    this.carregandoContas = true;

    this.contaFinanceiraService
      .listarContasUsuarioLogado()
      .subscribe({

        next: (res: IContas[]) => {

          if (!res || res.length === 0) {

            this.contaFinanceiraDados = [];

            this.contaSelecionadaIndex = 0;

            this.contaAnterior = 0;

            this.contaProxima = 0;

            this.atualizarGraficoSaldo();

            this.carregandoContas = false;

            this.cdr.detectChanges();

            return;
          }

          // ORDENA PELO MENOR ID
          this.contaFinanceiraDados = [...res].sort((a, b) => {

            const idA = a.id ?? 0;
            const idB = b.id ?? 0;

            return idA - idB;
          });

          // PRIMEIRA CONTA SELECIONADA
          this.contaSelecionadaIndex = 0;

          // SINCRONIZA CARROSSEL
          this.atualizarIndicesCarrossel();

          // SINCRONIZA GRÁFICO
          this.atualizarGraficoSaldo();

          this.listarUltimasTransacoes();

          // FINALIZA LOADING
          this.carregandoContas = false;

          // FORÇA RE-RENDER
          this.cdr.detectChanges();
        },

        error: (error: Error) => {

          console.error(error);

          this.carregandoContas = false;

          this.cdr.detectChanges();
        }
      });
  }

  // =========================================
  // GRÁFICO
  // =========================================

  public atualizarGraficoSaldo(): void {

    const saldoAtual = this.contaSelecionada?.saldo || 0;

    const labelsSemanal = [
      'Seg',
      'Ter',
      'Qua',
      'Qui',
      'Sex',
      'Sáb',
      'Dom'
    ];

    const labelsMensal = [
      'Jan',
      'Fev',
      'Mar',
      'Abr',
      'Mai',
      'Jun',
      'Jul',
      'Ago',
      'Set'
    ];

    const dadosSemanais = [
      1200,
      1800,
      1500,
      2200,
      3100,
      2800,
      saldoAtual
    ];

    const dadosMensais = [
      1200,
      2400,
      1800,
      3500,
      4200,
      3900,
      5100,
      4800,
      saldoAtual
    ];

    const labels =
      this.periodoSelecionado === 'semanal'
        ? labelsSemanal
        : labelsMensal;

    const dados =
      this.periodoSelecionado === 'semanal'
        ? dadosSemanais
        : dadosMensais;

    this.chartData = {

      labels: labels,

      datasets: [
        {

          label: 'Saldo',

          data: dados,

          tension: 0.4,

          fill: true,

          borderWidth: 3,

          pointRadius: 5,

          pointHoverRadius: 7,

          borderColor: '#F97316',

          backgroundColor: 'rgba(249, 115, 22, 0.12)'
        }
      ]
    };

    this.chartOptions = {

      maintainAspectRatio: false,

      responsive: true,

      plugins: {

        legend: {
          display: false
        }
      },

      scales: {

        x: {

          grid: {
            display: false
          },

          ticks: {
            color: '#64748B'
          }
        },

        y: {

          beginAtZero: true,

          grid: {
            color: '#E2E8F0'
          },

          ticks: {
            color: '#64748B'
          }
        }
      }
    };
  }
}
