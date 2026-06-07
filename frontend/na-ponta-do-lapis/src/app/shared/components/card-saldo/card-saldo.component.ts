import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { PrimeNGModuleModule } from '../../primeNg.module';
import { ContaFinanceiraService } from '../../../pages/contas/service/contas.service';
import { IContas } from '../../../model/IContas.models';
import { MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-card-saldo',
  imports: [PrimeNGModuleModule, CommonModule],
  templateUrl: './card-saldo.component.html',
  styleUrl: './card-saldo.component.css',
})
export class CardSaldoComponent {

  contaService = inject(ContaFinanceiraService);
  private cdr = inject(ChangeDetectorRef);
  private messageService = inject(MessageService)

  contas:IContas[] = [];
  saldoTotal:number = 0;

  
  ngOnInit(): void{
    this.listarContas();
  }

  public listarContas(): void {
    this.contaService.listarContasUsuarioLogado().subscribe({
      next: (res:IContas[]) => {
        this.contas = res;
        console.log(this.contas)
        this.saldoTotal = 0;
        res.forEach(atual =>  this.saldoTotal += atual.saldo);
        this.cdr.detectChanges();
      },
      error: (err:Error) => {
         console.log(err)
        this.messageService.add({
          severity: 'warn',
          summary: 'Erro as carregar Dados da conta',
          detail: '',
          life: 2000
        });
        this.cdr.detectChanges();
      }
    })
  }

}
