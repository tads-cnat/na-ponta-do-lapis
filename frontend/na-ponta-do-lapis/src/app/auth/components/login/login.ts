import { Component } from '@angular/core';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-login',
  imports: [ButtonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {}
