import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { PrimeNGModuleModule } from '../../primeNg.module';
import { ContaFinanceiraService } from '../../../pages/contas/service/contas.service';
import { TransacoesService } from '../../../pages/transacoes/service/transacoes.service';
import { IContas } from '../../../model/IContas.models';
import { ITransacoes } from '../../../model/ITransacoes.model';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-card-saldo',
  imports: [PrimeNGModuleModule, CommonModule],
  templateUrl: './card-saldo.component.html',
  styleUrl: './card-saldo.component.css',
})
export class CardSaldoComponent implements OnInit {

  contaService = inject(ContaFinanceiraService);
  transacoesService = inject(TransacoesService);
  private cdr = inject(ChangeDetectorRef);
  private messageService = inject(MessageService);

  contas: IContas[] = [];
  saldoTotal: number = 0;
  saldoInicial: number = 0;
  variacaoPercentual: number = 0;
  nomeMesAtual: string = '';

  ngOnInit(): void {
    this.atualizarNomeMes();
    this.listarContas();
  }

  private atualizarNomeMes(): void {
    const meses = [
      'janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho',
      'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'
    ];
    this.nomeMesAtual = meses[new Date().getMonth()];
  }

  public listarContas(): void {
    forkJoin({
      contas: this.contaService.listarContasUsuarioLogado().pipe(catchError(() => of([]))),
      transacoes: this.transacoesService.listarTransacoes().pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ contas, transacoes }) => {
        this.contas = contas || [];
        this.saldoTotal = this.contas.reduce((acc, c) => acc + (Number(c.saldo) || 0), 0);

        const agora = new Date();
        const inicioDoMes = new Date(agora.getFullYear(), agora.getMonth(), 1, 0, 0, 0, 0);

        let receitasMes = 0;
        let despesasMes = 0;

        if (Array.isArray(transacoes)) {
          transacoes.forEach((t: ITransacoes) => {
            if (t.estado === 'REALIZADA' && t.dataHora) {
              const dataT = new Date(t.dataHora);
              if (dataT >= inicioDoMes && dataT <= agora) {
                const valor = Number(t.valor) || 0;
                if (t.tipo === 'RECEITA') {
                  receitasMes += valor;
                } else {
                  despesasMes += valor;
                }
              }
            }
          });
        }

        if (this.contas.length === 0) {
          this.saldoInicial = 0;
          this.saldoTotal = 0;
          this.variacaoPercentual = 0;
        } else {
          this.saldoInicial = this.saldoTotal - receitasMes + despesasMes;

          if (this.saldoInicial !== 0) {
            this.variacaoPercentual = ((this.saldoTotal - this.saldoInicial) / Math.abs(this.saldoInicial)) * 100;
          } else if (this.saldoTotal !== 0) {
            this.variacaoPercentual = 100;
          } else {
            this.variacaoPercentual = 0;
          }
        }

        this.cdr.detectChanges();
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

}
