import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { LoginComponent } from '../login/login.component';
import { SignupComponent } from '../signup/signup.component';

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [LoginComponent, SignupComponent],
  templateUrl: './auth-page.component.html',
  styleUrl: './auth-page.component.css',
})
export class AuthPageComponent {
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly isSignup = signal(this.resolverModo() === 'signup');
  readonly isRecovery = signal(this.resolverModo() === 'recovery');

  constructor() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.isSignup.set(this.resolverModo() === 'signup');
        this.isRecovery.set(this.resolverModo() === 'recovery');
      });
  }

  private resolverModo(): 'login' | 'signup' | 'recovery' {
    const url = this.router.url.split('?')[0];
    if (url.startsWith('/signup')) return 'signup';
    if (url.startsWith('/recuperar-senha') || url.startsWith('/redefinir-senha') || url.startsWith('/confirmar-email')) return 'recovery';
    return 'login';
  }
}

