import { Routes } from '@angular/router';
import { LoginComponent } from './auth/components/login/login.component';
import { SignupComponent } from './auth/components/signup/signup.component';
import { TransacoesComponent } from './pages/transacoes/transacoes.component';
import { ContasComponent } from './pages/contas/contas.component';
import { MetasComponent } from './pages/metas/metas.component';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { LandingPageComponent } from './pages/landing-page/landing-page.component';
import { authGuard } from './core/guard/auth.guard';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component'
import { SignupComponent } from './auth/components/signup/signup.component';
import { FamiliaComponent } from './pages/familia/familia.component';


export const routes: Routes = [
    {
        path: "familia",
        component: MainLayoutComponent,
        children: [
            {path: "", component: FamiliaComponent},
        ]
    },
    {
        path: "app",
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            {path: "transacoes", component: TransacoesComponent },
            {path: "contas", component: ContasComponent },
            {path: "metas", component: MetasComponent },
            {path: "familia", component: FamiliaComponent},
        ]
    },


];
