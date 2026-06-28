import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { PrimeNGModuleModule } from '../../shared/primeNg.module';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MarcadorService } from '../transacoes/service/marcador.service';

@Component({
  selector: 'app-marcador-dlg',
  imports: [PrimeNGModuleModule, CommonModule, ReactiveFormsModule],
  templateUrl: './marcador-dlg.component.html',
  styleUrl: './marcador-dlg.component.css',
})
export class MarcadorDlgComponent {
  formMarcador: FormGroup;

  @Input() exibirDialog: boolean = false;
  @Output() onHide = new EventEmitter<void>();
  marcadores:any[] = []
  fb = inject(FormBuilder);
  marcadorService = inject(MarcadorService)
  constructor(){
    this.formMarcador = this.fb.group({
      nome: [``, [Validators.required, Validators.minLength(3)]],
      cor: [`#3b82f6`, [Validators.required]]
    })
  }

  ngOnInit(): void {
    this.listarMarcadores()
}

public fecharModal() {
  this.onHide.emit();
}
  public salvar(){}
  public listarMarcadores(){
    this.marcadorService.listar().subscribe({
      next: (res:any) => {
        this.marcadores = res
        
      }, 
      error: (erro:Error) => {
        console.error(erro)
      }
    })
  }


  public excluirMarcador(marcadorId:number){
    this.marcadorService.excluir(marcadorId).subscribe({
      next: (res:any) => {
        this.listarMarcadores()
      },
      error: (erro:Error) => {
        console.error(erro)
      }
    })
  }

exibirFormulario: boolean = false;

public abrirFormulario(): void {
    this.exibirFormulario = true;
}

public cancelarCriacao(): void {
    this.exibirFormulario = false;
    this.formMarcador.reset({ cor: '#3b82f6' }); 
}

public salvarMarcador(): void {
    if (this.formMarcador.valid) {
        const novoMarcador = this.formMarcador.value;
        
       
        this.marcadorService.salvar(novoMarcador).subscribe({
            next: () => {
                this.listarMarcadores();
                this.cancelarCriacao();
            },
            error: (erro: Error) => {
                console.error(erro);
            }
        }); 
    }
}
}
