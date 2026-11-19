import { Routes, UrlMatchResult, UrlSegment } from '@angular/router';
import { Capacitor } from '@capacitor/core';
import { authGuard } from './core/guard/auth.guard';
import { guestGuard } from './core/guard/guest.guard';


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
        loadComponent: () => import('./pages/privacidade/privacidade.component').then(m => m.PrivacidadeComponent)
    },
    {
        path: 'termos',
        loadComponent: () => import('./pages/privacidade/privacidade.component').then(m => m.PrivacidadeComponent)
    },
    {
        matcher: authRouteMatcher,
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
            { path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent) },
            { path: 'transacoes', loadComponent: () => import('./pages/transacoes/transacoes.component').then(m => m.TransacoesComponent) },
            { path: 'contas', loadComponent: () => import('./pages/contas/contas.component').then(m => m.ContasComponent) },
            { path: 'metas', loadComponent: () => import('./pages/metas/metas.component').then(m => m.MetasComponent) },
            { path: 'perfil', loadComponent: () => import('./pages/perfil/perfil.component').then(m => m.PerfilComponent) },
            { path: 'familia', loadComponent: () => import('./pages/familia/familia.component').then(m => m.FamiliaComponent) },
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
        ]
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
        loadComponent: () => import('./pages/not-found/not-found.component').then(m => m.NotFoundComponent)
    }
];
