import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Meta } from '@models/IMetas.models';
import { Button } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';

@Component({
  selector: 'app-meta-item',
  imports: [Button, ProgressBarModule],
  templateUrl: './meta-item.component.html',
  styleUrl: './meta-item.component.css',
})
export class MetaItemComponent {
  @Input() meta!: Meta;
  @Output() deletarMeta: EventEmitter<number> = new EventEmitter<number>();

  onDeletar(): void {
    this.deletarMeta.emit(this.meta.id);
  }

  get valorFalta(): number { return this.meta.valorTotal - this.meta.valorAtual; }
}
