import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';

@Component({
  selector: 'app-login',
  imports: [PrimeNGModuleModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {}
