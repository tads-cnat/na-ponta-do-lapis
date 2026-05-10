import { ChangeDetectorRef, Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransacoesService } from './service/transacoes.service';
import { Categoria, ITransacoes, Tipo } from '../../model/ITransacoes.model';
import { IContaFinanceira, IContaFinanceiraRequest } from '../../model/IContas.models';
import { Marcador } from '../../model/IMarcador.models';

@Component({
  selector: 'app-transacoes',
  imports: [PrimeNGModuleModule, CommonModule, ReactiveFormsModule],
  templateUrl: './transacoes.component.html',
  styleUrl: './transacoes.component.css',
})
export class TransacoesComponent {
    exibirDialog: boolean = false;
    transacoesDados: any[] = [];

   formTransacao:FormGroup;
   constructor(private fb:FormBuilder ,private transacoesService:TransacoesService, private cdr: ChangeDetectorRef){
    this.formTransacao = this.fb.group({
      descricao: ['', [Validators.required, Validators.minLength(3)]],
      categoria: ['', [Validators.required]],
      valor: [0, [Validators.required, Validators.min(0.01)]],
      conta: ['', [Validators.required]],
      estado: ['', [Validators.required]],
      tipo: ['', [Validators.required]]
    })
  }

   ngOnInit():void {
    this.listarTransacoes()
    this.listarContas()
    this.listarCategorias()
    this.listarMarcadores()
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
        alert("Erro ao listar TRANSACOES(LEMBRAR DE COLOCAR UM FEEDBACK MELHOR)")
      }
    })
   }

   public excluir(id: number):void {
    this.transacoesService.deletarTransacaoPorId(id).subscribe({
      next: (res:any) => {
        console.log(res)
        this.listarTransacoes()
      },
      error: (res:Error) => {
        console.error("Erro ao deletar Transação", res)
        alert("Erro ao EXCLUIR TRANSACAO(LEMBRAR DE COLOCAR UM FEEDBACK MELHOR)")
      }
    })
  }


  novaTransacao: ITransacoes = this.resetForm();
  opcoesConta:IContaFinanceiraRequest[] = [];

  public listarContas(){
      this.transacoesService.listarContasUsuarioLogado().subscribe({
      next: (res:IContaFinanceiraRequest[]) => {
        this.opcoesConta = res
      },
      error: (error:Error) => {
        console.error("Erro ao listar Contas Financeiras", error)
        alert("Erro ao listar TRANSACOES(LEMBRAR DE COLOCAR UM FEEDBACK MELHOR)")
      }
    })
  }

  opcoesCategoria:Categoria[] = []
  public listarCategorias(){
    this.transacoesService.listarCategorias().subscribe({
      next: (res:Categoria[]) => {
        this.opcoesCategoria = res
      },
      error: (error:Error) =>{
        console.error("Erro ao listar Categorias", error)
        alert("Erro ao listar categorias(LEMBRAR DE COLOCAR UM FEEDBACK MELHOR)")
      }
    })
  }

  opcoesMarcador:Marcador[] = []
  public listarMarcadores(){
    this.transacoesService.listarMarcadores().subscribe({
      next: (res:Marcador[]) => {
        this.opcoesMarcador = res
        console.log(res)
      },
      error: (error:Error) => {
        console.error("Erro a listar Marcadores", error)
        alert("Erro ao listar marcadores(LEMBRAR DE COLOCAR UM FEEDBACK MELHOR)")
      }
    })
  }

  opcoesEstado:any[] = [
     { label: 'Pendente', value: 'PENDENTE' },
     { label: 'Realizada', value: 'REALIZADA' }
  ]
 opcoesTipo:any[] = [
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
      dataHora: new Date().toISOString()
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
