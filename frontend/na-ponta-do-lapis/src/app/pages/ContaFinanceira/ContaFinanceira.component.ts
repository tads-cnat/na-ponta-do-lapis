import { UsuarioResume } from './../../model/IUsuario.models';
import { ChangeDetectorRef, Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CommonModule } from '@angular/common';
import { ColorPickerModule } from 'primeng/colorpicker';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ContaFinanceiraService } from './service/ContaFinanceira.service';
import { IContaFinanceira, IContaFinanceiraRequest } from '../../model/IContas.models';

@Component({
  selector: 'app-ContaFinanceira',
  imports: [PrimeNGModuleModule, CommonModule, ReactiveFormsModule, ColorPickerModule],
  templateUrl: './ContaFinanceira.component.html',
  styleUrl: './ContaFinanceira.component.css',
})
export class ContaFinanceiraComponent {
    exibirDialog: boolean = false;
    contaFinanceiraDados: any[] = [];

   formContaFinanceira:FormGroup;
   constructor(private fb:FormBuilder ,private contaFinanceiraService:ContaFinanceiraService, private cdr: ChangeDetectorRef){
    this.formContaFinanceira = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      saldo: [0, [Validators.required, Validators.min(0.01)]],
      UsuarioResume: ['', [Validators.required]]
    })
  }

   ngOnInit():void {
    this.listarContas()
   }

   public listarConta():any {
    this.contaFinanceiraService.listarContasUsuarioLogado().subscribe({
      next: (res:any) => {
        this.contaFinanceiraDados = res
        //serve para atualizar a pagina após receber a lista de transacoes
        this.cdr.detectChanges()
      },
      error: (erro:Error) => {
        console.log(erro)
        alert("Erro ao listar CONTA(LEMBRAR DE COLOCAR UM FEEDBACK MELHOR)")
      }
    })
   }

   public excluir(id: number):void {
    this.contaFinanceiraService.deletarContaPorId(id).subscribe({
      next: (res:any) => {
        console.log(res)
        this.listarContas()
      },
      error: (res:Error) => {
        console.error("Erro ao deletar Conta", res)
        alert("Erro ao EXCLUIR CONTA(LEMBRAR DE COLOCAR UM FEEDBACK MELHOR)")
      }
    })
  }


  novaConta: IContaFinanceira = this.resetForm();
  opcoesConta:IContaFinanceiraRequest[] = [];

  public listarContas(){
      this.contaFinanceiraService.listarContasUsuarioLogado().subscribe({
      next: (res:IContaFinanceiraRequest[]) => {
        this.opcoesConta = res
      },
      error: (error:Error) => {
        console.error("Erro ao listar Contas Financeiras", error)
        alert("Erro ao listar CONTA(LEMBRAR DE COLOCAR UM FEEDBACK MELHOR)")
      }
    })
  }

  resetForm(): IContaFinanceira {
    return {
      id: 0,
      nome: '',
      saldo: 0,
      cor: undefined,
      usuarioResume: null,
    };
  }

  abrirDialog() {
    this.novaConta = this.resetForm();
    this.exibirDialog = true;
  }

  salvar() {
  }

}
