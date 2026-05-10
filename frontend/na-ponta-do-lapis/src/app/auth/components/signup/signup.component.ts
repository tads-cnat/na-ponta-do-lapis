import { Component } from '@angular/core';
import { PrimeNGModuleModule } from '../../../shared/primeNg.module';
import { RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-signup',
  imports: [PrimeNGModuleModule, ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css',
})
export class SignupComponent {
  registerForm:FormGroup;
  constructor(fb:FormBuilder){
    this.registerForm = fb.group({})
  }
  public register(){

  }
}
