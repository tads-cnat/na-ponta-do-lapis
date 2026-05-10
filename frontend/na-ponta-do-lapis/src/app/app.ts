import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PrimeNGModuleModule } from "./shared/primeNg.module";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PrimeNGModuleModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('na-ponta-do-lapis');
}
