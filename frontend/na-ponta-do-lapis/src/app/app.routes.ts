import { Routes } from '@angular/router';
import { LoginComponent } from './auth/components/login/login.component';
import { SignupComponent } from './auth/components/signup/signup.component';
import { TransacoesComponent } from './pages/transacoes/transacoes.component';
import { ContasComponent } from './pages/contas/contas.component';
import { MetasComponent } from './pages/metas/metas.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { LandingPageComponent } from './pages/landing-page/landing-page.component';
import { authGuard } from './core/guard/auth.guard';
import { FamiliaComponent } from './pages/familia/familia.component';


export const routes: Routes = [
    { 
        path: '', 
        component: LandingPageComponent, 
        pathMatch: 'full' 
    },
    { path: 'login', component: LoginComponent },
    { path: 'signup', component: SignupComponent },

    // Área interna protegida
    {
        path: 'app',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: 'transacoes', component: TransacoesComponent },
            { path: 'contas', component: ContasComponent },
            { path: 'metas', component: MetasComponent },
            { path: 'familia', component: FamiliaComponent },
            { path: '', redirectTo: 'transacoes', pathMatch: 'full' }
        ]
    },
    { path: '**', redirectTo: '' }
];
