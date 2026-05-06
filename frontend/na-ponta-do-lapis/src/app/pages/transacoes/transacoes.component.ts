import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { Conta, EstadoTransacao, ITransacoes } from '../../model/ITransacoes';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-transacoes',
  imports: [PrimeNGModuleModule, CommonModule, FormsModule],
  templateUrl: './transacoes.component.html',
  styleUrl: './transacoes.component.css',
})
export class TransacoesComponent {
   exibirDialog: boolean = false;
  
 
  novaTransacao: ITransacoes = this.resetForm();

  
  opcoesConta: { label: string, value: Conta }[] = [
    { label: 'Nubank', value: 'Nubank' },
    { label: 'Santander', value: 'Santander' },
    { label: 'Itaú', value: 'Itau' },
    { label: 'Caixa', value: 'Caixa' }
  ];

  opcoesEstado: { label: string, value: EstadoTransacao }[] = [
    { label: 'Pendente', value: 'PENDENTE' },
    { label: 'Concluído', value: 'CONCLUIDO' },
    { label: 'Cancelado', value: 'CANCELADO' }
  ];

  tiposTransacao = [
    { label: 'Receita', value: 'RECEITA' },
    { label: 'Despesa', value: 'DESPESA' }
  ];

  transacoes: ITransacoes[] = [
    { id: 3, descricao: 'Supermercado Extra', categoria: 'Alimentação', estado: 'CONCLUIDO', tipoTransacao: 'DESPESA', dataHora: new Date(), conta: 'Santander', valor: 450.50 },
    { id: 4, descricao: 'Venda de Teclado', categoria: 'Vendas', estado: 'CONCLUIDO', tipoTransacao: 'RECEITA', dataHora: new Date(), conta: 'Santander', valor: 200.00 },
    { id: 5, descricao: 'Assinatura Netflix', categoria: 'Lazer', estado: 'PENDENTE', tipoTransacao: 'DESPESA', dataHora: new Date(), conta: 'Santander', valor: 55.90 },
    { id: 6, descricao: 'Bônus Mensal', categoria: 'Trabalho', estado: 'CONCLUIDO', tipoTransacao: 'RECEITA', dataHora: new Date(), conta: 'Santander', valor: 1200.00 },
    { id: 7, descricao: 'Academia Mensal', categoria: 'Saúde', estado: 'PENDENTE', tipoTransacao: 'DESPESA', dataHora: new Date(), conta: 'Santander', valor: 110.00 }
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
    const novoId = this.transacoes.length > 0 ? Math.max(...this.transacoes.map(t => t.id)) + 1 : 1;
    const transacaoParaAdicionar = { ...this.novaTransacao, id: novoId };
    
    this.transacoes = [...this.transacoes, transacaoParaAdicionar];
    this.exibirDialog = false;
  }

  excluir(id: number) {
    this.transacoes = this.transacoes.filter(t => t.id !== id);
  }

  getSeverity(status: string) {
    return status === 'RECEITA' ? 'success' : 'danger';
  }
}
