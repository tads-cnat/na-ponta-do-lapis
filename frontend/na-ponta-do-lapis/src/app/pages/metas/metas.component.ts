import { Component, OnInit, signal } from '@angular/core';
import { MetaItemComponent } from './components/meta-item/meta-item.component';
import { MetasService } from './services/metas.service';
import { Button } from 'primeng/button';

@Component({
  selector: 'app-metas',
  imports: [MetaItemComponent, Button],
  templateUrl: './metas.component.html',
  styleUrl: './metas.component.css',
})
export class MetasComponent implements OnInit {
  metas = signal<any[]>([]);
  constructor(private metasService: MetasService) {}

  ngOnInit(): void {
    this.listarMetas();
  }

  public listarMetas(): void {
    this.metasService.listarMetas().subscribe({
      next: (response) => {
        this.metas.set(response);
        console.log(this.metas());
      },
      error: (error) => {
        console.error('Erro ao listar metas:', error);
      }
    });
  }
 
}
