import { Component, inject } from '@angular/core';
import { DadosPerfilComponent } from '../dados-perfil-component/dados-perfil-component.component';
import { TemaComponentComponent } from '../tema-component/tema-component.component';
import { PwaService } from '../../../../shared/service/pwa.service';

@Component({
  selector: 'app-informacoes-perfil-component',
  imports: [DadosPerfilComponent,TemaComponentComponent],
  templateUrl: './informacoes-perfil-component.component.html',
  styleUrl: './informacoes-perfil-component.component.css',
})
export class InformacoesPerfilComponentComponent {
  protected readonly pwaService = inject(PwaService);
}
