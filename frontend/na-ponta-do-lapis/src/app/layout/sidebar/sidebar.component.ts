import { Component, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { RouterLink, RouterLinkActive } from '@angular/router'
import { IconComponent } from '../../shared/components/icon/icon.component';


@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, IconComponent, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent implements OnInit {
  aberto: boolean = false;
  items: MenuItem[] | undefined ;

  ngOnInit(): void {
    this.items = [
      {label: 'Transações', icon: 'transacao', routerLink:'/app/transacoes'},
      {label: 'Contas', icon: 'conta', routerLink:'/app/contas'},
      {label: 'Metas', icon: 'meta', routerLink:'/app/metas'},
      {label: 'Familia', icon: 'familia', routerLink:'/app/familia'},
      {label: 'Dashboard', icon:'dashboard', routerLink:'/app/dashboard'},
      {label: 'Ajustes', icon:'ajuste', routerLink:'/app/ajustes'},
    ]
  }

  toggleSidebar(){
    this.aberto = !this.aberto;
  }

}
