import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RevealOnScrollDirective } from '../../../../shared/directives/reveal-on-scroll.directive';

@Component({
  selector: 'app-hero-section',
  standalone: true,
  imports: [RouterLink, RevealOnScrollDirective],
  templateUrl: './hero-section.component.html',
  styleUrl: './hero-section.component.css',
})
export class HeroSectionComponent implements AfterViewInit, OnDestroy {
  @ViewChild('heroSection', { static: true }) heroSection?: ElementRef<HTMLElement>;

  readonly parallaxX = signal(0);
  readonly parallaxY = signal(0);
  readonly heroActive = signal(true);

  private observer?: IntersectionObserver;
  private readonly reducedMotion =
    typeof window !== 'undefined' &&
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  ngAfterViewInit(): void {
    const el = this.heroSection?.nativeElement;
    if (!el || this.reducedMotion || typeof IntersectionObserver === 'undefined') {
      this.heroActive.set(true);
      return;
    }

    this.observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          this.heroActive.set(entry.isIntersecting);
        }
      },
      { threshold: 0.1 }
    );

    this.observer.observe(el);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  onHeroMouseMove(event: MouseEvent): void {
    if (this.reducedMotion || window.innerWidth < 768) return;

    const target = event.currentTarget as HTMLElement | null;
    if (!target) return;

    const rect = target.getBoundingClientRect();
    const nx = (event.clientX - rect.left) / rect.width - 0.5;
    const ny = (event.clientY - rect.top) / rect.height - 0.5;

    this.parallaxX.set(Math.round(nx * 44));
    this.parallaxY.set(Math.round(ny * 44));
  }

  onHeroMouseLeave(): void {
    this.parallaxX.set(0);
    this.parallaxY.set(0);
  }
}
