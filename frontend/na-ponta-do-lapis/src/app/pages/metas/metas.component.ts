import { Component, OnInit, signal } from '@angular/core';
import { MetaItemComponent } from './components/meta-item/meta-item.component';
import { MetasService } from './services/metas.service';
import { Meta } from '@models/IMetas.models';
import { Button } from 'primeng/button';

@Component({
  selector: 'app-metas',
  imports: [MetaItemComponent, Button],
  templateUrl: './metas.component.html',
  styleUrl: './metas.component.css',
})
export class MetasComponent implements OnInit {

  metas = signal<Meta[]>([]);

  constructor(private metasService: MetasService) {}

  ngOnInit(): void {
    this.listarMetas();
  }

  listarMetas(): void {
    this.metasService.listarMetas().subscribe({
      next: (response) => {
        this.metas.set(response);
      },
      error: (error) => {
        console.error('Erro ao listar metas:', error);
      }
    });
  }

  deletarMeta(id: number): void {
    this.metasService.deletarMeta(id).subscribe({
      next: () => {
        this.metas.update( metas => metas.filter(meta => meta.id !== id));
      },
      error: (error) => {
        console.error('Erro ao deletar meta:', error);
      }
    });
  }



}
