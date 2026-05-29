import { Component } from '@angular/core';
import { Button } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';

@Component({
  selector: 'app-meta-item',
  imports: [Button, ProgressBarModule],
  templateUrl: './meta-item.component.html',
  styleUrl: './meta-item.component.css',
})
export class MetaItemComponent {
  titulo: string = "Primeira Casa";
  valorAtual: number = 300.00;
  valorTotal: number = 500.00;
  valorFalta: number = this.valorTotal - this.valorAtual;
  progressoAtual: number = this.valorAtual / this.valorTotal * 100;
}
