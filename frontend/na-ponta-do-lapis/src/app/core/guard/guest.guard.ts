import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { StorageService } from '../../auth/service/storage.service';

export const guestGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const path = route.url[0]?.path ?? '';
  const rotasSomenteVisitantes = ['login', 'signup', 'recuperar-senha'];

  if (rotasSomenteVisitantes.includes(path) && StorageService.temTokenValido()) {
    return router.createUrlTree(['/app/dashboard']);
  }

  return true;
};

