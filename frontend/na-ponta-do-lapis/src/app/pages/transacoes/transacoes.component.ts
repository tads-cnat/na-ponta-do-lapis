import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransacoesService } from './service/transacoes.service';
import { Categoria, ITransacoes, Tipo } from '../../model/ITransacoes.model';
import { ContasRequest } from '../../model/IContas.models';
import { Marcador } from '../../model/IMarcador.models';
import { Popover } from 'primeng/popover';

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
      idCategoria: ['', [Validators.required]],
      valor: [0, [Validators.required, Validators.min(0.01)]],
      idContaFinanceira: ['', [Validators.required]],
      dataHora: [[null, Validators.required]],
      estado: ['', [Validators.required]],
      tipo: ['', [Validators.required]],
      marcadorId: [null]
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
  opcoesConta:ContasRequest[] = [];

  public listarContas(){
      this.transacoesService.listarContasUsuarioLogado().subscribe({
      next: (res:ContasRequest[]) => {
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

  marcadorSelecionado: any = null
  opcoesMarcador:Marcador[] = []
  @ViewChild('op') op!: Popover;
  toggle(event:any){
    this.op.toggle(event);
  }
  selecionarMarcador(marcador:Marcador){
    this.marcadorSelecionado = marcador
    this.formTransacao.patchValue({
      marcadorId: marcador.id
    })
    this.op.hide();
    this.formTransacao.get('marcadorId')?.markAsDirty();
  }
  
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
    if(this.formTransacao.valid){
      const dadosParaEnviar = this.formTransacao.value
      console.log(dadosParaEnviar)

      this.transacoesService.adicionarTransacao(dadosParaEnviar).subscribe({
        next: (res:ITransacoes) => {
          console.log(res)
          alert("Lembrar de colocar um feedback melhor. TRANSACAO ENVIADA COM SUCESSO")
        },
        error: (error:Error) => {
          alert("Errro ao enviar Transacao LEMBRAR DE COLOCAR UM FEEDBACK MELHOR" )
          console.error(error)
        }
      })
    }
  }

  

  getSeverity(status: string) {
    return status === 'RECEITA' ? 'success' : 'danger';
  }
}
