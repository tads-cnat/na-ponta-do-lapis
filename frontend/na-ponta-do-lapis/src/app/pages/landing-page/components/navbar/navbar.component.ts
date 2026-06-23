import { Component } from '@angular/core';
import { RouterLink } from '@angular/router'; // 1. Certifique-se de que essa linha existe

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink], // 2. Adicione o RouterLink aqui dentro dos imports!
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent { }