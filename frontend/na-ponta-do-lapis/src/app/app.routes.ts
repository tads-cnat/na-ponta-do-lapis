import { Routes } from '@angular/router';
import { LoginComponent } from './auth/components/login/login.component';
import { TransacoesComponent } from './pages/transacoes/transacoes.component';
import { authGuard } from './core/guard/auth.guard';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component'
import { SignupComponent } from './auth/components/signup/signup.component';


export const routes: Routes = [
    {path: "", redirectTo: "login", pathMatch:"full"},
    {path: "login", component: LoginComponent},
    {path: 'signup', component:SignupComponent},
    {
        path: "app",
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            {path: "transacoes", component: TransacoesComponent }
        ]
    },
   
];
