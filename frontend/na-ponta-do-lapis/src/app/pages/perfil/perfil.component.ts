import { Component } from '@angular/core';
import { PerfilContainerComponent } from './components/perfil-container/perfil-container/perfil-container.component';

@Component({
  selector: 'app-perfil',
  imports: [PerfilContainerComponent],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.css',
})
export class PerfilComponent {}
