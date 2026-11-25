import { Routes, UrlMatchResult, UrlSegment } from '@angular/router';
import { Capacitor } from '@capacitor/core';
import { authGuard } from './core/guard/auth.guard';
import { guestGuard } from './core/guard/guest.guard';
import { adminGuard } from './admin/guards/admin.guard';


export function authRouteMatcher(segments: UrlSegment[]): UrlMatchResult | null {
    if (segments.length === 1 && (
        segments[0].path === 'login' ||
        segments[0].path === 'signup' ||
        segments[0].path === 'recuperar-senha' ||
        segments[0].path === 'redefinir-senha' ||
        segments[0].path === 'confirmar-email'
    )) {
        return { consumed: segments };
    }
    return null;
}

export const routes: Routes = [
    {
        path: '',
        title: 'Na Ponta do Lápis — Controle Financeiro Pessoal e Familiar',
        loadComponent: () => import('./pages/landing-page/landing-page.component').then(m => m.LandingPageComponent),
        canMatch: [() => !Capacitor.isNativePlatform()],
        pathMatch: 'full'
    },
    {
        path: '',
        redirectTo: 'app/dashboard',
        pathMatch: 'full'
    },
    {
        path: 'privacidade',
        title: 'Política de Privacidade — Na Ponta do Lápis',
        loadComponent: () => import('./pages/privacidade/privacidade.component').then(m => m.PrivacidadeComponent)
    },
    {
        path: 'termos',
        title: 'Termos de Uso — Na Ponta do Lápis',
        loadComponent: () => import('./pages/privacidade/privacidade.component').then(m => m.PrivacidadeComponent)
    },
    {
        matcher: authRouteMatcher,
        title: 'Acesso — Na Ponta do Lápis',
        loadComponent: () => import('./auth/components/auth-page/auth-page.component').then(m => m.AuthPageComponent),
        canActivate: [guestGuard]
    },

    // Área interna protegida
    {
        path: 'app',
        loadComponent: () => import('./layout/main-layout/main-layout.component').then(m => m.MainLayoutComponent),
        canActivate: [authGuard],
        canActivateChild: [authGuard],
        children: [
            { path: 'dashboard', title: 'Dashboard — Na Ponta do Lápis', loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent) },
            { path: 'transacoes', title: 'Transações — Na Ponta do Lápis', loadComponent: () => import('./pages/transacoes/transacoes.component').then(m => m.TransacoesComponent) },
            { path: 'contas', title: 'Contas e Cartões — Na Ponta do Lápis', loadComponent: () => import('./pages/contas/contas.component').then(m => m.ContasComponent) },
            { path: 'metas', title: 'Metas — Na Ponta do Lápis', loadComponent: () => import('./pages/metas/metas.component').then(m => m.MetasComponent) },
            { path: 'perfil', title: 'Perfil — Na Ponta do Lápis', loadComponent: () => import('./pages/perfil/perfil.component').then(m => m.PerfilComponent) },
            { path: 'familia', title: 'Família — Na Ponta do Lápis', loadComponent: () => import('./pages/familia/familia.component').then(m => m.FamiliaComponent) },
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
        ]
    },
    // Painel Administrativo da Plataforma (exclusivo web + ADMIN_SITE)
    {
        path: 'admin',
        canMatch: [() => !Capacitor.isNativePlatform()],
        canActivate: [adminGuard],
        canActivateChild: [adminGuard],
        loadChildren: () => import('./admin/admin.routes').then(m => m.ADMIN_ROUTES)
    },
    // Redirecionamentos de conveniência para rotas internas
    { path: 'perfil', redirectTo: 'app/perfil' },
    { path: 'dashboard', redirectTo: 'app/dashboard' },
    { path: 'transacoes', redirectTo: 'app/transacoes' },
    { path: 'contas', redirectTo: 'app/contas' },
    { path: 'metas', redirectTo: 'app/metas' },
    { path: 'familia', redirectTo: 'app/familia' },
    {
        path: '**',
        title: 'Página não encontrada — Na Ponta do Lápis',
        loadComponent: () => import('./pages/not-found/not-found.component').then(m => m.NotFoundComponent)
    }
];
