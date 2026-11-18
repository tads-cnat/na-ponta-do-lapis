import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { Routes, UrlMatchResult, UrlSegment } from '@angular/router';
import { Capacitor } from '@capacitor/core';
import { AuthPageComponent } from './auth/components/auth-page/auth-page.component';
import { TransacoesComponent } from './pages/transacoes/transacoes.component';
import { ContasComponent } from './pages/contas/contas.component';
import { MetasComponent } from './pages/metas/metas.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { LandingPageComponent } from './pages/landing-page/landing-page.component';
import { authGuard } from './core/guard/auth.guard';
import { PerfilComponent } from './pages/perfil/perfil.component';
import { FamiliaComponent } from './pages/familia/familia.component';

import { NotFoundComponent } from './pages/not-found/not-found.component';
import { PrivacidadeComponent } from './pages/privacidade/privacidade.component';


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
        component: LandingPageComponent,
        canMatch: [() => !Capacitor.isNativePlatform()],
        pathMatch: 'full'
    },
    {
        path: '',
        redirectTo: 'app/dashboard',
        pathMatch: 'full'
    },
    { path: 'privacidade', component: PrivacidadeComponent },
    { path: 'termos', component: PrivacidadeComponent },
    { matcher: authRouteMatcher, component: AuthPageComponent },

    // Área interna protegida
    {
        path: 'app',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        canActivateChild: [authGuard],
        children: [
            { path: 'dashboard', component: DashboardComponent },
            { path: 'transacoes', component: TransacoesComponent },
            { path: 'contas', component: ContasComponent },
            { path: 'metas', component: MetasComponent },
            { path: 'perfil', component: PerfilComponent},
            { path: 'familia', component: FamiliaComponent },
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
    { path: '**', component: NotFoundComponent }
];
