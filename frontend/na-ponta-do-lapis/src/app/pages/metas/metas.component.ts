import { Component } from '@angular/core';
import { MetaItemComponent } from './components/meta-item/meta-item.component';
import { Button } from 'primeng/button';

@Component({
  selector: 'app-metas',
  imports: [MetaItemComponent, Button],
  templateUrl: './metas.component.html',
  styleUrl: './metas.component.css',
})
export class MetasComponent {
  
  constructor(){}

  public listarMetas(): any {
      this.transacozService.listarTransacoes().subscribe({
        next: (res: ITransacoes[]) => {
          this.transacoesDados = res
          this.cdr.detectChanges()
        },
        error: (erro: Error) => {
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
}
