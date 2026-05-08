import { ChangeDetectorRef, Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { Conta, EstadoTransacao, ITransacoes } from '../../model/ITransacoes';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransacoesService } from './service/transacoes.service';

@Component({
  selector: 'app-transacoes',
  imports: [PrimeNGModuleModule, CommonModule, FormsModule],
  templateUrl: './transacoes.component.html',
  styleUrl: './transacoes.component.css',
})
export class TransacoesComponent {
   exibirDialog: boolean = false;
    transacoesDados: any[] = [];
   constructor(private transacoesService:TransacoesService, private cdr: ChangeDetectorRef){
    
   }

   ngOnInit():void {
    this.listarTransacoes()
   }

   public listarTransacoes():any {
    this.transacoesService.listarTransacoes().subscribe({
      next: (res:any) => {
        this.transacoesDados = res
        //serve para atualizar a pagina após receber a lista de transacoes
        this.cdr.detectChanges()
      },
      error: (erro:Error) => {
        console.log(erro)
      }
    })
   }

   excluir(id: number):void {
    this.transacoesService.deletarTransacaoPorId(id).subscribe({
      next: (res:any) => {
        console.log(res)
        this.listarTransacoes()
      },
      error: (res:Error) => {
        console.error("Erro ao deletar Transação", res)
      }
    })
  }
   
  
 
  novaTransacao: ITransacoes = this.resetForm();

  
  opcoesConta: { label: string, value: Conta }[] = [
  ];

  opcoesEstado: { label: string, value: EstadoTransacao }[] = [
  ];

  tiposTransacao = [
  ];




  resetForm(): ITransacoes {
    return {
      id: 0,
      descricao: '',
      categoria: '',
      conta: 'Santander',
      tipoTransacao: 'RECEITA',
      estado: 'PENDENTE',
      valor: 0,
      dataHora: new Date()
    };
  }

  abrirDialog() {
    this.novaTransacao = this.resetForm();
    this.exibirDialog = true;
  }

  salvar() {
  }

  

  getSeverity(status: string) {
    return status === 'RECEITA' ? 'success' : 'danger';
  }
}
