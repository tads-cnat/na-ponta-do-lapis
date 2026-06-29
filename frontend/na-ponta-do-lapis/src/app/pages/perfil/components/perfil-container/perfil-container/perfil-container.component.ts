import { Component } from '@angular/core';
import { AreaPerfilComponentComponent } from '../../area-perfil-component/area-perfil-component.component';
import { AjustesPerfilComponent } from '../../ajustes-do-perfil/ajustes-do-perfil.component';


@Component({
  selector: 'app-perfil-container',
  imports: [AreaPerfilComponentComponent, AjustesPerfilComponent],
  templateUrl: './perfil-container.component.html',
  styleUrl: './perfil-container.component.css',
})
export class PerfilContainerComponent {}
