import { Component, HostListener, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

type SecaoLanding = 'inicio' | 'por-que-nos' | 'o-que-fazemos';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  readonly activeSection = signal<SecaoLanding>('inicio');

  navegarPara(event: Event, secao: SecaoLanding): void {
    event.preventDefault();
    this.activeSection.set(secao);

    if (secao === 'inicio') {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    document.getElementById(secao)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  @HostListener('window:scroll')
  onWindowScroll(): void {
    const scrollY = window.scrollY;
    if (scrollY < 260) {
      this.activeSection.set('inicio');
      return;
    }

    const oQueFazemos = document.getElementById('o-que-fazemos');
    if (oQueFazemos && scrollY >= oQueFazemos.offsetTop - 160) {
      this.activeSection.set('o-que-fazemos');
      return;
    }

    const porQueNos = document.getElementById('por-que-nos');
    if (porQueNos && scrollY >= porQueNos.offsetTop - 160) {
      this.activeSection.set('por-que-nos');
    }
  }
}
