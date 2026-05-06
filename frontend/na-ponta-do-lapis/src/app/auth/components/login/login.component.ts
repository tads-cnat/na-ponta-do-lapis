import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../service/auth.service';
import th from '@angular/common/locales/th';

@Component({
  selector: 'app-login',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {

  loginForm:FormGroup;

  constructor(private fb:FormBuilder, private authService:AuthService, private router:Router){
    this.loginForm = this.fb.group({})
  }
}
