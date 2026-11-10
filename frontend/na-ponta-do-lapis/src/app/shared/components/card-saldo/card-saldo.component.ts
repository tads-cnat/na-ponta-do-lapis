import { ChangeDetectorRef, Component, inject, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { PrimeNGModuleModule } from '../../primeNg.module';
import { ContaFinanceiraService } from '../../../pages/contas/service/contas.service';
import { TransacoesService } from '../../../pages/transacoes/service/transacoes.service';
import { IContas, ehCartaoCredito } from '../../../model/IContas.models';
import { ITransacoes } from '../../../model/ITransacoes.model';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { AppMoedaPipe } from '../../pipes/app-moeda.pipe';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-card-saldo',
  imports: [PrimeNGModuleModule, CommonModule, AppMoedaPipe],
  templateUrl: './card-saldo.component.html',
  styleUrl: './card-saldo.component.css',
})
export class CardSaldoComponent implements OnInit, OnChanges {

  @Input() dataRange: Date[] | null = null;
  @Input() periodoRapido: 'TODOS' | 'MES_ATUAL' | 'MES_ANTERIOR' | 'ULTIMOS_30' | 'ANO_ATUAL' | 'CUSTOM' = 'TODOS';
  @Input() contaId: number | null = null;

  contaService = inject(ContaFinanceiraService);
  transacoesService = inject(TransacoesService);
  private cdr = inject(ChangeDetectorRef);
  private messageService = inject(MessageService);

  contas: IContas[] = [];
  todasTransacoes: ITransacoes[] = [];

  saldoInicial: number = 0;
  saldoFinal: number = 0;
  variacaoPercentual: number = 0;

  totalDinheiroContas: number = 0;
  totalFaturasCartoes: number = 0;
  totalDespesasPendentes: number = 0;
  totalReceitasPendentes: number = 0;
  totalSaldoDevedor: number = 0;
  balancoLiquido: number = 0;
  balancoProjetado: number = 0;
  contaSelecionadaEhCartao: boolean = false;
  contaSelecionadaNome: string = '';
  faturaFechadaSelecionada: number = 0;
  faturaAbertaSelecionada: number = 0;
  vencimentoFaturaSelecionada: string | null = null;
  limiteDisponivelSelecionado: number | null = null;
  limiteTotalSelecionado: number | null = null;

  headerFinal: string = 'Balanço líquido atual';
  subtituloInicial: string = 'Referente ao início do período';
  subtituloFinal: string = 'Disponível em suas contas';
  rodapeInicial: string = '';

  readonly meses = [
    'janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho',
    'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'
  ];

