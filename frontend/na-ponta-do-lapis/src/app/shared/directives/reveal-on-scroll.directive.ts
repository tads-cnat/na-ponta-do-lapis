import { AfterViewInit, Directive, ElementRef, Input, OnDestroy, Renderer2, inject } from '@angular/core';

/**
 * Diretiva standalone que revela elementos suavemente uma única vez
 * quando entram na viewport, respeitando `prefers-reduced-motion`.
 */
@Directive({
  selector: '[appReveal]',
  standalone: true,
})
export class RevealOnScrollDirective implements AfterViewInit, OnDestroy {
  @Input() revealDelay = 0;
  @Input() revealType: 'fade-up' | 'scale' = 'fade-up';

  private readonly el = inject(ElementRef<HTMLElement>);
  private readonly renderer = inject(Renderer2);
  private observer?: IntersectionObserver;

  ngAfterViewInit(): void {
    const node = this.el.nativeElement;
    const baseClass = this.revealType === 'scale' ? 'npl-reveal-scale' : 'npl-reveal';
    this.renderer.addClass(node, baseClass);

    if (this.revealDelay > 0) {
      this.renderer.setStyle(node, '--npl-delay', `${this.revealDelay}ms`);
    }

    const prefersReducedMotion =
      typeof window !== 'undefined' &&
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (prefersReducedMotion || typeof IntersectionObserver === 'undefined') {
      this.renderer.addClass(node, 'npl-revealed');
      return;
    }

    this.observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            this.renderer.addClass(node, 'npl-revealed');
          } else {
            this.renderer.removeClass(node, 'npl-revealed');
          }
        }
      },
      { threshold: 0.08, rootMargin: '0px 0px -20px 0px' }
    );

    this.observer.observe(node);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}

