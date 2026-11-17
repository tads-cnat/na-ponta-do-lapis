import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})

export class SidebarService {
  aberto = signal(typeof window !== 'undefined' ? window.innerWidth >= 769 : true);

  toggleSidebar() {
    this.aberto.update(valor => !valor);
  }

  fecharSidebar() {
    this.aberto.set(false);
  }
}
