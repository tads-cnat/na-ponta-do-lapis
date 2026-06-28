import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MetaResponse } from '@models/IMetas.models';
import { Button } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';

@Component({
  selector: 'app-meta-item',
  imports: [Button, ProgressBarModule],
  templateUrl: './meta-item.component.html',
  styleUrl: './meta-item.component.css',
})
export class MetaItemComponent {
  @Input() meta!: MetaResponse;
  @Output() deletarMeta: EventEmitter<number> = new EventEmitter<number>();
  @Output() editarMeta: EventEmitter<MetaResponse> = new EventEmitter<MetaResponse>();

  onDeletar(): void {
    this.deletarMeta.emit(this.meta.id);
  }

  onEditar(): void {
    this.editarMeta.emit(this.meta);
  }

  get valorFalta(): number { return this.meta.valorTotal - this.meta.valorAtual!; }
}
