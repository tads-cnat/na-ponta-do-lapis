import { ChangeDetectorRef, Component, inject, ViewChild } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransacoesService } from './service/transacoes.service';
import { Categoria, ITransacoes, Tipo } from '../../model/ITransacoes.model';
import { ContasRequest } from '../../model/IContas.models';
import { Marcador } from '../../model/IMarcador.models';
import { Popover } from 'primeng/popover';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-transacoes',
  imports: [PrimeNGModuleModule, CommonModule, ReactiveFormsModule],
  templateUrl: './transacoes.component.html',
  providers: [MessageService],
  styleUrl: './transacoes.component.css',
})
export class TransacoesComponent {
    exibirDialog: boolean = false;
    private messageService = inject(MessageService)

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
    transacoesDados: ITransacoes[] = [];
   public listarTransacoes():any {
    this.transacoesService.listarTransacoes().subscribe({
      next: (res:ITransacoes[]) => {
        this.transacoesDados = res
        this.cdr.detectChanges()
      },
      error: (erro:Error) => {
        console.log(erro)
            this.messageService.add({
               severity: 'warn',
               summary: 'Erro as carregar transação',
               detail: '',
            life: 2000
        });
      }
    })
   }

   public excluir(id: number):void {
    this.transacoesService.deletarTransacaoPorId(id).subscribe({
      next: (res:any) => {
        this.listarTransacoes()
            this.messageService.add({
               severity: 'success',
               summary: 'Transação excluida com sucesso',
               detail: '',
            life: 2000
        });
      },
      error: (res:Error) => {
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
  opcoesConta:ContasRequest[] = [];

  public listarContas(){
      this.transacoesService.listarContasUsuarioLogado().subscribe({
      next: (res:ContasRequest[]) => {
        this.opcoesConta = res
      },
      error: (error:Error) => {
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

  opcoesCategoria:Categoria[] = []
  public listarCategorias(){
    this.transacoesService.listarCategorias().subscribe({
      next: (res:Categoria[]) => {
        this.opcoesCategoria = res
      },
      error: (error:Error) =>{
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
      },
      error: (error:Error) => {
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
      dataHora: new Date().toISOString(),
      marcador: null
    };
  }

  abrirDialog() {
    this.novaTransacao = this.resetForm();
    this.exibirDialog = true;
  }

  salvar() {
    if(this.formTransacao.valid){
      const dadosParaEnviar = this.formTransacao.value
      this.exibirDialog = false
      this.transacoesService.adicionarTransacao(dadosParaEnviar).subscribe({
        next: (res:ITransacoes) => {
            this.messageService.add({
            severity: 'success',
            summary: 'Transação salva com sucesso',
            detail: '',
            life: 2000
        });
        },
        error: (error:Error) => {
               this.messageService.add({
               severity: 'warn',
               summary: 'Erro ao excluir transação',
               detail: '',
            life: 2000
        });
          console.error(error)
        }
      })
    }
  }

  

  getSeverity(status: string) {
    if(status == 'RECEITA'){
      return 'success'
    }
    if(status == 'DESPESA'){
      return 'danger'
    }
    if (status == 'PENDENTE'){
      return 'danger'
    }
    if (status == 'REALIZADA'){
      return 'success'
    }
    return 'info'
  }
  
}
