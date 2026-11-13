import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { Routes, UrlMatchResult, UrlSegment } from '@angular/router';
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


export function authRouteMatcher(segments: UrlSegment[]): UrlMatchResult | null {
    if (segments.length === 1 && (segments[0].path === 'login' || segments[0].path === 'signup')) {
        return { consumed: segments };
    }
    return null;
}

export const routes: Routes = [
    {
        path: '',
        component: LandingPageComponent,
        pathMatch: 'full'
    },
    { matcher: authRouteMatcher, component: AuthPageComponent },

    // Área interna protegida
    {
        path: 'app',
        component: MainLayoutComponent,
        canActivate: [authGuard],
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