  ngOnInit(): void {
    this.listarContas();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.contas.length > 0 || this.todasTransacoes.length > 0) {
      this.recalcular();
    }
  }

  private formatarData(d: Date): string {
    const dia = String(d.getDate()).padStart(2, '0');
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    const ano = d.getFullYear();
    return `${dia}/${mes}/${ano}`;
  }

  public listarContas(): void {
    forkJoin({
      contas: this.contaService.listarContasUsuarioLogado().pipe(catchError(() => of([]))),
      transacoes: this.transacoesService.listarTransacoes().pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ contas, transacoes }) => {
        this.contas = contas || [];
        this.todasTransacoes = Array.isArray(transacoes) ? transacoes : [];
        this.recalcular();
      },
      error: (err: Error) => {
        console.error(err);
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro ao carregar dados do saldo',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
      }
    });
  }

  public recalcular(): void {
    let saldoBaseAtual = 0;
    let transacoesFiltradas = this.todasTransacoes;

    const contasCorrentes = this.contas.filter(c => !ehCartaoCredito(c));
    const contasCartao = this.contas.filter(c => ehCartaoCredito(c));

    this.totalDinheiroContas = contasCorrentes.reduce((acc, c) => acc + (Number(c.saldo) || 0), 0);
    this.totalFaturasCartoes = contasCartao.reduce((acc, c) => acc + (Number(c.saldo) || 0), 0);
    this.totalDespesasPendentes = this.todasTransacoes
      .filter(t => t.tipo !== 'RECEITA' && t.estado === 'PENDENTE')
      .reduce((acc, t) => acc + (Number(t.valor) || 0), 0);
    this.totalReceitasPendentes = this.todasTransacoes
      .filter(t => t.tipo === 'RECEITA' && t.estado === 'PENDENTE')
      .reduce((acc, t) => acc + (Number(t.valor) || 0), 0);
    this.totalSaldoDevedor = this.totalFaturasCartoes + this.totalDespesasPendentes;
    this.balancoLiquido = this.totalDinheiroContas - this.totalSaldoDevedor;
    this.balancoProjetado = this.balancoLiquido + this.totalReceitasPendentes;

    if (this.contaId !== null) {
      const contaSelecionada = this.contas.find(c => c.id === this.contaId);
      this.contaSelecionadaNome = contaSelecionada?.nome || 'Conta';
      this.contaSelecionadaEhCartao = ehCartaoCredito(contaSelecionada);
      if (this.contaSelecionadaEhCartao) {
        this.faturaFechadaSelecionada = Number(contaSelecionada?.valorFaturaFechada ?? contaSelecionada?.saldo ?? 0);
        this.faturaAbertaSelecionada = Number(contaSelecionada?.valorFaturaAberta ?? 0);
        this.vencimentoFaturaSelecionada = contaSelecionada?.dataVencimentoFatura ?? null;
        this.limiteDisponivelSelecionado = contaSelecionada?.limiteDisponivel ?? null;
        this.limiteTotalSelecionado = contaSelecionada?.limiteTotal ?? null;
        saldoBaseAtual = -(Number(contaSelecionada?.saldo) || 0);
      } else {
        saldoBaseAtual = contaSelecionada ? Number(contaSelecionada.saldo) || 0 : 0;
      }
      transacoesFiltradas = this.todasTransacoes.filter(t => t.conta?.id === this.contaId);
    } else {
      this.contaSelecionadaEhCartao = false;
      this.contaSelecionadaNome = '';
      saldoBaseAtual = this.balancoLiquido;
    }

    if (this.contas.length === 0) {
      this.saldoInicial = 0;
      this.saldoFinal = 0;
      this.variacaoPercentual = 0;
      this.headerFinal = 'Balanço líquido atual';
      this.subtituloInicial = 'Referente ao início do período';
      this.subtituloFinal = 'Disponível em suas contas';
      this.rodapeInicial = 'Sem contas cadastradas';
      this.cdr.detectChanges();
      return;
    }

    const agora = new Date();
    let dataInicio: Date | null = null;
    let dataFim: Date | null = null;

    if (this.dataRange && this.dataRange.length > 0 && this.dataRange[0]) {
      dataInicio = new Date(this.dataRange[0]);
      dataInicio.setHours(0, 0, 0, 0);

      const refFim = this.dataRange[1] || this.dataRange[0];
      dataFim = new Date(refFim);
      dataFim.setHours(23, 59, 59, 999);
    } else if (this.periodoRapido === 'TODOS') {
      dataInicio = null;
      dataFim = null;
    } else if (this.periodoRapido === 'MES_ATUAL') {
      dataInicio = new Date(agora.getFullYear(), agora.getMonth(), 1, 0, 0, 0, 0);
      dataFim = new Date(agora.getFullYear(), agora.getMonth() + 1, 0, 23, 59, 59, 999);
    } else if (this.periodoRapido === 'MES_ANTERIOR') {
      dataInicio = new Date(agora.getFullYear(), agora.getMonth() - 1, 1, 0, 0, 0, 0);
      dataFim = new Date(agora.getFullYear(), agora.getMonth(), 0, 23, 59, 59, 999);
    } else if (this.periodoRapido === 'ULTIMOS_30') {
      dataInicio = new Date();
      dataInicio.setDate(dataInicio.getDate() - 30);
      dataInicio.setHours(0, 0, 0, 0);
      dataFim = new Date();
      dataFim.setHours(23, 59, 59, 999);
    } else if (this.periodoRapido === 'ANO_ATUAL') {
      dataInicio = new Date(agora.getFullYear(), 0, 1, 0, 0, 0, 0);
      dataFim = new Date(agora.getFullYear(), 11, 31, 23, 59, 59, 999);
    }

    // Saldo Inicial: desfaz movimentações realizadas de dataInicio até agora
    if (dataInicio === null) {
      let receitasHist = 0;
      let despesasHist = 0;
      transacoesFiltradas.forEach(t => {
        if (t.estado === 'REALIZADA' && t.dataHora) {
          const val = Number(t.valor) || 0;
          if (t.tipo === 'RECEITA') receitasHist += val;
          else despesasHist += val;
        }
      });
      this.saldoInicial = saldoBaseAtual - receitasHist + despesasHist;
      this.subtituloInicial = 'Referente ao início do histórico';
      this.rodapeInicial = 'Saldo inicial de abertura';
    } else {
      let receitasAposInicio = 0;
      let despesasAposInicio = 0;
      transacoesFiltradas.forEach(t => {
        if (t.estado === 'REALIZADA' && t.dataHora) {
          const dt = new Date(t.dataHora);
          if (dt >= dataInicio) {
            const val = Number(t.valor) || 0;
            if (t.tipo === 'RECEITA') receitasAposInicio += val;
            else despesasAposInicio += val;
          }
        }
      });
      this.saldoInicial = saldoBaseAtual - receitasAposInicio + despesasAposInicio;

      if (this.periodoRapido === 'MES_ATUAL') {
        this.subtituloInicial = 'Referente ao início do mês';
        this.rodapeInicial = `Posição em 1º de ${this.meses[agora.getMonth()]}`;
      } else if (this.periodoRapido === 'MES_ANTERIOR') {
        const mesAntIdx = (agora.getMonth() - 1 + 12) % 12;
        this.subtituloInicial = `Referente ao início de ${this.meses[mesAntIdx]}`;
        this.rodapeInicial = `Posição em 1º de ${this.meses[mesAntIdx]}`;
      } else {
        this.subtituloInicial = 'Referente ao início do período';
        this.rodapeInicial = `Posição em ${this.formatarData(dataInicio)}`;
      }
    }

    // Saldo Final: se o período encerrou no passado, desfaz movimentações após dataFim
    if (dataFim === null || dataFim >= agora) {
      this.saldoFinal = saldoBaseAtual;
      if (this.contaId !== null) {
        this.headerFinal = this.contaSelecionadaEhCartao ? 'Fatura atual' : 'Saldo atual';
        this.subtituloFinal = this.contaSelecionadaEhCartao ? 'Fatura acumulada no cartão' : 'Disponível na conta selecionada';
      } else {
        this.headerFinal = 'Balanço líquido atual';
        this.subtituloFinal = 'Dinheiro em contas menos faturas';
      }
    } else {
      let receitasAposFim = 0;
      let despesasAposFim = 0;
      transacoesFiltradas.forEach(t => {
        if (t.estado === 'REALIZADA' && t.dataHora) {
          const dt = new Date(t.dataHora);
          if (dt > dataFim) {
            const val = Number(t.valor) || 0;
            if (t.tipo === 'RECEITA') receitasAposFim += val;
            else despesasAposFim += val;
          }
        }
      });
      this.saldoFinal = saldoBaseAtual - receitasAposFim + despesasAposFim;
      this.headerFinal = 'Balanço final';
      this.subtituloFinal = `Posição em ${this.formatarData(dataFim)}`;
    }

    // Variação Percentual entre o Saldo Inicial e o Saldo Final do período
    const diferenca = this.saldoFinal - this.saldoInicial;
    if (this.saldoInicial !== 0) {
      this.variacaoPercentual = (diferenca / Math.abs(this.saldoInicial)) * 100;
    } else if (this.saldoFinal !== 0) {
      this.variacaoPercentual = this.saldoFinal > 0 ? 100 : -100;
    } else {
      this.variacaoPercentual = 0;
    }

    this.cdr.detectChanges();
  }

}
