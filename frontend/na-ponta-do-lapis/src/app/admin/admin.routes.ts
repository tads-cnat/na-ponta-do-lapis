import { Routes } from '@angular/router';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      {
        path: 'dashboard',
        title: 'Admin Dashboard — Na Ponta do Lápis',
        loadComponent: () => import('./pages/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent),
      },
      {
        path: 'usuarios',
        title: 'Admin Usuários — Na Ponta do Lápis',
        loadComponent: () => import('./pages/admin-usuarios/admin-usuarios.component').then(m => m.AdminUsuariosComponent),
      },
      {
        path: 'familias',
        title: 'Admin Famílias — Na Ponta do Lápis',
        loadComponent: () => import('./pages/admin-familias/admin-familias.component').then(m => m.AdminFamiliasComponent),
      },
      {
        path: 'catalogos',
        title: 'Admin Catálogos — Na Ponta do Lápis',
        loadComponent: () => import('./pages/admin-catalogos/admin-catalogos.component').then(m => m.AdminCatalogosComponent),
      },
      {
        path: 'comunicacao',
        title: 'Admin Comunicação — Na Ponta do Lápis',
        loadComponent: () => import('./pages/admin-comunicacao/admin-comunicacao.component').then(m => m.AdminComunicacaoComponent),
      },
      {
        path: 'auditoria',
        title: 'Admin Auditoria — Na Ponta do Lápis',
        loadComponent: () => import('./pages/admin-auditoria/admin-auditoria.component').then(m => m.AdminAuditoriaComponent),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];
