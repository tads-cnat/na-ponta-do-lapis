import { Component, Input } from '@angular/core';
import { Button } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';

@Component({
  selector: 'app-meta-item',
  imports: [Button, ProgressBarModule],
  templateUrl: './meta-item.component.html',
  styleUrl: './meta-item.component.css',
})
export class MetaItemComponent {
  @Input() titulo: string = "";
  @Input() valorAtual: number =0;
  @Input() valorTotal: number = 0;
  
  get valorFalta(): number { return this.valorTotal - this.valorAtual; }
  get progressoAtual(): number { return this.valorAtual / this.valorTotal * 100; }
}
