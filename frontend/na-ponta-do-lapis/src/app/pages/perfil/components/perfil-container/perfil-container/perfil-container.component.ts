import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AreaPerfilComponentComponent } from '../../area-perfil-component/area-perfil-component.component';
import { AjustesPerfilComponent } from '../../ajustes-do-perfil/ajustes-do-perfil.component';
import { StorageService } from '../../../../../auth/service/storage.service';


@Component({
  selector: 'app-perfil-container',
  imports: [AreaPerfilComponentComponent, AjustesPerfilComponent],
  templateUrl: './perfil-container.component.html',
  styleUrl: './perfil-container.component.css',
})
export class PerfilContainerComponent {
  private readonly router = inject(Router);

  sairDaConta(): void {
    StorageService.logout();
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }
}
