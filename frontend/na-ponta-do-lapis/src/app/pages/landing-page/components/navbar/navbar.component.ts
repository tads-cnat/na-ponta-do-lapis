import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  isMobile: boolean = false;

  ngOnInit(): void {
    if (window.innerWidth < 769) {
      this.isMobile = true;
    }
  }
}
