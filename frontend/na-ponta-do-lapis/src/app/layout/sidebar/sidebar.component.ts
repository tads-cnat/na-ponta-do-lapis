import { Component, inject, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { RouterLink, RouterLinkActive } from '@angular/router'
import { IconComponent } from '../../shared/components/icon/icon.component';
import { SidebarService } from '../../service/sidebar-service.service';


@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, IconComponent, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent implements OnInit {
  aberto: boolean = true;
  items: MenuItem[] | undefined ;
  sidebarService: SidebarService = inject(SidebarService);
  isMobile: boolean = false;

  ngOnInit(): void {
    this.items = [
      {label: 'Dashboard', icon:'dashboard', routerLink:'/app/dashboard'},
      {label: 'Transações', icon: 'transacao', routerLink:'/app/transacoes'},
      {label: 'Contas', icon: 'conta', routerLink:'/app/contas'},
      {label: 'Metas', icon: 'meta', routerLink:'/app/metas'},
      {label: 'Familia', icon: 'familia', routerLink:'/app/familia'},
      {label: 'Perfil', icon:'ajuste', routerLink:'/app/perfil'},
    ]
    if (window.innerWidth < 769) {
      this.isMobile = true;
    }
  }

  toggleSidebar(){
    this.aberto = !this.aberto;
  }

}
