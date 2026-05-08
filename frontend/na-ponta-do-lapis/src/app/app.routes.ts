import { Routes } from '@angular/router';
import { LoginComponent } from './auth/components/login/login.component';
import { TransacoesComponent } from './pages/transacoes/transacoes.component';
import { authGuard } from './core/guard/auth.guard';


export const routes: Routes = [
    {path: "", redirectTo: "login", pathMatch:"full"},
    {path: "login", component: LoginComponent},
    {path: "transacoes", component: TransacoesComponent , canActivate: [authGuard]}
];
