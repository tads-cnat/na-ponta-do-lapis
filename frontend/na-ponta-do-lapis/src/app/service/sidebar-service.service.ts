import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  aberto = signal(true);

  toggleSidebar() {
    this.aberto.update(valor => !valor);
  }

  fecharSidebar() {
    this.aberto.set(false);
  }
}
