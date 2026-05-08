import { Component, OnInit } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { RouterLink } from '@angular/router'
import { IconComponent } from '../../shared/components/icon/icon.component';


@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, IconComponent],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent implements OnInit {
  aberto: boolean = false;
  items: MenuItem[] | undefined ;

  ngOnInit(): void {
    this.items = [
      {label: 'Transações', icon: 'transacao', routerLink:'/app/transacoes'},
      {label: 'Contas', icon: 'conta', routerLink:''},
      {label: 'Metas', icon: 'meta', routerLink:''},
      {label: 'Familia', icon: 'familia', routerLink:''},
      {label: 'Dashboard', icon:'dashboard', routerLink:''},
      {label: 'Ajustes', icon:'ajuste', routerLink:''},
    ]
  }

  toggleSidebar(){
    this.aberto =  !this.aberto;
  }

}
