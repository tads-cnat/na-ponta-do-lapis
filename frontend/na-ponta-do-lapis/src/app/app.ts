import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
// Importe o seu Navbar e o seu LandingPage (ou Hero) apontando para as pastas deles:

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet], 
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('na-ponta-do-lapis');
}
