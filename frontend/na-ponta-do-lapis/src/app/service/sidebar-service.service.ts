import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  aberto = signal(false);

  toggleSidebar() {
    this.aberto.update(valor => !valor);
  }

  fecharSidebar() {
    this.aberto.set(false);
  }
}